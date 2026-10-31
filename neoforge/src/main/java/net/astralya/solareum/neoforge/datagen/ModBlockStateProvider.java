package net.astralya.solareum.neoforge.datagen;

import net.astralya.solareum.Solareum;
import net.astralya.solareum.block.ModBlocks;
import net.astralya.solareum.block.custom.VineConduitBlock;
import net.astralya.solareum.block.custom.VineConduitBlock.Connection;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public final class ModBlockStateProvider extends BlockStateProvider {
  public ModBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
    super(output, Solareum.MOD_ID, existingFileHelper);
  }

  @Override
  protected void registerStatesAndModels() {
    var capacitor = models().getExistingFile(modLoc("block/moss_capacitor"));
    var capacitorEmissive = models().getExistingFile(modLoc("block/moss_capacitor_emissive"));
    getMultipartBuilder(ModBlocks.MOSS_CAPACITOR.get())
        .part()
        .modelFile(capacitor)
        .addModel()
        .end()
        .part()
        .modelFile(capacitorEmissive)
        .addModel()
        .end();
    simpleBlockItem(ModBlocks.MOSS_CAPACITOR.get(), capacitor);

    var core = models().getExistingFile(modLoc("block/vine_conduit/vine_conduit_core"));
    var conduit = getMultipartBuilder(ModBlocks.VINE_CONDUIT.get());
    for (boolean active : new boolean[] {false, true}) {
      String suffix = active ? "_on" : "";
      var activeCore =
          models().getExistingFile(modLoc("block/vine_conduit/vine_conduit_core" + suffix));
      var arm = models().getExistingFile(modLoc("block/vine_conduit/vine_conduit_part" + suffix));
      conduit
          .part()
          .modelFile(activeCore)
          .addModel()
          .condition(VineConduitBlock.ACTIVE, active)
          .end();
      for (Direction side : Direction.values()) {
        int x = side == Direction.UP ? 270 : side == Direction.DOWN ? 90 : 0;
        int y =
            switch (side) {
              case EAST -> 90;
              case SOUTH -> 180;
              case WEST -> 270;
              default -> 0;
            };
        conduit
            .part()
            .modelFile(arm)
            .rotationX(x)
            .rotationY(y)
            .addModel()
            .condition(VineConduitBlock.ACTIVE, active)
            .condition(
                VineConduitBlock.CONNECTIONS.get(side), Connection.CONDUIT, Connection.ENDPOINT)
            .end();
      }
    }
    simpleBlockItem(ModBlocks.VINE_CONDUIT.get(), core);

    var baseModel = models().getExistingFile(modLoc("block/hand_crank_press/base"));
    var itemModel = models().getExistingFile(modLoc("block/hand_crank_press/item"));
    horizontalBlock(ModBlocks.HAND_CRANK_PRESS.get(), baseModel);
    simpleBlockItem(ModBlocks.HAND_CRANK_PRESS.get(), itemModel);

    var collectorModel = models().getExistingFile(modLoc("block/sunflower_collector"));
    simpleBlock(ModBlocks.SUNFLOWER_COLLECTOR.get(), collectorModel);
    simpleBlockItem(ModBlocks.SUNFLOWER_COLLECTOR.get(), collectorModel);

    var ovenModel = models().getExistingFile(modLoc("block/radiant_oven/radiant_oven"));
    var ovenOnModel = models().getExistingFile(modLoc("block/radiant_oven/radiant_oven_on"));
    horizontalBlock(
        ModBlocks.RADIANT_OVEN.get(),
        state -> state.getValue(BlockStateProperties.LIT) ? ovenOnModel : ovenModel);
    simpleBlockItem(ModBlocks.RADIANT_OVEN.get(), ovenModel);
  }
}
