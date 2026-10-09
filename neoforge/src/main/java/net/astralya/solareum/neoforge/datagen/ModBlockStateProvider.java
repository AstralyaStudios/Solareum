package net.astralya.solareum.neoforge.datagen;

import net.astralya.solareum.Solareum;
import net.astralya.solareum.block.ModBlocks;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public final class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Solareum.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        var baseModel = models().getExistingFile(modLoc("block/hand_crank_press/base"));
        var itemModel = models().getExistingFile(modLoc("block/hand_crank_press/item"));
        horizontalBlock(ModBlocks.HAND_CRANK_PRESS.get(), baseModel);
        simpleBlockItem(ModBlocks.HAND_CRANK_PRESS.get(), itemModel);
    }
}
