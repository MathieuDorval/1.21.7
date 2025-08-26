/**
 * ORES MOD | __mathieu
 * Handles loading and accessing all mod configurations from datapacks and user files.
 */
package com.ores.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ores.ORESMod;
import com.ores.core.Materials;
import com.ores.core.Variants;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class ModConfig {

    // -=-=-=- CONSTANTS -=-=-=-
    public static final List<String> TO_GENERATE = new ArrayList<>();
    public static final List<String> VANILLA_EXCLUSIONS = List.of(
            "coal","raw_copper", "raw_iron", "raw_gold", "diamond", "emerald", "lapis", "lapis_lazuli", "netherite_scrap", "quartz",
            "copper_ingot", "iron_ingot", "gold_ingot", "netherite_ingot",
            "iron_nugget", "gold_nugget",
            "raw_iron_block", "raw_copper_block", "raw_gold_block",
            "coal_block", "iron_block", "gold_block", "copper_block", "diamond_block", "emerald_block", "lapis_block", "netherite_block", "quartz_block", "redstone_block"
    );
    private static final Set<String> VALID_IDS = new HashSet<>();
    private static final Path CONFIG_PATH = Paths.get("config", "ores_config.toml");

    // -=-=-=- CONFIG SETTINGS -=-=-=-
    private static final Map<String, Boolean> SPECIFIC_VARIANT_SETTINGS = new HashMap<>();
    private static final Map<String, Boolean> MATERIAL_SETTINGS = new HashMap<>();
    private static final Map<String, Boolean> ORE_GENERATION_SETTINGS = new HashMap<>();
    private static boolean DEBUG_MODE = false;

    // -=-=-=- INITIALIZATION -=-=-=-
    public static void initialize() {
        loadDatapackConfig();
        loadUserConfig();
    }

    private static void loadDatapackConfig() {
        populateValidIds();
        String configPath = "data/ores/generated.json";
        ORESMod.LOGGER.info("Scanning for datapack config files at: {}", configPath);

        try {
            Enumeration<URL> resources = ModConfig.class.getClassLoader().getResources(configPath);
            if (!resources.hasMoreElements()) {
                ORESMod.LOGGER.warn("No '{}' files found in any loaded mods.", configPath);
                return;
            }

            for (URL url : Collections.list(resources)) {
                try (InputStream inputStream = url.openStream()) {
                    JsonObject jsonObject = JsonParser.parseReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)).getAsJsonObject();
                    if (jsonObject.has("generate")) {
                        JsonArray generateArray = jsonObject.getAsJsonArray("generate");
                        for (int i = 0; i < generateArray.size(); i++) {
                            String idToGenerate = generateArray.get(i).getAsString();
                            if (VALID_IDS.contains(idToGenerate) && !TO_GENERATE.contains(idToGenerate) && !VANILLA_EXCLUSIONS.contains(idToGenerate)) {
                                TO_GENERATE.add(idToGenerate);
                            }
                        }
                    }
                } catch (Exception e) {
                    ORESMod.LOGGER.error("Failed to read or parse config file at {}. It will be skipped.", url.getPath(), e);
                }
            }
            ORESMod.LOGGER.info("Finished loading datapack configs. Total entries to generate: {}", TO_GENERATE.size());
        } catch (Exception e) {
            ORESMod.LOGGER.error("An error occurred while searching for '{}' files.", configPath, e);
        }
    }

    private static void loadUserConfig() {
        try {
            if (Files.notExists(CONFIG_PATH)) {
                Files.createDirectories(CONFIG_PATH.getParent());
                String defaultConfig = generateDefaultTomlContent();
                Files.writeString(CONFIG_PATH, defaultConfig);
            }

            ORESMod.LOGGER.info("Loading ORES user configuration from ores_config.toml...");
            SPECIFIC_VARIANT_SETTINGS.clear();
            MATERIAL_SETTINGS.clear();
            ORE_GENERATION_SETTINGS.clear();
            DEBUG_MODE = false;

            List<String> lines = Files.readAllLines(CONFIG_PATH);
            String currentSection = "";

            for (String line : lines) {
                line = line.trim();
                if (line.startsWith("#") || line.isEmpty()) {
                    continue;
                }

                if (line.equalsIgnoreCase("debug = true")) {
                    DEBUG_MODE = true;
                }

                if (line.startsWith("[") && line.endsWith("]")) {
                    currentSection = line.substring(1, line.length() - 1);
                    continue;
                }

                if (line.contains("=")) {
                    String[] parts = line.split("=", 2);
                    String key = parts[0].trim();
                    String valueStr = parts[1].trim();

                    switch (currentSection) {
                        case "materials" -> MATERIAL_SETTINGS.put(key, Boolean.parseBoolean(valueStr));
                        case "specific_variants" -> SPECIFIC_VARIANT_SETTINGS.put(key, Boolean.parseBoolean(valueStr));
                        case "ore_variants" -> ORE_GENERATION_SETTINGS.put(key, Boolean.parseBoolean(valueStr));
                        case "custom_item" -> {
                            if ("generate".equals(key) && valueStr.startsWith("[") && valueStr.endsWith("]")) {
                                String listContent = valueStr.substring(1, valueStr.length() - 1).trim();
                                if (!listContent.isEmpty()) {
                                    String[] items = listContent.split(",");
                                    for (String item : items) {
                                        String idToGenerate = item.trim().replace("\"", "");
                                        if (VALID_IDS.contains(idToGenerate) && !TO_GENERATE.contains(idToGenerate) && !VANILLA_EXCLUSIONS.contains(idToGenerate)) {
                                            TO_GENERATE.add(idToGenerate);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            ORESMod.LOGGER.info("ORES user configuration loaded. Debug mode is: {}", (DEBUG_MODE ? "ON" : "OFF"));
        } catch (IOException e) {
            ORESMod.LOGGER.error("Could not create or read the configuration file.", e);
        }
    }

    // -=-=-=- PUBLIC ACCESSORS -=-=-=-
    public static boolean isVariantEnabled(Materials material, Variants variant) {
        String variantId = variant.getFormattedId(material.getId());

        if (VANILLA_EXCLUSIONS.contains(variantId)) {
            return false;
        }

        boolean isDatagen = System.getProperty("fabric-api.datagen") != null;
        if (DEBUG_MODE || isDatagen) {
            return true;
        }

        if (TO_GENERATE.contains(variantId)) {
            return true;
        }

        if (!MATERIAL_SETTINGS.getOrDefault(material.getId(), false)) {
            return false;
        }

        Variants.Category category = variant.getCategory();
        if (category == Variants.Category.ORE || category == Variants.Category.FALLING_ORE || category == Variants.Category.INVERTED_FALLING_ORE) {
            return isOreVariantEnabled(variant.name());
        }

        String[] configKeys = variant.getConfig();
        if (configKeys == null) {
            return false;
        }

        for (String key : configKeys) {
            if (SPECIFIC_VARIANT_SETTINGS.getOrDefault(key, false)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isVariantEnabled(String variantId) {
        if (variantId.contains(":")) {
            variantId = variantId.substring(variantId.indexOf(":") + 1);
        }

        if (VANILLA_EXCLUSIONS.contains(variantId)) {
            return false;
        }

        boolean isDatagen = System.getProperty("fabric-api.datagen") != null;
        if (DEBUG_MODE || isDatagen) {
            return true;
        }

        if (TO_GENERATE.contains(variantId)) {
            return true;
        }

        for (Materials material : Materials.values()) {
            for (Variants variant : Variants.values()) {
                String id = variant.getFormattedId(material.getId());
                if (id.equals(variantId)) {
                    return isVariantEnabled(material, variant);
                }
            }
        }

        return false;
    }

    public static boolean isOreVariantEnabled(String variantName) {
        boolean isDatagen = System.getProperty("fabric-api.datagen") != null;
        if (DEBUG_MODE || isDatagen) {
            return true;
        }
        return ORE_GENERATION_SETTINGS.getOrDefault(variantName, true);
    }

    // -=-=-=- HELPERS -=-=-=-
    private static void populateValidIds() {
        for (Materials material : Materials.values()) {
            for (Variants variant : Variants.values()) {
                VALID_IDS.add(variant.getFormattedId(material.getId()));
            }
        }
    }

    private static String generateDefaultTomlContent() {
        StringBuilder content = new StringBuilder();
        content.append("# ORES Mod Configuration File\n\n");
        content.append("# Enable debug mode to register all items and blocks, ignoring all other settings.\n");
        content.append("debug = false\n\n");
        content.append("[materials]\n");
        for (Materials material : Materials.values()) {
            boolean shouldBeEnabledByDefault = false;
            for (Variants variant : Variants.values()) {
                String variantId = variant.getFormattedId(material.getId());
                if (TO_GENERATE.contains(variantId) || VANILLA_EXCLUSIONS.contains(variantId)) {
                    shouldBeEnabledByDefault = true;
                    break;
                }
            }
            content.append(material.getId()).append(" = ").append(shouldBeEnabledByDefault).append("\n");
        }
        content.append("\n[specific_variants]\n");
        content.append("nuggets = false\n");
        content.append("dusts = false\n");
        content.append("raw = false\n");
        content.append("mekanism_compat = false\n");
        content.append("compressed_x3 = false\n");
        content.append("compressed_x6 = false\n");
        content.append("compressed_x9 = false\n");
        content.append("\n[ore_variants]\n");
        for (Variants variant : Variants.values()) {
            if (variant.getCategory() == Variants.Category.ORE || variant.getCategory() == Variants.Category.FALLING_ORE || variant.getCategory() == Variants.Category.INVERTED_FALLING_ORE) {
                content.append(variant.name()).append(" = true\n");
            }
        }
        content.append("\n[custom_item]\n");
        content.append("# Add any item variant ID to this list to generate it, for example: [\"tin_ingot\", \"tin_nugget\"]\n");
        content.append("generate = []\n");
        return content.toString();
    }
}