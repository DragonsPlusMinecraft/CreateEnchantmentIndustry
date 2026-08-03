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

package plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.processing.infuser;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.simple.DeferralBehaviour;
import com.simibubi.create.foundation.recipe.RecipeFinder;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.ench.table.ApothEnchantmentMenu;
import dev.shadowsoffire.apotheosis.ench.table.EnchantingRecipe;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createdragonsplus.common.advancements.AdvancementBehaviour;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.registry.CEIAFluids;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.registry.CEIARecipes;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.config.CEIAConfig;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.util.CEIALang;

public class InfuserBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {
    public static final int PROCESSING_TIME = 80;
    private static final List<Recipe<?>> CACHED_RECIPES = new ArrayList<>();
    public static final ResourceManagerReloadListener RELOAD_LISTENER = resourceManager -> CACHED_RECIPES.clear();

    public int runningTicks;
    public int processingTicks;
    public boolean running;
    public InfusionStats infusionStats;
    public DeferralBehaviour basinChecker;
    public boolean basinRemoved;
    protected Recipe<?> currentRecipe;

    protected SmartFluidTankBehaviour tank;
    private AdvancementBehaviour advancement;

    public InfuserBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
        infusionStats = new InfusionStats(0, 0, 0);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        tank = SmartFluidTankBehaviour.single(this, CEIAConfig.server().fluids().infuserFluidCapacity.get());
        advancement = new AdvancementBehaviour(this);
        basinChecker = new DeferralBehaviour(this, this::updateBasin);
        behaviours.add(tank);
        behaviours.add(advancement);
        behaviours.add(basinChecker);
    }

    @Override
    public void tick() {
        if (basinRemoved) {
            basinRemoved = false;
            onBasinRemoved();
            sendData();
            return;
        }

        super.tick();

        if (runningTicks >= 40) {
            running = false;
            runningTicks = 0;
            basinChecker.scheduleUpdate();
            return;
        }

        if (running && level != null) {

            if (level.isClientSide) {
                if (processingTicks > 0)
                    processingTicks--;
                if (processingTicks > 10)
                    spawnParticles();
            }

            if ((!level.isClientSide || isVirtual()) && runningTicks == 10) {
                if (processingTicks < 0) {
                    processingTicks = PROCESSING_TIME;
                    sendData();
                } else {
                    processingTicks--;
                    if (processingTicks == 0) {
                        runningTicks++;
                        processingTicks = -1;
                        level.playSound(null, worldPosition, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, level.random.nextFloat() * 0.1F + 0.9F);
                        applyBasinRecipe();
                        sendData();
                    }
                }
            }

            if (runningTicks != 10)
                runningTicks++;
        }

        if (!running && level != null && level.getGameTime() % 200 == 0 && !level.isClientSide && updateInfusionStats()) {
            sendData();
            basinChecker.scheduleUpdate();
        }
    }

    private void spawnParticles() {
        ParticleOptions data = ParticleTypes.ENCHANT;
        Vec3 center = VecHelper.getCenterOf(worldPosition).add(0, -0.5 - 2 / 16f, 0);
        for (int i = 0; i < 5; i++) {
            var c = VecHelper.offsetRandomly(center, level.random, 4 / 16f);
            level.addParticle(data, c.x, center.y, c.z, 0, 0, 0);
        }
    }

    protected boolean updateBasin() {
        if (isRunning())
            return true;
        if (level == null || level.isClientSide)
            return true;
        Optional<BasinBlockEntity> basin = getBasin();
        if (!basin.filter(BasinBlockEntity::canContinueProcessing)
                .isPresent())
            return true;

        List<Recipe<?>> recipes = getMatchingRecipes();
        if (recipes.isEmpty())
            return true;
        currentRecipe = recipes.get(0);
        startProcessingBasin();
        sendData();
        return true;
    }

    protected boolean matchBasinRecipe(Recipe<?> recipe) {
        if (recipe == null)
            return false;
        Optional<BasinBlockEntity> basin = getBasin();
        if (!basin.isPresent())
            return false;
        return InfusingRecipe.match(this, basin.get(), recipe);
    }

    protected void applyBasinRecipe() {
        if (currentRecipe == null)
            return;

        Optional<BasinBlockEntity> optionalBasin = getBasin();
        if (!optionalBasin.isPresent())
            return;
        BasinBlockEntity basin = optionalBasin.get();
        boolean wasEmpty = basin.canContinueProcessing();
        if (!InfusingRecipe.apply(this, basin, currentRecipe))
            return;
        basin.inputTank.sendDataImmediately();

        // Continue infusing
        if (wasEmpty && matchBasinRecipe(currentRecipe)) {
            continueWithPreviousRecipe();
            sendData();
        }

        basin.notifyChangeOfContents();
    }

    protected List<Recipe<?>> getMatchingRecipes() {
        if (!Apotheosis.enableEnch)
            return new ArrayList<>();
        if (getBasin().map(BasinBlockEntity::isEmpty)
                .orElse(true))
            return new ArrayList<>();

        collectRecipeCache(level);

        List<Recipe<?>> list = new ArrayList<>();
        for (Recipe<?> recipe : CACHED_RECIPES)
            if (matchBasinRecipe(recipe))
                list.add(recipe);

        return list;
    }

    private static void collectRecipeCache(Level level) {
        if (CACHED_RECIPES.isEmpty()) {
            CACHED_RECIPES.addAll(RecipeFinder.get(null, level, r -> r.getType() == CEIARecipes.INFUSING.getType()));
            List<Recipe<?>> apothicRecipes = new ArrayList<>(RecipeFinder.get(null, level, InfuserBlockEntity::matchStaticFilters));
            apothicRecipes.sort(Comparator.comparingDouble((Recipe<?> recipe) -> ((EnchantingRecipe) recipe).getRequirements().eterna()).reversed());
            CACHED_RECIPES.addAll(apothicRecipes);
        }
    }

    protected boolean updateInfusionStats() {
        var newStats = ApothEnchantmentMenu.gatherStats(level, getBlockPos().below(), 0);
        boolean changed = newStats.arcana() != infusionStats.arcana() || newStats.eterna() != infusionStats.eterna() || newStats.quanta() != infusionStats.quanta();
        if (changed) infusionStats = new InfusionStats(newStats.eterna(), newStats.quanta(), newStats.arcana());
        return changed;
    }

    protected boolean isRunning() {
        return running;
    }

    protected void onBasinRemoved() {
        if (!running)
            return;
        runningTicks = 40;
        running = false;
    }

    public void startProcessingBasin() {
        if (running && runningTicks <= 10)
            return;
        running = true;
        runningTicks = 0;
    }

    public boolean continueWithPreviousRecipe() {
        runningTicks = 10;
        return true;
    }

    protected static boolean matchStaticFilters(Recipe<?> recipe) {
        return recipe instanceof EnchantingRecipe && !AllRecipeTypes.shouldIgnoreInAutomation(recipe);
    }

    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        running = compound.getBoolean("Running");
        runningTicks = compound.getInt("Ticks");
        processingTicks = compound.getInt("ProcessingTicks");
        infusionStats = InfusionStats.parse(compound.get("InfusionStats"));
        super.read(compound, clientPacket);

        if (clientPacket && hasLevel())
            getBasin().ifPresent(bte -> bte.setAreFluidsMoving(running && runningTicks <= 20));
    }

    protected Optional<BasinBlockEntity> getBasin() {
        if (level == null)
            return Optional.empty();
        BlockEntity basinBE = level.getBlockEntity(worldPosition.below(2));
        if (!(basinBE instanceof BasinBlockEntity))
            return Optional.empty();
        return Optional.of((BasinBlockEntity) basinBE);
    }

    @Override
    protected void write(CompoundTag compound, boolean clientPacket) {
        compound.putBoolean("Running", running);
        compound.putInt("Ticks", runningTicks);
        compound.putInt("ProcessingTicks", processingTicks);
        compound.put("InfusionStats", infusionStats.tag());
        super.write(compound, clientPacket);
    }

    public @Nullable IFluidHandler getFluidHandler(@Nullable Direction side) {
        if (side != Direction.DOWN)
            return tank.getCapability().orElse(null);
        return null;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.FLUID_HANDLER && tank != null && side != Direction.DOWN)
            return tank.getCapability().cast();
        return super.getCapability(capability, side);
    }

    public void setInfusionStats(InfusionStats infusionStats) { // Only used by ponder!
        this.infusionStats = infusionStats;
    }

    @Override
    protected AABB createRenderBoundingBox() {
        return super.createRenderBoundingBox().expandTowards(0, -2, 0);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        assert level != null;
        containedFluidTooltip(tooltip, isPlayerSneaking, tank.getCapability().cast());
        if (!tank.getPrimaryHandler().isEmpty()
                && !tank.getPrimaryHandler().getFluid().getFluid().is(CEIAFluids.MOD_TAGS.infusing_ingredients))
            CEIALang.translate("gui.goggles.infuser.incorrect_liquid").style(ChatFormatting.RED).forGoggles(tooltip, 1);
        infusionStats.addToGoggleTooltip(tooltip, isPlayerSneaking);
        return true;
    }

    public static Boolean canBeInfused(ItemStack stack, Level level) {
        if (!Apotheosis.enableEnch)
            return false;
        collectRecipeCache(level);
        return CACHED_RECIPES.stream().anyMatch(recipe -> InfusingRecipe.canProcessInput(recipe, stack));
    }
}
