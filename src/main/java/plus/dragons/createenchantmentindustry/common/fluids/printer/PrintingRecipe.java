/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package plus.dragons.createenchantmentindustry.common.fluids.printer;

import com.google.gson.JsonObject;
import com.simibubi.create.compat.jei.category.sequencedAssembly.SequencedAssemblySubCategory;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder.ProcessingRecipeParams;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeSerializer;
import com.simibubi.create.content.processing.sequenced.IAssemblyRecipe;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import plus.dragons.createenchantmentindustry.common.registry.CEIBlocks;
import plus.dragons.createenchantmentindustry.common.registry.CEIRecipes;
import plus.dragons.createenchantmentindustry.integration.jei.category.assembly.AssemblyPrintingCategory;
import plus.dragons.createenchantmentindustry.util.CEILang;

public class PrintingRecipe extends ProcessingRecipe<PrintingInput> implements IAssemblyRecipe {
    private SoundEvent sound = SoundEvents.ENCHANTMENT_TABLE_USE;
    private float volume = 1.0F;
    private float minimumPitch = 0.9F;
    private float maximumPitch = 1.1F;

    public PrintingRecipe(ProcessingRecipeParams params) {
        super(CEIRecipes.PRINTING, params);
    }

    public static Builder builder(ResourceLocation id, SoundEvent sound) {
        return new Builder(id, sound);
    }

    public static Builder builder(ResourceLocation id) {
        return new Builder(id, SoundEvents.ENCHANTMENT_TABLE_USE);
    }

    public void playSound(Level level, BlockPos pos, SoundSource source) {
        float pitch = minimumPitch + level.random.nextFloat() * (maximumPitch - minimumPitch);
        level.playSound(null, pos, sound, source, volume, pitch);
    }

    @Override
    protected int getMaxInputCount() {
        return 2;
    }

    @Override
    protected int getMaxFluidInputCount() {
        return 1;
    }

    @Override
    protected int getMaxOutputCount() {
        return 1;
    }

    @Override
    public boolean matches(PrintingInput input, Level level) {
        return ingredients.size() == 2
                && fluidIngredients.size() == 1
                && ingredients.get(0).test(input.base())
                && ingredients.get(1).test(input.template())
                && (input.fluid().isEmpty() || fluidIngredients.get(0).test(input.fluid()));
    }

    @Override
    public Component getDescriptionForAssembly() {
        ItemStack[] matchingStacks = ingredients.get(1).getItems();
        List<FluidStack> matchingFluids = fluidIngredients.get(0).getMatchingFluidStacks();
        if (matchingStacks.length == 0 || matchingFluids.isEmpty()) {
            return Component.literal("Invalid");
        }
        return CEILang.translate(
                "recipe.assembly.printing",
                matchingStacks[0].getHoverName(),
                matchingFluids.get(0).getDisplayName())
                .component();
    }

    @Override
    public void addRequiredMachines(Set<ItemLike> required) {
        required.add(CEIBlocks.PRINTER.get());
    }

    @Override
    public void addAssemblyIngredients(List<Ingredient> list) {
        list.add(getIngredients().get(1));
    }

    @Override
    public void addAssemblyFluidIngredients(List<FluidIngredient> list) {
        list.add(getFluidIngredients().get(0));
    }

    @Override
    public Supplier<Supplier<SequencedAssemblySubCategory>> getJEISubCategory() {
        return () -> AssemblyPrintingCategory::new;
    }

    @Override
    public void readAdditional(JsonObject json) {
        if (!json.has("sound")) {
            return;
        }
        JsonObject soundData;
        if (json.get("sound").isJsonPrimitive()) {
            ResourceLocation id = new ResourceLocation(json.get("sound").getAsString());
            sound = BuiltInRegistries.SOUND_EVENT.getOptional(id).orElse(SoundEvents.ENCHANTMENT_TABLE_USE);
            return;
        }
        soundData = json.getAsJsonObject("sound");
        ResourceLocation id = new ResourceLocation(GsonHelper.getAsString(soundData, "sound"));
        sound = BuiltInRegistries.SOUND_EVENT.getOptional(id).orElse(SoundEvents.ENCHANTMENT_TABLE_USE);
        volume = GsonHelper.getAsFloat(soundData, "volume", 1.0F);
        minimumPitch = GsonHelper.getAsFloat(soundData, "minimum_pitch", 0.9F);
        maximumPitch = GsonHelper.getAsFloat(soundData, "maximum_pitch", 1.1F);
    }

    @Override
    public void writeAdditional(JsonObject json) {
        JsonObject soundData = new JsonObject();
        soundData.addProperty("sound", BuiltInRegistries.SOUND_EVENT.getKey(sound).toString());
        soundData.addProperty("volume", volume);
        soundData.addProperty("minimum_pitch", minimumPitch);
        soundData.addProperty("maximum_pitch", maximumPitch);
        json.add("sound", soundData);
    }

    @Override
    public void readAdditional(FriendlyByteBuf buffer) {
        sound = BuiltInRegistries.SOUND_EVENT.get(buffer.readResourceLocation());
        volume = buffer.readFloat();
        minimumPitch = buffer.readFloat();
        maximumPitch = buffer.readFloat();
    }

    @Override
    public void writeAdditional(FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(BuiltInRegistries.SOUND_EVENT.getKey(sound));
        buffer.writeFloat(volume);
        buffer.writeFloat(minimumPitch);
        buffer.writeFloat(maximumPitch);
    }

    public static class Builder extends ProcessingRecipeBuilder<PrintingRecipe> {
        private final SoundEvent sound;

        protected Builder(ResourceLocation id, SoundEvent sound) {
            super(PrintingRecipe::new, id);
            this.sound = sound;
        }

        @Override
        public PrintingRecipe build() {
            PrintingRecipe recipe = super.build();
            recipe.sound = sound;
            return recipe;
        }
    }

    public static class Serializer<R extends PrintingRecipe> extends ProcessingRecipeSerializer<R> {
        public Serializer(ProcessingRecipeBuilder.ProcessingRecipeFactory<R> factory) {
            super(factory);
        }
    }
}
