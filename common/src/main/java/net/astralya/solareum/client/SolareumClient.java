package net.astralya.solareum.client;

import dev.architectury.registry.menu.MenuRegistry;
import net.astralya.solareum.client.screen.RadiantOvenScreen;
import net.astralya.solareum.menu.ModMenuTypes;

public final class SolareumClient {
  private SolareumClient() {}

  public static void registerScreens() {
    MenuRegistry.registerScreenFactory(ModMenuTypes.RADIANT_OVEN.get(), RadiantOvenScreen::new);
  }
}
