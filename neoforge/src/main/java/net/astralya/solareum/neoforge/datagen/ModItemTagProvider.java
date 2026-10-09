package net.astralya.solareum.neoforge.datagen;

import java.util.concurrent.CompletableFuture;
import net.astralya.solareum.Solareum;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public final class ModItemTagProvider extends ItemTagsProvider {
  public ModItemTagProvider(
      PackOutput output,
      CompletableFuture<HolderLookup.Provider> registries,
      CompletableFuture<TagLookup<Block>> blockTags,
      ExistingFileHelper existingFileHelper) {
    super(output, registries, blockTags, Solareum.MOD_ID, existingFileHelper);
  }

  @Override
  protected void addTags(HolderLookup.Provider registries) {
    tag(TagKey.create(
            Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath(Solareum.MOD_ID, "bio_fiber_materials")))
        .addTag(ItemTags.LEAVES)
        .add(Items.VINE, Items.BAMBOO);
  }
}
