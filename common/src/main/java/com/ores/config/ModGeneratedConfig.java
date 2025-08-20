package com.ores.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ores.core.Materials;
import com.ores.core.Variants;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Handles reading generated configuration files from the data directory of all loaded mods.
 */
public class ModGeneratedConfig {

    /**
     * A list of item/block IDs that are marked for generation in all found generated.json files.
     */
    public static final List<String> TO_GENERATE = new ArrayList<>();
    /**
     * A list of all vanilla variant IDs that should be excluded from generation.
     * This replaces the dynamic list from the Materials enum.
     */
    public static final List<String> VANILLA_EXCLUSIONS = List.of(
            "coal", "raw_iron", "raw_copper", "raw_gold", "diamond", "emerald", "lapis_lazuli", "netherite_scrap", "quartz",
            "iron_ingot", "gold_ingot", "copper_ingot", "netherite_ingot",
            "iron_nugget", "gold_nugget",
            "coal_block", "raw_iron_block", "raw_copper_block", "raw_gold_block", "iron_block", "gold_block", "copper_block", "diamond_block", "emerald_block", "lapis_block", "netherite_block", "quartz_block"
    );
    private static final Set<String> VALID_IDS = new HashSet<>();

    /**
     * Populates the set of all possible valid IDs from the Materials and Variants enums.
     */
    private static void populateValidIds() {
        for (Materials material : Materials.values()) {
            for (Variants variant : Variants.values()) {
                VALID_IDS.add(variant.getFormattedId(material.getId()));
            }
        }
    }

    /**
     * Initializes the loading of the generated configuration from all mods.
     * This method should be called once during mod initialization.
     */
    public static void init() {
        populateValidIds();
        String path = "data/ores/generated.json";
        System.out.println("Scanning all mods for generated config files at: " + path);

        try {
            Enumeration<URL> resources = ModGeneratedConfig.class.getClassLoader().getResources(path);

            if (!resources.hasMoreElements()) {
                System.out.println("No 'data/ores/generated.json' files found in any loaded mods.");
                return;
            }

            for (URL url : Collections.list(resources)) {
                System.out.println("Found generated.json: " + url.getPath());
                try (InputStream inputStream = url.openStream()) {
                    InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                    JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();

                    if (jsonObject.has("generate")) {
                        JsonArray generateArray = jsonObject.getAsJsonArray("generate");
                        int countBefore = TO_GENERATE.size();
                        for (int i = 0; i < generateArray.size(); i++) {
                            String idToGenerate = generateArray.get(i).getAsString();
                            if (VALID_IDS.contains(idToGenerate) && !TO_GENERATE.contains(idToGenerate) && !VANILLA_EXCLUSIONS.contains(idToGenerate)) {
                                TO_GENERATE.add(idToGenerate);
                            }
                        }
                        int countAfter = TO_GENERATE.size();
                        System.out.println("Loaded " + (countAfter - countBefore) + " new valid entries from " + url.getPath());
                    } else {
                        System.err.println("File " + url.getPath() + " is missing the 'generate' array.");
                    }
                } catch (Exception e) {
                    System.err.println("Failed to read or parse " + url.getPath() + ". This file will be skipped.");
                    e.printStackTrace();
                }
            }
            System.out.println("Finished loading generated configs. Total valid entries to generate: " + TO_GENERATE.size());
        } catch (Exception e) {
            System.err.println("An error occurred while searching for generated.json files.");
            e.printStackTrace();
        }
    }
}
