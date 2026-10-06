package net.astralya.solareum.neoforge;

import net.astralya.solareum.Solareum;
import net.neoforged.fml.common.Mod;

@Mod(Solareum.MOD_ID)
public final class SolareumNeoForge {
    public SolareumNeoForge() {
        Solareum.init();
    }
}
