package plus.dragons.createenchantmentindustry.entry;

import com.google.common.collect.ImmutableMap;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.event.RegistryEvent;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.printer.PrinterBlockEntity;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.printer.PrinterRenderer;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.disenchanter.DisenchanterBlockEntity;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.disenchanter.DisenchanterRenderer;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter.BlazeEnchanterBlockEntity;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter.BlazeEnchanterRenderer;

public class CeiBlockEntities {

    public static final BlockEntityEntry<DisenchanterBlockEntity> DISENCHANTER = EnchantmentIndustry.registrate()
            .tileEntity("disenchanter", DisenchanterBlockEntity::new)
            .validBlocks(CeiBlocks.DISENCHANTER)
            .renderer(() -> DisenchanterRenderer::new)
            .register();

    public static final BlockEntityEntry<PrinterBlockEntity> PRINTER = EnchantmentIndustry.registrate()
            .tileEntity("printer", PrinterBlockEntity::new)
            .validBlocks(CeiBlocks.PRINTER)
            .renderer(() -> PrinterRenderer::new)
            .register();

    public static final BlockEntityEntry<BlazeEnchanterBlockEntity> BLAZE_ENCHANTER = EnchantmentIndustry.registrate()
            .tileEntity("blaze_enchanter", BlazeEnchanterBlockEntity::new)
            .validBlocks(CeiBlocks.BLAZE_ENCHANTER)
            .renderer(() -> BlazeEnchanterRenderer::new)
            .register();
    
    public static void remap(RegistryEvent.MissingMappings<BlockEntityType<?>> event) {
        var mappings = event.getMappings(EnchantmentIndustry.ID);
        var remaps = ImmutableMap.<ResourceLocation, BlockEntityEntry<?>>builder()
            .put(EnchantmentIndustry.genRL("copier_machine"), PRINTER)
            .put(EnchantmentIndustry.genRL("blaze_enchanting_later"), BLAZE_ENCHANTER)
            .build();
        for (var mapping : mappings) {
            var remap = remaps.get(mapping.key);
            if (remap != null) {
                mapping.remap(remap.get());
                EnchantmentIndustry.LOGGER.warn("Remapping block entity [{}] to [{}]...", mapping.key, remap.getId());
            }
        }
    }

    public static void register() {}
    
}
