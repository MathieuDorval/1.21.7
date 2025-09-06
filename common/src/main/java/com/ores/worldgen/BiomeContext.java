package com.ores.worldgen;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

public class BiomeContext {
    /**
     * Stocke la ResourceKey du biome en cours de chargement par le RegistryDataLoader.
     * Ceci est géré par un Mixin pour fournir le contexte là où il est autrement indisponible.
     */
    public static final ThreadLocal<ResourceKey<Biome>> CURRENT_BIOME_KEY = new ThreadLocal<>();
}
