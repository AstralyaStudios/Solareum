package net.astralya.solareum.item;

import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.astralya.solareum.Solareum;
import net.astralya.solareum.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public final class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Solareum.MOD_ID, Registries.CREATIVE_MODE_TAB);

    public static final RegistrySupplier<CreativeModeTab> SOLAREUM_TAB = TABS.register("main", () ->
            CreativeTabRegistry.create(builder -> builder
                    .title(Component.translatable("itemGroup.solareum"))
                    .icon(() -> new ItemStack(ModItems.PHOTOVOLTAIC_CELL.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.BIO_FIBER.get());
                        output.accept(ModItems.BIO_FIBER_SHEET.get());
                        output.accept(ModItems.PHOTOVOLTAIC_CELL.get());
                        output.accept(ModItems.COPPER_COIL.get());
                        output.accept(ModItems.WRENCH.get());
                        output.accept(ModBlocks.HAND_CRANK_PRESS.get());
                    })));

    private ModCreativeModeTabs() {
    }

    public static void init() {
        TABS.register();
    }
}
