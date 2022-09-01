package plus.dragons.createenchantmentindustry.content.contraptions.enchantments;

import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.simibubi.create.foundation.block.ITE;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import plus.dragons.createenchantmentindustry.entry.ModBlockEntities;

public class EnchantingAlterBlock  extends Block implements IWrenchable, ITE<EnchantingAlterBlockEntity> {
    public EnchantingAlterBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public Class<EnchantingAlterBlockEntity> getTileEntityClass() {
        return EnchantingAlterBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends EnchantingAlterBlockEntity> getTileEntityType() {
        return ModBlockEntities.BLAZE_ENCHANTING_ALTER.get();
    }
}
