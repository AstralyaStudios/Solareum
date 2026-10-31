package net.astralya.solareum.menu;

import dev.architectury.registry.menu.MenuRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.astralya.solareum.Solareum;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;

public final class ModMenuTypes {
  public static final DeferredRegister<MenuType<?>> MENUS =
      DeferredRegister.create(Solareum.MOD_ID, Registries.MENU);

  @SuppressWarnings("removal")
  public static final RegistrySupplier<MenuType<RadiantOvenMenu>> RADIANT_OVEN =
      MENUS.register("radiant_oven", () -> MenuRegistry.of(RadiantOvenMenu::new));

  private ModMenuTypes() {}

  public static void init() {
    MENUS.register();
  }
}
