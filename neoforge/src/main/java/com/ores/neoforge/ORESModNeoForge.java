/**
 * ORES MOD | __mathieu
 * The NeoForge-specific entry point for the ORES mod.
 */
package com.ores.neoforge;

import com.ores.ORESMod;
import com.ores.registries.ModFuels;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.bus.api.SubscribeEvent;

@Mod(ORESMod.MOD_ID)
public class ORESModNeoForge {

    // -=-=-=- CONSTRUCTOR -=-=-=-
    public ORESModNeoForge(IEventBus modEventBus) {
        ORESMod.initialize();
        modEventBus.addListener(this::onCommonSetup);
    }

    // -=-=-=- LIFECYCLE EVENTS -=-=-=-
    private void onCommonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(ModFuels::registerAll);
    }

    // -=-=-=- SERVER HOOK -=-=-=-
    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        ORESMod.onServerStarted(event.getServer());
    }
}
