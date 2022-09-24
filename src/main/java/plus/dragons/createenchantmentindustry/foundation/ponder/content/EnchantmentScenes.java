package plus.dragons.createenchantmentindustry.foundation.ponder.content;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.fluids.actors.SpoutTileEntity;
import com.simibubi.create.content.contraptions.fluids.tank.CreativeFluidTankTileEntity;
import com.simibubi.create.content.contraptions.fluids.tank.FluidTankTileEntity;
import com.simibubi.create.content.contraptions.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.foundation.ponder.*;
import com.simibubi.create.foundation.ponder.element.BeltItemElement;
import com.simibubi.create.foundation.ponder.element.EntityElement;
import com.simibubi.create.foundation.ponder.element.InputWindowElement;
import com.simibubi.create.foundation.utility.Pointing;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import plus.dragons.createenchantmentindustry.content.contraptions.enchantments.CopierBlockEntity;
import plus.dragons.createenchantmentindustry.content.contraptions.enchantments.BlazeEnchanterBlockEntity;
import plus.dragons.createenchantmentindustry.entry.ModBlocks;
import plus.dragons.createenchantmentindustry.entry.ModFluids;
import plus.dragons.createenchantmentindustry.entry.ModItems;


import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Stream;

public class EnchantmentScenes {
    public static void disenchant(SceneBuilder scene, SceneBuildingUtil util){
        scene.title("disenchant", ""); // We do not use PonderLocalization. For title only
        scene.configureBasePlate(0, 0, 7);
        scene.scaleSceneView(.68f);
        scene.world.setKineticSpeed(util.select.everywhere(), 32f);
        scene.showBasePlate();
        scene.idle(5);
        scene.world.showSection(util.select.fromTo(0, 1, 0, 6, 4, 6), Direction.DOWN);

        scene.overlay.showText(100)
                .text("") // We do not use PonderLocalization. For registerText only
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector.topOf(1, 2, 1));

        // Must propagatePipeChange first or it won't work correctly
         scene.world.propagatePipeChange(util.grid.at(2,1, 5));

        BlockPos beltStart = util.grid.at(6, 2, 1);
        List<ItemStack> items = Stream.of(Items.NETHERITE_SWORD,Items.IRON_PICKAXE, Items.DIAMOND_CHESTPLATE, Items.ENCHANTED_BOOK, Items.LEATHER_HELMET, Items.GOLDEN_BOOTS, Items.WOODEN_AXE).map(Item::getDefaultInstance).toList();
        for(var item:items){
            enchantRandomly(item);
            ElementLink<EntityElement> itemEntity = scene.world.createItemEntity(util.vector.centerOf(6, 5, 1), util.vector.of(0, 0, 0), item);
            scene.idle(13);
            scene.world.modifyEntity(itemEntity, Entity::discard);
            scene.world.createItemOnBelt(beltStart, Direction.DOWN, item);
            scene.idle(2);
        }

        scene.idle(80);

