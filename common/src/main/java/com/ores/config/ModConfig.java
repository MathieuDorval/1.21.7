package com.ores.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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

    // --- Propriétés de ModGeneratedConfig ---
    public static final List<String> TO_GENERATE = new ArrayList<>();
    public static final List<String> VANILLA_EXCLUSIONS = List.of(
            "coal", "raw_iron", "raw_copper", "raw_gold", "diamond", "emerald", "lapis", "lapis_lazuli", "netherite_scrap", "quartz",
            "iron_ingot", "gold_ingot", "copper_ingot", "netherite_ingot",
            "iron_nugget", "gold_nugget",
            "raw_iron_block", "raw_copper_block", "raw_gold_block",
            "coal_block", "iron_block", "gold_block", "copper_block", "diamond_block", "emerald_block", "lapis_block", "netherite_block", "quartz_block"
    );
    private static final Set<String> VALID_IDS = new HashSet<>();

    // --- Propriétés de ModConfig ---
    private static final Path CONFIG_PATH = Paths.get("config", "ores_config.toml");
    private static final Map<String, Boolean> specificVariantSettings = new HashMap<>();
    private static final Map<String, Boolean> materialSettings = new HashMap<>();
    private static final Map<String, Boolean> oreGenerationSettings = new HashMap<>();
    private static boolean debugMode = false;

    /**
     * Méthode d'initialisation principale qui charge toutes les configurations.
     */
    public static void init() {
        // Étape 1 : Charger la configuration des datapacks (.json)
        loadGeneratedConfig();
        // Étape 2 : Charger la configuration de l'utilisateur (.toml)
        loadUserConfig();
    }

    private static void loadGeneratedConfig() {
        populateValidIds();
        String path = "data/ores/generated.json";
        System.out.println("Scanning all mods for generated config files at: " + path);

        try {
            Enumeration<URL> resources = ModConfig.class.getClassLoader().getResources(path);
            if (!resources.hasMoreElements()) {
                System.out.println("No 'data/ores/generated.json' files found in any loaded mods.");
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
                    System.err.println("Failed to read or parse " + url.getPath() + ". This file will be skipped.");
                }
            }
            System.out.println("Finished loading generated configs. Total entries to generate from datapacks: " + TO_GENERATE.size());
        } catch (Exception e) {
            System.err.println("An error occurred while searching for generated.json files.");
        }
    }

    private static void loadUserConfig() {
        try {
            if (Files.notExists(CONFIG_PATH)) {
                Files.createDirectories(CONFIG_PATH.getParent());
                String defaultConfig = generateDefaultConfigContent();
                Files.writeString(CONFIG_PATH, defaultConfig);
            }

            System.out.println("Loading ORES user configuration from ores_config.toml...");
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
                        case "materials" -> materialSettings.put(key, value);
                        case "specific_variants" -> specificVariantSettings.put(key, value);
                        case "ore_variants" -> oreGenerationSettings.put(key, value);
                    }
                }
            }
            System.out.println("ORES user configuration loaded. Debug mode is: " + (debugMode ? "ON" : "OFF"));
        } catch (IOException e) {
            System.err.println("Critical error: Could not create or read the configuration file for the ORES mod.");
        }
    }

    public static boolean isVariantEnabled(Materials material, Variants variant) {
        String variantId = variant.getFormattedId(material.getId());

        if (VANILLA_EXCLUSIONS.contains(variantId)) {
            return false;
        }

        // Le mode Datagen ou le mode Debug active tout
        if (debugMode || System.getProperty("fabric-api.datagen") != null) {
            return true;
        }

        if (TO_GENERATE.contains(variantId)) {
            return true;
        }

        String[] configKeys = variant.getConfig();
        if (configKeys == null || configKeys.length == 0) {
            return false;
        }

        if (!materialSettings.getOrDefault(material.getId(), false)) {
            return false;
        }

        for (String key : configKeys) {
            if (specificVariantSettings.getOrDefault(key, false)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isOreVariantEnabled(String variantName) {
        if (debugMode || System.getProperty("fabric-api.datagen") != null) {
            return true;
        }
        return oreGenerationSettings.getOrDefault(variantName, true);
    }

    private static void populateValidIds() {
        for (Materials material : Materials.values()) {
            for (Variants variant : Variants.values()) {
                VALID_IDS.add(variant.getFormattedId(material.getId()));
            }
        }
    }

    private static String generateDefaultConfigContent() {
        // Cette méthode dépend de TO_GENERATE, c'est pourquoi on la laisse ici.
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
        return content.toString();
    }
}