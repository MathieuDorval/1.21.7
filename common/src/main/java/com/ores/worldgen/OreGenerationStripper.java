package com.ores.worldgen;

import com.ores.ORESMod;
import com.ores.core.Materials;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.block.Block;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public final class OreGenerationStripper {

    private OreGenerationStripper() {}

    public static void stripAllOreGenerations(RegistryAccess registryAccess, ServerLevel server) {
        try {
            Set<String> keywords = buildMaterialKeywords();

            Registry<PlacedFeature> placedReg = registryAccess.lookupOrThrow(Registries.PLACED_FEATURE);
            Registry<ConfiguredFeature<?, ?>> configuredReg = registryAccess.lookupOrThrow(Registries.CONFIGURED_FEATURE);
            Registry<Block> blockReg = registryAccess.lookupOrThrow(Registries.BLOCK);

            Set<ResourceLocation> placedToRemove = new HashSet<>();
            Set<ResourceLocation> configuredToRemove = new HashSet<>();

            for (ConfiguredFeature<?, ?> cfg : configuredReg) {
                if (cfg.config() instanceof OreConfiguration oreCfg) {
                    boolean matches = oreConfigurationTargetsKeywords(oreCfg, blockReg, keywords);
                    if (matches) {
                        ResourceLocation key = configuredReg.getKey(cfg);
                        if (key != null) configuredToRemove.add(key);
                    }
                }
            }

            for (PlacedFeature pf : placedReg) {
                Holder<ConfiguredFeature<?, ?>> referenced = pf.feature();
                if (referenced.isBound()) {
                    ResourceLocation key = configuredReg.getKey(referenced.value());
                    if (key != null && configuredToRemove.contains(key)) {
                        ResourceLocation pfKey = placedReg.getKey(pf);
                        if (pfKey != null) placedToRemove.add(pfKey);
                    }
                }
            }

            Registry<Biome> biomeReg = registryAccess.lookupOrThrow(Registries.BIOME);
            int totalRemoved = 0;
            for (Biome biome : biomeReg) {
                try {
                    var genSettings = biome.getGenerationSettings();
                    List<HolderSet<PlacedFeature>> features = genSettings.features();

                    boolean modified = false;
                    List<HolderSet<PlacedFeature>> newFeatures = new ArrayList<>();

                    for (HolderSet<PlacedFeature> holderSet : features) {
                        List<Holder<PlacedFeature>> filtered = holderSet.stream()
                                .filter(h -> {
                                    try {
                                        ResourceLocation id = placedReg.getKey(h.value());
                                        return id == null || !placedToRemove.contains(id);
                                    } catch (Exception ex) {
                                        return true;
                                    }
                                })
                                .collect(Collectors.toList());

                        if (filtered.size() != holderSet.size()) modified = true;
                        newFeatures.add(HolderSet.direct(filtered));
                    }

                    if (modified) {
                        totalRemoved += computeRemovedCount(features, newFeatures);
                        boolean replaced = replaceBiomeFeaturesWithReflection(biome, newFeatures);
                        if (!replaced) {
                            ORESMod.LOGGER.warn("Couldn't mutate biome generation features for biome");
                        }
                    }

                } catch (Exception e) {
                    ORESMod.LOGGER.error("Error while processing biome : {}", e.getMessage());
                }
            }

            ORESMod.LOGGER.info("OreGenerationStripper removed {} placed-feature references matching ORES keywords.", totalRemoved);

        } catch (Exception ex) {
            ORESMod.LOGGER.error("OreGenerationStripper failed: ", ex);
        }
    }

    private static Set<String> buildMaterialKeywords() {
        Set<String> set = new HashSet<>();
        for (Materials m : Materials.values()) {
            set.add(m.getId());
            String base = m.getIdBase();
            if (base != null && base.contains(":")) {
                String after = base.substring(base.indexOf(':') + 1);
                set.add(after);
            }
        }
        return set;
    }

    private static boolean oreConfigurationTargetsKeywords(OreConfiguration oreCfg, Registry<Block> blockReg, Set<String> keywords) {
        try {
            for (OreConfiguration.TargetBlockState t : oreCfg.targetStates) {
                BlockState state = t.state;
                Block block = state.getBlock();
                ResourceLocation id = blockReg.getKey(block);
                if (id != null) {
                    String path = id.getPath().toLowerCase(Locale.ROOT);
                    for (String kw : keywords) {
                        if (path.contains(kw.toLowerCase(Locale.ROOT))) return true;
                    }
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    private static int computeRemovedCount(List<HolderSet<PlacedFeature>> oldFeatures, List<HolderSet<PlacedFeature>> newFeatures) {
        int oldCount = oldFeatures.stream().mapToInt(HolderSet::size).sum();
        int newCount = newFeatures.stream().mapToInt(HolderSet::size).sum();
        return Math.max(0, oldCount - newCount);
    }

    @SuppressWarnings("unchecked")
    private static boolean replaceBiomeFeaturesWithReflection(Biome biome, List<HolderSet<PlacedFeature>> newFeatures) {
        try {
            Object genSettings = biome.getGenerationSettings();
            Field[] fields = genSettings.getClass().getDeclaredFields();
            for (Field f : fields) {
                if (!f.canAccess(genSettings)) f.setAccessible(true);
                Object val = f.get(genSettings);
                if (val instanceof List) {
                    f.set(genSettings, newFeatures);
                    return true;
                }
            }
        } catch (Exception e) {
            ORESMod.LOGGER.debug("Reflection replaceBiomeFeatures failed: {}", e.getMessage());
        }
        return false;
    }
}
