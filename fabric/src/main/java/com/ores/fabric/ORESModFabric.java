package com.ores.fabric;

import com.ores.registries.ModFuels;
import net.fabricmc.api.ModInitializer;

import com.ores.ORESMod;

public final class ORESModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ORESMod.init();
        ModFuels.registerAll();
    }
}
