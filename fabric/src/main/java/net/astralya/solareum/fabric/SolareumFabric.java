package net.astralya.solareum.fabric;

import net.astralya.solareum.Solareum;
import net.fabricmc.api.ModInitializer;

public final class SolareumFabric implements ModInitializer {
  @Override
  public void onInitialize() {
    Solareum.init();
  }
}
