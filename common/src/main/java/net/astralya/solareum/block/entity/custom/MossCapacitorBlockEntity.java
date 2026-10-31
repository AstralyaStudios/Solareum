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

public final class MossCapacitorBlockEntity extends BlockEntity implements LuxContainer {
  public static final long CAPACITY = 250_000L;
  public static final long MAX_RECEIVE = 100L;
  public static final long MAX_EXTRACT = 100L;
  private static final String LUX_KEY = "StoredLux";

  private final LuxStorage storage = new LuxStorage(CAPACITY, MAX_RECEIVE, MAX_EXTRACT);
  private long receiveTick = Long.MIN_VALUE;
  private long received;
  private long extractTick = Long.MIN_VALUE;
  private long extracted;

  public MossCapacitorBlockEntity(BlockPos pos, BlockState state) {
    super(ModBlockEntityTypes.MOSS_CAPACITOR.get(), pos, state);
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
    return Role.STORAGE;
  }

  @Override
  public boolean canReceiveLux(Direction face) {
    return true;
  }

  @Override
  public boolean canExtractLux(Direction face) {
    return true;
  }

  @Override
  public long receiveLux(long amount, boolean simulate, Direction face) {
    if (!(level instanceof ServerLevel server)) return 0;
    long tick = server.getGameTime();
    long used = receiveTick == tick ? received : 0;
    long actual = storage.receiveLux(Math.min(amount, MAX_RECEIVE - used), simulate);
    if (!simulate && actual > 0) {
      receiveTick = tick;
      received = used + actual;
      setChanged();
    }
    return actual;
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

  public void serverTick() {
    if (level instanceof ServerLevel server) LuxTransfer.enqueue(server, worldPosition);
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
