package net.astralya.solareum.neoforge;

import net.astralya.solareum.block.ModBlocks;
import net.astralya.solareum.block.entity.ModBlockEntityTypes;
import net.astralya.solareum.client.renderer.HandCrankPressRenderer;
import net.astralya.solareum.client.screen.RadiantOvenScreen;
import net.astralya.solareum.menu.ModMenuTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public final class SolareumNeoForgeClient {
  private SolareumNeoForgeClient() {}

  public static void init(IEventBus modEventBus) {
    modEventBus.addListener(SolareumNeoForgeClient::setupClient);
    modEventBus.addListener(SolareumNeoForgeClient::registerScreens);
    modEventBus.addListener(SolareumNeoForgeClient::registerModels);
    modEventBus.addListener(SolareumNeoForgeClient::registerRenderers);
  }

  private static void registerScreens(RegisterMenuScreensEvent event) {
    event.register(ModMenuTypes.RADIANT_OVEN.get(), RadiantOvenScreen::new);
  }

  private static void registerModels(ModelEvent.RegisterAdditional event) {
    event.register(ModelResourceLocation.standalone(HandCrankPressRenderer.PRESS_MODEL));
    event.register(ModelResourceLocation.standalone(HandCrankPressRenderer.CRANK_MODEL));
  }

  private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
    event.registerBlockEntityRenderer(
        ModBlockEntityTypes.HAND_CRANK_PRESS.get(),
        context ->
            new HandCrankPressRenderer(
                context,
                id ->
                    Minecraft.getInstance()
                        .getModelManager()
                        .getModel(ModelResourceLocation.standalone(id)),
                RenderType.cutout()));
  }

  @SuppressWarnings("deprecation")
  private static void setupClient(FMLClientSetupEvent event) {
    event.enqueueWork(
        () ->
            ItemBlockRenderTypes.setRenderLayer(
                ModBlocks.HAND_CRANK_PRESS.get(), RenderType.cutout()));
  }
}
