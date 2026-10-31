package net.astralya.solareum.energy;

import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import net.astralya.solareum.block.custom.VineConduitBlock;
import net.astralya.solareum.block.custom.VineConduitBlock.Connection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;

/**
 * Server-thread-only, lossless routing. No ticking conduits, network buffers or persistent graph.
 */
public final class LuxNetworkManager {
  public static final long MAX_THROUGHPUT = 400;
  public static final int ACTIVE_DECAY_TICKS = 20;
  private static final Map<ServerLevel, LuxNetworkManager> LEVELS = new WeakHashMap<>();
  private final Map<BlockPos, LuxNetwork> networks = new HashMap<>();
  // Actual deliveries attributed to ingress nodes survive same-tick split/join invalidation.
  private final Map<BlockPos, Long> deliveries = new HashMap<>();
  private final Set<LuxNetwork> glowing = new HashSet<>();
  private final Map<Long, WeakReference<LevelChunk>> pendingChunks = new HashMap<>();
  private long accountingTick = Long.MIN_VALUE;

  private LuxNetworkManager() {}

  public static void invalidate(ServerLevel level, BlockPos pos) {
    LuxNetworkManager manager = LEVELS.get(level);
    if (manager == null) return;
    manager.remove(manager.networks.get(pos));
    for (Direction side : Direction.values())
      manager.remove(manager.networks.get(pos.relative(side)));
  }

  private void remove(LuxNetwork network) {
    if (network != null) for (BlockPos node : network.conduits) networks.remove(node, network);
  }

  public static long pushIntoNetwork(
      ServerLevel level,
      BlockPos conduitPos,
      LuxContainer source,
      Direction sourceFace,
      long maxAmount) {
    long moved =
        pushIntoNetwork(
            level, conduitPos, source, sourceFace, maxAmount, LuxContainer.Role.CONSUMER);
    return moved
        + pushIntoNetwork(
            level, conduitPos, source, sourceFace, maxAmount - moved, LuxContainer.Role.STORAGE);
  }

  static long pushIntoNetwork(
      ServerLevel level,
      BlockPos conduitPos,
      LuxContainer source,
      Direction sourceFace,
      long maxAmount,
      LuxContainer.Role receiverRole) {
    if (maxAmount <= 0 || !source.canExtractLux(sourceFace)) return 0;
    if (!LuxTransfer.canSupply(source.getLuxRole(), receiverRole)) return 0;
    if (!VineConduitBlock.allows(level, conduitPos, sourceFace.getOpposite())) return 0;
    LuxNetworkManager manager = LEVELS.computeIfAbsent(level, ignored -> new LuxNetworkManager());
    return manager.push(level, conduitPos, source, sourceFace, maxAmount, receiverRole);
  }

  private long push(
      ServerLevel level,
      BlockPos ingress,
      LuxContainer source,
      Direction sourceFace,
      long maximum,
      LuxContainer.Role receiverRole) {
    if (!level.hasChunkAt(ingress)
        || !(level.getBlockState(ingress).getBlock() instanceof VineConduitBlock)) return 0;
    long tick = level.getGameTime();
    if (accountingTick != tick) {
      accountingTick = tick;
      deliveries.clear();
    }
    LuxNetwork network = networks.get(ingress);
    if (network != null && !chunksUnchanged(level, network)) {
      remove(network);
      network = null;
    }
    if (network == null) network = discover(level, ingress);
    if (network.tick != tick) {
      network.tick = tick;
      network.transferred = 0;
    }
    long budget =
        source.extractLux(
            Math.min(maximum, Math.max(0, MAX_THROUGHPUT - network.transferred)), true, sourceFace);
    if (budget == 0) return 0;

    // Group faces by position: a machine touching several conduits receives one fair share.
    Map<BlockPos, List<LuxNetwork.Endpoint>> grouped = new LinkedHashMap<>();
    for (LuxNetwork.Endpoint endpoint : network.endpoints) {
      if (!level.hasChunkAt(endpoint.pos())) continue;
      if (!(level.getBlockEntity(endpoint.pos()) instanceof LuxContainer receiver)
          || receiver == source
          || receiver.getLuxRole() != receiverRole) continue;
      if (receiver.canReceiveLux(endpoint.face())
          && receiver.receiveLux(budget, true, endpoint.face()) > 0) {
        grouped.computeIfAbsent(endpoint.pos(), ignored -> new ArrayList<>()).add(endpoint);
      }
    }
    List<List<LuxNetwork.Endpoint>> receivers = new ArrayList<>(grouped.values());
    if (receivers.isEmpty()) return 0;
    Collections.rotate(receivers, -Math.floorMod(network.cursor, receivers.size()));
    long moved = 0;
    while (moved < budget && !receivers.isEmpty()) {
      int count = receivers.size();
      long remaining = budget - moved;
      long share = remaining / count;
      int remainder = (int) (remaining % count);
      List<List<LuxNetwork.Endpoint>> next = new ArrayList<>();
      long roundMoved = 0;
      for (int i = 0; i < count; i++) {
        long allowance = share + (i < remainder ? 1 : 0);
        if (allowance == 0) continue;
        long delivered = deliver(level, receivers.get(i), source, sourceFace, allowance);
        roundMoved += delivered;
        if (delivered == allowance) next.add(receivers.get(i));
      }
      moved += roundMoved;
      if (roundMoved == 0) break;
      receivers = next;
    }
    network.cursor = (network.cursor + 1) % grouped.size();
    network.transferred += moved;
    if (moved > 0) {
      network.lastSuccessfulTransferTick = tick;
      if (!network.visuallyActive) {
        network.visuallyActive = true;
        glowing.add(network);
        for (BlockPos node : network.conduits) setActive(level, node, true);
      }
      deliveries.merge(ingress.immutable(), moved, Long::sum);
    }
    return moved;
  }

