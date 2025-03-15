package plus.dragons.createenchantmentindustry.dragonLibLegacy.fluid;

import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.FluidInteractionRegistry;
import net.neoforged.neoforge.fluids.FluidType;

import javax.annotation.Nullable;
import java.util.IdentityHashMap;

public record FluidLavaReaction(BlockState withLava, BlockState withFlowingLava, BlockState lavaOnSelf) {

    private static final IdentityHashMap<FluidType, FluidLavaReaction> REACTIONS = new IdentityHashMap<>();

    public static void register(FluidType type, BlockState withLava, BlockState withFlowingLava, BlockState lavaOnSelf) {
        FluidInteractionRegistry.addInteraction(NeoForgeMod.LAVA_TYPE.value(), new FluidInteractionRegistry.InteractionInformation(
                type, fluidState -> fluidState.isSource() ? withLava : withFlowingLava
        ));
        FluidInteractionRegistry.addInteraction(type, new FluidInteractionRegistry.InteractionInformation(
                NeoForgeMod.LAVA_TYPE.value(), lavaOnSelf
        ));
        REACTIONS.put(type, new FluidLavaReaction(withLava, withFlowingLava, lavaOnSelf));
    }

    @Nullable
    public static FluidLavaReaction get(FluidType fluid) {
        return REACTIONS.get(fluid);
    }

}
