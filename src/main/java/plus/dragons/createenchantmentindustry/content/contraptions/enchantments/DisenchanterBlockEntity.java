package plus.dragons.createenchantmentindustry.content.contraptions.enchantments;

import com.simibubi.create.content.contraptions.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.contraptions.relays.belt.transport.TransportedItemStack;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.DirectBeltInputBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.utility.BlockHelper;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Pair;
import com.simibubi.create.foundation.utility.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import plus.dragons.createenchantmentindustry.entry.ModFluids;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class DisenchanterBlockEntity extends SmartTileEntity implements IHaveGoggleInformation {

    public static final int DISENCHANTER_TIME = 10;
    private static final int ABSORB_AMOUNT = 100;

    SmartFluidTankBehaviour internalTank;
    TransportedItemStack heldItem;
    int processingTicks;
    Map<Direction, LazyOptional<DisenchanterItemHandler>> itemHandlers;

    AABB absorbArea;

    public DisenchanterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        itemHandlers = new IdentityHashMap<>();
        for (Direction d : Iterate.horizontalDirections) {
            DisenchanterItemHandler disenchanterItemHandler = new DisenchanterItemHandler(this, d);
            itemHandlers.put(d, LazyOptional.of(() -> disenchanterItemHandler));
        }
        absorbArea = new AABB(pos.above());
    }

    @Override
    public void addBehaviours(List<TileEntityBehaviour> behaviours) {
        behaviours.add(new DirectBeltInputBehaviour(this).allowingBeltFunnels()
                .setInsertionHandler(this::tryInsertingFromSide));
        behaviours.add(internalTank = SmartFluidTankBehaviour.single(this, 1500)
                .allowExtraction()
                .forbidInsertion());
        // registerAwardables(behaviours, AllAdvancements.DRAIN, AllAdvancements.CHAINED_DRAIN);
    }

    @Override
    public void tick() {
        super.tick();

        boolean onClient = level.isClientSide && !isVirtual();

        if (!onClient && level.getDayTime() % 20 == 0)
            absorbExperienceFromWorld();

        if (heldItem == null) {
            processingTicks = 0;
            return;
        }

        if (processingTicks > 0) {
            heldItem.prevBeltPosition = .5f;
            boolean wasAtBeginning = processingTicks == DISENCHANTER_TIME;
            if (!onClient || processingTicks < DISENCHANTER_TIME)
                processingTicks--;
            if (!continueProcessing()) {
                processingTicks = 0;
                notifyUpdate();
                return;
            }
            // Interesting Trigger Sync Design
            if (wasAtBeginning != (processingTicks == DISENCHANTER_TIME))
                sendData();
            // A return here
            return;
        }

        heldItem.prevBeltPosition = heldItem.beltPosition;
        heldItem.prevSideOffset = heldItem.sideOffset;

        heldItem.beltPosition += itemMovementPerTick();
        if (heldItem.beltPosition > 1) {
            heldItem.beltPosition = 1;

            if (onClient)
                return;

            Direction side = heldItem.insertedFrom;

            /* DirectBeltInputBehaviour#tryExportingToBeltFunnel(ItemStack, Direction, boolean) return null to
             * represent insertion is invalid due to invalidity
             * of funnel (excludes funnel being powered) or something go wrong. */
            ItemStack tryExportingToBeltFunnel = getBehaviour(DirectBeltInputBehaviour.TYPE)
                    .tryExportingToBeltFunnel(heldItem.stack, side.getOpposite(), false);
            if (tryExportingToBeltFunnel != null) {
                if (tryExportingToBeltFunnel.getCount() != heldItem.stack.getCount()) {
                    if (tryExportingToBeltFunnel.isEmpty())
                        heldItem = null;
                    else
                        heldItem.stack = tryExportingToBeltFunnel;
                    notifyUpdate();
                    return;
                }
                if (!tryExportingToBeltFunnel.isEmpty())
                    return;
            }

            BlockPos nextPosition = worldPosition.relative(side);
            DirectBeltInputBehaviour directBeltInputBehaviour =
                    TileEntityBehaviour.get(level, nextPosition, DirectBeltInputBehaviour.TYPE);
            if (directBeltInputBehaviour == null) {
                if (!BlockHelper.hasBlockSolidSide(level.getBlockState(nextPosition), level, nextPosition,
                        side.getOpposite())) {
                    ItemStack ejected = heldItem.stack;
                    // Following "Launching out" process can be used as standard.
                    Vec3 outPos = VecHelper.getCenterOf(worldPosition)
                            .add(Vec3.atLowerCornerOf(side.getNormal())
                                    .scale(.75));
                    float movementSpeed = itemMovementPerTick();
                    Vec3 outMotion = Vec3.atLowerCornerOf(side.getNormal())
                            .scale(movementSpeed)
                            .add(0, 1 / 8f, 0);
                    outPos.add(outMotion.normalize());
                    ItemEntity entity = new ItemEntity(level, outPos.x, outPos.y + 6 / 16f, outPos.z, ejected);
                    entity.setDeltaMovement(outMotion);
                    entity.setDefaultPickUpDelay();
                    entity.hurtMarked = true;
                    level.addFreshEntity(entity);

                    heldItem = null;
                    notifyUpdate();
                }
                return;
            }

            if (!directBeltInputBehaviour.canInsertFromSide(side))
                return;

            ItemStack returned = directBeltInputBehaviour.handleInsertion(heldItem.copy(), side, false);

            if (returned.isEmpty()) {
                heldItem = null;
                notifyUpdate();
                return;
            }

            if (returned.getCount() != heldItem.stack.getCount()) {
                heldItem.stack = returned;
                notifyUpdate();
                return;
            }

            return;
        }

        if (heldItem.prevBeltPosition < .5f && heldItem.beltPosition >= .5f) {
            if (!Disenchanting.valid(heldItem.stack))
                return;
            heldItem.beltPosition = .5f;
            if (onClient)
                return;
            processingTicks = DISENCHANTER_TIME;
            sendData();
        }

    }

    protected void absorbExperienceFromWorld() {
        List<Player> players = level.getEntitiesOfClass(Player.class, absorbArea, LivingEntity::isAlive);
        if (!players.isEmpty()) {
            AtomicInteger sum = new AtomicInteger();
            internalTank.allowInsertion();
            players.forEach(player -> {
                if (player.totalExperience >= ABSORB_AMOUNT) {
                    sum.addAndGet(ABSORB_AMOUNT);
                } else if (player.totalExperience != 0) {
                    sum.addAndGet(player.totalExperience);
                }
            });
            if (sum.get() != 0) {
                var fluidStack = new FluidStack(ModFluids.EXPERIENCE.get().getSource(), sum.get());
                var inserted = internalTank.getPrimaryHandler().fill(fluidStack, IFluidHandler.FluidAction.EXECUTE);
                if (inserted != 0) {
                    for (var player : players) {
                        if (inserted >= ABSORB_AMOUNT) {
                            if (player.totalExperience >= ABSORB_AMOUNT) {
                                player.giveExperiencePoints(-ABSORB_AMOUNT);
                                inserted -= ABSORB_AMOUNT;
                            } else if (player.totalExperience != 0) {
                                inserted -= player.totalExperience;
                                player.giveExperiencePoints(-player.totalExperience);
                            }
                        } else if (sum.get() > 0) {
                            if (player.totalExperience >= sum.get()) {
                                player.giveExperiencePoints(-sum.get());
                                inserted = 0;
                            } else {
                                inserted -= player.totalExperience;
                                player.giveExperiencePoints(-player.totalExperience);
                            }
                        } else {
                            break;
                        }
                    }
                }
            }
            internalTank.forbidInsertion();
        }
        List<ExperienceOrb> experienceOrbs = level.getEntitiesOfClass(ExperienceOrb.class, absorbArea);
        if (!experienceOrbs.isEmpty()) {
            internalTank.allowInsertion();
            for (var orb : experienceOrbs) {
                var amount = orb.value;
                var fluidStack = new FluidStack(ModFluids.EXPERIENCE.get().getSource(), amount);
                var inserted = internalTank.getPrimaryHandler().fill(fluidStack, IFluidHandler.FluidAction.EXECUTE);
                if (inserted == amount) {
                    orb.remove(Entity.RemovalReason.DISCARDED);
                } else {
                    if (inserted != 0)
                        orb.value -= inserted;
                    break;
                }
            }
            internalTank.forbidInsertion();
        }
    }

    protected boolean continueProcessing() {
        if (level.isClientSide && !isVirtual())
            return true;
        if (processingTicks < 5)
            return true;
        if (!Disenchanting.valid(heldItem.stack))
            return false;

        Pair<FluidStack, ItemStack> stackPair = Disenchanting.disenchant(heldItem.stack, true);
        FluidStack fluidFromItem = stackPair.getFirst();

        if (processingTicks > 5) {
            internalTank.allowInsertion();
            if (internalTank.getPrimaryHandler()
                    .fill(fluidFromItem, IFluidHandler.FluidAction.SIMULATE) != fluidFromItem.getAmount()) {
                internalTank.forbidInsertion();
                processingTicks = DISENCHANTER_TIME;
                return true;
            }
            internalTank.forbidInsertion();
            return true;
        }

        stackPair = Disenchanting.disenchant(heldItem.stack, true);
        // award(AllAdvancements.DRAIN);

        // Process finished
        heldItem.stack = stackPair.getSecond();
        internalTank.allowInsertion();
        internalTank.getPrimaryHandler()
                .fill(fluidFromItem, IFluidHandler.FluidAction.EXECUTE);
        internalTank.forbidInsertion();
        level.levelEvent(1042, getBlockPos(), 0);
        notifyUpdate();
        return true;
    }

    private float itemMovementPerTick() {
        return 1 / 8f;
    }

    public SmartFluidTankBehaviour getInternalTank() {
        return internalTank;
    }

    private ItemStack tryInsertingFromSide(TransportedItemStack transportedStack, Direction side, boolean simulate) {
        ItemStack inserted = transportedStack.stack;
        ItemStack returned = ItemStack.EMPTY;

        if (!getHeldItemStack().isEmpty())
            return inserted;

        if (inserted.getCount() > 1 && Disenchanting.valid(inserted)) {
            returned = ItemHandlerHelper.copyStackWithSize(inserted, inserted.getCount() - 1);
            inserted = ItemHandlerHelper.copyStackWithSize(inserted, 1);
        }

        if (simulate)
            return returned;

        transportedStack = transportedStack.copy();
        transportedStack.stack = inserted.copy();
        transportedStack.beltPosition = side.getAxis()
                .isVertical() ? .5f : 0;
        transportedStack.prevSideOffset = transportedStack.sideOffset;
        transportedStack.prevBeltPosition = transportedStack.beltPosition;
        setHeldItem(transportedStack, side);
        setChanged();
        sendData();

        return returned;
    }

    public ItemStack getHeldItemStack() {
        return heldItem == null ? ItemStack.EMPTY : heldItem.stack;
    }

    public void setHeldItem(TransportedItemStack heldItem, Direction insertedFrom) {
        this.heldItem = heldItem;
        this.heldItem.insertedFrom = insertedFrom;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        for (LazyOptional<DisenchanterItemHandler> lazyOptional : itemHandlers.values())
            lazyOptional.invalidate();
    }

    @Override
    public void write(CompoundTag compoundTag, boolean clientPacket) {
        compoundTag.putInt("ProcessingTicks", processingTicks);
        if (heldItem != null)
            compoundTag.put("HeldItem", heldItem.serializeNBT());
        super.write(compoundTag, clientPacket);
    }

    @Override
    protected void read(CompoundTag compoundTag, boolean clientPacket) {
        heldItem = null;
        processingTicks = compoundTag.getInt("ProcessingTicks");
        if (compoundTag.contains("HeldItem"))
            heldItem = TransportedItemStack.read(compoundTag.getCompound("HeldItem"));
        super.read(compoundTag, clientPacket);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction side) {
        if (side != null && side.getAxis()
                .isHorizontal() && isItemHandlerCap(capability))
            return itemHandlers.get(side)
                    .cast();

        if ((side == Direction.DOWN || side == null) && isFluidHandlerCap(capability))
            return internalTank.getCapability()
                    .cast();
        return super.getCapability(capability, side);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        return containedFluidTooltip(tooltip, isPlayerSneaking, getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY));
    }
}
