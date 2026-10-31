package net.astralya.solareum.block.entity;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.astralya.solareum.Solareum;
import net.astralya.solareum.block.ModBlocks;
import net.astralya.solareum.block.entity.custom.HandCrankPressBlockEntity;
import net.astralya.solareum.block.entity.custom.MossCapacitorBlockEntity;
import net.astralya.solareum.block.entity.custom.RadiantOvenBlockEntity;
import net.astralya.solareum.block.entity.custom.SunflowerCollectorBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ModBlockEntityTypes {
  public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
      DeferredRegister.create(Solareum.MOD_ID, Registries.BLOCK_ENTITY_TYPE);

  public static final RegistrySupplier<BlockEntityType<HandCrankPressBlockEntity>>
      HAND_CRANK_PRESS =
          BLOCK_ENTITY_TYPES.register(
              "hand_crank_press",
              () ->
                  BlockEntityType.Builder.of(
                          HandCrankPressBlockEntity::new, ModBlocks.HAND_CRANK_PRESS.get())
                      .build(null));

  public static final RegistrySupplier<BlockEntityType<SunflowerCollectorBlockEntity>>
      SUNFLOWER_COLLECTOR =
          BLOCK_ENTITY_TYPES.register(
              "sunflower_collector",
              () ->
                  BlockEntityType.Builder.of(
                          SunflowerCollectorBlockEntity::new, ModBlocks.SUNFLOWER_COLLECTOR.get())
                      .build(null));

  public static final RegistrySupplier<BlockEntityType<RadiantOvenBlockEntity>> RADIANT_OVEN =
      BLOCK_ENTITY_TYPES.register(
          "radiant_oven",
          () ->
              BlockEntityType.Builder.of(RadiantOvenBlockEntity::new, ModBlocks.RADIANT_OVEN.get())
                  .build(null));

  public static final RegistrySupplier<BlockEntityType<MossCapacitorBlockEntity>> MOSS_CAPACITOR =
      BLOCK_ENTITY_TYPES.register(
          "moss_capacitor",
          () ->
              BlockEntityType.Builder.of(
                      MossCapacitorBlockEntity::new, ModBlocks.MOSS_CAPACITOR.get())
                  .build(null));

  private ModBlockEntityTypes() {}

  public static void init() {
    BLOCK_ENTITY_TYPES.register();
  }
}
