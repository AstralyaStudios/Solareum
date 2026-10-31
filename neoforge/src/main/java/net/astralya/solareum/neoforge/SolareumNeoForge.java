package net.astralya.solareum.neoforge;

import net.astralya.solareum.Solareum;
import net.astralya.solareum.energy.LuxNetworkManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.ChunkEvent;

@Mod(Solareum.MOD_ID)
public final class SolareumNeoForge {
  public SolareumNeoForge(IEventBus modEventBus) {
    Solareum.init();
    NeoForge.EVENT_BUS.addListener(SolareumNeoForge::chunkLoaded);
    if (FMLEnvironment.dist == Dist.CLIENT) {
      SolareumNeoForgeClient.init(modEventBus);
    }
  }

  private static void chunkLoaded(ChunkEvent.Load event) {
    if (event.getLevel() instanceof ServerLevel level
        && event.getChunk() instanceof LevelChunk chunk) {
      LuxNetworkManager.chunkLoaded(level, chunk);
    }
  }
}
