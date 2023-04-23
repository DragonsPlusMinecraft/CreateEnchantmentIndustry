package plus.dragons.createenchantmentindustry.compat.apotheosis;

import com.simibubi.create.content.contraptions.components.mixer.MixingRecipe;
import com.simibubi.create.content.contraptions.fluids.potion.PotionFluidHandler;
import com.simibubi.create.content.contraptions.fluids.recipe.PotionMixingRecipes;
import com.simibubi.create.content.contraptions.processing.HeatCondition;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipeBuilder;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;

public class ApotheosisCompat {
    public static final RegistryObject<Potion> KNOWLEDGE =
        RegistryObject.create(new ResourceLocation("apotheosis", "knowledge"), ForgeRegistries.POTIONS);
    
    public static void addPotionMixingRecipes() {
        if (!KNOWLEDGE.isPresent())
            return;
        PotionMixingRecipes.SUPPORTED_CONTAINERS
            .stream()
            .filter(container -> PotionBrewing.ALLOWED_CONTAINER.test(new ItemStack(container)))
            .map(PotionFluidHandler::bottleTypeFromItem)
            .distinct()
            .sorted()
            .forEachOrdered(bottle -> {
                String prefix = switch (bottle) {
                    case REGULAR -> "";
                    case SPLASH -> "splash_";
                    case LINGERING -> "lingering_";
                };
                FluidStack awkward = PotionFluidHandler.getFluidFromPotion(Potions.AWKWARD, bottle, 1000);
                FluidStack knowledge = PotionFluidHandler.getFluidFromPotion(KNOWLEDGE.get(), bottle, 1000);
                MixingRecipe recipe = new ProcessingRecipeBuilder<>(MixingRecipe::new,
                    EnchantmentIndustry.genRL("compat/apotheosis/potion_mixing/" + prefix + "knowledge"))
                    .require(CeiFluids.EXPERIENCE.get(), 10)
                    .require(FluidIngredient.fromFluidStack(awkward))
                    .output(knowledge)
                    .requiresHeat(HeatCondition.HEATED)
                    .build();
                PotionMixingRecipes.ALL.add(recipe);
            });
    }
    
}
