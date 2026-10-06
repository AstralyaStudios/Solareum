package net.astralya.solareum.neoforge.datagen;

import net.astralya.solareum.Solareum;
import net.astralya.solareum.item.ModItems;
import net.astralya.solareum.block.ModBlocks;
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
        add(ModItems.PHOTOVOLTAIC_CELL.get(), "Photovoltaic Cell");
        add(ModItems.COPPER_COIL.get(), "Copper Coil");
        add(ModItems.WRENCH.get(), "Wrench");
        add(ModBlocks.HAND_CRANK_PRESS.get(), "Hand Crank Press");
        add("itemGroup.solareum", "Solareum");
    }
}
