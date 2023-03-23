package plus.dragons.createenchantmentindustry.foundation.ponder.content;

import com.google.common.collect.Lists;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.contraptions.components.deployer.DeployerTileEntity;
import com.simibubi.create.content.contraptions.fluids.actors.SpoutTileEntity;
import com.simibubi.create.content.contraptions.fluids.tank.CreativeFluidTankTileEntity;
import com.simibubi.create.content.contraptions.fluids.tank.FluidTankTileEntity;
import com.simibubi.create.content.contraptions.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.foundation.ponder.*;
import com.simibubi.create.foundation.ponder.element.BeltItemElement;
import com.simibubi.create.foundation.ponder.element.EntityElement;
import com.simibubi.create.foundation.ponder.element.InputWindowElement;
import com.simibubi.create.foundation.ponder.instruction.EmitParticlesInstruction;
import com.simibubi.create.foundation.utility.Pointing;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter.BlazeEnchanterBlock;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter.BlazeEnchanterBlockEntity;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.printer.PrinterBlockEntity;
import plus.dragons.createenchantmentindustry.entry.CeiBlocks;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;
import plus.dragons.createenchantmentindustry.entry.CeiItems;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class EnchantmentScenes {
    public static void disenchant(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("disenchant", "Disenchanting");
        scene.configureBasePlate(0, 0, 7);
        scene.scaleSceneView(.68f);
        scene.world.setKineticSpeed(util.select.everywhere(), 32f);
        scene.showBasePlate();
        scene.idle(5);
        scene.world.showSection(util.select.fromTo(0, 1, 0, 6, 4, 6), Direction.DOWN);

        scene.overlay.showText(100)
                .text("All received items will have their enchantments removed and the removed enchantments will be converted to liquid experience for storage.")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector.topOf(1, 1, 1));

        // Must propagatePipeChange first or it won't work correctly
        scene.world.propagatePipeChange(util.grid.at(2, 1, 5));

        BlockPos beltStart = util.grid.at(6, 1, 1);
        List<ItemStack> items = Stream.of(Items.NETHERITE_SWORD, Items.IRON_PICKAXE, Items.DIAMOND_CHESTPLATE, Items.ENCHANTED_BOOK, Items.LEATHER_HELMET, Items.GOLDEN_BOOTS, Items.WOODEN_AXE).map(Item::getDefaultInstance).toList();
        for (var item : items) {
            enchantRandomly(item);
            ElementLink<EntityElement> itemEntity = scene.world.createItemEntity(util.vector.centerOf(6, 4, 1), util.vector.of(0, 0, 0), item);
            scene.idle(13);
            scene.world.modifyEntity(itemEntity, Entity::discard);
            scene.world.createItemOnBelt(beltStart, Direction.DOWN, item);
            scene.idle(2);
        }

        scene.idle(80);

        scene.overlay.showText(100)
                .text("Players standing on the disenchanter will be quickly washed away their experience level, and the washed away experience value will be converted into liquid experience for storage")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector.topOf(1, 1, 1));

        scene.idle(120);
    }

    public static void transformBlazeBurner(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("transform", "Using Enchanting Guide");
        scene.configureBasePlate(0, 0, 3);
        scene.showBasePlate();
        scene.idle(5);
        scene.world.showSection(util.select.fromTo(0, 1, 0, 2, 1, 2), Direction.DOWN);

        scene.overlay.showText(40)
                .text("Right-click the Blaze Burner with an Enchanting Guide in hand when sneaking to transform it to a Blaze Enchanter.")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector.topOf(1, 1, 1));
        scene.idle(40);

        scene.world.setBlock(util.grid.at(1, 1, 1), CeiBlocks.BLAZE_ENCHANTER.getDefaultState(), false);
        scene.world.modifyTileEntity(util.grid.at(1, 1, 1), BlazeEnchanterBlockEntity.class, be -> be.setTargetItem(enchantingGuide(Enchantments.MENDING, 1)));

        scene.overlay.showText(140)
                .text("To make Blaze Enchanter work, Enchanting Guide must be configured first. " +
                        "Right-clicking Blaze Enchanter or right-clicking Enchanting Guide in hands can open configuration panel.")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector.topOf(1, 1, 1));
        scene.idle(140);

        scene.overlay.showControls(new InputWindowElement(util.vector.centerOf(1, 1, 1), Pointing.DOWN).whileSneaking().rightClick()
                .withItem(CeiItems.ENCHANTING_GUIDE.asStack()), 40);
        scene.idle(50);
        scene.overlay.showText(40)
                .text("To retrieve the enchanting guide, right-click the Blaze Enchanter with wrench when sneaking.")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector.topOf(1, 1, 1));
        scene.idle(40);
        scene.world.setBlock(util.grid.at(1, 1, 1), AllBlocks.BLAZE_BURNER.getDefaultState().setValue(BlazeBurnerBlock.HEAT_LEVEL, BlazeBurnerBlock.HeatLevel.SMOULDERING), false);
        scene.overlay.showControls(new InputWindowElement(util.vector.centerOf(1, 1, 1), Pointing.DOWN).whileSneaking().rightClick().withWrench(), 40);
        scene.idle(40);
    }

    public static void enchant(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("enchant", "Enchanting");
        scene.configureBasePlate(0, 0, 8);
        scene.scaleSceneView(.60f);
        scene.world.setKineticSpeed(util.select.everywhere(), 0);
        scene.showBasePlate();
        scene.idle(5);
        scene.world.setKineticSpeed(util.select.everywhere(), 80F);
        scene.world.setKineticSpeed(util.select.fromTo(0, 2, 7, 5, 2, 7), -80F);
        scene.world.setKineticSpeed(util.select.fromTo(7, 2, 2, 7, 2, 7), -80F);
        scene.world.setBlock(util.grid.at(1,2,0),CeiBlocks.BLAZE_ENCHANTER.getDefaultState().setValue(BlazeEnchanterBlock.HEAT_LEVEL,
                BlazeEnchanterBlock.HeatLevel.KINDLED),false);
        scene.world.setBlock(util.grid.at(0,2,6),CeiBlocks.BLAZE_ENCHANTER.getDefaultState().setValue(BlazeEnchanterBlock.HEAT_LEVEL,
                BlazeEnchanterBlock.HeatLevel.KINDLED),false);
        scene.world.setBlock(util.grid.at(6,2,7),CeiBlocks.BLAZE_ENCHANTER.getDefaultState().setValue(BlazeEnchanterBlock.HEAT_LEVEL,
                BlazeEnchanterBlock.HeatLevel.KINDLED),false);
        scene.world.setBlock(util.grid.at(7,2,1),CeiBlocks.BLAZE_ENCHANTER.getDefaultState().setValue(BlazeEnchanterBlock.HEAT_LEVEL,
                BlazeEnchanterBlock.HeatLevel.KINDLED),false);
        scene.world.modifyTileEntity(util.grid.at(1, 2, 0), BlazeEnchanterBlockEntity.class, be -> {
            be.setTargetItem(enchantingGuide(Enchantments.MENDING, 1));
            be.getCapability(ForgeCapabilities.FLUID_HANDLER).ifPresent(tank ->
                    tank.fill(new FluidStack(CeiFluids.EXPERIENCE.get().getSource(), 1000), IFluidHandler.FluidAction.EXECUTE));
        });
        scene.world.modifyTileEntity(util.grid.at(0, 2, 6), BlazeEnchanterBlockEntity.class, be -> {
            be.setTargetItem(enchantingGuide(Enchantments.UNBREAKING, 3));
            be.getCapability(ForgeCapabilities.FLUID_HANDLER).ifPresent(tank ->
                    tank.fill(new FluidStack(CeiFluids.EXPERIENCE.get().getSource(), 1000), IFluidHandler.FluidAction.EXECUTE));
        });
        scene.world.modifyTileEntity(util.grid.at(6, 2, 7), BlazeEnchanterBlockEntity.class, be -> {
            be.setTargetItem(enchantingGuide(Enchantments.THORNS, 1));
            be.getCapability(ForgeCapabilities.FLUID_HANDLER).ifPresent(tank ->
                    tank.fill(new FluidStack(CeiFluids.EXPERIENCE.get().getSource(), 1000), IFluidHandler.FluidAction.EXECUTE));
        });
        scene.world.modifyTileEntity(util.grid.at(7, 2, 1), BlazeEnchanterBlockEntity.class, be -> {
            be.setTargetItem(enchantingGuide(Enchantments.ALL_DAMAGE_PROTECTION, 3));
            be.getCapability(ForgeCapabilities.FLUID_HANDLER).ifPresent(tank ->
                    tank.fill(new FluidStack(CeiFluids.EXPERIENCE.get().getSource(), 1000), IFluidHandler.FluidAction.EXECUTE));
        });
        scene.world.modifyTileEntity(util.grid.at(3, 1, 3), CreativeFluidTankTileEntity.class, be -> ((CreativeFluidTankTileEntity.CreativeSmartFluidTank) be.getTankInventory())
                .setContainedFluid(new FluidStack(CeiFluids.EXPERIENCE.get().getSource(), 1000)));
        // Must propagatePipeChange first or it won't work correctly
        scene.world.propagatePipeChange(util.grid.at(2, 1, 2));
        scene.world.propagatePipeChange(util.grid.at(5, 1, 2));
        scene.world.propagatePipeChange(util.grid.at(5, 1, 5));
        scene.world.propagatePipeChange(util.grid.at(5, 1, 2));
        scene.world.showSection(util.select.fromTo(0, 1, 0, 7, 4, 7), Direction.DOWN);

        scene.idle(5);
        List<ItemStack> items = Stream.of(Items.NETHERITE_CHESTPLATE, Items.DIAMOND_CHESTPLATE, Items.IRON_CHESTPLATE, Items.CHAINMAIL_CHESTPLATE, Items.GOLDEN_CHESTPLATE, Items.LEATHER_CHESTPLATE).map(Item::getDefaultInstance).toList();
        for (var item : items) {
            BlockPos beltStart = util.grid.at(7, 2, 0);
            ElementLink<EntityElement> itemEntity = scene.world.createItemEntity(util.vector.centerOf(7, 5, 0), util.vector.of(0, 0, 0), item);
            scene.idle(13);
            scene.world.modifyEntity(itemEntity, Entity::discard);
            scene.world.createItemOnBelt(beltStart, Direction.DOWN, item);
            scene.idle(10);
        }

        scene.idle(400);
    }

    public static void hyperEnchant(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("hyper_enchant", "Hyper-enchanting");
        scene.configureBasePlate(0, 0, 5);
        scene.scaleSceneView(.68f);
        scene.showBasePlate();
        scene.idle(5);
        scene.world.setBlock(util.grid.at(1,2,1),CeiBlocks.BLAZE_ENCHANTER.getDefaultState().setValue(BlazeEnchanterBlock.HEAT_LEVEL,
                BlazeEnchanterBlock.HeatLevel.KINDLED),false);
        scene.world.setBlock(util.grid.at(1,2,3),CeiBlocks.BLAZE_ENCHANTER.getDefaultState().setValue(BlazeEnchanterBlock.HEAT_LEVEL,
                BlazeEnchanterBlock.HeatLevel.SEETHING),false);
        scene.world.modifyTileEntity(util.grid.at(3, 1, 1), CreativeFluidTankTileEntity.class, be -> ((CreativeFluidTankTileEntity.CreativeSmartFluidTank) be.getTankInventory())
                .setContainedFluid(new FluidStack(CeiFluids.EXPERIENCE.get().getSource(), 1000)));
        scene.world.modifyTileEntity(util.grid.at(3, 1, 3), CreativeFluidTankTileEntity.class, be -> ((CreativeFluidTankTileEntity.CreativeSmartFluidTank) be.getTankInventory())
                .setContainedFluid(new FluidStack(CeiFluids.HYPER_EXPERIENCE.get().getSource(), 1000)));
        scene.world.propagatePipeChange(util.grid.at(2, 1, 1));
        scene.world.propagatePipeChange(util.grid.at(2, 1, 3));
        scene.world.showSection(util.select.fromTo(0, 1, 0, 4, 3, 4), Direction.DOWN);

        scene.overlay.showText(60)
                .text("This is liquid hyper experience")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector.topOf(3, 3, 3));
        scene.overlay.showOutline(PonderPalette.BLUE, new Object(), util.select.fromTo(3, 1, 3, 3, 3, 3), 80);
        scene.idle(90);

        scene.world.setKineticSpeed(util.select.everywhere(), 128F);
        scene.idle(40);

        scene.overlay.showText(80)
                .text("Hyper experience can make the Blaze Enchanter into seething state, and the level of the enchantment produced in this state will be one level higher than the set enchantment.")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector.topOf(1, 2, 3));
        scene.idle(90);

        scene.overlay.showText(60)
                .text("Enchantment with level cap of 1 level cannot be upgraded to level 2 in hyper-enchant.")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector.topOf(1, 2, 3));
        scene.idle(70);
    }

    public static void handleExperienceNugget(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("absorb_experience_nugget", "Converting Experience Nugget to Liquid");
        scene.configureBasePlate(0, 0, 5);
        scene.scaleSceneView(.68f);
        scene.world.setKineticSpeed(util.select.everywhere(), 32f);
        scene.world.setKineticSpeed(util.select.fromTo(0, 1, 2, 2, 1, 4), -32f);
        scene.showBasePlate();
        scene.idle(5);
        scene.world.showSection(util.select.fromTo(0, 1, 0, 4, 1, 4), Direction.DOWN);

        var poses = Lists.newArrayList(util.grid.at(2, 1, 0), util.grid.at(0, 1, 2), util.grid.at(2, 1, 4), util.grid.at(4, 1, 2));
        for (var pos : poses) {
            var item = AllItems.EXP_NUGGET.asStack(64);
            ElementLink<EntityElement> itemEntity = scene.world.createItemEntity(Vec3.atCenterOf(pos.above(3)), util.vector.of(0, 0, 0), item);
            scene.idle(13);
            scene.world.modifyEntity(itemEntity, Entity::discard);
            scene.world.createItemOnBelt(pos, Direction.DOWN, item);
            scene.idle(10);
        }

        scene.overlay.showText(60)
                .text("Experience nugget can be absorbed by disenchanter.")
                .placeNearTarget()
                .pointAt(util.vector.topOf(2, 1, 2));

        scene.idle(60);
    }

    public static void dropExperienceNugget(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("drop_experience_nugget", "Maybe A Exp-farm?");
        scene.configureBasePlate(0, 0, 3);
        scene.showBasePlate();
        scene.idle(5);
        scene.world.showSection(util.select.fromTo(0, 1, 0, 2, 1, 2), Direction.DOWN);
        BlockPos deployerPos = util.grid.at(1, 1, 2);
        Selection deployerSelection = util.select.position(deployerPos);

        ItemStack sword = new ItemStack(Items.NETHERITE_SWORD);
        scene.idle(10);
        scene.world.modifyTileNBT(deployerSelection, DeployerTileEntity.class, nbt -> {
            nbt.put("HeldItem", sword.serializeNBT());
            nbt.putString("mode", "PUNCH");
        });
        scene.idle(30);
        scene.world.setKineticSpeed(util.select.everywhere(), 32f);

        scene.addKeyframe();
        ElementLink<EntityElement> sheep = scene.world.createEntity(w -> {
            Sheep entity = EntityType.SHEEP.create(w);
            entity.setColor(DyeColor.PINK);
            Vec3 p = util.vector.topOf(util.grid.at(1, 0, 0));
            entity.setPos(p.x, p.y, p.z);
            entity.xo = p.x;
            entity.yo = p.y;
            entity.zo = p.z;
            entity.animationPosition = 0;
            entity.yRotO = 210;
            entity.setYRot(210);
            entity.yHeadRotO = 210;
            entity.yHeadRot = 210;
            return entity;
        });
        scene.idle(5);
        scene.world.moveDeployer(deployerPos, 1, 25);
        scene.idle(26);
        scene.world.modifyEntity(sheep, Entity::discard);
        scene.effects.emitParticles(util.vector.topOf(deployerPos.west(2))
                        .add(0, -.25, 0),
                EmitParticlesInstruction.Emitter.withinBlockSpace(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.PINK_WOOL.defaultBlockState()),
                        util.vector.of(0, 0, 0)),
                25, 1);
        scene.world.moveDeployer(deployerPos, -1, 25);

        scene.overlay.showText(60)
                .text("When mob is killed by deployer, experience nuggets are dropped.")
                .placeNearTarget()
                .pointAt(util.vector.topOf(2, 1, 2));

        scene.world.flapFunnel(deployerPos.north(), true);
        scene.world.createItemEntity(util.vector.centerOf(deployerPos.west())
                .subtract(0, .45, 0), util.vector.of(-0.1, 0, 0), new ItemStack(Items.PINK_WOOL));
        scene.idle(10);

        scene.world.flapFunnel(deployerPos.north(), true);
        scene.world.createItemEntity(util.vector.centerOf(deployerPos.west())
                .subtract(0, .45, 0), util.vector.of(-0.1, 0, 0), new ItemStack(Items.MUTTON));
        scene.idle(10);

        scene.world.flapFunnel(deployerPos.north(), true);
        scene.world.createItemEntity(util.vector.centerOf(deployerPos.west())
                .subtract(0, .45, 0), util.vector.of(-0.1, 0, 0), AllItems.EXP_NUGGET.asStack());
        scene.idle(40);
    }

    public static void handleExperienceBottle(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("experience_bottle", "Dealing with Bottle o' Enchanting");
        scene.configureBasePlate(0, 0, 6);
        scene.scaleSceneView(.68f);
        scene.world.setKineticSpeed(util.select.everywhere(), 16f);
        scene.showBasePlate();
        scene.idle(5);
        scene.world.showSection(util.select.fromTo(0, 1, 0, 5, 4, 5), Direction.DOWN);

        // Must propagatePipeChange first or it won't work correctly
        scene.world.propagatePipeChange(util.grid.at(3, 1, 1));
        scene.world.propagatePipeChange(util.grid.at(2, 3, 3));

        var item = Items.EXPERIENCE_BOTTLE.getDefaultInstance();
        item.setCount(64);
        BlockPos beltStart = util.grid.at(4, 1, 0);
        ElementLink<EntityElement> itemEntity = scene.world.createItemEntity(util.vector.centerOf(4, 5, 0), util.vector.of(0, 0, 0), item);
        scene.idle(13);
        scene.world.modifyEntity(itemEntity, Entity::discard);
        scene.world.createItemOnBelt(beltStart, Direction.DOWN, item);

        scene.idle(10);

        scene.overlay.showText(80)
                .text("Bottle o' Enchanting can be emptied at Item Drain.")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector.topOf(2, 1, 0));

        scene.idle(80);

        scene.overlay.showText(80)
                .text("Bottle o' Enchanting also can be manufactured by Spout as well.")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector.topOf(0, 3, 3));

        var item2 = Items.GLASS_BOTTLE.getDefaultInstance();
        BlockPos beltStart2 = util.grid.at(0, 1, 1);
        ElementLink<EntityElement> itemEntity2 = scene.world.createItemEntity(util.vector.centerOf(0, 5, 1), util.vector.of(0, 0, 0), item2);
        scene.idle(13);
        scene.world.modifyEntity(itemEntity2, Entity::discard);
        ElementLink<BeltItemElement> beltItem = scene.world.createItemOnBelt(beltStart2, Direction.DOWN, item2);
        Selection spoutS = util.select.position(0, 3, 3);
        BlockPos spoutPos = util.grid.at(0, 3, 3);
        scene.idle(60);
        scene.world.stallBeltItem(beltItem, true);
        scene.world.modifyTileNBT(spoutS, SpoutTileEntity.class, nbt -> nbt.putInt("ProcessingTicks", 20));
        scene.idle(10);
        scene.world.removeItemsFromBelt(spoutPos.below(2));
        scene.world.createItemOnBelt(spoutPos.below(2), Direction.UP, Items.EXPERIENCE_BOTTLE.getDefaultInstance());

        scene.idle(70);
    }

    public static void copy(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("copy", "Using Printer");
        scene.configureBasePlate(0, 0, 7);
        scene.scaleSceneView(.68f);
        scene.world.setKineticSpeed(util.select.everywhere(), 32f);
        scene.showBasePlate();
        scene.idle(5);
        scene.world.showSection(util.select.fromTo(0, 1, 0, 6, 3, 6), Direction.DOWN);

        scene.overlay.showControls(new InputWindowElement(util.vector.centerOf(2, 3, 2), Pointing.DOWN).rightClick()
                .withItem(Items.ENCHANTED_BOOK.getDefaultInstance()), 40);
        scene.world.modifyTileEntity(util.grid.at(2, 1, 5), CreativeFluidTankTileEntity.class, be -> ((CreativeFluidTankTileEntity.CreativeSmartFluidTank) be.getTankInventory())
                .setContainedFluid(new FluidStack(CeiFluids.EXPERIENCE.get().getSource(), 1000)));
        scene.world.modifyTileEntity(util.grid.at(2, 3, 2), PrinterBlockEntity.class, be ->
                be.getCapability(ForgeCapabilities.FLUID_HANDLER).ifPresent(tank ->
                        tank.fill(new FluidStack(CeiFluids.EXPERIENCE.get().getSource(), 3000), IFluidHandler.FluidAction.EXECUTE)));
        scene.overlay.showText(40)
                .text("Liquid Experience is required to duplicate enchanted books.")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector.topOf(2, 3, 2));
        scene.idle(40);

        var item = Items.BOOK.getDefaultInstance();
        BlockPos beltStart = util.grid.at(6, 1, 2);
        ElementLink<BeltItemElement> beltItem = scene.world.createItemOnBelt(beltStart, Direction.DOWN, item);
        Selection copier = util.select.position(2, 3, 2);
        BlockPos copierPos = util.grid.at(2, 3, 2);
        scene.idle(60);
        scene.world.stallBeltItem(beltItem, true);
        scene.world.modifyTileNBT(copier, PrinterBlockEntity.class, nbt -> nbt.putInt("ProcessingTicks", 100));
        scene.idle(95);
        scene.world.removeItemsFromBelt(copierPos.below(2));
        scene.world.createItemOnBelt(copierPos.below(2), Direction.UP, Items.ENCHANTED_BOOK.getDefaultInstance());

        scene.idle(40);

        scene.overlay.showControls(new InputWindowElement(util.vector.centerOf(2, 3, 2), Pointing.DOWN).rightClick()
                .withItem(Items.WRITTEN_BOOK.getDefaultInstance()), 40);
        scene.world.modifyTileEntity(util.grid.at(2, 1, 5), CreativeFluidTankTileEntity.class, be -> ((CreativeFluidTankTileEntity.CreativeSmartFluidTank) be.getTankInventory())
                .setContainedFluid(new FluidStack(CeiFluids.INK.get().getSource(), 1000)));
        scene.world.modifyTileEntity(util.grid.at(2, 3, 2), PrinterBlockEntity.class, be ->
                be.getCapability(ForgeCapabilities.FLUID_HANDLER).ifPresent(tank -> {
                    tank.drain(3000, IFluidHandler.FluidAction.EXECUTE);
                    tank.fill(new FluidStack(CeiFluids.INK.get().getSource(), 3000), IFluidHandler.FluidAction.EXECUTE);
                }));
        scene.overlay.showText(40)
                .text("Ink is required to duplicate written books.")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector.topOf(2, 3, 2));
        scene.idle(40);

        item = Items.BOOK.getDefaultInstance();
        beltStart = util.grid.at(6, 1, 2);
        beltItem = scene.world.createItemOnBelt(beltStart, Direction.DOWN, item);
        copier = util.select.position(2, 3, 2);
        copierPos = util.grid.at(2, 3, 2);
        scene.idle(60);
        scene.world.stallBeltItem(beltItem, true);
        scene.world.modifyTileNBT(copier, PrinterBlockEntity.class, nbt -> nbt.putInt("ProcessingTicks", 100));
        scene.idle(95);
        scene.world.removeItemsFromBelt(copierPos.below(2));
        scene.world.createItemOnBelt(copierPos.below(2), Direction.UP, Items.WRITTEN_BOOK.getDefaultInstance());
    }

    public static void leak(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("leak", "Oh no! It's leaking!");
        scene.configureBasePlate(0, 0, 5);
        scene.scaleSceneView(.5f);
        scene.world.setKineticSpeed(util.select.everywhere(), 0f);
        scene.showBasePlate();
        scene.idle(5);

        scene.world.showSection(util.select.fromTo(3, 1, 3, 4, 4, 4), Direction.DOWN);
        scene.idle(5);
        scene.world.modifyTileEntity(util.grid.at(3, 1, 3), FluidTankTileEntity.class, be -> ((FluidTank) be.getTankInventory())
                .setFluid(new FluidStack(CeiFluids.EXPERIENCE.get().getSource(), 48000)));
        scene.overlay.showText(40)
                .text("I have a tank full of experience")
                .placeNearTarget()
                .pointAt(util.vector.topOf(2, 4, 2));
        scene.idle(50);

        scene.world.showSection(util.select.fromTo(0, 1, 0, 4, 4, 2), Direction.DOWN);
        scene.world.showSection(util.select.fromTo(0, 1, 3, 2, 4, 4), Direction.DOWN);

        scene.idle(5);
        scene.overlay.showText(40)
                .text("I have an open-ended pipe")
                .placeNearTarget()
                .pointAt(util.vector.topOf(3, 4, 0));
        scene.idle(50);

        scene.world.setKineticSpeed(util.select.everywhere(), 128f);
        // Must propagatePipeChange first or it won't work correctly
        scene.world.propagatePipeChange(util.grid.at(0, 1, 3));
        scene.world.propagatePipeChange(util.grid.at(3, 1, 0));
        scene.world.propagatePipeChange(util.grid.at(3, 4, 0));
        scene.idle(80);
        scene.overlay.showText(40)
                .text("Ugh!")
                .colored(PonderPalette.RED)
                .placeNearTarget()
                .pointAt(util.vector.topOf(3, 4, 0));
        scene.idle(50);
        scene.overlay.showText(40)
                .text("Don't worry, the leaked liquid experience will turn into experience orbs. Players can also stand at the opening of the pipe to absorb experience.") // We do not use PonderLocalization. For registerText only
                .colored(PonderPalette.GREEN)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector.topOf(3, 4, 0));
        scene.idle(50);

    }

    private static void enchantItem(ItemStack itemStack, Enchantment enchantment, int level) {
        var m = EnchantmentHelper.getEnchantments(itemStack);
        m.put(enchantment, level);
        EnchantmentHelper.setEnchantments(m, itemStack);
    }

    private static void enchantRandomly(ItemStack itemStack) {
        if (itemStack.is(Items.ENCHANTED_BOOK)) {
            enchantItem(itemStack, Enchantments.MENDING, 1);
        } else EnchantmentHelper.enchantItem(RandomSource.create(), itemStack, 30, true);
    }

    private static ItemStack enchantingGuide(Enchantment enchantment, int level) {
        var ret = CeiItems.ENCHANTING_GUIDE.asStack();
        ret.getOrCreateTag().putInt("index", 0);
        var book = Items.ENCHANTED_BOOK.getDefaultInstance();
        EnchantmentHelper.setEnchantments(Map.of(enchantment, level), book);
        ret.getOrCreateTag().put("target", book.serializeNBT());
        return ret;
    }
}
