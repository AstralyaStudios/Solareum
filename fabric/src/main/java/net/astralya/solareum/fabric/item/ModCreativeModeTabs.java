package net.astralya.solareum.fabric.item;

import net.astralya.solareum.Solareum;
import net.astralya.solareum.item.ModItems;
import net.astralya.solareum.block.ModBlocks;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public final class ModCreativeModeTabs {
    private ModCreativeModeTabs() {
    }

    public static void register() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
                new ResourceLocation(Solareum.MOD_ID, "main"),
                FabricItemGroup.builder()
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
    }
}
