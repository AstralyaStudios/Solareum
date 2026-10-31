package net.astralya.solareum.item;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.astralya.solareum.Solareum;
import net.astralya.solareum.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

public final class ModItems {
  public static final DeferredRegister<Item> ITEMS =
      DeferredRegister.create(Solareum.MOD_ID, Registries.ITEM);

  public static final RegistrySupplier<Item> BIO_FIBER =
      ITEMS.register("bio_fiber", () -> new Item(new Item.Properties()));
  public static final RegistrySupplier<Item> BIO_FIBER_SHEET =
      ITEMS.register("bio_fiber_sheet", () -> new Item(new Item.Properties()));
  public static final RegistrySupplier<Item> COPPER_PLATE =
      ITEMS.register("copper_plate", () -> new Item(new Item.Properties()));
  public static final RegistrySupplier<Item> PHOTOVOLTAIC_CELL =
      ITEMS.register("photovoltaic_cell", () -> new Item(new Item.Properties()));
  public static final RegistrySupplier<Item> COPPER_COIL =
      ITEMS.register("copper_coil", () -> new Item(new Item.Properties()));
  public static final RegistrySupplier<Item> WRENCH =
      ITEMS.register("wrench", () -> new Item(new Item.Properties()));

  public static final RegistrySupplier<Item> HAND_CRANK_PRESS =
      ITEMS.register(
          "hand_crank_press",
          () -> new BlockItem(ModBlocks.HAND_CRANK_PRESS.get(), new Item.Properties()));

  public static final RegistrySupplier<Item> SUNFLOWER_COLLECTOR =
      ITEMS.register(
          "sunflower_collector",
          () -> new BlockItem(ModBlocks.SUNFLOWER_COLLECTOR.get(), new Item.Properties()));

  public static final RegistrySupplier<Item> RADIANT_OVEN =
      ITEMS.register(
          "radiant_oven", () -> new BlockItem(ModBlocks.RADIANT_OVEN.get(), new Item.Properties()));

  public static final RegistrySupplier<Item> VINE_CONDUIT =
      ITEMS.register(
          "vine_conduit", () -> new BlockItem(ModBlocks.VINE_CONDUIT.get(), new Item.Properties()));

  public static final RegistrySupplier<Item> MOSS_CAPACITOR =
      ITEMS.register(
          "moss_capacitor",
          () -> new BlockItem(ModBlocks.MOSS_CAPACITOR.get(), new Item.Properties()));

  private ModItems() {}

  public static void init() {
    ITEMS.register();
  }
}
