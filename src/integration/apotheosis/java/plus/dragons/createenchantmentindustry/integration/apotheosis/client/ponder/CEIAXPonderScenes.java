/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package plus.dragons.createenchantmentindustry.integration.apotheosis.client.ponder;

import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import dev.shadowsoffire.apotheosis.adventure.Adventure;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemItem;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemRegistry;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.data.loading.DatagenModLoader;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import plus.dragons.createenchantmentindustry.integration.apotheosis.common.processing.affix.affixEnhancer.AffixAugmentorBlockEntity;
import plus.dragons.createenchantmentindustry.integration.apotheosis.common.processing.socket.gem.gemCutter.GemCutterBlockEntity;
import plus.dragons.createenchantmentindustry.integration.apotheosis.common.processing.socket.gem.gemCutter.GemCutting;
import plus.dragons.createenchantmentindustry.integration.apotheosis.common.registry.CEIAXBlocks;
import plus.dragons.createenchantmentindustry.integration.apotheosis.common.registry.CEIAXFluids;

public class CEIAXPonderScenes {
    public static void bulkSalvaging(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("bulk_salvaging", "Bulk Salvaging");
        scene.configureBasePlate(0, 0, 5);
        scene.world().showSection(util.select().layer(0)
                .substract(util.select().position(2, 0, 5)), Direction.UP);
        scene.idle(5);
        var salvageTable = scene.world().showIndependentSection(util.select().position(2, 3, 1), Direction.DOWN);
        scene.world().moveSection(salvageTable, new Vec3(0, -2, 0), 0);
        scene.idle(5);
        scene.overlay()
                .showText(50)
                .pointAt(util.vector().centerOf(2, 1, 1))
                .text("In Apotheosis, player can salvage, or dissemble, item in Salvaging Table");
        scene.idle(55);

        scene.world().showSection(util.select().position(2, 1, 3), Direction.UP);
        scene.idle(3);
        scene.world().showSection(util.select().position(2, 1, 4), Direction.UP);
        scene.idle(3);
        scene.world().hideIndependentSection(salvageTable, Direction.DOWN);
        scene.idle(3);
        scene.world().showSection(util.select().position(2, 2, 3), Direction.DOWN);
        scene.idle(3);
        scene.world().showSection(util.select().position(2, 2, 4), Direction.DOWN);
        scene.idle(3);

        scene.overlay()
                .showText(80)
                .pointAt(util.vector().centerOf(2, 2, 3))
                .attachKeyFrame()
                .text("Air Flows passing through Infused Dragon Breath create a Salvaging Setup");
        scene.idle(10);
        scene.world().showSection(util.select().position(2, 1, 2), Direction.DOWN);
        scene.idle(3);
        scene.world().showSection(util.select().position(2, 1, 1), Direction.DOWN);
        scene.idle(3);
        scene.world().showSection(util.select().position(2, 1, 0), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().position(2, 2, 5), Direction.NORTH);
        scene.idle(3);
        scene.world().showSection(util.select().position(1, 1, 5), Direction.NORTH);
        scene.idle(3);
        scene.world().showSection(util.select().position(2, 0, 5), Direction.NORTH);
        scene.idle(3);
        scene.world().setKineticSpeed(util.select().position(2, 2, 4).add(util.select().position(2, 0, 5)).add(util.select().position(2, 2, 5)), -8f);
        scene.world().setKineticSpeed(util.select().position(1, 1, 5), 4f);
        scene.idle(3);

        var airCurrent = util.select().fromTo(2, 2, 0, 2, 2, 3);
        scene.overlay()
                .showOutline(PonderPalette.BLACK, airCurrent, airCurrent, 20);
        scene.idle(20);
        scene.world().modifyBlockEntity(util.grid().at(2, 1, 2), DepotBlockEntity.class,
                depot -> depot.setHeldItem(createGem(GemRegistry.INSTANCE.getValues().stream().findAny().orElseThrow(), GemCutting.Tier.ANCIENT)));
        scene.idle(3);
        scene.world().modifyBlockEntity(util.grid().at(2, 1, 1), DepotBlockEntity.class, depot -> depot.setHeldItem(Items.NETHERITE_SWORD.getDefaultInstance()));
        scene.idle(3);
        scene.world().modifyBlockEntity(util.grid().at(2, 1, 0), DepotBlockEntity.class, depot -> depot.setHeldItem(Items.LEATHER_BOOTS.getDefaultInstance()));
        scene.idle(60);
        scene.world().modifyBlockEntity(util.grid().at(2, 1, 2), DepotBlockEntity.class, depot -> depot.setHeldItem(new ItemStack(Adventure.Items.GEM_DUST.get(), 8)));
        scene.idle(3);
        scene.world().modifyBlockEntity(util.grid().at(2, 1, 1), DepotBlockEntity.class, depot -> depot.setHeldItem(new ItemStack(Adventure.Items.MYTHIC_MATERIAL.get(), 2)));
        scene.idle(3);
        scene.world().modifyBlockEntity(util.grid().at(2, 1, 0), DepotBlockEntity.class, depot -> depot.setHeldItem(new ItemStack(Items.LEATHER, 4)));
        scene.idle(20);
        scene.world().hideSection(util.select().fromTo(2, 1, 0, 2, 1, 2), Direction.UP);
        scene.idle(20);

        var base2 = scene.world().showIndependentSection(util.select().position(2, 3, 0), Direction.UP);
        scene.world().moveSection(base2, new Vec3(0, -2, 0), 0);
        scene.idle(10);
        scene.addKeyframe();
        var pos = util.grid().at(2, 2, 0);
        var armorStand = scene.world().createEntity(level -> {
            ArmorStand as = new ArmorStand(EntityType.ARMOR_STAND, level);
            as.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.DIAMOND_HELMET));
            as.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.DIAMOND_BOOTS));
            as.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.DIAMOND_LEGGINGS));
            as.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.DIAMOND_CHESTPLATE));
            as.setPos(Vec3.atBottomCenterOf(pos));
            as.lookAt(EntityAnchorArgument.Anchor.EYES, as.getEyePosition().add(0, 0, -1));
            return as;
        });
        scene.overlay()
                .showText(80)
                .pointAt(util.vector().topOf(pos))
                .colored(PonderPalette.RED)
                .text("Warning: Infused Dragon Breath has a chance to disassemble equipped items");
        scene.idle(50);
        scene.world().modifyEntity(armorStand, it -> {
            var as = (ArmorStand) it;
            as.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
        });
        scene.world().createItemEntity(util.vector().centerOf(2, 2, 0), new Vec3(0, -0.1, -1), new ItemStack(Items.DIAMOND));
        scene.idle(50);
        scene.world().modifyEntity(armorStand, it -> {
            var as = (ArmorStand) it;
            as.setItemSlot(EquipmentSlot.LEGS, ItemStack.EMPTY);
        });
        scene.world().createItemEntity(util.vector().centerOf(2, 2, 0), new Vec3(0, -0.1, -1), new ItemStack(Adventure.Items.RARE_MATERIAL.get(), 2));
        scene.idle(20);
    }

    public static void gemCutter(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("gem_cutter", "Gem Cutter");
        scene.configureBasePlate(0, 0, 5);
        scene.world().showSection(util.select().layer(0).substract(util.select().position(4, 0, 5)).substract(util.select().position(5, 0, 3)), Direction.UP);
        scene.idle(5);

        scene.world().showSection(util.select().position(2, 1, 3), Direction.DOWN);
        scene.overlay()
                .showText(50)
                .pointAt(util.vector().centerOf(2, 1, 3))
                .attachKeyFrame()
                .text("In Apotheosis, player can upgrade Gem in Gem Cutting Table");
        scene.idle(60);

        scene.world().showSection(util.select().position(2, 1, 1), Direction.DOWN);
        scene.overlay()
                .showText(50)
                .pointAt(util.vector().centerOf(2, 1, 1))
                .text("Gem Cutter can upgrade gem automatically");
        scene.idle(60);

        scene.world().hideSection(util.select().position(2, 1, 3).add(util.select().position(2, 1, 1)), Direction.UP);
        scene.idle(10);
        var belt = scene.world().showIndependentSection(util.select().layer(2).substract(util.select().position(3, 2, 3)), Direction.DOWN);
        scene.world().moveSection(belt, new Vec3(0, -1, 0), 0);
        scene.idle(10);
        scene.overlay().showControls(util.vector().centerOf(2, 1, 2), Pointing.UP, 40).rightClick().withItem(CEIAXBlocks.GEM_CUTTER.asStack());
        scene.overlay()
                .showText(50)
                .attachKeyFrame()
                .independent()
                .text("Gem Cutter must be installed on horizontal Belt or Depot");
        scene.idle(20);
        var cutter = scene.world().showIndependentSection(util.select().position(2, 3, 2), Direction.DOWN);
        scene.world().moveSection(cutter, new Vec3(0, -1, 0), 0);
        scene.idle(40);

        scene.world().moveSection(belt, new Vec3(0, 1, 0), 5);
        scene.world().moveSection(cutter, new Vec3(0, 1, 0), 5);
        scene.idle(10);
        scene.overlay()
                .showText(80)
                .attachKeyFrame()
                .independent()
                .text("Gem Cutter requires a tank of Crystal Essence to work. Fluid Tank must be installed at 2 blocks beneath it");
        scene.idle(10);
        scene.world().showSection(util.select().position(2, 1, 2), Direction.SOUTH);
        scene.idle(10);
        scene.world().showSection(util.select().fromTo(3, 1, 2, 4, 1, 5).add(util.select().position(4, 0, 5)), Direction.WEST);
        scene.idle(7);
        scene.world().showSection(util.select().fromTo(0, 1, 0, 1, 1, 2), Direction.EAST);
        scene.idle(7);
        scene.world().showSection(util.select().position(5, 0, 3), Direction.WEST);
        scene.idle(3);
        scene.world().showSection(util.select().position(3, 2, 3), Direction.NORTH);
        scene.idle(5);
        scene.world().setKineticSpeed(util.select().layer(0), 64);
        scene.world().setKineticSpeed(util.select().layer(1), -128);
        scene.world().setKineticSpeed(util.select().layer(2), 64);
        scene.world().setKineticSpeed(util.select().fromTo(0, 2, 0, 0, 2, 2), -64);
        scene.idle(20);
        scene.world().modifyBlockEntity(util.grid().at(2, 1, 2), FluidTankBlockEntity.class,
                be -> be.getTankInventory().fill(new FluidStack(CEIAXFluids.CRYSTAL_ESSENCE.get(), 4000), IFluidHandler.FluidAction.EXECUTE));
        scene.idle(10);
        scene.world().modifyBlockEntity(util.grid().at(2, 1, 2), FluidTankBlockEntity.class,
                be -> be.getTankInventory().fill(new FluidStack(CEIAXFluids.CRYSTAL_ESSENCE.get(), 8000), IFluidHandler.FluidAction.EXECUTE));
        scene.world().modifyBlockEntity(util.grid().at(2, 3, 2), GemCutterBlockEntity.class, be -> be.powered = true);
        scene.idle(40);

        if (!DatagenModLoader.isRunningDataGen()) {
            scene.addKeyframe();
            var gemKind = GemRegistry.INSTANCE.getValues().stream().findAny().orElseThrow();
            var gem = createGem(gemKind, GemCutting.Tier.MYTHIC);
            var gemStack = scene.world().createItemOnBelt(util.grid().at(4, 2, 2), Direction.EAST, gem);
            scene.idle(18);
            scene.world().stallBeltItem(gemStack, true);
            scene.world().modifyBlockEntity(util.grid().at(2, 3, 2), GemCutterBlockEntity.class, be -> be.processingTicks = 200);
            scene.idle(5);
            scene.overlay()
                    .showText(50)
                    .pointAt(util.vector().centerOf(2, 3, 2))
                    .text("Processing...");
            scene.idle(205);
            scene.world().removeItemsFromBelt(util.grid().at(2, 2, 2));
            gem = createGem(gemKind, GemCutting.Tier.ANCIENT);
            scene.world().createItemOnBelt(util.grid().at(2, 2, 2), Direction.UP, gem);
        } else scene.overlay().showText(1).independent().text("Processing...");
    }

    private static ItemStack createGem(Gem gem, GemCutting.Tier tier) {
        ItemStack stack = new ItemStack(Adventure.Items.GEM.get());
        GemItem.setGem(stack, gem);
        var rarity = tier.holder();
        if (rarity.isBound())
            AffixHelper.setRarity(stack, rarity.get());
        return stack;
    }

    public static void affixAugmentor(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("affix", "Affix Augmentor");
        scene.configureBasePlate(0, 0, 5);
        scene.world().showSection(util.select().layer(0).substract(util.select().position(5, 0, 2)), Direction.UP);
        scene.idle(5);

        var aug = scene.world().showIndependentSection(util.select().position(2, 2, 3), Direction.DOWN);
        scene.world().moveSection(aug, new Vec3(0, -1, 0), 0);
        scene.overlay()
                .showText(50)
                .pointAt(util.vector().centerOf(2, 1, 3))
                .attachKeyFrame()
                .text("In Apotheosis, player can upgrade Affix of equipment in Augmenting Table");
        scene.idle(60);

        var augt = scene.world().showIndependentSection(util.select().position(2, 2, 1), Direction.DOWN);
        scene.world().moveSection(augt, new Vec3(0, -1, 0), 0);
        scene.overlay()
                .showText(50)
                .pointAt(util.vector().centerOf(2, 1, 1))
                .text("Affix Augmentor can upgrade Affix of equipment automatically");
        scene.idle(60);

        scene.world().hideIndependentSection(aug, Direction.UP);
        scene.world().hideIndependentSection(augt, Direction.UP);
        scene.idle(10);
        var belt = scene.world().showIndependentSection(util.select().fromTo(0, 2, 2, 4, 2, 2), Direction.DOWN);
        scene.world().moveSection(belt, new Vec3(0, -1, 0), 0);
        scene.idle(10);
        scene.overlay().showControls(util.vector().centerOf(2, 1, 2), Pointing.UP, 40).rightClick().withItem(CEIAXBlocks.AFFIX_AUGMENTOR.asStack());
        scene.overlay()
                .showText(50)
                .attachKeyFrame()
                .independent()
                .text("Affix Augmentor must be installed on horizontal Belt or Depot");
        scene.idle(20);
        var augmentor = scene.world().showIndependentSection(util.select().position(2, 3, 2), Direction.DOWN);
        scene.world().moveSection(augmentor, new Vec3(0, -1, 0), 0);
        scene.idle(40);

        scene.world().moveSection(belt, new Vec3(0, 1, 0), 5);
        scene.world().moveSection(augmentor, new Vec3(0, 1, 0), 5);
        scene.idle(10);
        scene.overlay()
                .showText(80)
                .attachKeyFrame()
                .independent()
                .text("Affix Augmentor requires a tank of Apotheotic Essence to work. Fluid Tank must be installed at 2 blocks beneath it");
        scene.idle(10);
        scene.world().showSection(util.select().fromTo(1, 1, 1, 3, 1, 3), Direction.SOUTH);
        scene.idle(10);
        scene.world().showSection(util.select().fromTo(4, 1, 2, 5, 1, 2).add(util.select().position(5, 0, 2)), Direction.WEST);
        scene.idle(7);
        scene.world().showSection(util.select().position(0, 1, 2), Direction.EAST);
        scene.idle(7);
        scene.world().showSection(util.select().position(5, 1, 3), Direction.WEST);
        scene.idle(3);
        scene.world().showSection(util.select().position(4, 2, 3), Direction.WEST);
        scene.idle(4);
        scene.world().setKineticSpeed(util.select().layer(0), 256);
        scene.idle(20);
        scene.world().modifyBlockEntity(util.grid().at(1, 1, 1), FluidTankBlockEntity.class,
                be -> be.getTankInventory().fill(new FluidStack(CEIAXFluids.APOTHEOTIC_ESSENCE.get(), 36000), IFluidHandler.FluidAction.EXECUTE));
        scene.world().setKineticSpeed(util.select().layer(2), 64);
        scene.world().setKineticSpeed(util.select().layer(1), -32);
        scene.idle(10);
        scene.world().modifyBlockEntity(util.grid().at(1, 1, 1), FluidTankBlockEntity.class,
                be -> be.getTankInventory().fill(new FluidStack(CEIAXFluids.APOTHEOTIC_ESSENCE.get(), 72000), IFluidHandler.FluidAction.EXECUTE));
        scene.world().modifyBlockEntity(util.grid().at(2, 3, 2), AffixAugmentorBlockEntity.class, be -> be.powered = true);
        scene.idle(40);

        scene.addKeyframe();
        var stack = scene.world().createItemOnBelt(util.grid().at(4, 2, 2), Direction.EAST, Items.DIAMOND_SWORD.getDefaultInstance());
        scene.idle(18);
        scene.world().stallBeltItem(stack, true);
        scene.world().modifyBlockEntity(util.grid().at(2, 3, 2), AffixAugmentorBlockEntity.class, be -> be.processingTicks = 200);
        scene.idle(5);
        scene.overlay()
                .showText(50)
                .pointAt(util.vector().centerOf(2, 3, 2))
                .text("Processing...");
        scene.idle(205);
        scene.world().stallBeltItem(stack, false);
    }
}
