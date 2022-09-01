package plus.dragons.createenchantmentindustry.entry;

import com.simibubi.create.AllTags;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.client.renderer.RenderType;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;
import plus.dragons.createenchantmentindustry.contraptions.enchantments.CopierBlock;
import plus.dragons.createenchantmentindustry.contraptions.enchantments.DisenchanterBlock;


public class ModBlocks {

    private static final CreateRegistrate REGISTRATE = EnchantmentIndustry.registrate()
            // TODO Consider should we makke our own tab?
            .creativeModeTab(() -> Create.BASE_CREATIVE_TAB);

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

    public static void register(){}
}
