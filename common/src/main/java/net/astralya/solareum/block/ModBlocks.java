package net.astralya.solareum.block;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.astralya.solareum.Solareum;
import net.astralya.solareum.block.custom.HandCrankPressBlock;
import net.astralya.solareum.block.custom.MossCapacitorBlock;
import net.astralya.solareum.block.custom.RadiantOvenBlock;
import net.astralya.solareum.block.custom.SunflowerCollectorBlock;
import net.astralya.solareum.block.custom.VineConduitBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class ModBlocks {
  public static final DeferredRegister<Block> BLOCKS =
      DeferredRegister.create(Solareum.MOD_ID, Registries.BLOCK);

  public static final RegistrySupplier<Block> HAND_CRANK_PRESS =
      BLOCKS.register(
          "hand_crank_press",
          () ->
              new HandCrankPressBlock(BlockBehaviour.Properties.of().strength(2.0F).noOcclusion()));

  public static final RegistrySupplier<Block> SUNFLOWER_COLLECTOR =
      BLOCKS.register(
          "sunflower_collector",
          () ->
              new SunflowerCollectorBlock(
                  BlockBehaviour.Properties.of()
                      .strength(2.0F)
                      .sound(SoundType.METAL)
                      .noOcclusion()));

  public static final RegistrySupplier<Block> RADIANT_OVEN =
      BLOCKS.register(
          "radiant_oven",
          () -> new RadiantOvenBlock(BlockBehaviour.Properties.of().strength(3.5F)));

  public static final RegistrySupplier<Block> VINE_CONDUIT =
      BLOCKS.register(
          "vine_conduit",
          () ->
              new VineConduitBlock(
                  BlockBehaviour.Properties.of()
                      .strength(0.5F)
                      .sound(SoundType.VINE)
                      .noOcclusion()));

  public static final RegistrySupplier<Block> MOSS_CAPACITOR =
      BLOCKS.register(
          "moss_capacitor",
          () ->
              new MossCapacitorBlock(
                  BlockBehaviour.Properties.of().strength(3.0F).sound(SoundType.METAL)));

  private ModBlocks() {}

  public static void init() {
    BLOCKS.register();
  }
}
