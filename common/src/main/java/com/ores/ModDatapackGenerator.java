package com.ores;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ores.config.ModOreGenConfig;
import com.ores.core.Materials;
import com.ores.core.Variants;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ores.config.ModOreGenConfig.OreConfig;

public class ModDatapackGenerator {

    private static final String DATAPACK_FOLDER = "ores_generations";

    public static void generateDynamicDatapack(Path gameDirectory) {
        ORESMod.LOGGER.info("Generating dynamic datapack...");

        ModOreGenConfig.initialize();

        Path datapackPath = gameDirectory.resolve("datapacks").resolve(DATAPACK_FOLDER);
        Path dataPath = datapackPath.resolve("data").resolve(ORESMod.MOD_ID);
        Path configuredFeaturePath = dataPath.resolve("worldgen").resolve("configured_feature");
        Path placedFeaturePath = dataPath.resolve("worldgen").resolve("placed_feature");

        try {
            if (Files.exists(datapackPath)) {
                FileUtils.deleteDirectory(datapackPath.toFile());
            }
            Files.createDirectories(configuredFeaturePath);
            Files.createDirectories(placedFeaturePath);
        } catch (IOException e) {
            ORESMod.LOGGER.error("Failed to prepare dynamic datapack directory.", e);
            return;
        }

        for (var entry : ModOreGenConfig.getOreGenerationConfigs().entrySet()) {
            String configName = entry.getKey();
            ModOreGenConfig.OreGenConfig config = entry.getValue();

            if (config instanceof OreConfig oreConfig) {
                try {
                    JsonObject configuredFeatureJson = createConfiguredFeatureJson(oreConfig);
                    JsonObject placedFeatureJson = createPlacedFeatureJson(configName, oreConfig);

                    Path configuredFile = configuredFeaturePath.resolve(configName + ".json");
                    Path placedFile = placedFeaturePath.resolve(configName + ".json");

                    Files.writeString(configuredFile, configuredFeatureJson.toString());
                    Files.writeString(placedFile, placedFeatureJson.toString());
                } catch (IOException e) {
                    ORESMod.LOGGER.error("Failed to write JSON files for config: {}", configName, e);
                }
            }
        }

        JsonObject packMeta = new JsonObject();
        JsonObject pack = new JsonObject();
        pack.addProperty("pack_format", 4438);
        pack.addProperty("description", "Dynamic ore generation for ORES Mod");
        packMeta.add("pack", pack);

        try {
            Files.writeString(datapackPath.resolve("pack.mcmeta"), packMeta.toString());
        } catch (IOException e) {
            ORESMod.LOGGER.error("Failed to create pack.mcmeta for dynamic datapack.", e);
        }

        ORESMod.LOGGER.info("Dynamic datapack generated successfully at {}", datapackPath);
    }

    private static JsonObject createConfiguredFeatureJson(OreConfig config) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "minecraft:ore");

        JsonObject configJson = new JsonObject();
        configJson.addProperty("size", config.size());
        configJson.addProperty("discard_chance_on_air_exposure", config.discardChanceOnAirExposure());

        JsonArray targets = new JsonArray();

        List<String> replaceableBlocks;
        if (config.replaceableBlocks() != null && !config.replaceableBlocks().isEmpty()) {
            replaceableBlocks = config.replaceableBlocks();
        } else {
            replaceableBlocks = Stream.of(Variants.values())
                    .filter(variant -> {
                        Variants.Category category = variant.getCategory();
                        return (category == Variants.Category.ORE || category == Variants.Category.FALLING_ORE || category == Variants.Category.INVERTED_FALLING_ORE)
                                && variant.getOreProps() != null;
                    })
                    .map(variant -> variant.getOreProps().idStone())
                    .distinct()
                    .collect(Collectors.toList());
        }

        for (String replaceableBlockId : replaceableBlocks) {
            Optional<Variants> oreVariantOpt = Variants.fromStoneId(replaceableBlockId);
            oreVariantOpt.ifPresent(variant -> {
                JsonObject target = new JsonObject();
                JsonObject targetPredicate = new JsonObject();
                targetPredicate.addProperty("predicate_type", "minecraft:block_match");
                targetPredicate.addProperty("block", replaceableBlockId);
                target.add("target", targetPredicate);

                JsonObject state = new JsonObject();
                state.addProperty("Name", getBlockId(config.ore(), variant));
                target.add("state", state);
                targets.add(target);
            });
        }

        configJson.add("targets", targets);
        json.add("config", configJson);
        return json;
    }

    private static JsonObject createPlacedFeatureJson(String configName, OreConfig config) {
        JsonObject json = new JsonObject();
        json.addProperty("feature", "ores:" + configName);

        JsonArray placement = new JsonArray();

        JsonObject countPlacement = new JsonObject();
        countPlacement.addProperty("type", "minecraft:count");
        countPlacement.addProperty("count", config.count());
        placement.add(countPlacement);

        JsonObject inSquarePlacement = new JsonObject();
        inSquarePlacement.addProperty("type", "minecraft:in_square");
        placement.add(inSquarePlacement);

        JsonObject heightRangePlacement = new JsonObject();
        heightRangePlacement.addProperty("type", "minecraft:height_range");
        JsonObject height = new JsonObject();
        height.addProperty("type", "minecraft:trapezoid");

        JsonObject minInclusive = new JsonObject();
        minInclusive.addProperty("above_bottom", config.minHeight());
        height.add("min_inclusive", minInclusive);

        JsonObject maxInclusive = new JsonObject();
        maxInclusive.addProperty("above_bottom", config.maxHeight());
        height.add("max_inclusive", maxInclusive);

        heightRangePlacement.add("height", height);
        placement.add(heightRangePlacement);

        JsonObject biomeFilter = new JsonObject();
        biomeFilter.addProperty("type", "minecraft:biome");
        placement.add(biomeFilter);

        json.add("placement", placement);
        return json;
    }

    private static String getBlockId(Materials material, Variants variant) {
        return "ores:" + variant.getFormattedId(material.getId());
    }
}