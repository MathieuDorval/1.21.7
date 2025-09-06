package com.ores.worldgen;

import com.ores.ORESMod;
import com.ores.config.ModConfig;
import com.ores.config.ModOreGenConfig;
import com.ores.core.Materials;
import com.ores.core.Variants;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class OreGenerationManager {

    private static final Map<String, ConfiguredOre> CONFIGURED_ORES = new HashMap<>();

    // Enums for validation
    public enum GenerationShape { VANILLA, SINGLE }
    public enum GenerationType { UNIFORM, TRIANGLE }
    public enum Option { OCEAN_FLOOR, CAVE_SURFACE }


    /**
     * Contient les données de génération d'un minerai, traitées et prêtes à l'emploi.
     */
    public record ConfiguredOre(
            String name,
            Materials material,
            int size,
            int count,
            float discardChanceOnAirExposure,
            float discardChanceOnLiquidExposure,
            GenerationShape generationShape,
            GenerationType generationType,
            int minHeight,
            int maxHeight,
            ResourceLocation dimension,
            @Nullable List<ResourceLocation> biomes,
            @Nullable List<ResourceLocation> blacklistedBiomes,
            List<String> replaceableBlocks,
            @Nullable List<String> blacklistedBlocks,
            @Nullable List<Option> options
    ) {}

    public static void initialize() {
        ORESMod.LOGGER.info("Processing custom ore generation configurations...");
        CONFIGURED_ORES.clear();

        Map<String, ModOreGenConfig.OreGenConfig> rawConfigs = ModOreGenConfig.getOreGenerationConfigs();

        for (Map.Entry<String, ModOreGenConfig.OreGenConfig> entry : rawConfigs.entrySet()) {
            if (entry.getValue() instanceof ModOreGenConfig.OreConfig(
                    Materials ore, int size, int count, float discardChanceOnAirExposure,
                    float discardChanceOnLiquidExposure, String generationShape, String generationType, int minHeight,
                    int maxHeight, String dimension, List<String> biomes, List<String> blacklistedBiomes,
                    List<String> replaceableBlocks, List<String> blacklistedBlocks, List<String> options
            )) {
                try {
                    // Valider que le matériau existe
                    if (ore == null) {
                        throw new IllegalArgumentException("Material '" + ore + "' does not exist in Materials enum.");
                    }

                    // Gérer les blocs remplaçables
                    List<String> replaceableBlockIds = new ArrayList<>();
                    if (replaceableBlocks != null && !replaceableBlocks.isEmpty()) {
                        replaceableBlockIds.addAll(replaceableBlocks);
                    } else {
                        // Auto-génération basée sur les variants activés
                        for (Variants variant : Variants.values()) {
                            Variants.Category category = variant.getCategory();
                            if (category == Variants.Category.ORE || category == Variants.Category.FALLING_ORE || category == Variants.Category.INVERTED_FALLING_ORE) {
                                if (ModConfig.isOreVariantEnabled(variant.name())) {
                                    Variants.OreProps oreProps = variant.getOreProps();
                                    if (oreProps != null && oreProps.idStone() != null && !replaceableBlockIds.contains(oreProps.idStone())) {
                                        replaceableBlockIds.add(oreProps.idStone());
                                    }
                                }
                            }
                        }
                    }

                    // Valider et définir GenerationShape
                    GenerationShape finalShape;
                    try {
                        finalShape = GenerationShape.valueOf(generationShape.toUpperCase(Locale.ROOT));
                    } catch (IllegalArgumentException e) {
                        ORESMod.LOGGER.warn("Invalid generationShape '{}' for ore '{}'. Defaulting to VANILLA.", generationShape, entry.getKey());
                        finalShape = GenerationShape.VANILLA;
                    }

                    // Valider et définir GenerationType
                    GenerationType finalType;
                    try {
                        finalType = GenerationType.valueOf(generationType.toUpperCase(Locale.ROOT));
                    } catch (IllegalArgumentException e) {
                        ORESMod.LOGGER.warn("Invalid generationType '{}' for ore '{}'. Defaulting to UNIFORM.", generationType, entry.getKey());
                        finalType = GenerationType.UNIFORM;
                    }

                    // Filtrer les options valides
                    List<Option> validOptions = null;
                    if (options != null) {
                        validOptions = new ArrayList<>();
                        for (String optionStr : options) {
                            try {
                                validOptions.add(Option.valueOf(optionStr.toUpperCase(Locale.ROOT)));
                            } catch (IllegalArgumentException e) {
                                ORESMod.LOGGER.warn("Ignoring invalid option '{}' for ore config '{}'.", optionStr, entry.getKey());
                            }
                        }
                    }

                    // Traiter les biomes
                    List<ResourceLocation> biomeWhitelist = biomes != null ?
                            biomes.stream().map(ResourceLocation::parse).collect(Collectors.toList()) : null;

                    List<ResourceLocation> biomeBlacklist = blacklistedBiomes != null ?
                            blacklistedBiomes.stream().map(ResourceLocation::parse).collect(Collectors.toList()) : null;

                    ConfiguredOre configuredOre = new ConfiguredOre(
                            entry.getKey(),
                            ore,
                            size,
                            count,
                            discardChanceOnAirExposure,
                            discardChanceOnLiquidExposure,
                            finalShape,
                            finalType,
                            minHeight,
                            maxHeight,
                            ResourceLocation.parse(dimension),
                            biomeWhitelist,
                            biomeBlacklist,
                            replaceableBlockIds,
                            blacklistedBlocks,
                            validOptions
                    );

                    CONFIGURED_ORES.put(entry.getKey(), configuredOre);

                } catch (Exception e) {
                    ORESMod.LOGGER.error("Failed to process ore generation config section '{}'. Error: {}", entry.getKey(), e.getMessage());
                }
            }
        }
        ORESMod.LOGGER.info("Finished processing {} custom ore generation configurations.", CONFIGURED_ORES.size());
    }

    public static Map<String, ConfiguredOre> getConfiguredOres() {
        return Collections.unmodifiableMap(CONFIGURED_ORES);
    }
}

