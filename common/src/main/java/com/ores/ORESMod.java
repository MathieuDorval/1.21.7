package com.ores;

import com.ores.config.ModConfig;
import com.ores.event.WorldGenerationEvents;
import com.ores.registries.ModBlocks;
import com.ores.registries.ModCreativeTab;
import com.ores.registries.ModItems;

public final class ORESMod {
    public static final String MOD_ID = "ores";

    public static void init() {
        ModConfig.init();
        ModCreativeTab.initTabs();
        ModBlocks.initBlocks();
        ModItems.initItems();
        WorldGenerationEvents.init();
    }
}
