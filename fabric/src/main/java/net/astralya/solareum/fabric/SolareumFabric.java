package net.astralya.solareum.fabric;

import net.astralya.solareum.Solareum;
import net.astralya.solareum.energy.LuxNetworkManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;

public final class SolareumFabric implements ModInitializer {
  @Override
  public void onInitialize() {
    Solareum.init();
    ServerChunkEvents.CHUNK_LOAD.register(LuxNetworkManager::chunkLoaded);
  }
}
