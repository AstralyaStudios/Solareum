package net.astralya.solareum.forge;

import dev.architectury.platform.forge.EventBuses;
import net.astralya.solareum.Solareum;
import net.astralya.solareum.datagen.DataGenerators;
import net.astralya.solareum.forge.item.ModCreativeModeTabs;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.common.Mod;

@Mod(Solareum.MOD_ID)
public final class SolareumForge {
    public SolareumForge() {
        EventBuses.registerModEventBus(Solareum.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        Solareum.init();
        FMLJavaModLoadingContext.get().getModEventBus().addListener(DataGenerators::gatherData);
        ModCreativeModeTabs.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
