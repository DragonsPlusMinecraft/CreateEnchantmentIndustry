package plus.dragons.createenchantmentindustry.entry;

import com.simibubi.create.Create;
import com.simibubi.create.content.AllSections;
import com.simibubi.create.content.contraptions.components.AssemblyOperatorBlockItem;
import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.client.renderer.RenderType;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;
import plus.dragons.createenchantmentindustry.content.contraptions.enchantments.CopierBlock;
import plus.dragons.createenchantmentindustry.content.contraptions.enchantments.DisenchanterBlock;
import plus.dragons.createenchantmentindustry.content.contraptions.enchantments.BlazeEnchanterBlock;


public class ModBlocks {

    private static final CreateRegistrate REGISTRATE = EnchantmentIndustry.registrate()
            .creativeModeTab(() -> Create.BASE_CREATIVE_TAB).startSection(AllSections.KINETICS);

    public static final BlockEntry<DisenchanterBlock> DISENCHANTER = REGISTRATE
            .block("disenchanter", DisenchanterBlock::new)
            .initialProperties(SharedProperties::copperMetal)
            .addLayer(() -> RenderType::cutoutMipped)
            .transform(ModTags.pickaxeOnly())
            .blockstate((ctx, pov) -> pov.simpleBlock(ctx.get(), AssetLookup.standardModel(ctx, pov)))
            .simpleItem()
            .register();

    public static final BlockEntry<CopierBlock> COPIER = REGISTRATE
            .block("copier", CopierBlock::new)
            .initialProperties(SharedProperties::copperMetal)
            .addLayer(() -> RenderType::cutoutMipped)
            .transform(ModTags.pickaxeOnly())
            .blockstate((ctx, pov) -> pov.simpleBlock(ctx.get(), AssetLookup.partialBaseModel(ctx, pov)))
            .item(AssemblyOperatorBlockItem::new)
            .model(AssetLookup::customItemModel)
            .build()
            .register();

    public static final BlockEntry<BlazeEnchanterBlock> BLAZE_ENCHANTER = REGISTRATE
            .block("blaze_enchanter", BlazeEnchanterBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .addLayer(() -> RenderType::cutoutMipped)
            .transform(ModTags.pickaxeOnly())
            .blockstate((ctx, pov) -> pov.simpleBlock(ctx.get(), AssetLookup.standardModel(ctx, pov)))
            .register();
    
    public static void register() {
    }
}
