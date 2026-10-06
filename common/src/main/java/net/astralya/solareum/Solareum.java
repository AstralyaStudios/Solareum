package net.astralya.solareum;

import net.astralya.solareum.block.ModBlocks;
import net.astralya.solareum.item.ModItems;
import net.astralya.solareum.item.ModCreativeModeTabs;

public final class Solareum {
    public static final String MOD_ID = "solareum";

    private Solareum() {
    }

    public static void init() {
        ModBlocks.init();
        ModItems.init();
        ModCreativeModeTabs.init();
    }
}
