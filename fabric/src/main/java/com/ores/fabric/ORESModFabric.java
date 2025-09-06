/**
 * ORES MOD | __mathieu
 * The Fabric-specific entry point for the ORES mod.
 */
package com.ores.fabric;

import com.ores.ORESMod;
import com.ores.registries.ModFuels;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

public final class ORESModFabric implements ModInitializer {

    // -=-=-=- INITIALIZATION -=-=-=-
    @Override
    public void onInitialize() {
        ORESMod.initialize();
        ModFuels.initialize();
        ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStarted);
    }
    private void onServerStarted(MinecraftServer server) {
    }
}