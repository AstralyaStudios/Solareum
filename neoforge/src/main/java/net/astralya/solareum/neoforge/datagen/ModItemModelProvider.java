package net.astralya.solareum.neoforge.datagen;

import net.astralya.solareum.Solareum;
import net.astralya.solareum.item.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public final class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Solareum.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(ModItems.BIO_FIBER.get());
        basicItem(ModItems.BIO_FIBER_SHEET.get());
        basicItem(ModItems.COPPER_PLATE.get());
        basicItem(ModItems.PHOTOVOLTAIC_CELL.get());
        basicItem(ModItems.COPPER_COIL.get());
        basicItem(ModItems.WRENCH.get());
    }
}
