/**
 * ORES MOD | __mathieu
 * The Fabric-specific entry point for the ORES mod.
 */
package com.ores.fabric;

import com.ores.ORESMod;
import com.ores.fabric.event.FabricLootEvents;
import com.ores.fabric.event.FabricWorldGenerationEvents;
import com.ores.registries.ModFuels;
import net.fabricmc.api.ModInitializer;

public final class ORESModFabric implements ModInitializer {

    // -=-=-=- INITIALIZATION -=-=-=-
    @Override
    public void onInitialize() {
        ORESMod.initialize();
        ModFuels.registerAll();
        FabricWorldGenerationEvents.initialize();
        FabricLootEvents.initialize();
    }
}
