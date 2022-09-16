package plus.dragons.createenchantmentindustry.entry;

import com.simibubi.create.AllTags;
import com.simibubi.create.content.AllSections;
import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.client.renderer.RenderType;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;
import plus.dragons.createenchantmentindustry.content.contraptions.enchantments.CopierBlock;
import plus.dragons.createenchantmentindustry.content.contraptions.enchantments.DisenchanterBlock;
import plus.dragons.createenchantmentindustry.content.contraptions.enchantments.EnchantingAlterBlock;


public class ModBlocks {

    private static final CreateRegistrate REGISTRATE = EnchantmentIndustry.registrate()
            // TODO Consider should we make our own tab?
            .creativeModeTab(() -> EnchantmentIndustry.CREATIVE_TAB);

    static {
        REGISTRATE.startSection(AllSections.KINETICS);
    }

    public static final BlockEntry<DisenchanterBlock> DISENCHANTER = REGISTRATE
            .block("disenchanter", DisenchanterBlock::new)
            .initialProperties(SharedProperties::copperMetal)
            .transform(AllTags.pickaxeOnly())
            .addLayer(() -> RenderType::cutoutMipped)
            .blockstate((ctx, pov) -> pov.simpleBlock(ctx.get(), AssetLookup.standardModel(ctx, pov)))
            .simpleItem()
            .register();

    public static final BlockEntry<CopierBlock> COPIER = REGISTRATE
            .block("copier_machine", CopierBlock::new)
            .initialProperties(SharedProperties::copperMetal)
            .transform(AllTags.pickaxeOnly())
            .addLayer(() -> RenderType::cutoutMipped)
            .blockstate((ctx, pov) -> pov.simpleBlock(ctx.get(), AssetLookup.standardModel(ctx, pov)))
            .simpleItem()
            .register();

    public static final BlockEntry<EnchantingAlterBlock> BLAZE_ENCHANTING_ALTER = REGISTRATE
            .block("blaze_enchanting_alter", EnchantingAlterBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .transform(AllTags.pickaxeOnly())
            .addLayer(() -> RenderType::cutoutMipped)
            .blockstate((ctx, pov) -> pov.simpleBlock(ctx.get(), AssetLookup.standardModel(ctx, pov)))
            .register();

    public static void register() {
    }
}
