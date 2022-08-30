package plus.dragons.createenchantmentindustry.contraptions.enchantments;

import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.simibubi.create.foundation.block.ITE;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class DisenchanterBlock extends Block implements IWrenchable, ITE<DisenchanterBlockEntity> {

    public DisenchanterBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public Class<DisenchanterBlockEntity> getTileEntityClass() {
        return null;
    }

    @Override
    public BlockEntityType<? extends DisenchanterBlockEntity> getTileEntityType() {
        return null;
    }
}
