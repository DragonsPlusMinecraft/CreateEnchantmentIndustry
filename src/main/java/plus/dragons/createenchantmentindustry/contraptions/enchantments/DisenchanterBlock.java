package plus.dragons.createenchantmentindustry.contraptions.enchantments;

import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.simibubi.create.foundation.block.ITE;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import plus.dragons.createenchantmentindustry.entry.ModBlockEntities;

public class DisenchanterBlock extends Block implements IWrenchable, ITE<DisenchanterBlockEntity> {

    public DisenchanterBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public Class<DisenchanterBlockEntity> getTileEntityClass() {
        return DisenchanterBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends DisenchanterBlockEntity> getTileEntityType() {
        return ModBlockEntities.DISENCHANTER.get();
    }
}
