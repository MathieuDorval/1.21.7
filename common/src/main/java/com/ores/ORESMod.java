/**
 * ORES MOD | __mathieu
 * The main entry point for the ORES mod, responsible for initializing all components.
 */
package com.ores;

import com.ores.config.ModConfig;
import com.ores.registries.ModBlocks;
import com.ores.registries.ModCreativeTab;
import com.ores.registries.ModItems;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ORESMod {

    // -=-=-=- CONSTANTS -=-=-=-
    public static final String MOD_ID = "ores";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // -=-=-=- INITIALIZATION -=-=-=-
    public static void initialize() {
        LOGGER.info("Initializing ORES Mod...");
        ModConfig.initialize();
        ModCreativeTab.initialize();
        ModBlocks.initialize();
        ModItems.initialize();
        LOGGER.info("ORES Mod initialized successfully.");
    }
}
