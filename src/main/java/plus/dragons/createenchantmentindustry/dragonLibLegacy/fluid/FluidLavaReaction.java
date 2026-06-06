package plus.dragons.createenchantmentindustry.dragonLibLegacy.fluid;

import java.util.IdentityHashMap;
import javax.annotation.Nullable;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.fluids.FluidInteractionRegistry;
import net.minecraftforge.fluids.FluidType;

public record FluidLavaReaction(BlockState withLava, BlockState withFlowingLava, BlockState lavaOnSelf) {

    private static final IdentityHashMap<FluidType, FluidLavaReaction> REACTIONS = new IdentityHashMap<>();
    public static void register(FluidType type, BlockState withLava, BlockState withFlowingLava, BlockState lavaOnSelf) {
        FluidInteractionRegistry.addInteraction(ForgeMod.LAVA_TYPE.get(), new FluidInteractionRegistry.InteractionInformation(
                type, fluidState -> fluidState.isSource() ? withLava : withFlowingLava));
        FluidInteractionRegistry.addInteraction(type, new FluidInteractionRegistry.InteractionInformation(
                ForgeMod.LAVA_TYPE.get(), lavaOnSelf));
        REACTIONS.put(type, new FluidLavaReaction(withLava, withFlowingLava, lavaOnSelf));
    }

    @Nullable
    public static FluidLavaReaction get(FluidType fluid) {
        return REACTIONS.get(fluid);
    }
}
