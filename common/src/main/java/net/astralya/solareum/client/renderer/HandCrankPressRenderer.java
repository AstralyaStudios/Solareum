package net.astralya.solareum.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.function.Function;
import net.astralya.solareum.Solareum;
import net.astralya.solareum.block.custom.HandCrankPressBlock;
import net.astralya.solareum.block.entity.custom.HandCrankPressBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class HandCrankPressRenderer
    implements BlockEntityRenderer<HandCrankPressBlockEntity> {
  private static final double PRESS_TRAVEL_BLOCKS = 4.0 / 16.0;
  private static final float CRANK_ROTATIONS_PER_STROKE = 1.0F;
  private static final double CRANK_PIVOT_X = 14.0 / 16.0;
  private static final double CRANK_PIVOT_Y = 15.2 / 16.0;
  private static final double CRANK_PIVOT_Z = 7.5 / 16.0;
  private static final float TRAY_ITEM_SCALE = 0.625F;
  public static final ResourceLocation PRESS_MODEL =
      ResourceLocation.fromNamespaceAndPath(Solareum.MOD_ID, "block/hand_crank_press/press");
  public static final ResourceLocation CRANK_MODEL =
      ResourceLocation.fromNamespaceAndPath(Solareum.MOD_ID, "block/hand_crank_press/crank");

  private final AnimatedModelBuffer pressBuffer = new AnimatedModelBuffer();
  private final AnimatedModelBuffer crankBuffer = new AnimatedModelBuffer();
  private final Function<ResourceLocation, BakedModel> modelLoader;
  private final RenderType renderType;

  public HandCrankPressRenderer(
      BlockEntityRendererProvider.Context context,
      Function<ResourceLocation, BakedModel> modelLoader,
      RenderType renderType) {
    this.modelLoader = modelLoader;
    this.renderType = renderType;
  }

  @Override
  public void render(
      HandCrankPressBlockEntity blockEntity,
      float partialTick,
      PoseStack poseStack,
      MultiBufferSource bufferSource,
      int packedLight,
      int packedOverlay) {
    if (blockEntity.getLevel() == null) {
      return;
    }
    float progress = blockEntity.getInterpolatedProgress(partialTick);
    var level = blockEntity.getLevel();
    var pos = blockEntity.getBlockPos();
    var state = blockEntity.getBlockState();
    var colors = Minecraft.getInstance().getBlockColors();
    poseStack.pushPose();
    poseStack.translate(0.5, 0.5, 0.5);
    poseStack.mulPose(
        Axis.YP.rotationDegrees(
            switch (state.getValue(HandCrankPressBlock.FACING)) {
              case NORTH -> 0.0F;
              case EAST -> -90.0F;
              case SOUTH -> 180.0F;
              case WEST -> 90.0F;
              default -> throw new IllegalStateException("Non-horizontal press facing");
            }));
    poseStack.translate(-0.5, -0.5, -0.5);
    poseStack.pushPose();
    poseStack.translate(0.0, -PRESS_TRAVEL_BLOCKS * progress, 0.0);
    pressBuffer.render(
        modelLoader.apply(PRESS_MODEL),
        state,
        level,
        pos,
        colors,
        poseStack.last(),
        bufferSource.getBuffer(renderType),
        packedLight,
        packedOverlay);
    poseStack.popPose();

    poseStack.pushPose();
    poseStack.translate(CRANK_PIVOT_X, CRANK_PIVOT_Y, CRANK_PIVOT_Z);
    poseStack.mulPose(Axis.XP.rotationDegrees(360.0F * CRANK_ROTATIONS_PER_STROKE * progress));
    poseStack.translate(-CRANK_PIVOT_X, -CRANK_PIVOT_Y, -CRANK_PIVOT_Z);
    crankBuffer.render(
        modelLoader.apply(CRANK_MODEL),
        state,
        level,
        pos,
        colors,
        poseStack.last(),
        bufferSource.getBuffer(renderType),
        packedLight,
        packedOverlay);
    poseStack.popPose();

    ItemStack trayStack = blockEntity.getTrayStack();
    if (!trayStack.isEmpty()) {
      renderTrayItem(
          trayStack,
          level,
          poseStack,
          bufferSource,
          packedLight,
          packedOverlay,
          0.5,
          4.25 / 16.0,
          9.0 / 16.0,
          0.0F);
      if (trayStack.getCount() >= 2) {
        renderTrayItem(
            trayStack,
            level,
            poseStack,
            bufferSource,
            packedLight,
            packedOverlay,
            0.5 + 0.6 / 16.0,
            4.75 / 16.0,
            8.5 / 16.0,
            12.0F);
      }
    }
    poseStack.popPose();
  }

  private void renderTrayItem(
      ItemStack stack,
      Level level,
      PoseStack poseStack,
      MultiBufferSource bufferSource,
      int packedLight,
      int packedOverlay,
      double x,
      double y,
      double z,
      float rotation) {
    poseStack.pushPose();
    // Ground's built-in +2px Y offset rotates toward -Z; compensate after laying the sprite flat.
    poseStack.translate(x, y, z);
    poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
    poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
    poseStack.scale(TRAY_ITEM_SCALE, TRAY_ITEM_SCALE, TRAY_ITEM_SCALE);
    Minecraft.getInstance()
        .getItemRenderer()
        .renderStatic(
            stack,
            ItemDisplayContext.GROUND,
            packedLight,
            packedOverlay,
            poseStack,
            bufferSource,
            level,
            0);
    poseStack.popPose();
  }

  @Override
  public boolean shouldRenderOffScreen(HandCrankPressBlockEntity blockEntity) {
    return true;
  }
}
