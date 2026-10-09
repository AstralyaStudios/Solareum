package net.astralya.solareum.neoforge;

import net.astralya.solareum.Solareum;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(Solareum.MOD_ID)
public final class SolareumNeoForge {
  public SolareumNeoForge(IEventBus modEventBus) {
    Solareum.init();
    if (FMLEnvironment.dist == Dist.CLIENT) {
      SolareumNeoForgeClient.init(modEventBus);
    }
  }
}
