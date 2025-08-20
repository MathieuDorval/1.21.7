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

            List<ResourceKey<PlacedFeature>> standardOresToRemove = new ArrayList<>();
            for (Holder<PlacedFeature> featureHolder : generationProperties.getFeatures(GenerationStep.Decoration.UNDERGROUND_ORES)) {
                if (isStandardOre(featureHolder)) {
                    featureHolder.unwrapKey().ifPresent(standardOresToRemove::add);
                }
            }

            if (!standardOresToRemove.isEmpty()) {
                standardOresToRemove.forEach(key -> generationProperties.removeFeature(GenerationStep.Decoration.UNDERGROUND_ORES, key));
            }

            List<ResourceKey<PlacedFeature>> veinsToRemove = new ArrayList<>();
            for (Holder<PlacedFeature> featureHolder : generationProperties.getFeatures(GenerationStep.Decoration.VEGETAL_DECORATION)) {
                if (isOreVein(featureHolder)) {
                    featureHolder.unwrapKey().ifPresent(veinsToRemove::add);
                }
            }

            if (!veinsToRemove.isEmpty()) {
                veinsToRemove.forEach(key -> generationProperties.removeFeature(GenerationStep.Decoration.VEGETAL_DECORATION, key));
            }
        });
    }

    /**
     * Vérifie si une PlacedFeature est un minerai standard que nous voulons supprimer.
     * @param holder Le Holder de la PlacedFeature à vérifier.
     * @return true si la feature doit être supprimée, false sinon.
     */
    private static boolean isStandardOre(Holder<PlacedFeature> holder) {
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

    /**
     * Vérifie si une PlacedFeature est un filon de minerai (ore vein).
     * @param holder Le Holder de la PlacedFeature à vérifier.
     * @return true si la feature est un filon à supprimer, false sinon.
     */
    private static boolean isOreVein(Holder<PlacedFeature> holder) {
        return holder.unwrapKey()
                .map(key -> key.location().getPath().contains("ore_vein"))
                .orElse(false);
    }
}
