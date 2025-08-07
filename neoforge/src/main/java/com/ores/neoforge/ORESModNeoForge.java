package com.ores.neoforge;

import com.ores.registries.ModFuels;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

import com.ores.ORESMod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(ORESMod.MOD_ID)
public class ORESModNeoForge {
    public ORESModNeoForge(IEventBus modEventBus) {
        ORESMod.init();
        modEventBus.addListener(this::onCommonSetup);
    }

    private void onCommonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(ModFuels::registerAll);
    }
}
