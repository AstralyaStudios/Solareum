package net.astralya.solareum.energy;

import net.astralya.solareum.block.custom.VineConduitBlock;
import net.astralya.solareum.block.custom.VineConduitBlock.Connection;
import net.astralya.solareum.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/** Shared server-authoritative Wrench connection configuration. */
public final class VineConduitInteraction {
  private VineConduitInteraction() {}

  public static boolean canHandle(Player player, InteractionHand hand, BlockPos pos) {
    return !player.isSpectator()
        && player.getItemInHand(hand).is(ModItems.WRENCH.get())
        && player.level().hasChunkAt(pos)
        && player.canInteractWithBlock(pos, 0)
        && player.level().getBlockState(pos).getBlock() instanceof VineConduitBlock;
  }

  public static boolean use(Player player, InteractionHand hand, BlockPos pos, BlockHitResult hit) {
    if (!canHandle(player, hand, pos)) return false;
    if (!(player.level() instanceof ServerLevel level)) return true;
    BlockState state = level.getBlockState(pos);
    Direction side = targetSide(state, pos, hit, hit.getDirection());
    boolean disabled = VineConduitConnectionData.get(level).toggle(pos, side);
    LuxNetworkManager.invalidate(level, pos);
    VineConduitBlock.refresh(level, pos);
    VineConduitBlock.refresh(level, pos.relative(side));
    player.displayClientMessage(
        Component.translatable(
            "message.solareum.vine_conduit.side",
            Component.translatable("direction.solareum." + side.getSerializedName()),
            Component.translatable(
                disabled ? "message.solareum.disabled" : "message.solareum.auto")),
        true);
    return true;
  }

  static Direction targetSide(
      BlockState state, BlockPos pos, BlockHitResult hit, Direction fallback) {
    if (hit != null) {
      Vec3 local = hit.getLocation().subtract(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
      Direction arm = Direction.getNearest(local.x, local.y, local.z);
      double distance =
          Math.abs(local.x * arm.getStepX() + local.y * arm.getStepY() + local.z * arm.getStepZ());
      if (distance > 2.0 / 16.0 + 0.00001
          && state.getValue(VineConduitBlock.CONNECTIONS.get(arm)) != Connection.NONE) return arm;
      return hit.getDirection();
    }
    return fallback == null ? Direction.UP : fallback;
  }
}
