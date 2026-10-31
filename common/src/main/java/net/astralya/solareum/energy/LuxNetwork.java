package net.astralya.solareum.energy;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.chunk.LevelChunk;

/** Runtime topology and accounting only; never stores Lux or block entity references. */
final class LuxNetwork {
  record Endpoint(BlockPos pos, Direction face) {}

  record ChunkSnapshot(boolean loaded, WeakReference<LevelChunk> chunk) {}

  final Set<BlockPos> conduits;
  final List<Endpoint> endpoints;
  final Map<Long, ChunkSnapshot> chunks;
  long tick = Long.MIN_VALUE;
  long transferred;
  int cursor;
  long lastSuccessfulTransferTick = Long.MIN_VALUE;
  boolean visuallyActive;

  LuxNetwork(Set<BlockPos> conduits, List<Endpoint> endpoints, Map<Long, ChunkSnapshot> chunks) {
    this.conduits = conduits;
    this.endpoints = endpoints;
    this.chunks = chunks;
  }
}
