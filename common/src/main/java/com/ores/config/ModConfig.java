package com.ores.config;

import com.ores.core.Materials;
import com.ores.core.Variants;

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

    // Defines the standard path for the configuration file.
    private static final Path CONFIG_PATH = Paths.get("config", "ores_config.toml");
    // Stores the loaded settings from the config file.
    private static final Map<String, Boolean> oreVariantSettings = new HashMap<>();
    // Flag for debug mode. If true, all variants are registered regardless of config.
    private static boolean debugMode = false;

    /**
     * Generates the default content for the configuration file.
     * @return A string containing the default configuration.
     */
    private static String generateDefaultConfigContent() {
        StringBuilder content = new StringBuilder();

        content.append("# ORES Mod Configuration File\n");
        content.append("# Use this file to enable/disable features or adjust parameters.\n\n");

        // --- Section 1: Ore Variants ---
        content.append("# Enable or disable generation for specific ore variant types.\n");
        content.append("[ore_variants]\n");
        for (Variants variant : Variants.values()) {
            // We only add ore-related types to this section
            if (variant.getCategory() == Variants.Category.ORE || variant.getCategory() == Variants.Category.FALLING_ORE || variant.getCategory() == Variants.Category.INVERTED_FALLING_ORE) {
                content.append(variant.name()).append(" = true\n");
            }
        }

        // --- Section 2: Specific Variants (for future use) ---
        content.append("\n# Enable or disable specific variants.\n");
        content.append("# This section will be used for more granular control in the future.\n");
        content.append("[specific_variants]\n");
        // Example placeholder:
        // granite_coal_ore = true

        return content.toString();
    }

    /**
     * Initializes the configuration.
     * Creates the configuration file if it doesn't exist, then loads the settings.
     */
    public static void init() {
        try {
            // Check if the configuration file does not exist.
            if (Files.notExists(CONFIG_PATH)) {
                System.out.println("Configuration file 'ores_config.toml' not found, creating...");

                // Ensure the parent 'config' directory exists.
                Files.createDirectories(CONFIG_PATH.getParent());

                // Create the file and write the dynamically generated content.
                String defaultConfig = generateDefaultConfigContent();
                Files.writeString(CONFIG_PATH, defaultConfig);

                System.out.println("Configuration file created successfully at: " + CONFIG_PATH.toAbsolutePath());
            }
            // Load the configuration from the file.
            loadConfig();
        } catch (IOException e) {
            // In case of an error during file creation or reading.
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
        // Reset state before loading
        oreVariantSettings.clear();
        debugMode = false;

        List<String> lines = Files.readAllLines(CONFIG_PATH);
        String currentSection = "";

        for (String line : lines) {
            line = line.trim();

            // Check for debug mode globally, outside of any section.
            if (line.equalsIgnoreCase("debug = true")) {
                debugMode = true;
                System.out.println("ORES mod: Debug mode enabled. All variants will be registered.");
            }

            if (line.startsWith("[") && line.endsWith("]")) {
                currentSection = line.substring(1, line.length() - 1);
                continue;
            }

            if (currentSection.equals("ore_variants") && line.contains("=")) {
                String[] parts = line.split("=", 2);
                String key = parts[0].trim();
                boolean value = Boolean.parseBoolean(parts[1].trim());
                oreVariantSettings.put(key, value);
            }
        }
        System.out.println("ORES mod configuration loaded.");
    }

    /**
     * Checks if a specific ore variant is enabled in the configuration.
     * @param variantName The name of the variant (e.g., "STONE_ORE").
     * @return true if the variant is enabled or not found in the config, false otherwise.
     */
    public static boolean isOreVariantEnabled(String variantName) {
        // If debug mode is on, always return true, bypassing the config.
        if (debugMode) {
            return true;
        }
        // Defaults to true if the key is not present in the config file.
        return oreVariantSettings.getOrDefault(variantName, true);
    }
}
