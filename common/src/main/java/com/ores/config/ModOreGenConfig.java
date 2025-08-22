package com.ores.config;

import com.ores.ORESMod;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModOreGenConfig {

    private static final Path CONFIG_PATH = Paths.get("config", "ores_generation.toml");
    private static final Map<String, OreGenParams> ORE_GENERATION_CONFIGS = new HashMap<>();

    /**
     * Représente les paramètres de génération pour un seul type de minerai.
     */
    public record OreGenParams(
            String ore,
            int size,
            float density,
            float discardChanceOnAirExposure,
            float discardChanceOnLiquidExposure,
            @Nullable String generationShape,
            int minHeight,
            int maxHeight,
            String dimension,
            @Nullable List<String> biomes,
            @Nullable List<String> replaceableBlocks,
            String generationType,
            @Nullable String associatedBlock,
            @Nullable String bonusBlock,
            // Nouvelle liste d'options facultatives
            @Nullable List<String> options
    ) {}

    /**
     * Initialise la configuration, crée le fichier si nécessaire et le charge.
     */
    public static void initialize() {
        try {
            if (Files.notExists(CONFIG_PATH)) {
                ORESMod.LOGGER.info("Creating default ore generation config file at {}...", CONFIG_PATH.toString());
                Files.createDirectories(CONFIG_PATH.getParent());
                String defaultConfig = generateDefaultTomlContent();
                Files.writeString(CONFIG_PATH, defaultConfig);
            }
            loadConfig();
        } catch (IOException e) {
            ORESMod.LOGGER.error("Could not create or read the ore generation configuration file.", e);
        }
    }

    /**
     * Charge les configurations depuis le fichier ores_generation.toml.
     */
    private static void loadConfig() {
        ORE_GENERATION_CONFIGS.clear();
        try {
            List<String> lines = Files.readAllLines(CONFIG_PATH);
            Map<String, Object> currentOreParams = null;
            String currentOreGenName = null;

            for (String line : lines) {
                line = line.trim();
                if (line.startsWith("#") || line.isEmpty()) {
                    continue;
                }

                if (line.startsWith("[") && line.endsWith("]")) {
                    if (currentOreGenName != null && currentOreParams != null) {
                        buildAndStoreParams(currentOreGenName, currentOreParams);
                    }
                    currentOreGenName = line.substring(1, line.length() - 1).replace("\"", "");
                    currentOreParams = new HashMap<>();
                } else if (currentOreParams != null && line.contains("=")) {
                    String[] parts = line.split("=", 2);
                    String key = parts[0].trim();
                    String valueStr = parts[1].trim();
                    currentOreParams.put(key, parseValue(valueStr));
                }
            }
            if (currentOreGenName != null && currentOreParams != null) {
                buildAndStoreParams(currentOreGenName, currentOreParams);
            }

        } catch (IOException e) {
            ORESMod.LOGGER.error("Failed to read ore generation config file.", e);
        }
    }

    /**
     * Valide, construit et stocke un objet OreGenParams à partir des données parsées.
     */
    private static void buildAndStoreParams(String name, Map<String, Object> params) {
        String generationType = (String) params.get("generation_type");
        if (generationType == null) {
            ORESMod.LOGGER.error("Skipping ore generation '{}': Missing required parameter 'generation_type'.", name);
            return;
        }

        List<String> requiredKeys = new ArrayList<>(List.of("ore", "size", "density", "discard_chance_on_air_exposure", "discard_chance_on_liquid_exposure", "min_height", "max_height", "dimension"));
        if ("ore".equalsIgnoreCase(generationType)) {
            requiredKeys.add("generation_shape");
        }

        for (String key : requiredKeys) {
            if (!params.containsKey(key)) {
                ORESMod.LOGGER.error("Skipping ore generation '{}': Missing required parameter '{}' for generation_type '{}'.", name, key, generationType);
                return;
            }
        }

        try {
            String associatedBlock = null;
            String bonusBlock = null;
            String generationShape = null;
            List<String> replaceableBlocks = null;

            if ("vein".equalsIgnoreCase(generationType)) {
                associatedBlock = (String) params.get("associated_block");
                bonusBlock = (String) params.get("bonus_block");
            } else { // "ore"
                generationShape = (String) params.get("generation_shape");
                if (params.containsKey("replaceable_blocks")) {
                    replaceableBlocks = (List<String>) params.get("replaceable_blocks");
                }
            }

            OreGenParams genParams = new OreGenParams(
                    (String) params.get("ore"),
                    ((Number) params.get("size")).intValue(),
                    ((Number) params.get("density")).floatValue(),
                    ((Number) params.get("discard_chance_on_air_exposure")).floatValue(),
                    ((Number) params.get("discard_chance_on_liquid_exposure")).floatValue(),
                    generationShape,
                    ((Number) params.get("min_height")).intValue(),
                    ((Number) params.get("max_height")).intValue(),
                    (String) params.get("dimension"),
                    params.containsKey("biomes") ? (List<String>) params.get("biomes") : null,
                    replaceableBlocks,
                    generationType,
                    associatedBlock,
                    bonusBlock,
                    params.containsKey("options") ? (List<String>) params.get("options") : null
            );
            ORE_GENERATION_CONFIGS.put(name, genParams);
            ORESMod.LOGGER.info("Loaded ore generation config for: {}", name);
        } catch (Exception e) {
            ORESMod.LOGGER.error("Failed to parse ore generation parameters for '{}'. Please check the config format and data types.", name, e);
        }
    }

    /**
     * Analyse une valeur de chaîne de caractères du TOML et la convertit dans le type approprié.
     */
    private static Object parseValue(String valueStr) {
        valueStr = valueStr.trim();
        if (valueStr.startsWith("[") && valueStr.endsWith("]")) {
            String content = valueStr.substring(1, valueStr.length() - 1).trim();
            if (content.isEmpty()) {
                return new ArrayList<String>();
            }
            String[] items = content.split(",");
            List<String> list = new ArrayList<>();
            for (String item : items) {
                list.add(item.trim().replace("\"", ""));
            }
            return list;
        }
        if (valueStr.startsWith("\"") && valueStr.endsWith("\"")) {
            return valueStr.substring(1, valueStr.length() - 1);
        }
        if ("true".equalsIgnoreCase(valueStr) || "false".equalsIgnoreCase(valueStr)) {
            return Boolean.parseBoolean(valueStr);
        }
        if (valueStr.contains(".")) {
            return Float.parseFloat(valueStr);
        }
        try {
            return Integer.parseInt(valueStr);
        } catch (NumberFormatException e) {
            return valueStr;
        }
    }

    /**
     * Génère le contenu par défaut pour le fichier ores_generation.toml.
     */
    private static String generateDefaultTomlContent() {
        return """
               # ORES Mod - Ore Generation Configuration
               # --- OVERWORLD ORES ---
               ["vanilla_coal_upper"]
               ore = "coal"
               size = 17
               density = 1.0
               discard_chance_on_air_exposure = 0.2
               discard_chance_on_liquid_exposure = 0.0
               generation_shape = "uniform"
               min_height = 136
               max_height = 320
               dimension = "minecraft:overworld"
               generation_type = "ore"
               
               ["vanilla_coal_lower"]
               ore = "coal"
               size = 17
               density = 1.0
               discard_chance_on_air_exposure = 0.2
               discard_chance_on_liquid_exposure = 0.0
               generation_shape = "trapezoid" # Peaks at Y=96
               min_height = 0
               max_height = 192
               dimension = "minecraft:overworld"
               generation_type = "ore"
               
               ["vanilla_copper"]
               ore = "copper"
               size = 9
               density = 1.0
               discard_chance_on_air_exposure = 0.2
               discard_chance_on_liquid_exposure = 0.0
               generation_shape = "trapezoid" # Peaks at Y=48
               min_height = -16
               max_height = 112
               dimension = "minecraft:overworld"
               generation_type = "ore"
               
               ["vanilla_iron_main"]
               ore = "iron"
               size = 9
               density = 1.0
               discard_chance_on_air_exposure = 0.2
               discard_chance_on_liquid_exposure = 0.0
               generation_shape = "trapezoid" # Peaks at Y=16
               min_height = -64
               max_height = 72
               dimension = "minecraft:overworld"
               generation_type = "ore"
               
               ["vanilla_iron_high"]
               ore = "iron"
               size = 9
               density = 0.8
               discard_chance_on_air_exposure = 0.2
               discard_chance_on_liquid_exposure = 0.0
               generation_shape = "trapezoid" # Peaks at Y=232
               min_height = 80
               max_height = 384
               dimension = "minecraft:overworld"
               generation_type = "ore"
               
               ["vanilla_gold"]
               ore = "gold"
               size = 9
               density = 1.0
               discard_chance_on_air_exposure = 0.2
               discard_chance_on_liquid_exposure = 0.0
               generation_shape = "uniform"
               min_height = -64
               max_height = 32
               dimension = "minecraft:overworld"
               generation_type = "ore"
               
               ["vanilla_lapis"]
               ore = "lapis"
               size = 7
               density = 1.0
               discard_chance_on_air_exposure = 0.2
               discard_chance_on_liquid_exposure = 0.0
               generation_shape = "trapezoid" # Peaks at Y=0
               min_height = -64
               max_height = 64
               dimension = "minecraft:overworld"
               generation_type = "ore"
               
               ["vanilla_redstone"]
               ore = "redstone"
               size = 8
               density = 1.0
               discard_chance_on_air_exposure = 0.2
               discard_chance_on_liquid_exposure = 0.0
               generation_shape = "uniform"
               min_height = -64
               max_height = 15
               dimension = "minecraft:overworld"
               generation_type = "ore"
               
               ["vanilla_diamond"]
               ore = "diamond"
               size = 8
               density = 1.0
               discard_chance_on_air_exposure = 0.5
               discard_chance_on_liquid_exposure = 0.0
               generation_shape = "trapezoid" # Peaks near the bottom of the world
               min_height = -64
               max_height = 16
               dimension = "minecraft:overworld"
               generation_type = "ore"
               
               ["vanilla_emerald"]
               ore = "emerald"
               size = 6
               density = 1.0
               discard_chance_on_air_exposure = 0.2
               discard_chance_on_liquid_exposure = 0.0
               generation_shape = "trapezoid" # Peaks high in mountains
               min_height = -16
               max_height = 320
               dimension = "minecraft:overworld"
               generation_type = "ore"
               biomes = ["minecraft:windswept_hills", "minecraft:windswept_forest", "minecraft:windswept_gravelly_hills", "minecraft:stony_peaks", "minecraft:snowy_slopes", "minecraft:meadow", "minecraft:grove", "minecraft:jagged_peaks", "minecraft:frozen_peaks"]
               
               # --- NETHER ORES ---
               ["vanilla_nether_quartz"]
               ore = "quartz"
               size = 10
               density = 1.0
               discard_chance_on_air_exposure = 0.0
               discard_chance_on_liquid_exposure = 0.0
               generation_shape = "uniform"
               min_height = 10
               max_height = 118
               dimension = "minecraft:the_nether"
               generation_type = "ore"
               
               ["vanilla_nether_gold"]
               ore = "gold"
               size = 10
               density = 1.0
               discard_chance_on_air_exposure = 0.0
               discard_chance_on_liquid_exposure = 0.0
               generation_shape = "uniform"
               min_height = 10
               max_height = 118
               dimension = "minecraft:the_nether"
               generation_type = "ore"
               """;
    }

    /**
     * Permet d'accéder aux configurations de génération de minerais chargées.
     * @return Une map immuable des configurations.
     */
    public static Map<String, OreGenParams> getOreGenerationConfigs() {
        return Collections.unmodifiableMap(ORE_GENERATION_CONFIGS);
    }
}
