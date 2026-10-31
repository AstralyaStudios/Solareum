package net.astralya.solareum.block.entity.custom;

import net.astralya.solareum.block.entity.ModBlockEntityTypes;
import net.astralya.solareum.energy.LuxContainer;
import net.astralya.solareum.energy.LuxStorage;
import net.astralya.solareum.energy.LuxTransfer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class SunflowerCollectorBlockEntity extends BlockEntity implements LuxContainer {
  public static final long CAPACITY = 1_000;
  public static final long MAX_RECEIVE = 20;
  public static final long MAX_EXTRACT = 20;
  private static final String LUX_KEY = "StoredLux";

  private final LuxStorage storage = new LuxStorage(CAPACITY, MAX_RECEIVE, MAX_EXTRACT);
  private long extractTick = Long.MIN_VALUE;
  private long extracted;

  public SunflowerCollectorBlockEntity(BlockPos pos, BlockState state) {
    super(ModBlockEntityTypes.SUNFLOWER_COLLECTOR.get(), pos, state);
  }

  public long getStoredLux() {
    return storage.getStoredLux();
  }

  public long getCapacity() {
    return storage.getCapacity();
  }

  @Override
  public LuxStorage getLuxStorage() {
    return storage;
  }

  @Override
  public Role getLuxRole() {
    return Role.SOURCE;
  }

  @Override
  public boolean canExtractLux(Direction face) {
    return true;
  }

  @Override
  public long extractLux(long amount, boolean simulate, Direction face) {
    if (!(level instanceof ServerLevel server)) return 0;
    long tick = server.getGameTime();
    long used = extractTick == tick ? extracted : 0;
    long actual = storage.extractLux(Math.min(amount, MAX_EXTRACT - used), simulate);
    if (!simulate && actual > 0) {
      extractTick = tick;
      extracted = used + actual;
      setChanged();
    }
    return actual;
  }

  /** Potential production under current conditions, before available buffer space is applied. */
  public long getCurrentGenerationRate() {
    if (!(level instanceof ServerLevel serverLevel)) return 0;
    BlockPos above = worldPosition.above();
    // isDay() includes weather darkness, which would suppress daytime thunder generation.
    if (!serverLevel.dimensionType().hasSkyLight()
        || serverLevel.dimensionType().hasFixedTime()
        || Math.floorMod(serverLevel.getDayTime(), 24_000L) >= 12_000L
        || !serverLevel.canSeeSky(above)) return 0;
    if (serverLevel.isThundering()) return 5;
    if (serverLevel.isRainingAt(above)) return 10;
    return 20;
  }

  public void serverTick() {
    if (!(level instanceof ServerLevel serverLevel)) return;
    boolean changed = storage.receiveLux(getCurrentGenerationRate(), false) > 0;
    LuxTransfer.enqueue(serverLevel, worldPosition);
    if (changed) setChanged();
  }

  @Override
  protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.saveAdditional(tag, registries);
    tag.putLong(LUX_KEY, storage.getStoredLux());
  }

  @Override
  protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.loadAdditional(tag, registries);
    storage.setStoredLux(tag.getLong(LUX_KEY));
  }
}
