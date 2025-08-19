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

/**
 * Gère les modifications de la génération du monde, spécifiquement pour la suppression
 * des caractéristiques (features) de minerais des biomes.
 */
public class WorldGenerationEvents {

    /**
     * Initialise la suppression des features de minerais vanilla et moddés.
     * Cette méthode doit être appelée une seule fois lors de l'initialisation du mod.
     */
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

    /**
     * Vérifie si une PlacedFeature est un minerai que nous voulons supprimer.
     * @param holder Le Holder de la PlacedFeature à vérifier.
     * @return true si la feature doit être supprimée, false sinon.
     */
    private static boolean isVanillaOrModdedOre(Holder<PlacedFeature> holder) {
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
}
