package com.ores.config;

import com.ores.core.Materials;
import com.ores.core.Variants;
import com.ores.config.ModGeneratedConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages the creation and loading of the mod's configuration file.
 */
public class ModConfig {

    private static final Path CONFIG_PATH = Paths.get("config", "ores_config.toml");
    private static final Map<String, Boolean> specificVariantSettings = new HashMap<>();
    private static final Map<String, Boolean> materialSettings = new HashMap<>();
    private static final Map<String, Boolean> oreGenerationSettings = new HashMap<>();
    private static boolean debugMode = false;

    /**
     * Generates the default content for the configuration file.
     * @return A string containing the default configuration.
     */
    private static String generateDefaultConfigContent() {
        StringBuilder content = new StringBuilder();

        content.append("# ORES Mod Configuration File\n");
        content.append("# Use this file to enable/disable features or adjust parameters.\n\n");

        content.append("# Enable or disable all variants for a specific material.\n");
        content.append("# Defaults to true if the material is used by vanilla or a datapack.\n");
        content.append("[materials]\n");
        for (Materials material : Materials.values()) {
            boolean shouldBeEnabledByDefault = false;
            for (Variants variant : Variants.values()) {
                String variantId = variant.getFormattedId(material.getId());
                if (ModGeneratedConfig.TO_GENERATE.contains(variantId) || ModGeneratedConfig.VANILLA_EXCLUSIONS.contains(variantId)) {
                    shouldBeEnabledByDefault = true;
                    break; // Found one, no need to check further for this material
                }
            }
            content.append(material.getId()).append(" = ").append(shouldBeEnabledByDefault).append("\n");
        }

        content.append("\n# Enable or disable specific variants groups.\n");
        content.append("[specific_variants]\n");
        content.append("nuggets = false\n");
        content.append("dusts = false\n");
        content.append("raw = false\n");
        content.append("mekanism_compat = false\n");
        content.append("compressed_x3 = false\n");
        content.append("compressed_x6 = false\n");
        content.append("compressed_x9 = false\n");

        content.append("\n# Enable or disable world generation for specific ore variant types.\n");
        content.append("[ore_variants]\n");
        for (Variants variant : Variants.values()) {
            if (variant.getCategory() == Variants.Category.ORE || variant.getCategory() == Variants.Category.FALLING_ORE || variant.getCategory() == Variants.Category.INVERTED_FALLING_ORE) {
                content.append(variant.name()).append(" = true\n");
            }
        }

        return content.toString();
    }

    /**
     * Initializes the configuration.
     * Creates the configuration file if it doesn't exist, then loads the settings.
     */
    public static void init() {
        try {
            if (Files.notExists(CONFIG_PATH)) {
                System.out.println("Configuration file 'ores_config.toml' not found, creating...");
                Files.createDirectories(CONFIG_PATH.getParent());
                String defaultConfig = generateDefaultConfigContent();
                Files.writeString(CONFIG_PATH, defaultConfig);
                System.out.println("Configuration file created successfully at: " + CONFIG_PATH.toAbsolutePath());
            }
            loadConfig();
        } catch (IOException e) {
            System.err.println("Critical error: Could not create or read the configuration file for the ORES mod.");
            e.printStackTrace();
        }
    }

    /**
     * Loads the settings from the ores_config.toml file into memory.
     * @throws IOException if there is an issue reading the file.
     */
    private static void loadConfig() throws IOException {
        System.out.println("Loading ORES mod configuration...");
        specificVariantSettings.clear();
        materialSettings.clear();
        oreGenerationSettings.clear();
        debugMode = false;

        List<String> lines = Files.readAllLines(CONFIG_PATH);
        String currentSection = "";

        for (String line : lines) {
            line = line.trim();

            if (line.equalsIgnoreCase("debug = true")) {
                debugMode = true;
                System.out.println("ORES mod: Debug mode enabled. All variants will be registered.");
            }

            if (line.startsWith("[") && line.endsWith("]")) {
                currentSection = line.substring(1, line.length() - 1);
                continue;
            }

            if (line.contains("=")) {
                String[] parts = line.split("=", 2);
                String key = parts[0].trim();
                boolean value = Boolean.parseBoolean(parts[1].trim());

                switch (currentSection) {
                    case "materials":
                        materialSettings.put(key, value);
                        break;
                    case "specific_variants":
                        specificVariantSettings.put(key, value);
                        break;
                    case "ore_variants":
                        oreGenerationSettings.put(key, value);
                        break;
                }
            }
        }
        System.out.println("ORES mod configuration loaded.");
    }

    /**
     * Checks if a specific ore variant generation is enabled in the configuration's [ore_variants] section.
     * @param variantName The name of the variant (e.g., "STONE_ORE").
     * @return true if the variant is enabled or not found in the config, false otherwise.
     */
    public static boolean isOreVariantEnabled(String variantName) {
        if (debugMode) {
            return true;
        }
        return oreGenerationSettings.getOrDefault(variantName, true);
    }

    /**
     * Checks if a variant should be enabled based on multiple conditions.
     * @param material The material of the variant.
     * @param variant The variant to check.
     * @return true if the variant should be enabled.
     */
    public static boolean isVariantEnabled(Materials material, Variants variant) {
        String variantId = variant.getFormattedId(material.getId());

        // Always skip vanilla items. The TO_GENERATE list is already filtered, so this is a final safety check.
        if (ModGeneratedConfig.VANILLA_EXCLUSIONS.contains(variantId)) {
            return false;
        }

        // Debug mode enables everything.
        if (debugMode) {
            return true;
        }

        // Priority 1: Always enable if it's in the generated list from a datapack.
        if (ModGeneratedConfig.TO_GENERATE.contains(variantId)) {
            return true;
        }

        // After this point, the variant is not in TO_GENERATE.
        // It can only be enabled via the config file.

        String[] configKeys = variant.getConfig();

        // If a variant has no specific group (e.g., INGOT, BLOCK), it's considered a "base" variant.
        // These can only be enabled by the TO_GENERATE list.
        // Since we already checked that list, these variants are disabled if not present there.
        if (configKeys == null || configKeys.length == 0) {
            return false;
        }

        // If the variant has a specific group, check if both the material AND the group are enabled in the config.
        if (!materialSettings.getOrDefault(material.getId(), false)) {
            return false; // Material is disabled in config.
        }

        // Material is enabled, now check if any of its specific variant groups are enabled.
        for (String key : configKeys) {
            if (specificVariantSettings.getOrDefault(key, false)) {
                // Material is enabled AND at least one variant group is enabled.
                return true;
            }
        }

        // Material is enabled, but none of its required variant groups are.
        return false;
    }
}
