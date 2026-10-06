package net.astralya.solareum.block;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.astralya.solareum.Solareum;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Solareum.MOD_ID, Registries.BLOCK);

    public static final RegistrySupplier<Block> HAND_CRANK_PRESS =
            BLOCKS.register("hand_crank_press", () -> new Block(BlockBehaviour.Properties.of().strength(2.0F)));

    private ModBlocks() {
    }

    public static void init() {
        BLOCKS.register();
    }
}
