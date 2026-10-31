package net.astralya.solareum.energy;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/** Only explicit manual disconnections are persisted. Topology and flow remain runtime state. */
public final class VineConduitConnectionData extends SavedData {
  private static final Factory<VineConduitConnectionData> FACTORY =
      new Factory<>(VineConduitConnectionData::new, VineConduitConnectionData::load, null);
  private final Map<Long, Integer> disabled = new HashMap<>();

  public static VineConduitConnectionData get(ServerLevel level) {
    return level.getDataStorage().computeIfAbsent(FACTORY, "solareum_vine_connections");
  }

  public boolean isDisabled(BlockPos pos, Direction side) {
    return (disabled.getOrDefault(pos.asLong(), 0) & (1 << side.ordinal())) != 0;
  }

  public boolean toggle(BlockPos pos, Direction side) {
    int mask = disabled.getOrDefault(pos.asLong(), 0) ^ (1 << side.ordinal());
    if (mask == 0) disabled.remove(pos.asLong());
    else disabled.put(pos.asLong(), mask);
    setDirty();
    return (mask & (1 << side.ordinal())) != 0;
  }

  public void remove(BlockPos pos) {
    if (disabled.remove(pos.asLong()) != null) setDirty();
  }

  private static VineConduitConnectionData load(CompoundTag tag, HolderLookup.Provider registries) {
    VineConduitConnectionData data = new VineConduitConnectionData();
    ListTag entries = tag.getList("Connections", Tag.TAG_COMPOUND);
    for (int i = 0; i < entries.size(); i++) {
      CompoundTag entry = entries.getCompound(i);
      int mask = entry.getByte("Disabled") & 63;
      if (mask != 0) data.disabled.put(entry.getLong("Pos"), mask);
    }
    return data;
  }

  @Override
  public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
    ListTag entries = new ListTag();
    disabled.entrySet().stream()
        .sorted(Map.Entry.comparingByKey())
        .forEach(
            entry -> {
              CompoundTag value = new CompoundTag();
              value.putLong("Pos", entry.getKey());
              value.putByte("Disabled", entry.getValue().byteValue());
              entries.add(value);
            });
    tag.put("Connections", entries);
    return tag;
  }
}
