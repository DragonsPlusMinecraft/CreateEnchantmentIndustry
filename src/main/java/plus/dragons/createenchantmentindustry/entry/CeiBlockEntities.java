package plus.dragons.createenchantmentindustry.entry;

import com.tterrag.registrate.util.entry.BlockEntityEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.disenchanter.DisenchanterBlockEntity;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.disenchanter.DisenchanterRenderer;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter.BlazeEnchanterBlockEntity;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter.BlazeEnchanterRenderer;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.printer.PrinterBlockEntity;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.printer.PrinterRenderer;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.FurnaceExpExtractor;

import java.util.Optional;

import static plus.dragons.createenchantmentindustry.EnchantmentIndustry.REGISTRATE;

public class CeiBlockEntities {

    public static final BlockEntityEntry<DisenchanterBlockEntity> DISENCHANTER = REGISTRATE
            .blockEntity("disenchanter", DisenchanterBlockEntity::new)
            .validBlocks(CeiBlocks.DISENCHANTER)
            .renderer(() -> DisenchanterRenderer::new)
            .register();

    public static final BlockEntityEntry<PrinterBlockEntity> PRINTER = REGISTRATE
            .blockEntity("printer", PrinterBlockEntity::new)
            .validBlocks(CeiBlocks.PRINTER)
            .renderer(() -> PrinterRenderer::new)
            .register();

    public static final BlockEntityEntry<BlazeEnchanterBlockEntity> BLAZE_ENCHANTER = REGISTRATE
            .blockEntity("blaze_enchanter", BlazeEnchanterBlockEntity::new)
            .validBlocks(CeiBlocks.BLAZE_ENCHANTER)
            .renderer(() -> BlazeEnchanterRenderer::new)
            .register();
    
    public static void register() {}

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        DisenchanterBlockEntity.registerCapabilities(event);
        PrinterBlockEntity.registerCapabilities(event);
        BlazeEnchanterBlockEntity.registerCapabilities(event);

        registerOptional(event, AbstractFurnaceBlockEntity.class, (be, side) -> new FurnaceExpExtractor(be.recipesUsed, be));
    }

    public static <BE extends BlockEntity> void registerOptional(RegisterCapabilitiesEvent e, Class<BE> beClass, ICapabilityProvider<BE, Direction, IFluidHandler> provider) {
        for (BlockEntityType<?> beType : BuiltInRegistries.BLOCK_ENTITY_TYPE) {
            final Optional<Block> optState = beType.getValidBlocks().stream().findAny();
            if(optState.isEmpty()) continue;
            BlockState blockState = optState.get().defaultBlockState();
            if(beClass.isInstance(beType.create(BlockPos.ZERO, blockState))) {
                //noinspection unchecked
                e.registerBlockEntity(Capabilities.FluidHandler.BLOCK, (BlockEntityType<? extends BE>) beType,
                        (be, side) -> provider.getCapability(beClass.cast(be), side));
            }
        }
    }
}
