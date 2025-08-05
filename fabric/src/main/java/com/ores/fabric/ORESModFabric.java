package com.ores.fabric;

import net.fabricmc.api.ModInitializer;

import com.ores.ORESMod;

public final class ORESModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        ORESMod.init();
    }
}
