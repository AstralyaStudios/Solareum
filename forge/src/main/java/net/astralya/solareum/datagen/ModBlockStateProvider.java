package net.astralya.solareum.datagen;

import net.astralya.solareum.Solareum;
import net.astralya.solareum.block.ModBlocks;
import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public final class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Solareum.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        var model = models().getExistingFile(modLoc("block/hand_crank_press"));
        simpleBlock(ModBlocks.HAND_CRANK_PRESS.get(), model);
        simpleBlockItem(ModBlocks.HAND_CRANK_PRESS.get(), model);
    }
}