  private static long deliver(
      ServerLevel level,
      List<LuxNetwork.Endpoint> faces,
      LuxContainer source,
      Direction sourceFace,
      long maximum) {
    long moved = 0;
    for (LuxNetwork.Endpoint endpoint : faces) {
      if (!level.hasChunkAt(endpoint.pos())) continue;
      if (!(level.getBlockEntity(endpoint.pos()) instanceof LuxContainer receiver)
          || !receiver.canReceiveLux(endpoint.face())) continue;
      moved += LuxTransfer.deliver(source, sourceFace, receiver, endpoint.face(), maximum - moved);
      if (moved == maximum) break;
    }
    return moved;
  }

  private static boolean chunksUnchanged(ServerLevel level, LuxNetwork network) {
    for (var entry : network.chunks.entrySet()) {
      long key = entry.getKey();
      LevelChunk current =
          level.getChunkSource().getChunkNow(ChunkPos.getX(key), ChunkPos.getZ(key));
      LuxNetwork.ChunkSnapshot snapshot = entry.getValue();
      if ((current != null) != snapshot.loaded() || current != snapshot.chunk().get()) return false;
    }
    return true;
  }

  /** Loader chunk-load adapters enqueue work; world access happens on the server level tick. */
  public static void chunkLoaded(ServerLevel level, LevelChunk chunk) {
    if (!level.getServer().isSameThread()) {
      level.getServer().execute(() -> chunkLoaded(level, chunk));
      return;
    }
    for (LevelChunkSection section : chunk.getSections()) {
      if (section.maybeHas(state -> state.getBlock() instanceof VineConduitBlock)) {
        LEVELS
            .computeIfAbsent(level, ignored -> new LuxNetworkManager())
            .pendingChunks
            .put(chunk.getPos().toLong(), new WeakReference<>(chunk));
        return;
      }
    }
  }

  public static void levelTick(ServerLevel level) {
    LuxNetworkManager manager = LEVELS.get(level);
    if (manager == null) return;
    manager.restoreLoadedChunks(level);
    long tick = level.getGameTime();
    var iterator = manager.glowing.iterator();
    while (iterator.hasNext()) {
      LuxNetwork network = iterator.next();
      if (tick - network.lastSuccessfulTransferTick < ACTIVE_DECAY_TICKS) continue;
      for (BlockPos pos : network.conduits) {
        LuxNetwork current = manager.networks.get(pos);
        // A retired cache entry must never extinguish a newly rebuilt, still-flowing network.
        if (current == null
            || !current.visuallyActive
            || tick - current.lastSuccessfulTransferTick >= ACTIVE_DECAY_TICKS)
          setActive(level, pos, false);
      }
      network.visuallyActive = false;
      iterator.remove();
    }
  }

