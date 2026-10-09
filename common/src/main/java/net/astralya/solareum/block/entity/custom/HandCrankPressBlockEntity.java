package net.astralya.solareum.block.entity.custom;

import net.astralya.solareum.block.entity.ModBlockEntityTypes;
import net.astralya.solareum.recipe.HandCrankPressRecipe;
import net.astralya.solareum.recipe.ModPressRecipes;
import net.astralya.solareum.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class HandCrankPressBlockEntity extends BlockEntity {
  public static final int STROKE_TICKS = 32;
  public static final int BOTTOM_HOLD_TICKS = 20;
  private static final int IMPACT_PARTICLE_COUNT = 8;
  private static final float CRANK_SOUND_VOLUME = 0.8F;
  private static final float IMPACT_SOUND_VOLUME = 0.8F;
  private static final float TRAY_SOUND_VOLUME = 0.5F;
  private static final float SOUND_PITCH = 1.0F;
  private static final String PROGRESS_KEY = "OperationProgress";
  private static final String TARGET_KEY = "TargetOperationProgress";
  private static final String BOTTOM_HOLD_KEY = "BottomHoldTicks";
  private static final String INPUT_KEY = "Input";
  private static final String OUTPUT_KEY = "Output";
  private float operationProgress;
  private float previousOperationProgress;
  private float targetOperationProgress;
  private int bottomHoldTicks;
  private ItemStack input = ItemStack.EMPTY;
  private ItemStack output = ItemStack.EMPTY;

  public HandCrankPressBlockEntity(BlockPos pos, BlockState state) {
    super(ModBlockEntityTypes.HAND_CRANK_PRESS.get(), pos, state);
  }

  public float getInterpolatedProgress(float partialTick) {
    return Mth.lerp(partialTick, previousOperationProgress, operationProgress);
  }

  public ItemStack getTrayStack() {
    return output.isEmpty() ? input : output;
  }

  public boolean isIdle() {
    return operationProgress == 0.0F && targetOperationProgress == 0.0F;
  }

  public void tick() {
    previousOperationProgress = operationProgress;
    if (bottomHoldTicks > 0) {
      if (level != null && !level.isClientSide && --bottomHoldTicks == 0) {
        targetOperationProgress = 0.0F;
        setChanged();
        sync();
      } else if (level != null && !level.isClientSide) {
        setChanged();
      }
      return;
    }
    if (operationProgress == targetOperationProgress) return;
    float step = 1.0F / STROKE_TICKS;
    operationProgress =
        Mth.clamp(
            operationProgress + Math.copySign(step, targetOperationProgress - operationProgress),
            0.0F,
            1.0F);
    if (Math.abs(operationProgress - targetOperationProgress) < step / 2.0F)
      operationProgress = targetOperationProgress;
    if (level != null && !level.isClientSide) {
      setChanged();
      if (operationProgress == 1.0F && previousOperationProgress < 1.0F) {
        bottomHoldTicks = BOTTOM_HOLD_TICKS;
        onPressImpact();
        sync();
      } else if (operationProgress == targetOperationProgress) {
        sync();
      }
    }
  }

  private RecipeHolder<HandCrankPressRecipe> findRecipe(ItemStack stack) {
    if (level == null || stack.isEmpty()) return null;
    for (var holder : level.getRecipeManager().getAllRecipesFor(ModPressRecipes.TYPE.get())) {
      if (holder.value().ingredient().test(stack)) return holder;
    }
    return null;
  }

  public boolean canInsert(ItemStack held) {
    if (level == null || !isIdle() || !output.isEmpty() || held.isEmpty()) return false;
    if (!input.isEmpty() && !ItemStack.isSameItemSameComponents(input, held)) return false;
    var holder = input.isEmpty() ? findRecipe(held) : findRecipe(input);
    if (holder == null || !holder.value().ingredient().test(held)) return false;
    return input.getCount() < holder.value().inputCount();
  }

  public boolean insert(ItemStack held) {
    if (level == null || level.isClientSide || !canInsert(held)) return false;
    if (input.isEmpty()) input = held.copyWithCount(1);
    else input.grow(1);
    held.shrink(1);
    level.playSound(
        null,
        worldPosition,
        SoundEvents.ITEM_FRAME_ADD_ITEM,
        SoundSource.BLOCKS,
        TRAY_SOUND_VOLUME,
        SOUND_PITCH);
    changed();
    return true;
  }

  public boolean beginStroke() {
    if (level == null || level.isClientSide || !isIdle() || !output.isEmpty()) return false;
    var holder = findRecipe(input);
    if (holder == null || input.getCount() < holder.value().inputCount()) return false;
    targetOperationProgress = 1.0F;
    level.playSound(
        null,
        worldPosition,
        ModSounds.CRANK_PRESS.get(),
        SoundSource.BLOCKS,
        CRANK_SOUND_VOLUME,
        SOUND_PITCH);
    changed();
    return true;
  }

  private void onPressImpact() {
    var holder = findRecipe(input);
    if (holder == null || input.getCount() < holder.value().inputCount() || !output.isEmpty())
      return;
    level.playSound(
        null,
        worldPosition,
        SoundEvents.ANVIL_LAND,
        SoundSource.BLOCKS,
        IMPACT_SOUND_VOLUME,
        SOUND_PITCH);
    ItemStack crushed = input.copyWithCount(1);
    if (level instanceof ServerLevel serverLevel) {
      var particle = new ItemParticleOption(ParticleTypes.ITEM, crushed);
      var random = serverLevel.getRandom();
      for (int i = 0; i < IMPACT_PARTICLE_COUNT; i++) {
        serverLevel.sendParticles(
            particle,
            worldPosition.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.32,
            worldPosition.getY() + 4.5 / 16.0 + random.nextDouble() * 0.04,
            worldPosition.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.32,
            0,
            (random.nextDouble() - 0.5) * 0.02,
            0.02 + random.nextDouble() * 0.015,
            (random.nextDouble() - 0.5) * 0.02,
            1.0);
      }
    }
    input.shrink(holder.value().inputCount());
    if (input.isEmpty()) input = ItemStack.EMPTY;
    output = holder.value().output().copy();
    changed();
  }

  public boolean takeTrayStack(Player player) {
    if (level == null || level.isClientSide || !isIdle()) return false;
    ItemStack stack = output.isEmpty() ? input : output;
    if (stack.isEmpty()) return false;
    give(player, stack);
    if (output.isEmpty()) input = ItemStack.EMPTY;
    else output = ItemStack.EMPTY;
    level.playSound(
        null,
        worldPosition,
        SoundEvents.ITEM_PICKUP,
        SoundSource.BLOCKS,
        TRAY_SOUND_VOLUME,
        SOUND_PITCH);
    changed();
    return true;
  }

  private void give(Player player, ItemStack stack) {
    ItemStack remaining = stack.copy();
    player.getInventory().add(remaining);
    if (!remaining.isEmpty()) Block.popResource(level, worldPosition, remaining);
  }

  public void dropContents() {
    if (level == null || level.isClientSide) return;
    if (!input.isEmpty()) Block.popResource(level, worldPosition, input);
    if (!output.isEmpty()) Block.popResource(level, worldPosition, output);
    input = ItemStack.EMPTY;
    output = ItemStack.EMPTY;
  }

  private void changed() {
    setChanged();
    sync();
  }

  private void sync() {
    if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
  }

  @Override
  protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.saveAdditional(tag, registries);
    tag.putFloat(PROGRESS_KEY, operationProgress);
    tag.putFloat(TARGET_KEY, targetOperationProgress);
    tag.putInt(BOTTOM_HOLD_KEY, bottomHoldTicks);
    if (!input.isEmpty()) tag.put(INPUT_KEY, input.save(registries));
    if (!output.isEmpty()) tag.put(OUTPUT_KEY, output.save(registries));
  }

  @Override
  protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.loadAdditional(tag, registries);
    operationProgress = Mth.clamp(tag.getFloat(PROGRESS_KEY), 0.0F, 1.0F);
    previousOperationProgress = operationProgress;
    targetOperationProgress = Mth.clamp(tag.getFloat(TARGET_KEY), 0.0F, 1.0F);
    bottomHoldTicks = Mth.clamp(tag.getInt(BOTTOM_HOLD_KEY), 0, BOTTOM_HOLD_TICKS);
    if (operationProgress == 1.0F && targetOperationProgress == 1.0F && bottomHoldTicks == 0)
      targetOperationProgress = 0.0F;
    input =
        tag.contains(INPUT_KEY)
            ? ItemStack.parseOptional(registries, tag.getCompound(INPUT_KEY))
            : ItemStack.EMPTY;
    output =
        tag.contains(OUTPUT_KEY)
            ? ItemStack.parseOptional(registries, tag.getCompound(OUTPUT_KEY))
            : ItemStack.EMPTY;
  }

  @Override
  public ClientboundBlockEntityDataPacket getUpdatePacket() {
    return ClientboundBlockEntityDataPacket.create(this);
  }

  @Override
  public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
    return saveWithoutMetadata(registries);
  }
}
