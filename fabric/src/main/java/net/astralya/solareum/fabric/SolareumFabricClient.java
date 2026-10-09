package net.astralya.solareum.fabric;

import net.astralya.solareum.block.ModBlocks;
import net.astralya.solareum.block.entity.ModBlockEntityTypes;
import net.astralya.solareum.client.renderer.HandCrankPressRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.model.loading.v1.FabricBakedModelManager;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

public final class SolareumFabricClient implements ClientModInitializer {
  @Override
  public void onInitializeClient() {
    ModelLoadingPlugin.register(
        context ->
            context.addModels(
                HandCrankPressRenderer.PRESS_MODEL, HandCrankPressRenderer.CRANK_MODEL));
    BlockEntityRenderers.register(
        ModBlockEntityTypes.HAND_CRANK_PRESS.get(),
        context ->
            new HandCrankPressRenderer(
                context,
                id ->
                    ((FabricBakedModelManager) Minecraft.getInstance().getModelManager())
                        .getModel(id),
                RenderType.cutout()));
    BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.HAND_CRANK_PRESS.get(), RenderType.cutout());
  }
}
