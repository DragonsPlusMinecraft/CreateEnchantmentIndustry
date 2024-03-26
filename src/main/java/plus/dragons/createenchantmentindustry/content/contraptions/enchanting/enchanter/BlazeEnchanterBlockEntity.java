package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter;

import com.simibubi.create.content.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.utility.*;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.FilteringFluidTankBehaviour;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.ExperienceFluid;
import plus.dragons.createenchantmentindustry.entry.CeiContainerTypes;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;
import plus.dragons.createenchantmentindustry.entry.CeiItems;
import plus.dragons.createenchantmentindustry.entry.CeiTags;
import plus.dragons.createenchantmentindustry.foundation.advancement.CeiAdvancements;
import plus.dragons.createenchantmentindustry.foundation.config.CeiConfigs;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static plus.dragons.createenchantmentindustry.EnchantmentIndustry.LANG;

public class BlazeEnchanterBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation, MenuProvider {

    public static final int ENCHANTING_TIME = 200;
    SmartFluidTankBehaviour internalTank;
    TransportedItemStack heldItem;
    ItemStack targetItem = new ItemStack(CeiItems.ENCHANTING_GUIDE.get());
    int processingTicks;
    Map<Direction, LazyOptional<EnchantingItemHandler>> itemHandlers;
    boolean sendParticles;
    LerpedFloat headAnimation;
    LerpedFloat headAngle;
    Random random = new Random();
    float flip;
    float oFlip;
    float flipT;
    float flipA;
    public boolean goggles;

