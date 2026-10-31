package net.astralya.solareum.energy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.astralya.solareum.block.custom.VineConduitBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Native server-side pushes, ordered after ticking machines. Never holds or buffers Lux. */
public final class LuxTransfer {
  // Positions only: do not retain BlockEntities or keep their chunks loaded between ticks.
  private static final Map<ServerLevel, Map<BlockPos, Long>> PENDING = new WeakHashMap<>();

  private LuxTransfer() {}

  /** Called by a sender's server ticker, even if it is empty before other sources tick. */
  public static void enqueue(ServerLevel level, BlockPos pos) {
    PENDING
        .computeIfAbsent(level, ignored -> new LinkedHashMap<>())
        .put(pos.immutable(), level.getGameTime());
  }

  /** All generators contribute before any storage is allowed to discharge. */
  public static void levelTick(ServerLevel level) {
    Map<BlockPos, Long> pending = PENDING.remove(level);
    if (pending == null) return;
    List<Output> outputs = new ArrayList<>();
    for (var entry : pending.entrySet()) {
      BlockPos pos = entry.getKey();
      if (entry.getValue() != level.getGameTime() || !level.hasChunkAt(pos)) continue;
      BlockEntity entity = level.getBlockEntity(pos);
      if (entity instanceof LuxContainer source && !entity.isRemoved()) {
        outputs.add(new Output(pos, source));
      }
    }
    if (outputs.isEmpty()) return;
    // Rotate providers too, so multiple capacitors can share the discharge duty over time.
    Collections.rotate(outputs, -Math.floorMod(level.getGameTime(), outputs.size()));
    pushPhase(level, outputs, LuxContainer.Role.SOURCE, LuxContainer.Role.CONSUMER);
    pushPhase(level, outputs, LuxContainer.Role.SOURCE, LuxContainer.Role.STORAGE);
    pushPhase(level, outputs, LuxContainer.Role.STORAGE, LuxContainer.Role.CONSUMER);
  }

  private static void pushPhase(
      ServerLevel level,
      List<Output> outputs,
      LuxContainer.Role senderRole,
      LuxContainer.Role receiverRole) {
    for (Output output : outputs) {
      if (output.source.getLuxRole() != senderRole || output.remaining <= 0) continue;
      long moved = push(level, output, receiverRole);
      output.remaining -= moved;
    }
  }

  private static long push(ServerLevel level, Output output, LuxContainer.Role receiverRole) {
    long remaining = output.remaining;
    Direction[] sides = Direction.values();
    int start = Math.floorMod(level.getGameTime(), sides.length);
    for (int i = 0; i < sides.length && remaining > 0; i++) {
      Direction face = sides[(start + i) % sides.length];
      if (!output.source.canExtractLux(face)) continue;
      BlockPos neighbor = output.pos.relative(face);
      if (!level.hasChunkAt(neighbor)) continue;
      if (level.getBlockState(neighbor).getBlock() instanceof VineConduitBlock) {
        remaining -=
            LuxNetworkManager.pushIntoNetwork(
                level, neighbor, output.source, face, remaining, receiverRole);
      } else if (level.getBlockEntity(neighbor) instanceof LuxContainer receiver
          && receiver.getLuxRole() == receiverRole) {
        remaining -= deliver(output.source, face, receiver, face.getOpposite(), remaining);
      }
    }
    return output.remaining - remaining;
  }

  /** Shared simulation/receipt/debit path for direct adjacency and network deliveries. */
  static long deliver(
      LuxContainer source,
      Direction sourceFace,
      LuxContainer receiver,
      Direction receiverFace,
      long maximum) {
    if (source == receiver
        || !source.canExtractLux(sourceFace)
        || !receiver.canReceiveLux(receiverFace)
        || !canSupply(source.getLuxRole(), receiver.getLuxRole())) return 0;
    long available = source.extractLux(maximum, true, sourceFace);
    long accepted = receiver.receiveLux(available, true, receiverFace);
    if (accepted <= 0) return 0;
    // The native contract forbids receiver callbacks from mutating the sender. Debit only the
    // actual receipt: a receiver may accept less than its simulation, never more than requested.
    long actual = receiver.receiveLux(Math.min(available, accepted), false, receiverFace);
    source.extractLux(actual, false, sourceFace);
    return actual;
  }

  static boolean canSupply(LuxContainer.Role source, LuxContainer.Role receiver) {
    return (source == LuxContainer.Role.SOURCE
            && (receiver == LuxContainer.Role.CONSUMER || receiver == LuxContainer.Role.STORAGE))
        || (source == LuxContainer.Role.STORAGE && receiver == LuxContainer.Role.CONSUMER);
  }

  private static final class Output {
    final BlockPos pos;
    final LuxContainer source;
    long remaining;

    Output(BlockPos pos, LuxContainer source) {
      this.pos = pos;
      this.source = source;
      this.remaining = source.getLuxStorage().getMaxExtract();
    }
  }
}