  private static void setActive(ServerLevel level, BlockPos pos, boolean active) {
    if (!level.hasChunkAt(pos)) return;
    BlockState state = level.getBlockState(pos);
    if (state.getBlock() instanceof VineConduitBlock
        && state.getValue(VineConduitBlock.ACTIVE) != active) {
      // Visual-only update: suppress shape propagation so it cannot invalidate cached topology.
      level.setBlock(
          pos,
          state.setValue(VineConduitBlock.ACTIVE, active),
          Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
    }
  }

  private void restoreLoadedChunks(ServerLevel level) {
    var pending = pendingChunks.entrySet().iterator();
    while (pending.hasNext()) {
      var entry = pending.next();
      long key = entry.getKey();
      LevelChunk chunk = level.getChunkSource().getChunkNow(ChunkPos.getX(key), ChunkPos.getZ(key));
      if (chunk == null) {
        if (entry.getValue().get() == null) pending.remove();
        continue;
      }
      pending.remove();
      LevelChunkSection[] sections = chunk.getSections();
      for (int sectionIndex = 0; sectionIndex < sections.length; sectionIndex++) {
        LevelChunkSection section = sections[sectionIndex];
        if (!section.maybeHas(state -> state.getBlock() instanceof VineConduitBlock)) continue;
        for (int y = 0; y < 16; y++)
          for (int z = 0; z < 16; z++)
            for (int x = 0; x < 16; x++) {
              if (!(section.getBlockState(x, y, z).getBlock() instanceof VineConduitBlock))
                continue;
              BlockPos pos =
                  new BlockPos(
                      chunk.getPos().getMinBlockX() + x,
                      level.getMinBuildHeight() + sectionIndex * 16 + y,
                      chunk.getPos().getMinBlockZ() + z);
              invalidate(level, pos);
              setActive(level, pos, false);
              VineConduitBlock.refresh(level, pos);
              for (Direction side : Direction.values())
                VineConduitBlock.refresh(level, pos.relative(side));
            }
      }
    }
  }

  private LuxNetwork discover(ServerLevel level, BlockPos start) {
    Set<BlockPos> visited = new HashSet<>();
    Set<LuxNetwork.Endpoint> endpoints = new HashSet<>();
    Map<Long, LuxNetwork.ChunkSnapshot> chunks = new HashMap<>();
    ArrayDeque<BlockPos> queue = new ArrayDeque<>();
    visited.add(start.immutable());
    queue.add(start.immutable());
    while (!queue.isEmpty()) {
      BlockPos pos = queue.removeFirst();
      remove(networks.get(pos));
      BlockState state = level.getBlockState(pos);
      BlockState connected = state;
      for (Direction side : Direction.values()) {
        BlockPos neighbor = pos.relative(side);
        long key = ChunkPos.asLong(neighbor.getX() >> 4, neighbor.getZ() >> 4);
        LevelChunk chunk =
            level.getChunkSource().getChunkNow(neighbor.getX() >> 4, neighbor.getZ() >> 4);
        chunks.put(key, new LuxNetwork.ChunkSnapshot(chunk != null, new WeakReference<>(chunk)));
        Connection connection = VineConduitBlock.connection(level, pos, side);
        if (connection == Connection.CONDUIT) {
          if (visited.add(neighbor)) queue.addLast(neighbor);
        } else if (connection == Connection.ENDPOINT) {
          endpoints.add(new LuxNetwork.Endpoint(neighbor, side.getOpposite()));
        }
        connected = connected.setValue(VineConduitBlock.CONNECTIONS.get(side), connection);
      }
      // Refresh border visuals after chunks return; no neighbor notification or recursive rebuild.
      if (connected != state)
        level.setBlock(pos, connected, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
    }
    List<LuxNetwork.Endpoint> ordered = new ArrayList<>(endpoints);
    ordered.sort(
        Comparator.comparingLong((LuxNetwork.Endpoint e) -> e.pos().asLong())
            .thenComparingInt(e -> e.face().ordinal()));
    LuxNetwork network = new LuxNetwork(visited, ordered, chunks);
    network.tick = level.getGameTime();
    // Only rediscovery scans members for prior deliveries. Normal tick reset is constant-time.
    for (BlockPos node : visited) {
      network.transferred += deliveries.getOrDefault(node, 0L);
      networks.put(node, network);
    }
    return network;
  }
}
