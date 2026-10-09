package net.astralya.solareum.sound;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.astralya.solareum.Solareum;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(Solareum.MOD_ID, Registries.SOUND_EVENT);

    public static final RegistrySupplier<SoundEvent> CRANK_PRESS =
            SOUNDS.register("crank_press", () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(Solareum.MOD_ID, "crank_press")));

    private ModSounds() {
    }

    public static void init() {
        SOUNDS.register();
    }
}
