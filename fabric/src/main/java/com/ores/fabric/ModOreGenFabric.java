package com.ores.fabric;

import com.ores.config.ModOreGenConfig;
import com.ores.ORESMod;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class ModOreGenFabric {

    public static void registerFeatures() {
        ORESMod.LOGGER.info("Registering Fabric placed features from config...");

        ModOreGenConfig.getOreGenerationConfigs().forEach((name, config) -> {
            if (config instanceof ModOreGenConfig.OreConfig oreConfig) {
                ResourceKey<PlacedFeature> placedFeatureKey = ResourceKey.create(
                        Registries.PLACED_FEATURE,
                        ResourceLocation.fromNamespaceAndPath(ORESMod.MOD_ID, name)
                );

                Predicate<BiomeSelectionContext> biomeSelector;
                List<String> biomeList = oreConfig.biomes();

                if (biomeList == null || biomeList.isEmpty()) {
                    biomeSelector = BiomeSelectors.foundInOverworld();
                } else {
                    biomeSelector = BiomeSelectors.includeByKey(
                            biomeList.stream()
                                    .map(ResourceLocation::tryParse)
                                    .filter(Objects::nonNull)
                                    .map(rl -> ResourceKey.<Biome>create(Registries.BIOME, rl))
                                    .toArray(ResourceKey[]::new)
                    );
                }

                BiomeModifications.addFeature(
                        biomeSelector,
                        GenerationStep.Decoration.UNDERGROUND_ORES,
                        placedFeatureKey
                );
                ORESMod.LOGGER.info("Registered placed feature '{}' for biomes: {}", name, biomeList != null && !biomeList.isEmpty() ? biomeList : "Overworld");
            }
        });
    }
}
