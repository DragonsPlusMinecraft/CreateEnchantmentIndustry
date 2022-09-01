package plus.dragons.createenchantmentindustry.entry;

import com.tterrag.registrate.util.entry.BlockEntityEntry;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;
import plus.dragons.createenchantmentindustry.contraptions.enchantments.CopierBlockEntity;
import plus.dragons.createenchantmentindustry.contraptions.enchantments.CopierRenderer;
import plus.dragons.createenchantmentindustry.contraptions.enchantments.DisenchanterBlockEntity;
import plus.dragons.createenchantmentindustry.contraptions.enchantments.DisenchanterRenderer;

public class ModBlockEntities {

    public static final BlockEntityEntry<DisenchanterBlockEntity> DISENCHANTER = EnchantmentIndustry.registrate()
            .tileEntity("disenchanter", DisenchanterBlockEntity::new)
            .validBlocks(ModBlocks.DISENCHANTER)
            .renderer(() -> DisenchanterRenderer::new)
            .register();

    public static final BlockEntityEntry<CopierBlockEntity> COPIER = EnchantmentIndustry.registrate()
            .tileEntity("copier_machine", CopierBlockEntity::new)
            .validBlocks(ModBlocks.COPIER)
            .renderer(() -> CopierRenderer::new)
            .register();

    public static void register(){}
}
