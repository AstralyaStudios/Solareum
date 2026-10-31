package net.astralya.solareum.block.custom;

import java.util.EnumMap;
import java.util.Map;
import net.astralya.solareum.energy.LuxContainer;
import net.astralya.solareum.energy.LuxNetworkManager;
import net.astralya.solareum.energy.VineConduitConnectionData;
import net.astralya.solareum.energy.VineConduitInteraction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Unticked topology node: no block entity and no Lux buffer. */
public final class VineConduitBlock extends Block {
  public enum Connection implements StringRepresentable {
    NONE("none"),
    CONDUIT("conduit"),
    ENDPOINT("endpoint");

    private final String name;

    Connection(String name) {
      this.name = name;
    }

    @Override
    public String getSerializedName() {
      return name;
    }
  }

  public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
  public static final Map<Direction, EnumProperty<Connection>> CONNECTIONS = createProperties();
  private static final VoxelShape[] SHAPES = createShapes();

  public VineConduitBlock(Properties properties) {
    super(properties);
    BlockState state = stateDefinition.any().setValue(ACTIVE, false);
    for (Direction side : Direction.values())
      state = state.setValue(CONNECTIONS.get(side), Connection.NONE);
    registerDefaultState(state);
  }

  @Override
  protected ItemInteractionResult useItemOn(
      ItemStack stack,
      BlockState state,
      Level level,
      BlockPos pos,
      Player player,
      InteractionHand hand,
      BlockHitResult hit) {
    if (VineConduitInteraction.use(player, hand, pos, hit)) {
      return level.isClientSide ? ItemInteractionResult.SUCCESS : ItemInteractionResult.CONSUME;
    }
    return super.useItemOn(stack, state, level, pos, player, hand, hit);
  }

  private static Map<Direction, EnumProperty<Connection>> createProperties() {
    Map<Direction, EnumProperty<Connection>> properties = new EnumMap<>(Direction.class);
    for (Direction side : Direction.values()) {
      properties.put(side, EnumProperty.create(side.getSerializedName(), Connection.class));
    }
    return Map.copyOf(properties);
  }

  public static boolean allows(ServerLevel level, BlockPos pos, Direction side) {
    return !VineConduitConnectionData.get(level).isDisabled(pos, side);
  }

  /** Authoritative shared rule used by rendering, ingress checks, and network discovery. */
  public static Connection connection(ServerLevel level, BlockPos pos, Direction side) {
    if (!allows(level, pos, side)) return Connection.NONE;
    BlockPos neighbor = pos.relative(side);
    if (!level.hasChunkAt(neighbor)) return Connection.NONE;
    if (level.getBlockState(neighbor).getBlock() instanceof VineConduitBlock) {
      return allows(level, neighbor, side.getOpposite()) ? Connection.CONDUIT : Connection.NONE;
    }
    if (level.getBlockEntity(neighbor) instanceof LuxContainer endpoint) {
      Direction face = side.getOpposite();
      if (endpoint.canReceiveLux(face) || endpoint.canExtractLux(face)) return Connection.ENDPOINT;
    }
    return Connection.NONE;
  }

  public static BlockState resolve(ServerLevel level, BlockPos pos, BlockState state) {
    for (Direction side : Direction.values())
      state = state.setValue(CONNECTIONS.get(side), connection(level, pos, side));
    return state;
  }

  public static void refresh(ServerLevel level, BlockPos pos) {
    if (!level.hasChunkAt(pos)) return;
    BlockState state = level.getBlockState(pos);
    if (!(state.getBlock() instanceof VineConduitBlock)) return;
    BlockState updated = resolve(level, pos, state);
    if (updated != state)
      level.setBlock(pos, updated, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    builder.add(ACTIVE);
    for (Direction side : Direction.values()) builder.add(CONNECTIONS.get(side));
  }

  @Override
  public BlockState getStateForPlacement(BlockPlaceContext context) {
    // Clients receive resolved enum states from the server; manual masks are never client-owned.
    return context.getLevel() instanceof ServerLevel server
        ? resolve(server, context.getClickedPos(), defaultBlockState())
        : defaultBlockState();
  }

  @Override
  protected BlockState updateShape(
      BlockState state,
      Direction side,
      BlockState neighbor,
      LevelAccessor level,
      BlockPos pos,
      BlockPos neighborPos) {
    if (!(level instanceof ServerLevel server)) return state;
    LuxNetworkManager.invalidate(server, pos);
    return state.setValue(CONNECTIONS.get(side), connection(server, pos, side));
  }

  @Override
  protected void neighborChanged(
      BlockState state,
      Level level,
      BlockPos pos,
      Block block,
      BlockPos neighborPos,
      boolean moving) {
    if (level instanceof ServerLevel server) {
      LuxNetworkManager.invalidate(server, pos);
      refresh(server, pos);
    }
  }

  @Override
  protected void onPlace(
      BlockState state, Level level, BlockPos pos, BlockState old, boolean moving) {
    if (old.getBlock() != this && level instanceof ServerLevel server) {
      LuxNetworkManager.invalidate(server, pos);
      // Moved/copied blockstates must not bring an unowned active visual into a new position.
      BlockState resolved = resolve(server, pos, state.setValue(ACTIVE, false));
      if (resolved != state)
        level.setBlock(pos, resolved, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
    }
    super.onPlace(state, level, pos, old, moving);
  }

  @Override
  protected void onRemove(
      BlockState state, Level level, BlockPos pos, BlockState next, boolean moving) {
    if (next.getBlock() != this && level instanceof ServerLevel server) {
      LuxNetworkManager.invalidate(server, pos);
      VineConduitConnectionData.get(server).remove(pos);
    }
    super.onRemove(state, level, pos, next, moving);
  }

  @Override
  protected VoxelShape getShape(
      BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
    int index = 0;
    int factor = 1;
    for (Direction side : Direction.values()) {
      index += state.getValue(CONNECTIONS.get(side)).ordinal() * factor;
      factor *= 3;
    }
    return SHAPES[index];
  }

  private static VoxelShape[] createShapes() {
    // Match the authored core and north arm, including its six blockstate rotations.
    // Endpoint connections use the same vine arm, without the retired connector collar.
    VoxelShape[] arms = {
      box(6, 0, 8, 10, 6, 12), box(6, 10, 4, 10, 16, 8),
      box(6, 4, 0, 10, 8, 6), box(6, 4, 10, 10, 8, 16),
      box(0, 4, 6, 6, 8, 10), box(10, 4, 6, 16, 8, 10)
    };
    VoxelShape[] shapes = new VoxelShape[729];
    for (int index = 0; index < shapes.length; index++) {
      VoxelShape shape = box(6, 4, 6, 10, 8, 10);
      int remaining = index;
      for (Direction side : Direction.values()) {
        int connection = remaining % 3;
        remaining /= 3;
        if (connection != 0) shape = Shapes.or(shape, arms[side.ordinal()]);
      }
      shapes[index] = shape.optimize();
    }
    return shapes;
  }
}
