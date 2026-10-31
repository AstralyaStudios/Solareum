package net.astralya.solareum.neoforge.datagen;

import net.astralya.solareum.Solareum;
import net.astralya.solareum.block.ModBlocks;
import net.astralya.solareum.item.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public final class ModLanguageProvider extends LanguageProvider {
  public ModLanguageProvider(PackOutput output) {
    super(output, Solareum.MOD_ID, "en_us");
  }

  @Override
  protected void addTranslations() {
    add(ModItems.BIO_FIBER.get(), "Bio-Fiber");
    add(ModItems.BIO_FIBER_SHEET.get(), "Bio-Fiber Sheet");
    add(ModItems.COPPER_PLATE.get(), "Copper Plate");
    add(ModItems.PHOTOVOLTAIC_CELL.get(), "Photovoltaic Cell");
    add(ModItems.COPPER_COIL.get(), "Copper Coil");
    add(ModItems.WRENCH.get(), "Wrench");
    add(ModBlocks.HAND_CRANK_PRESS.get(), "Hand Crank Press");
    add(ModBlocks.SUNFLOWER_COLLECTOR.get(), "Sunflower Collector");
    add(ModBlocks.RADIANT_OVEN.get(), "Radiant Oven");
    add(ModBlocks.VINE_CONDUIT.get(), "Vine Conduit");
    add(ModBlocks.MOSS_CAPACITOR.get(), "Moss Capacitor");
    add("itemGroup.solareum", "Solareum");
    add("message.solareum.vine_conduit.side", "Vine Conduit %s: %s");
    add("message.solareum.auto", "Auto");
    add("message.solareum.disabled", "Disabled");
    add("direction.solareum.down", "Down");
    add("direction.solareum.up", "Up");
    add("direction.solareum.north", "North");
    add("direction.solareum.south", "South");
    add("direction.solareum.west", "West");
    add("direction.solareum.east", "East");
  }
}
