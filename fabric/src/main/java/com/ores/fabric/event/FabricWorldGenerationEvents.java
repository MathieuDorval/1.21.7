/**
 * ORES MOD | __mathieu
 * Handles world generation modifications, specifically removing vanilla ore features.
 */
package com.ores.fabric.event;

import com.ores.ORESMod;
import com.ores.core.Materials;
import dev.architectury.hooks.level.biome.GenerationProperties;
import dev.architectury.registry.level.biome.BiomeModifications;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.ArrayList;
import java.util.List;

public class FabricWorldGenerationEvents {

    // -=-=-=- INITIALIZATION -=-=-=-
    public static void initialize() {
        ORESMod.LOGGER.info("Applying biome modifications to remove ore features...");
        BiomeModifications.removeProperties((biomeContext, properties) -> {
            GenerationProperties.Mutable generationProperties = properties.getGenerationProperties();

            List<ResourceKey<PlacedFeature>> standardOresToRemove = new ArrayList<>();
            for (Holder<PlacedFeature> featureHolder : generationProperties.getFeatures(GenerationStep.Decoration.UNDERGROUND_ORES)) {
                if (isStandardOreFeature(featureHolder)) {
                    featureHolder.unwrapKey().ifPresent(standardOresToRemove::add);
                }
            }
            if (!standardOresToRemove.isEmpty()) {
                standardOresToRemove.forEach(key -> generationProperties.removeFeature(GenerationStep.Decoration.UNDERGROUND_ORES, key));
            }

            List<ResourceKey<PlacedFeature>> veinsToRemove = new ArrayList<>();
            for (Holder<PlacedFeature> featureHolder : generationProperties.getFeatures(GenerationStep.Decoration.VEGETAL_DECORATION)) {
                if (isOreVeinFeature(featureHolder)) {
                    featureHolder.unwrapKey().ifPresent(veinsToRemove::add);
                }
            }
            if (!veinsToRemove.isEmpty()) {
                veinsToRemove.forEach(key -> generationProperties.removeFeature(GenerationStep.Decoration.VEGETAL_DECORATION, key));
            }
        });
    }

    // -=-=-=- HELPERS -=-=-=-
    private static boolean isStandardOreFeature(Holder<PlacedFeature> holder) {
        return holder.unwrapKey().map(featureKey -> {
            String featurePath = featureKey.location().getPath();
            for (Materials material : Materials.values()) {
                String materialId = material.getId();
                if (featurePath.contains(materialId) || (material == Materials.NETHERITE && featurePath.contains("ancient_debris"))) {
                    return true;
                }
            }
            return false;
        }).orElse(false);
    }

    private static boolean isOreVeinFeature(Holder<PlacedFeature> holder) {
        return holder.unwrapKey()
                .map(key -> key.location().getPath().contains("ore_vein"))
                .orElse(false);
    }
}
