package plus.dragons.createenchantmentindustry.entry;

import com.tterrag.registrate.util.entry.BlockEntityEntry;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;
import plus.dragons.createenchantmentindustry.content.contraptions.enchantments.*;

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

    public static final BlockEntityEntry<BlazeEnchanterBlockEntity> BLAZE_ENCHANTING_ALTER = EnchantmentIndustry.registrate()
            .tileEntity("blaze_enchanting_later", BlazeEnchanterBlockEntity::new)
            .validBlocks(ModBlocks.BLAZE_ENCHANTER)
            .renderer(() -> BlazeEnchanterRenderer::new)
            .register();

    public static void register() {
    }
}
