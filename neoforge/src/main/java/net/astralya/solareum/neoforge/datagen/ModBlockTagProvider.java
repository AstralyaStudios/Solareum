package net.astralya.solareum.neoforge.datagen;

import java.util.concurrent.CompletableFuture;
import net.astralya.solareum.Solareum;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public final class ModBlockTagProvider extends BlockTagsProvider {
  public ModBlockTagProvider(
      PackOutput output,
      CompletableFuture<HolderLookup.Provider> registries,
      ExistingFileHelper existingFileHelper) {
    super(output, registries, Solareum.MOD_ID, existingFileHelper);
  }

  @Override
  protected void addTags(HolderLookup.Provider registries) {}
}
