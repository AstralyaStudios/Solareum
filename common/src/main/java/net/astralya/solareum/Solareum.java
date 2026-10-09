package net.astralya.solareum;

import net.astralya.solareum.block.ModBlocks;
import net.astralya.solareum.block.entity.ModBlockEntityTypes;
import net.astralya.solareum.item.ModCreativeModeTabs;
import net.astralya.solareum.item.ModItems;
import net.astralya.solareum.recipe.ModPressRecipes;
import net.astralya.solareum.sound.ModSounds;

public final class Solareum {
  public static final String MOD_ID = "solareum";

  private Solareum() {}

  public static void init() {
    ModBlocks.init();
    ModItems.init();
    ModBlockEntityTypes.init();
    ModSounds.init();
    ModPressRecipes.init();
    ModCreativeModeTabs.init();
  }
}
