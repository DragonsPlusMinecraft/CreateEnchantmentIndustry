package plus.dragons.createenchantmentindustry.entry;

import com.tterrag.registrate.util.entry.BlockEntityEntry;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.copier.CopierBlockEntity;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.copier.CopierRenderer;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.disenchanter.DisenchanterBlockEntity;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.disenchanter.DisenchanterRenderer;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter.BlazeEnchanterBlockEntity;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter.BlazeEnchanterRenderer;

import static plus.dragons.createenchantmentindustry.EnchantmentIndustry.REGISTRATE;

public class CeiBlockEntities {

    public static final BlockEntityEntry<DisenchanterBlockEntity> DISENCHANTER = REGISTRATE
            .tileEntity("disenchanter", DisenchanterBlockEntity::new)
            .validBlocks(CeiBlocks.DISENCHANTER)
            .renderer(() -> DisenchanterRenderer::new)
            .register();

    public static final BlockEntityEntry<CopierBlockEntity> COPIER = REGISTRATE
            .tileEntity("copier_machine", CopierBlockEntity::new)
            .validBlocks(CeiBlocks.COPIER)
            .renderer(() -> CopierRenderer::new)
            .register();

    public static final BlockEntityEntry<BlazeEnchanterBlockEntity> BLAZE_ENCHANTING_ALTER = REGISTRATE
            .tileEntity("blaze_enchanting_later", BlazeEnchanterBlockEntity::new)
            .validBlocks(CeiBlocks.BLAZE_ENCHANTER)
            .renderer(() -> BlazeEnchanterRenderer::new)
            .register();

    public static void register() {
    }
}