        scene.overlay.showText(100)
                .text("") // We do not use PonderLocalization. For registerText only
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector.topOf(1, 2, 1));

        scene.idle(120);
    }

    public static void transformBlazeBurner(SceneBuilder scene, SceneBuildingUtil util){
        scene.title("transform", ""); // We do not use PonderLocalization. For title only
        scene.configureBasePlate(0, 0, 3);
        scene.showBasePlate();
        scene.idle(5);
        scene.world.showSection(util.select.fromTo(0, 1, 0, 2, 1, 2), Direction.DOWN);

        scene.overlay.showText(40)
                .text("") // We do not use PonderLocalization. For registerText only
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector.topOf(1, 1, 1));
        scene.idle(40);
        scene.world.setBlock(util.grid.at(1,1,1), ModBlocks.BLAZE_ENCHANTER.getDefaultState(),false);
        scene.world.modifyTileEntity(util.grid.at(1,1,1), BlazeEnchanterBlockEntity.class, be-> be.setTargetItem(enchantingGuide(Enchantments.MENDING,1)));
        scene.overlay.showControls(new InputWindowElement(util.vector.centerOf(1, 1, 1), Pointing.DOWN).whileSneaking().rightClick()
                .withItem(ModItems.ENCHANTING_GUIDE_FOR_BLAZE.asStack()), 40);
        scene.idle(50);
        scene.overlay.showText(40)
                .text("") // We do not use PonderLocalization. For registerText only
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector.topOf(1, 1, 1));
        scene.idle(40);
        scene.world.setBlock(util.grid.at(1,1,1), AllBlocks.BLAZE_BURNER.getDefaultState().setValue(BlazeBurnerBlock.HEAT_LEVEL, BlazeBurnerBlock.HeatLevel.SMOULDERING),false);
        scene.overlay.showControls(new InputWindowElement(util.vector.centerOf(1, 1, 1), Pointing.DOWN).rightClick(), 40);
        scene.idle(40);
    }

    public static void enchant(SceneBuilder scene, SceneBuildingUtil util){
        scene.title("enchant", ""); // We do not use PonderLocalization. For title only
        scene.configureBasePlate(0, 0, 8);
        scene.scaleSceneView(.60f);
        scene.world.setKineticSpeed(util.select.everywhere(), 0);
        scene.showBasePlate();
        scene.idle(5);
        scene.world.setKineticSpeed(util.select.everywhere(), 80F);
        scene.world.setKineticSpeed(util.select.fromTo(0,2,7,5,2,7), -80F);
        scene.world.setKineticSpeed(util.select.fromTo(7,2,2,7,2,7), -80F);
        scene.world.modifyTileEntity(util.grid.at(1,2,0), BlazeEnchanterBlockEntity.class, be-> {
            be.setTargetItem(enchantingGuide(Enchantments.MENDING,1));
            be.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).ifPresent(tank->
                    tank.fill(new FluidStack(ModFluids.EXPERIENCE.get().getSource(), 1000), IFluidHandler.FluidAction.EXECUTE));
        });
        scene.world.modifyTileEntity(util.grid.at(0,2,6), BlazeEnchanterBlockEntity.class, be-> {
            be.setTargetItem(enchantingGuide(Enchantments.UNBREAKING,3));
            be.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).ifPresent(tank->
                    tank.fill(new FluidStack(ModFluids.EXPERIENCE.get().getSource(), 1000), IFluidHandler.FluidAction.EXECUTE));
        });
        scene.world.modifyTileEntity(util.grid.at(6,2,7), BlazeEnchanterBlockEntity.class, be-> {
            be.setTargetItem(enchantingGuide(Enchantments.THORNS,1));
            be.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).ifPresent(tank->
                    tank.fill(new FluidStack(ModFluids.EXPERIENCE.get().getSource(), 1000), IFluidHandler.FluidAction.EXECUTE));
        });
        scene.world.modifyTileEntity(util.grid.at(7,2,1), BlazeEnchanterBlockEntity.class, be-> {
            be.setTargetItem(enchantingGuide(Enchantments.ALL_DAMAGE_PROTECTION,3));
            be.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).ifPresent(tank->
                    tank.fill(new FluidStack(ModFluids.EXPERIENCE.get().getSource(), 1000), IFluidHandler.FluidAction.EXECUTE));
        });
        scene.world.modifyTileEntity(util.grid.at(3,1,3), CreativeFluidTankTileEntity.class, be -> ((CreativeFluidTankTileEntity.CreativeSmartFluidTank) be.getTankInventory())
                .setContainedFluid(new FluidStack(ModFluids.EXPERIENCE.get().getSource(), 1000)));
        // Must propagatePipeChange first or it won't work correctly
        scene.world.propagatePipeChange(util.grid.at(2,1, 2));
        scene.world.propagatePipeChange(util.grid.at(5,1, 2));
        scene.world.propagatePipeChange(util.grid.at(5,1, 5));
        scene.world.propagatePipeChange(util.grid.at(5,1, 2));
        scene.world.showSection(util.select.fromTo(0, 1, 0, 7, 4, 7), Direction.DOWN);

        scene.idle(5);
        List<ItemStack> items = Stream.of(Items.NETHERITE_CHESTPLATE,Items.DIAMOND_CHESTPLATE, Items.IRON_CHESTPLATE, Items.CHAINMAIL_CHESTPLATE, Items.GOLDEN_CHESTPLATE, Items.LEATHER_CHESTPLATE).map(Item::getDefaultInstance).toList();
        for(var item:items){
            BlockPos beltStart = util.grid.at(7, 2, 0);
            ElementLink<EntityElement> itemEntity = scene.world.createItemEntity(util.vector.centerOf(7, 5, 0), util.vector.of(0, 0, 0), item);
            scene.idle(13);
            scene.world.modifyEntity(itemEntity, Entity::discard);
            scene.world.createItemOnBelt(beltStart, Direction.DOWN, item);
            scene.idle(10);
        }

        scene.idle(400);
    }

    public static void handleExperienceBottle(SceneBuilder scene, SceneBuildingUtil util){
        scene.title("experience_bottle", ""); // We do not use PonderLocalization. For title only
        scene.configureBasePlate(0, 0, 6);
        scene.scaleSceneView(.68f);
        scene.world.setKineticSpeed(util.select.everywhere(), 16f);
        scene.showBasePlate();
        scene.idle(5);
        scene.world.showSection(util.select.fromTo(0, 1, 0, 5, 4, 5), Direction.DOWN);

        // Must propagatePipeChange first or it won't work correctly
         scene.world.propagatePipeChange(util.grid.at(3,1, 1));
         scene.world.propagatePipeChange(util.grid.at(2,3, 3));

        var item = Items.EXPERIENCE_BOTTLE.getDefaultInstance();
        item.setCount(64);
        BlockPos beltStart = util.grid.at(4, 1, 0);
        ElementLink<EntityElement> itemEntity = scene.world.createItemEntity(util.vector.centerOf(4, 5, 0), util.vector.of(0, 0, 0), item);
        scene.idle(13);
        scene.world.modifyEntity(itemEntity, Entity::discard);
        scene.world.createItemOnBelt(beltStart, Direction.DOWN, item);

        scene.idle(10);

        scene.overlay.showText(80)
                .text("") // We do not use PonderLocalization. For registerText only
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector.topOf(2, 1, 0));

        scene.idle(80);

        scene.overlay.showText(80)
                .text("") // We do not use PonderLocalization. For registerText only
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

    public static void copy(SceneBuilder scene, SceneBuildingUtil util){
        scene.title("copy", ""); // We do not use PonderLocalization. For title only
        scene.configureBasePlate(0, 0, 7);
        scene.scaleSceneView(.68f);
        scene.world.setKineticSpeed(util.select.everywhere(), 32f);
        scene.showBasePlate();
        scene.idle(5);
        scene.world.showSection(util.select.fromTo(0, 1, 0, 6, 3, 6), Direction.DOWN);

        scene.overlay.showControls(new InputWindowElement(util.vector.centerOf(2, 3, 2), Pointing.DOWN).rightClick()
                .withItem(Items.ENCHANTED_BOOK.getDefaultInstance()), 40);
        scene.world.modifyTileEntity(util.grid.at(2,1,5), CreativeFluidTankTileEntity.class, be -> ((CreativeFluidTankTileEntity.CreativeSmartFluidTank) be.getTankInventory())
                .setContainedFluid(new FluidStack(ModFluids.EXPERIENCE.get().getSource(), 1000)));
        scene.world.modifyTileEntity(util.grid.at(2,3,2), CopierBlockEntity.class, be ->
                be.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).ifPresent(tank->
                        tank.fill(new FluidStack(ModFluids.EXPERIENCE.get().getSource(),3000), IFluidHandler.FluidAction.EXECUTE)));
        scene.idle(40);

        var item = Items.BOOK.getDefaultInstance();
        BlockPos beltStart = util.grid.at(6, 1, 2);
        ElementLink<BeltItemElement> beltItem = scene.world.createItemOnBelt(beltStart, Direction.DOWN, item);
        Selection copier = util.select.position(2, 3, 2);
        BlockPos copierPos = util.grid.at(2, 3, 2);
        scene.idle(60);
        scene.world.stallBeltItem(beltItem, true);
        scene.world.modifyTileNBT(copier, CopierBlockEntity.class, nbt -> nbt.putInt("ProcessingTicks", 100));
        scene.idle(95);
        scene.world.removeItemsFromBelt(copierPos.below(2));
        scene.world.createItemOnBelt(copierPos.below(2), Direction.UP, Items.ENCHANTED_BOOK.getDefaultInstance());

        scene.idle(40);

        scene.overlay.showControls(new InputWindowElement(util.vector.centerOf(2, 3, 2), Pointing.DOWN).rightClick()
                .withItem(Items.WRITTEN_BOOK.getDefaultInstance()), 40);
        scene.world.modifyTileEntity(util.grid.at(2,1,5), CreativeFluidTankTileEntity.class, be -> ((CreativeFluidTankTileEntity.CreativeSmartFluidTank) be.getTankInventory())
                .setContainedFluid(new FluidStack(ModFluids.INK.get().getSource(), 1000)));
        scene.world.modifyTileEntity(util.grid.at(2,3,2), CopierBlockEntity.class, be ->
                be.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).ifPresent(tank->
                        tank.fill(new FluidStack(ModFluids.INK.get().getSource(),3000), IFluidHandler.FluidAction.EXECUTE)));
        scene.idle(40);

        item = Items.BOOK.getDefaultInstance();
        beltStart = util.grid.at(6, 1, 2);
        beltItem = scene.world.createItemOnBelt(beltStart, Direction.DOWN, item);
        copier = util.select.position(2, 3, 2);
        copierPos = util.grid.at(2, 3, 2);
        scene.idle(60);
        scene.world.stallBeltItem(beltItem, true);
        scene.world.modifyTileNBT(copier, CopierBlockEntity.class, nbt -> nbt.putInt("ProcessingTicks", 100));
        scene.idle(95);
        scene.world.removeItemsFromBelt(copierPos.below(2));
        scene.world.createItemOnBelt(copierPos.below(2), Direction.UP, Items.WRITTEN_BOOK.getDefaultInstance());
    }

    public static void leak(SceneBuilder scene, SceneBuildingUtil util){
        scene.title("leak", ""); // We do not use PonderLocalization. For title only
        scene.configureBasePlate(0, 0, 5);
        scene.scaleSceneView(.5f);
        scene.world.setKineticSpeed(util.select.everywhere(), 0f);
        scene.showBasePlate();
        scene.idle(5);

        scene.world.showSection(util.select.fromTo(3, 1, 3, 4, 4, 4), Direction.DOWN);
        scene.idle(5);
        scene.world.modifyTileEntity(util.grid.at(3,1,3), FluidTankTileEntity.class, be -> ((FluidTank) be.getTankInventory())
                .setFluid(new FluidStack(ModFluids.EXPERIENCE.get().getSource(), 48000)));
        scene.overlay.showText(40)
                .text("") // We do not use PonderLocalization. For registerText only
                .placeNearTarget()
                .pointAt(util.vector.topOf(2, 4, 2));
        scene.idle(50);

        scene.world.showSection(util.select.fromTo(0, 1, 0, 4, 4, 2), Direction.DOWN);
        scene.world.showSection(util.select.fromTo(0, 1, 3, 2, 4, 4), Direction.DOWN);

        scene.idle(5);
        scene.overlay.showText(40)
                .text("") // We do not use PonderLocalization. For registerText only
                .placeNearTarget()
                .pointAt(util.vector.topOf(3, 4, 0));
        scene.idle(50);

        scene.world.setKineticSpeed(util.select.everywhere(), 128f);
        // Must propagatePipeChange first or it won't work correctly
        scene.world.propagatePipeChange(util.grid.at(0,1, 3));
        scene.world.propagatePipeChange(util.grid.at(3,1, 0));
        scene.world.propagatePipeChange(util.grid.at(3,4, 0));
        scene.idle(80);
        scene.overlay.showText(40)
                .text("") // We do not use PonderLocalization. For registerText only
                .colored(PonderPalette.RED)
                .placeNearTarget()
                .pointAt(util.vector.topOf(3, 4, 0));
        scene.idle(50);
        scene.overlay.showText(40)
                .text("") // We do not use PonderLocalization. For registerText only
                .colored(PonderPalette.GREEN)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector.topOf(3, 4, 0));
        scene.idle(50);

    }

    private static void enchantItem(ItemStack itemStack, Enchantment enchantment, int level){
        var m = EnchantmentHelper.getEnchantments(itemStack);
        m.put(enchantment,level);
        EnchantmentHelper.setEnchantments(m,itemStack);
    }

    private static void enchantRandomly(ItemStack itemStack){
        if(itemStack.is(Items.ENCHANTED_BOOK)){
            enchantItem(itemStack, Enchantments.MENDING,1);
        }
        else EnchantmentHelper.enchantItem(new Random(),itemStack,30,true);
    }

    private static ItemStack enchantingGuide(Enchantment enchantment, int level){
        var ret = ModItems.ENCHANTING_GUIDE_FOR_BLAZE.asStack();
        ret.getOrCreateTag().putInt("index",0);
        var book =  Items.ENCHANTED_BOOK.getDefaultInstance();
        EnchantmentHelper.setEnchantments(Map.of(enchantment,level),book);
        ret.getOrCreateTag().put("target",book.serializeNBT());
        return ret;
    }
}
