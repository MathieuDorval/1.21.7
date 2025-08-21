package com.ores.neoforge.event;

import com.ores.ORESMod;
import com.ores.core.Materials;
import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 * ORES MOD | __mathieu
 * Handles world generation modifications for NeoForge using its specific event system.
 */
@EventBusSubscriber(modid = ORESMod.MOD_ID)
public class NeoForgeWorldEvents {

    @SubscribeEvent
    public static void onAddFeaturesToBiomes(AddFeaturesToBiomesEvent event) {
        // We get the mutable list of features for the underground ore generation step
        var features = event.getGenerationSettings().getFeatures(GenerationStep.Decoration.UNDERGROUND_ORES);

        // We remove vanilla ores based on our criteria
        // The removeIf method iterates through the list and removes elements that return true for the condition
        features.removeIf(holder -> isStandardOreFeature(holder));

        // We do the same for ore veins which are in a different generation step
        var vegetalFeatures = event.getGenerationSettings().getFeatures(GenerationStep.Decoration.VEGETAL_DECORATION);
        vegetalFeatures.removeIf(holder -> isOreVeinFeature(holder));
    }

    /**
     * Checks if a PlacedFeature corresponds to a standard vanilla ore that should be removed.
     * This logic is similar to the one in the common module.
     * @param holder The feature holder to check.
     * @return True if the feature is a standard ore to be removed, false otherwise.
     */
    private static boolean isStandardOreFeature(Holder<PlacedFeature> holder) {
        return holder.unwrapKey().map(featureKey -> {
            String featurePath = featureKey.location().getPath();
            for (Materials material : Materials.values()) {
                String materialId = material.getId();
                // Special case for ancient_debris which is Netherite
                if (featurePath.contains(materialId) || (material == Materials.NETHERITE && featurePath.contains("ancient_debris"))) {
                    return true;
                }
            }
            return false;
        }).orElse(false);
    }

    /**
     * Checks if a PlacedFeature is an ore vein.
     * @param holder The feature holder to check.
     * @return True if the feature is an ore vein, false otherwise.
     */
    private static boolean isOreVeinFeature(Holder<PlacedFeature> holder) {
        return holder.unwrapKey()
                .map(key -> key.location().getPath().contains("ore_vein"))
                .orElse(false);
    }
}
