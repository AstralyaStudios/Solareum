package net.astralya.solareum.block.custom;

import net.astralya.solareum.block.entity.ModBlockEntityTypes;
import net.astralya.solareum.block.entity.custom.SunflowerCollectorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public final class SunflowerCollectorBlock extends Block implements EntityBlock {
  public SunflowerCollectorBlock(BlockBehaviour.Properties properties) {
    super(properties);
  }

  @Override
  protected RenderShape getRenderShape(BlockState state) {
    return RenderShape.MODEL;
  }

  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new SunflowerCollectorBlockEntity(pos, state);
  }

  @Override
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
      Level level, BlockState state, BlockEntityType<T> type) {
    if (level.isClientSide || type != ModBlockEntityTypes.SUNFLOWER_COLLECTOR.get()) return null;
    return (tickLevel, pos, tickState, entity) ->
        ((SunflowerCollectorBlockEntity) entity).serverTick();
  }
}
