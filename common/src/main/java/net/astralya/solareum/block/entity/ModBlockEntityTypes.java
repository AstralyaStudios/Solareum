package net.astralya.solareum.block.entity;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.astralya.solareum.Solareum;
import net.astralya.solareum.block.ModBlocks;
import net.astralya.solareum.block.entity.custom.HandCrankPressBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ModBlockEntityTypes {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Solareum.MOD_ID, Registries.BLOCK_ENTITY_TYPE);

    public static final RegistrySupplier<BlockEntityType<HandCrankPressBlockEntity>> HAND_CRANK_PRESS =
            BLOCK_ENTITY_TYPES.register("hand_crank_press", () ->
                    BlockEntityType.Builder.of(HandCrankPressBlockEntity::new, ModBlocks.HAND_CRANK_PRESS.get()).build(null));

    private ModBlockEntityTypes() {
    }

    public static void init() {
        BLOCK_ENTITY_TYPES.register();
    }
}
