package net.astralya.solareum.forge.item;

import net.astralya.solareum.Solareum;
import net.astralya.solareum.item.ModItems;
import net.astralya.solareum.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Solareum.MOD_ID);

    public static final RegistryObject<CreativeModeTab> SOLAREUM_TAB = TABS.register("main", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.solareum"))
                    .icon(() -> new ItemStack(ModItems.PHOTOVOLTAIC_CELL.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.BIO_FIBER.get());
                        output.accept(ModItems.BIO_FIBER_SHEET.get());
                        output.accept(ModItems.PHOTOVOLTAIC_CELL.get());
                        output.accept(ModItems.COPPER_COIL.get());
                        output.accept(ModItems.WRENCH.get());
                        output.accept(ModBlocks.HAND_CRANK_PRESS.get());
                    }).build());

    private ModCreativeModeTabs() {
    }

    public static void register(IEventBus eventBus) {
        TABS.register(eventBus);
    }
}
