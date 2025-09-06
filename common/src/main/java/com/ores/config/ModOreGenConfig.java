// common/src/main/java/com/ores/config/ModOreGenConfig.java
package com.ores.config;

import com.ores.ORESMod;
import com.ores.core.Materials;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class ModOreGenConfig {

    private static final Path CONFIG_PATH = Paths.get("config", "ores_generation.toml");
    private static final Map<String, OreGenConfig> ORE_GENERATION_CONFIGS = new HashMap<>();

    public sealed interface OreGenConfig permits OreConfig, VeinConfig {}

    public record OreConfig(
            Materials ore,
            int size,
            int count,
            float discardChanceOnAirExposure,
            float discardChanceOnLiquidExposure,
            String generationShape,
            String generationType,
            int minHeight,
            int maxHeight,
            String dimension,
            @Nullable List<String> biomes,
            @Nullable List<String> blacklistedBiomes,
            @Nullable List<String> replaceableBlocks,
            @Nullable List<String> blacklistedBlocks,
            @Nullable List<String> options
    ) implements OreGenConfig {}

    public record VeinConfig(
            String ore,
            String associatedBlock,
            String bonusBlock,
            int minHeight,
            int maxHeight
    ) implements OreGenConfig {}


    public static void initialize() {
        try {
            if (Files.notExists(CONFIG_PATH)) {
                ORESMod.LOGGER.info("Creating default ore generation config file at {}...", CONFIG_PATH);
                Files.createDirectories(CONFIG_PATH.getParent());
                Files.writeString(CONFIG_PATH, generateDefaultTomlContent());
            }
            loadConfig();
        } catch (IOException e) {
            ORESMod.LOGGER.error("Could not create or read the ore generation configuration file.", e);
        }
    }

    private static void loadConfig() {
        ORE_GENERATION_CONFIGS.clear();
        try {
            List<String> lines = Files.readAllLines(CONFIG_PATH);
            Map<String, Object> currentSectionParams = null;
            String currentSectionName = null;

            for (String raw : lines) {
                String line = raw.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                if (line.startsWith("[") && line.endsWith("]")) {
                    if (currentSectionName != null && currentSectionParams != null) {
                        buildAndStoreParams(currentSectionName, currentSectionParams);
                    }
                    currentSectionName = line.substring(1, line.length() - 1).replace("\"", "");
                    currentSectionParams = new HashMap<>();
                } else if (currentSectionParams != null && line.contains("=")) {
                    String[] parts = line.split("=", 2);
                    String key = parts[0].trim();
                    String valueStr = parts[1].trim();

                    if (valueStr.contains("#")) {
                        valueStr = valueStr.substring(0, valueStr.indexOf('#')).trim();
                    }

                    currentSectionParams.put(key, parseValue(valueStr));
                }
            }
            if (currentSectionName != null && currentSectionParams != null) {
                buildAndStoreParams(currentSectionName, currentSectionParams);
            }

        } catch (IOException e) {
            ORESMod.LOGGER.error("Failed to read ore generation config file.", e);
        }
    }

    private static void buildAndStoreParams(String name, Map<String, Object> params) {
        String type = (String) params.get("type");
        if (type == null) {
            ORESMod.LOGGER.error("Skipping config section '{}': Missing required parameter 'type'. It must be 'ore' or 'vein'.", name);
            return;
        }

        try {
            switch (type.toLowerCase(Locale.ROOT)) {
                case "ore" -> {
                    List<String> requiredKeys = List.of("ore", "size", "count", "discardChanceOnAirExposure", "discardChanceOnLiquidExposure", "generationShape", "generationType", "minHeight", "maxHeight", "dimension");
                    validateKeys(name, params, requiredKeys);

                    String oreId = (String) params.get("ore");
                    Materials material = Materials.fromId(oreId)
                            .orElseThrow(() -> new IllegalArgumentException("Unknown material for type 'ore': '" + oreId + "'. Please use a valid ID from the Materials enum."));

                    OreConfig oreConfig = new OreConfig(
                            material,
                            ((Number) params.get("size")).intValue(),
                            ((Number) params.get("count")).intValue(),
                            ((Number) params.get("discardChanceOnAirExposure")).floatValue(),
                            ((Number) params.get("discardChanceOnLiquidExposure")).floatValue(),
                            (String) params.get("generationShape"),
                            (String) params.get("generationType"),
                            ((Number) params.get("minHeight")).intValue(),
                            ((Number) params.get("maxHeight")).intValue(),
                            (String) params.get("dimension"),
                            (List<String>) params.get("biomes"),
                            (List<String>) params.get("blacklistedBiomes"),
                            (List<String>) params.get("replaceableBlocks"),
                            (List<String>) params.get("blacklistedBlocks"),
                            (List<String>) params.get("options")
                    );
                    ORE_GENERATION_CONFIGS.put(name, oreConfig);
                    ORESMod.LOGGER.info("Loaded 'ore' config for: {}", name);
                }
                case "vein" -> {
                    List<String> requiredKeys = List.of("ore", "associatedBlock", "bonus_block", "minHeight", "maxHeight");
                    validateKeys(name, params, requiredKeys);

                    VeinConfig veinConfig = new VeinConfig(
                            (String) params.get("ore"),
                            (String) params.get("associatedBlock"),
                            (String) params.get("bonus_block"),
                            ((Number) params.get("minHeight")).intValue(),
                            ((Number) params.get("maxHeight")).intValue()
                    );
                    ORE_GENERATION_CONFIGS.put(name, veinConfig);
                    ORESMod.LOGGER.info("Loaded 'vein' config for: {}", name);
                }
                default -> ORESMod.LOGGER.error("Skipping config section '{}': Unknown type '{}'. Must be 'ore' or 'vein'.", name, type);
            }
        } catch (Exception e) {
            ORESMod.LOGGER.error("Failed to parse parameters for config section '{}'. Error: {}", name, e.getMessage());
        }
    }

    private static void validateKeys(String sectionName, Map<String, Object> params, List<String> requiredKeys) {
        List<String> missingKeys = requiredKeys.stream()
                .filter(key -> !params.containsKey(key))
                .toList();

        if (!missingKeys.isEmpty()) {
            throw new IllegalArgumentException(String.format("Section '%s' is missing required parameters: %s", sectionName, missingKeys));
        }
    }

    private static Object parseValue(String valueStr) {
        valueStr = valueStr.trim();
        if (valueStr.startsWith("[") && valueStr.endsWith("]")) {
            String content = valueStr.substring(1, valueStr.length() - 1).trim();
            if (content.isEmpty()) return new ArrayList<String>();
            return Arrays.stream(content.split(","))
                    .map(item -> item.trim().replace("\"", ""))
                    .collect(Collectors.toList());
        }
        if (valueStr.startsWith("\"") && valueStr.endsWith("\"")) {
            return valueStr.substring(1, valueStr.length() - 1);
        }
        if (valueStr.contains(".")) {
            try { return Float.parseFloat(valueStr); } catch (NumberFormatException ignored) {}
        }
        try { return Integer.parseInt(valueStr); } catch (NumberFormatException ignored) {}
        return valueStr;
    }

    private static String generateDefaultTomlContent() {
        return """             
               # --- EXEMPLE DE TYPE "ore" ---
               ["vanilla_diamond_deepslate"]
               type = "ore"
               ore = "diamond"
               size = 4
               count = 50
               discardChanceOnAirExposure = 0.5
               discardChanceOnLiquidExposure = 1.0
               generationShape = "vanilla"
               generationType = "uniform"
               minHeight = -64
               maxHeight = 16
               dimension = "minecraft:overworld"

               # Paramètres facultatifs
               # biomes = ["minecraft:deep_dark", "minecraft:dripstone_caves"] # Whitelist: le minerai n'apparaîtra QUE dans ces biomes.
               # blacklistedBiomes = ["minecraft:plains"] # Blacklist: le minerai n'apparaîtra PAS dans ce biome.
               # replaceableBlocks = ["minecraft:deepslate", "minecraft:tuff"]
               # blacklistedBlocks = ["minecraft:bedrock", "minecraft:obsidian"]
               # options = ["ocean_floor", "cave_surface"]

               # --- EXEMPLE DE TYPE "vein" ---
               # Tous les champs de type bloc ('ore', 'associatedBlock', 'bonus_block') DOIVENT avoir un namespace.
               ["custom_tin_vein"]
               type = "vein"
               ore = "ores:andesite_tin_ore"
               associatedBlock = "minecraft:andesite"
               bonus_block = "ores:raw_tin_block"
               minHeight = 0
               maxHeight = 64
               """;
    }

    public static Map<String, OreGenConfig> getOreGenerationConfigs() {
        return Collections.unmodifiableMap(ORE_GENERATION_CONFIGS);
    }
}

