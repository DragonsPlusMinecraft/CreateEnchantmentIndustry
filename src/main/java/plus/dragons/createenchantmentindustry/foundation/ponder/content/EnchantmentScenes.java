package plus.dragons.createenchantmentindustry.foundation.ponder.content;

import com.simibubi.create.foundation.ponder.ElementLink;
import com.simibubi.create.foundation.ponder.SceneBuilder;
import com.simibubi.create.foundation.ponder.SceneBuildingUtil;
import com.simibubi.create.foundation.ponder.element.EntityElement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

public class EnchantmentScenes {
    //TODO
    public static void disenchant(SceneBuilder scene, SceneBuildingUtil util){
        scene.title("disenchant", "Disenchanting");
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
            ElementLink<EntityElement> item1 = scene.world.createItemEntity(util.vector.centerOf(6, 5, 1), util.vector.of(0, 0, 0), item);
            scene.idle(13);
            scene.world.modifyEntity(item1, Entity::discard);
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

    private static void enchant(ItemStack itemStack, Enchantment enchantment, int level){
        var m = EnchantmentHelper.getEnchantments(itemStack);
        m.put(enchantment,level);
        EnchantmentHelper.setEnchantments(m,itemStack);
    }

    private static void enchantRandomly(ItemStack itemStack){
        if(itemStack.is(Items.ENCHANTED_BOOK)){
            enchant(itemStack, Enchantments.MENDING,1);
        }
        else EnchantmentHelper.enchantItem(new Random(),itemStack,30,true);
    }
}
