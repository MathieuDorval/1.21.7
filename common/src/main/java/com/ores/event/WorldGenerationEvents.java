package com.ores.event;

import com.ores.core.Materials;

import dev.architectury.hooks.level.biome.GenerationProperties;
import dev.architectury.registry.level.biome.BiomeModifications;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.ArrayList;
import java.util.List;

public class WorldGenerationEvents {

    public static void init() {
        BiomeModifications.removeProperties((biomeContext, properties) -> {
            GenerationProperties.Mutable generationProperties = properties.getGenerationProperties();

            List<ResourceKey<PlacedFeature>> featuresToRemove = new ArrayList<>();

            for (Holder<PlacedFeature> featureHolder : generationProperties.getFeatures(GenerationStep.Decoration.UNDERGROUND_ORES)) {
                if (isVanillaOrModdedOre(featureHolder)) {
                    featureHolder.unwrapKey().ifPresent(featuresToRemove::add);
                }
            }
            if (!featuresToRemove.isEmpty()) {
                featuresToRemove.forEach(key -> generationProperties.removeFeature(GenerationStep.Decoration.UNDERGROUND_ORES, key));
            }
        });
    }

    private static boolean isVanillaOrModdedOre(Holder<PlacedFeature> holder) {
        return holder.unwrapKey().map(featureKey -> {
            String featurePath = featureKey.location().getPath();

            for (Materials material : Materials.values()) {
                String materialId = material.getId();
                if (featurePath.contains(materialId)) {
                    return true;
                }
            }
            return false;
        }).orElse(false);
    }
}
