/**
 * ORES MOD | __mathieu
 * The Fabric-specific entry point for the ORES mod.
 */
package com.ores.fabric;

import com.ores.ORESMod;
import com.ores.registries.ModFuels;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public final class ORESModFabric implements ModInitializer {

    // -=-=-=- INITIALIZATION -=-=-=-
    @Override
    public void onInitialize() {
        ORESMod.initialize();
        ModFuels.registerAll();

        // Hook into server started event to strip ore generation
        ServerLifecycleEvents.SERVER_STARTED.register(ORESMod::onServerStarted);
    }
}