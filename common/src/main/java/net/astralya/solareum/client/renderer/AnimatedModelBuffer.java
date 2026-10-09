package net.astralya.solareum.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

final class AnimatedModelBuffer {
    private static final float[] VERTEX_BRIGHTNESS = {1.0F, 1.0F, 1.0F, 1.0F};

    private BakedModel cachedModel;
    private BlockState cachedState;
    private List<BakedQuad> cachedQuads = List.of();

    void render(BakedModel model, BlockState state, BlockAndTintGetter level, BlockPos pos,
                BlockColors blockColors, PoseStack.Pose pose, VertexConsumer consumer,
                int packedLight, int packedOverlay) {
        if (cachedModel != model || cachedState != state) {
            cachedModel = model;
            cachedState = state;
            cachedQuads = collectQuads(model, state, pos);
        }

        int[] vertexLights = {packedLight, packedLight, packedLight, packedLight};
        for (BakedQuad quad : cachedQuads) {
            var axis = quad.getDirection().getNormal();
            Vector3f normal = pose.normal()
                    .transform(new Vector3f(axis.getX(), axis.getY(), axis.getZ()))
                    .normalize();
            float shade = diffuseShade(level, normal, quad.isShade());

            int tint = quad.isTinted() ? blockColors.getColor(state, level, pos, quad.getTintIndex()) : 0xFFFFFF;
            float red = shade * ((tint >> 16) & 255) / 255.0F;
            float green = shade * ((tint >> 8) & 255) / 255.0F;
            float blue = shade * (tint & 255) / 255.0F;
            consumer.putBulkData(pose, quad, VERTEX_BRIGHTNESS, red, green, blue, 1.0F, vertexLights, packedOverlay, true);
        }
    }

    private static List<BakedQuad> collectQuads(BakedModel model, BlockState state, BlockPos pos) {
        List<BakedQuad> quads = new ArrayList<>();
        RandomSource random = RandomSource.create();
        long seed = state.getSeed(pos);
        for (Direction direction : Direction.values()) {
            random.setSeed(seed);
            quads.addAll(model.getQuads(state, direction, random));
        }
        random.setSeed(seed);
        quads.addAll(model.getQuads(state, null, random));
        return List.copyOf(quads);
    }

    private static float diffuseShade(BlockAndTintGetter level, Vector3f normal, boolean shade) {
        if (!shade) return 1.0F;
        float x = normal.x(), y = normal.y(), z = normal.z();
        return x * x * level.getShade(x >= 0 ? Direction.EAST : Direction.WEST, true)
                + y * y * level.getShade(y >= 0 ? Direction.UP : Direction.DOWN, true)
                + z * z * level.getShade(z >= 0 ? Direction.SOUTH : Direction.NORTH, true);
    }
}
