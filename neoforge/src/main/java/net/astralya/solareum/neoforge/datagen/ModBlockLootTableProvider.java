package net.astralya.solareum.neoforge.datagen;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.astralya.solareum.block.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

public final class ModBlockLootTableProvider extends LootTableProvider {
  public ModBlockLootTableProvider(
      PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
    super(
        output,
        Set.of(),
        List.of(new SubProviderEntry(ModBlockLootTables::new, LootContextParamSets.BLOCK)),
        registries);
  }

  private static final class ModBlockLootTables extends BlockLootSubProvider {
    private ModBlockLootTables(HolderLookup.Provider registries) {
      super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
      dropSelf(ModBlocks.HAND_CRANK_PRESS.get());
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
      return List.of(ModBlocks.HAND_CRANK_PRESS.get());
    }
  }
}