    public BlazeEnchanterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        itemHandlers = new IdentityHashMap<>();
        for (Direction d : Iterate.horizontalDirections) {
            EnchantingItemHandler enchantingItemHandler = new EnchantingItemHandler(this, d);
            itemHandlers.put(d, LazyOptional.of(() -> enchantingItemHandler));
        }
        headAnimation = LerpedFloat.linear();
        headAngle = LerpedFloat.angular();
        headAngle.startWithValue((AngleHelper
                .horizontalAngle(state.getOptionalValue(BlazeEnchanterBlock.FACING)
                        .orElse(Direction.SOUTH)) + 180) % 360
        );
        goggles = false;
    }

    @Override
    @SuppressWarnings("deprecation") //Fluid Tags are still useful for mod interaction
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        behaviours.add(new DirectBeltInputBehaviour(this).allowingBeltFunnels()
                .setInsertionHandler(this::tryInsertingFromSide));
        behaviours.add(internalTank = FilteringFluidTankBehaviour
                .single(fluidStack -> fluidStack.getFluid().is(CeiTags.FluidTag.BLAZE_ENCHANTER_INPUT.tag),
                    this, CeiConfigs.SERVER.blazeEnchanterTankCapacity.get())
                .whenFluidUpdates(() -> {
                    var fluid = internalTank.getPrimaryHandler().getFluid().getFluid();
                    if (CeiFluids.EXPERIENCE.is(fluid))
                        updateHeatLevel(BlazeEnchanterBlock.HeatLevel.KINDLED);
                    else if (CeiFluids.HYPER_EXPERIENCE.is(fluid))
                        updateHeatLevel(BlazeEnchanterBlock.HeatLevel.SEETHING);
                    else
                        updateHeatLevel(BlazeEnchanterBlock.HeatLevel.SMOULDERING);
                }));
        registerAwardables(behaviours,
                CeiAdvancements.FIRST_ORDER.asCreateAdvancement(),
                CeiAdvancements.ADDITIONAL_ORDER.asCreateAdvancement(),
                CeiAdvancements.HYPOTHETICAL_EXTENSION.asCreateAdvancement());
    }

    @Override
    public void tick() {
        super.tick();

        boolean onClient = level.isClientSide && !isVirtual();

        if (onClient) {
            bookTick();
            blazeTick();
        }

        if (heldItem == null) {
            processingTicks = 0;
            return;
        }


        if (processingTicks > 0) {
            heldItem.prevBeltPosition = .5f;
            boolean wasAtBeginning = processingTicks == ENCHANTING_TIME;
            if (!onClient || processingTicks < ENCHANTING_TIME)
                processingTicks--;
            if (!continueProcessing()) {
                processingTicks = 0;
                notifyUpdate();
                return;
            }
            // Interesting Trigger Sync Design
            if (wasAtBeginning != (processingTicks == ENCHANTING_TIME))
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
                    BlockEntityBehaviour.get(level, nextPosition, DirectBeltInputBehaviour.TYPE);
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
            if (Enchanting.getValidEnchantment(heldItem.stack, targetItem, hyper()) == null)
                return;
            heldItem.beltPosition = .5f;
            if (onClient)
                return;
            processingTicks = ENCHANTING_TIME;
            sendData();
        }

    }

    protected void blazeTick() {
        boolean active = processingTicks > 0;

        if (!active) {
            float target = 0;
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null && !player.isInvisible()) {
                double x;
                double z;
                if (isVirtual()) {
                    x = -4;
                    z = -10;
                } else {
                    x = player.getX();
                    z = player.getZ();
                }
                double dx = x - (getBlockPos().getX() + 0.5);
                double dz = z - (getBlockPos().getZ() + 0.5);
                target = AngleHelper.deg(-Mth.atan2(dz, dx)) - 90;
            }
            target = headAngle.getValue() + AngleHelper.getShortestAngleDiff(headAngle.getValue(), target);
            headAngle.chase(target, .25f, LerpedFloat.Chaser.exp(5));
            headAngle.tickChaser();
        } else {
            headAngle.chase((AngleHelper.horizontalAngle(getBlockState().getOptionalValue(BlazeEnchanterBlock.FACING)
                    .orElse(Direction.SOUTH)) + 180) % 360, .125f, LerpedFloat.Chaser.EXP);
            headAngle.tickChaser();
        }
        headAnimation.chase(1, .25f, LerpedFloat.Chaser.exp(.25f));
        headAnimation.tickChaser();

        spawnBlazeParticles();
    }

    protected void bookTick() {
        if (random.nextInt(40) == 0) {
            float oFlipT = flipT;
            while (oFlipT == flipT) {
                flipT += (random.nextInt(4) - random.nextInt(4));
            }
        }
        oFlip = flip;
        float flipDiff = (flipT - flip) * 0.4F;
        flipDiff = Mth.clamp(flipDiff, -0.2F, 0.2F);
        flipA += (flipDiff - flipA) * 0.9F;
        flip += flipA;
    }

    protected void spawnBlazeParticles() {
        if (level == null)
            return;
        BlazeEnchanterBlock.HeatLevel heatLevel = getBlockState().getValue(BlazeEnchanterBlock.HEAT_LEVEL);

        var r = level.random;

        Vec3 c = VecHelper.getCenterOf(worldPosition);
        Vec3 v = c.add(VecHelper.offsetRandomly(Vec3.ZERO, r, .125f)
                .multiply(1, 0, 1));

        if (r.nextInt(3) == 0)
            level.addParticle(ParticleTypes.LARGE_SMOKE, v.x, v.y, v.z, 0, 0, 0);
        if (r.nextInt(2) != 0)
            return;

        boolean empty = level.getBlockState(worldPosition.above())
                .getCollisionShape(level, worldPosition.above())
                .isEmpty();

        double yMotion = empty ? .0625f : r.nextDouble() * .0125f;
        Vec3 v2 = c.add(VecHelper.offsetRandomly(Vec3.ZERO, r, .5f)
                        .multiply(1, .25f, 1)
                        .normalize()
                        .scale((empty ? .25f : .5) + r.nextDouble() * .125f))
                .add(0, .5, 0);

        if (heatLevel.isAtLeast(BlazeEnchanterBlock.HeatLevel.SEETHING)) {
            level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, v2.x, v2.y, v2.z, 0, yMotion, 0);
        } else if (heatLevel.isAtLeast(BlazeEnchanterBlock.HeatLevel.KINDLED)) {
            level.addParticle(ParticleTypes.FLAME, v2.x, v2.y, v2.z, 0, yMotion, 0);
        }
    }

    protected static int ENCHANT_PARTICLE_COUNT = 20;

    protected void spawnEnchantParticles() {
        if (isVirtual())
            return;
        Vec3 vec = VecHelper.getCenterOf(worldPosition);
        vec = vec.add(0, 1, 0);
        ParticleOptions particle = ParticleTypes.ENCHANT;
        for (int i = 0; i < ENCHANT_PARTICLE_COUNT; i++) {
            Vec3 m = VecHelper.offsetRandomly(Vec3.ZERO, level.random, 1f);
            m = new Vec3(m.x, Math.abs(m.y), m.z);
            level.addAlwaysVisibleParticle(particle, vec.x, vec.y, vec.z, m.x, m.y, m.z);
        }
        level.playLocalSound(vec.x, vec.y, vec.z, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1f, level.random.nextFloat() * .1f + .9f, true);
    }

    protected boolean continueProcessing() {
        if (level.isClientSide && !isVirtual()) {
            if (processingTicks > 0 && processingTicks < 200 && level.getGameTime() % 80L == 0L)
                ((ClientLevel) level).playLocalSound(worldPosition, SoundEvents.BEACON_AMBIENT, SoundSource.BLOCKS, 1.0f, 1.0f, true);
            return true;
        }
        if (processingTicks < 5)
            return true;

        boolean hyper = hyper();
        Pair<Enchantment, Integer> entry = Enchanting.getValidEnchantment(heldItem.stack, targetItem, hyper);
        if (entry == null)
            return false;

        FluidStack exp = new FluidStack(hyper
                ? CeiFluids.HYPER_EXPERIENCE.get().getSource()
                : CeiFluids.EXPERIENCE.get().getSource(),
                (int) (Enchanting.getExperienceConsumption(entry.getFirst(), entry.getSecond()) *
                        (hyper? CeiConfigs.SERVER.hyperEnchantByBlazeEnchanterCostCoefficient.get():
                                CeiConfigs.SERVER.enchantByBlazeEnchanterCostCoefficient.get()))
        );

        if (processingTicks > 5) {
            var tankFluid = internalTank.getPrimaryHandler().getFluid().getFluid();
            if ((!CeiFluids.EXPERIENCE.is(tankFluid) && !CeiFluids.HYPER_EXPERIENCE.is(tankFluid) ||
                    internalTank.getPrimaryHandler().getFluidAmount() < exp.getAmount())) {
                processingTicks = ENCHANTING_TIME;
            }
            return true;
        }

        // Advancement
        if (EnchantmentHelper.getEnchantments(heldItem.stack).isEmpty())
            award(CeiAdvancements.FIRST_ORDER.asCreateAdvancement());
        else
            award(CeiAdvancements.ADDITIONAL_ORDER.asCreateAdvancement());
        if (hyper)
            award(CeiAdvancements.HYPOTHETICAL_EXTENSION.asCreateAdvancement());
        // Process finished
        Enchanting.enchantItem(heldItem.stack, entry);
        internalTank.getPrimaryHandler().getFluid().shrink(exp.getAmount());
        sendParticles = true;
        notifyUpdate();
        return true;
    }

    private float itemMovementPerTick() {
        return 1 / 8f;
    }

    public void setTargetItem(ItemStack itemStack) {
        targetItem = itemStack;
    }

    private ItemStack tryInsertingFromSide(TransportedItemStack transportedStack, Direction side, boolean simulate) {
        ItemStack inserted = transportedStack.stack;
        ItemStack returned = ItemStack.EMPTY;

        if (!getHeldItemStack().isEmpty())
            return inserted;

        if (inserted.getCount() > 1 && Enchanting.getValidEnchantment(inserted, targetItem, hyper()) != null) {
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
    public void invalidate() {
        super.invalidate();
        for (LazyOptional<EnchantingItemHandler> lazyOptional : itemHandlers.values())
            lazyOptional.invalidate();
    }

    @Override
    public ItemRequirement getRequiredItems(BlockState state){
        return new ItemRequirement(ItemRequirement.ItemUseType.CONSUME,targetItem);
    }

    @Override
    public void destroy() {
        super.destroy();
        if (level instanceof ServerLevel serverLevel) {
            ItemStack heldItemStack = getHeldItemStack();
            var pos = getBlockPos();
            if (!heldItemStack.isEmpty())
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), heldItemStack);
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), targetItem);
            var tank = internalTank.getPrimaryHandler();
            var fluidStack = tank.getFluid();
            if(fluidStack.getFluid() instanceof ExperienceFluid expFluid) {
                expFluid.drop(serverLevel, VecHelper.getCenterOf(pos), fluidStack.getAmount());
            }
        }
    }

    public boolean hyper() {
        return CeiFluids.HYPER_EXPERIENCE.is(internalTank.getPrimaryHandler().getFluid().getFluid());
    }

    @Override
    public void write(CompoundTag compoundTag, boolean clientPacket) {
        super.write(compoundTag, clientPacket);
        compoundTag.putInt("ProcessingTicks", processingTicks);
        compoundTag.put("TargetItem", targetItem.serializeNBT());
        compoundTag.putBoolean("Goggles", goggles);
        if (heldItem != null)
            compoundTag.put("HeldItem", heldItem.serializeNBT());
        if (sendParticles && clientPacket) {
            compoundTag.putBoolean("SpawnParticles", true);
            sendParticles = false;
        }
    }

    @Override
    public void writeSafe(CompoundTag tag) {
        super.writeSafe(tag);
        tag.put("TargetItem", new ItemStack(CeiItems.ENCHANTING_GUIDE.get()).serializeNBT());
        tag.putBoolean("Goggles", goggles);
    }

    @Override
    protected void read(CompoundTag compoundTag, boolean clientPacket) {
        super.read(compoundTag, clientPacket);
        heldItem = null;
        processingTicks = compoundTag.getInt("ProcessingTicks");
        targetItem = ItemStack.of(compoundTag.getCompound("TargetItem"));
        goggles = compoundTag.getBoolean("Goggles");
        if (compoundTag.contains("HeldItem"))
            heldItem = TransportedItemStack.read(compoundTag.getCompound("HeldItem"));
        if (!clientPacket)
            return;
        if (compoundTag.contains("SpawnParticles"))
            spawnEnchantParticles();
    }

    @Override
    @NotNull
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
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
        LANG.translate("gui.goggles.blaze_enchanter").forGoggles(tooltip);
        if (targetItem != null && targetItem.is(CeiItems.ENCHANTING_GUIDE.get())) {
            EnchantmentEntry entry = Enchanting.getTargetEnchantment(targetItem, hyper());
            if (entry != null) {
                tooltip.add(Component.literal("     ")
                        .append(entry.getFirst().getFullname(entry.getSecond())));
                if (!entry.valid())
                    tooltip.add(Component.literal("     ")
                            .append(LANG.translate("gui.goggles.blaze_enchanter.invalid_target").component())
                            .withStyle(ChatFormatting.RED));
                else {
                    int consumption = (int) (Enchanting.getExperienceConsumption(entry.getFirst(), entry.getSecond()) *
                            (hyper()? CeiConfigs.SERVER.hyperEnchantByBlazeEnchanterCostCoefficient.get():
                                    CeiConfigs.SERVER.enchantByBlazeEnchanterCostCoefficient.get()));
                    if (consumption > CeiConfigs.SERVER.blazeEnchanterTankCapacity.get())
                        tooltip.add(Component.literal("     ").append(LANG.translate("gui.goggles.too_expensive")
                                        .component())
                                .withStyle(ChatFormatting.RED));
                    else
                        tooltip.add(Component.literal("     ")
                                .append(LANG.translate("gui.goggles.xp_consumption", consumption).component())
                                .withStyle(ChatFormatting.GREEN));
                }
            }
        }
        containedFluidTooltip(tooltip, isPlayerSneaking, getCapability(ForgeCapabilities.FLUID_HANDLER));
        return true;
    }

    public void updateHeatLevel(BlazeEnchanterBlock.HeatLevel heatLevel) {
        if (level != null)
            level.setBlockAndUpdate(getBlockPos(), getBlockState().setValue(BlazeEnchanterBlock.HEAT_LEVEL, heatLevel));
    }

    @Override
    public Component getDisplayName() {
        return targetItem.getDisplayName();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new EnchantingGuideMenu(CeiContainerTypes.ENCHANTING_GUIDE_FOR_BLAZE.get(), pContainerId, pPlayerInventory, targetItem, getBlockPos());
    }
}
