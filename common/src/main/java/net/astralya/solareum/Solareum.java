package net.astralya.solareum;

import dev.architectury.event.events.common.TickEvent;
import net.astralya.solareum.block.ModBlocks;
import net.astralya.solareum.block.entity.ModBlockEntityTypes;
import net.astralya.solareum.energy.LuxNetworkManager;
import net.astralya.solareum.energy.LuxTransfer;
import net.astralya.solareum.item.ModCreativeModeTabs;
import net.astralya.solareum.item.ModItems;
import net.astralya.solareum.menu.ModMenuTypes;
import net.astralya.solareum.recipe.ModPressRecipes;
import net.astralya.solareum.sound.ModSounds;

public final class Solareum {
  public static final String MOD_ID = "solareum";

  private Solareum() {}

  public static void init() {
    ModBlocks.init();
    ModItems.init();
    ModBlockEntityTypes.init();
    ModMenuTypes.init();
    ModSounds.init();
    ModPressRecipes.init();
    ModCreativeModeTabs.init();
    TickEvent.SERVER_LEVEL_PRE.register(LuxNetworkManager::levelTick);
    TickEvent.SERVER_LEVEL_POST.register(LuxTransfer::levelTick);
  }
}
