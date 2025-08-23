package com.ores.worldgen;

import com.ores.config.ModOreGenConfig;
import com.ores.core.Variants;
import com.ores.registries.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

import java.util.Map;
import java.util.Optional;

/**
 * Contient la logique pour la génération de minerais personnalisée.
 */
public class ModOreGenerator {

    /**
     * Méthode principale appelée par le Mixin pour générer tous les minerais configurés.
     *
     * @param region L'accès au monde pendant la génération.
     * @param chunk Le chunk en cours de génération.
     */
    public static void generateOres(WorldGenRegion region, ChunkAccess chunk) {
        BlockPos chunkStartPos = new BlockPos(chunk.getPos().getMinBlockX(), region.getMinY(), chunk.getPos().getMinBlockZ());
        RandomSource random = region.getRandom();

        // Récupère toutes les configurations de génération de minerai
        Map<String, ModOreGenConfig.OreGenConfig> configs = ModOreGenConfig.getOreGenerationConfigs();

        // Itère sur chaque configuration
        for (ModOreGenConfig.OreGenConfig config : configs.values()) {
            // On ne traite que le type "ore" pour l'instant
            if (config instanceof ModOreGenConfig.OreConfig oreConfig) {
                generateSingleOre(oreConfig, region, random, chunkStartPos);
            }
        }
    }

    /**
     * Gère la génération pour une seule configuration de type "ore".
     * C'est ici que la répartition des filons est gérée.
     */
    private static void generateSingleOre(ModOreGenConfig.OreConfig config, WorldGenRegion region, RandomSource random, BlockPos chunkStartPos) {
        // --- 1. Vérification de la Dimension ---
        ResourceLocation currentDimension = region.getLevel().dimension().location();
        if (!currentDimension.toString().equals(config.dimension())) {
            return; // On n'est pas dans la bonne dimension, on arrête
        }

        // --- 2. Détermination du nombre de filons à placer ---
        int attempts = config.count();

        // --- 3. Boucle de placement de filon ---
        for (int i = 0; i < attempts; i++) {
            int y;
            // --- NOUVELLE LOGIQUE DE HAUTEUR ---
            if ("trapezoid".equalsIgnoreCase(config.generationShape())) {
                y = getYTriangular(random, config.minHeight(), config.maxHeight());
            } else { // "uniform" et autres cas par défaut
                y = config.minHeight() + random.nextInt(config.maxHeight() - config.minHeight() + 1);
            }

            int x = chunkStartPos.getX() + random.nextInt(16);
            int z = chunkStartPos.getZ() + random.nextInt(16);
            BlockPos originPos = new BlockPos(x, y, z);

            // --- 4. Vérification du Biome ---
            if (config.biomes() != null && !config.biomes().isEmpty()) {
                ResourceLocation biomeId = region.getBiome(originPos).unwrapKey().map(ResourceKey::location).orElse(null);
                if (biomeId == null || !config.biomes().contains(biomeId.toString())) {
                    continue; // Le biome ne correspond pas, on passe à la tentative suivante
                }
            }

            // --- 5. Génération du filon ---
            generateVein(config, region, random, originPos);
        }
    }

    /**
     * Place les blocs d'un seul filon de minerai. La taille du filon est contrôlée par 'size'.
     */
    private static void generateVein(ModOreGenConfig.OreConfig config, WorldGenRegion region, RandomSource random, BlockPos originPos) {
        int size = config.size();
        for (int i = 0; i < size; i++) {
            // Calcule une position aléatoire autour du point d'origine pour un effet "cluster"
            // La dispersion des blocs dépend aussi de la taille du filon
            int spread = Math.max(1, size / 2);
            int offsetX = random.nextInt(spread) - random.nextInt(spread);
            int offsetY = random.nextInt(spread / 2 + 1) - random.nextInt(spread / 2 + 1);
            int offsetZ = random.nextInt(spread) - random.nextInt(spread);
            BlockPos currentPos = originPos.offset(offsetX, offsetY, offsetZ);

            placeSingleBlock(config, region, currentPos);
        }
    }

    /**
     * Tente de placer un seul bloc de minerai à une position donnée.
     */
    private static void placeSingleBlock(ModOreGenConfig.OreConfig config, WorldGenRegion region, BlockPos pos) {
        BlockState stateAtPos = region.getBlockState(pos);

        // --- 6. Vérification du bloc remplaçable ---
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(stateAtPos.getBlock());
        if (config.replaceableBlocks() != null && !config.replaceableBlocks().isEmpty()) {
            if (!config.replaceableBlocks().contains(blockId.toString())) {
                return; // Ce bloc n'est pas dans la liste des blocs remplaçables
            }
        }

        // --- 7. Détermination du bon bloc de minerai à placer ---
        Optional<Variants> variant = Variants.fromStoneId(blockId.toString());
        if (variant.isEmpty()) {
            return; // On ne sait pas quel minerai placer sur ce type de bloc
        }

        // Récupère le bloc de minerai
        Block oreBlock = ModBlocks.ORES_MAP.get(config.ore()).get(variant.get()).get();
        if (oreBlock == null) {
            return; // Pas de bloc de minerai défini pour cette combinaison
        }

        // --- 8. Placement du bloc ---
        if (pos.getY() <= region.getMaxY() && pos.getY() >= region.getMinY()) {
            region.setBlock(pos, oreBlock.defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    /**
     * Calcule une hauteur Y en utilisant une distribution triangulaire.
     * Les valeurs proches du centre de la plage (min/max) ont plus de chance d'être choisies.
     *
     * @param random L'instance RandomSource.
     * @param min La hauteur minimale.
     * @param max La hauteur maximale.
     * @return Une hauteur Y.
     */
    private static int getYTriangular(RandomSource random, int min, int max) {
        // Le "mode" (pic de la distribution) est au centre de la plage
        float center = (min + max) / 2.0f;
        float a = random.nextFloat();
        float b = random.nextFloat();
        // Cette formule simple simule une distribution triangulaire
        return min + (int) (Math.abs(a - b) * (max - min + 1));
    }
}
