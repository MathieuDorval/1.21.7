/**
 * ORES MOD | __mathieu
 * Handles the datagen for block tags.
 */
package com.ores.fabric.datagen;

import com.ores.config.ModConfig;
import com.ores.core.Materials;
import com.ores.core.Variants;
import com.ores.registries.ModBlocks;
import dev.architectury.registry.registries.RegistrySupplier;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ModBlockTagProvider extends FabricTagProvider<Block> {

    // -=-=-=- CONSTRUCTOR -=-=-=-
    public ModBlockTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, Registries.BLOCK, registriesFuture);
    }

    // -=-=-=- TAG GENERATION -=-=-=-
    @Override
    protected void addTags(HolderLookup.Provider provider) {
        for (Materials material : Materials.values()) {
            for (Variants variant : Variants.values()) {
                Variants.Category category = variant.getCategory();
                if (category != Variants.Category.BLOCK && category != Variants.Category.ORE &&
                        category != Variants.Category.FALLING_ORE && category != Variants.Category.INVERTED_FALLING_ORE) {
                    continue;
                }

                findBlock(material, variant).ifPresent(block -> applyTagsToBlock(block, material, variant));
            }
        }
    }

    // -=-=-=- HELPERS -=-=-=-
    private void applyTagsToBlock(Block block, Materials material, Variants variant) {
        Integer toolLevel = null;
        Materials.Tools tool = null;

        if (variant.getBlockProps() != null) {
            toolLevel = variant.getBlockProps().toolLevel();
            tool = variant.getBlockProps().tool();
        } else if (variant.getOreProps() != null) {
            toolLevel = variant.getOreProps().toolLevel();
            tool = variant.getOreProps().tool();
        }

        // === EFFICIENT TOOLS ===
        if (tool != null) {
            switch (tool) {
                case PICKAXE -> builder(BlockTags.MINEABLE_WITH_PICKAXE).add(block.builtInRegistryHolder().key());
                case AXE -> builder(BlockTags.MINEABLE_WITH_AXE).add(block.builtInRegistryHolder().key());
                case SHOVEL -> builder(BlockTags.MINEABLE_WITH_SHOVEL).add(block.builtInRegistryHolder().key());
                case HOE -> builder(BlockTags.MINEABLE_WITH_HOE).add(block.builtInRegistryHolder().key());
                case SWORD -> builder(BlockTags.SWORD_EFFICIENT).add(block.builtInRegistryHolder().key());
            }
        }

        // === TOOL LEVELS ===
        if (toolLevel != null) {
            switch (toolLevel) {
                case 1 -> builder(BlockTags.NEEDS_STONE_TOOL).add(block.builtInRegistryHolder().key());
                case 2 -> builder(BlockTags.NEEDS_IRON_TOOL).add(block.builtInRegistryHolder().key());
                case 3 -> builder(BlockTags.NEEDS_DIAMOND_TOOL).add(block.builtInRegistryHolder().key());
            }
        }

        // === BEACON MATERIAL ===
        if (variant.getCategory() == Variants.Category.BLOCK) {
            Materials.Tags materialTags = material.getTags();
            Variants.BlockProps variantBlockProps = variant.getBlockProps();
            if (materialTags != null && variantBlockProps != null && materialTags.beacon() && variantBlockProps.beacon()) {
                builder(BlockTags.BEACON_BASE_BLOCKS).add(block.builtInRegistryHolder().key());
            }
        }
    }

    private Optional<Block> findBlock(Materials material, Variants variant) {
        String formattedId = variant.getFormattedId(material.getId());
        RegistrySupplier<Block> modBlockSupplier = ModBlocks.DYNAMIC_BLOCKS.get(formattedId);
        if (modBlockSupplier != null) {
            return Optional.of(modBlockSupplier.get());
        }
        if (ModConfig.VANILLA_EXCLUSIONS.contains(formattedId)) {
            return BuiltInRegistries.BLOCK.getOptional(ResourceLocation.fromNamespaceAndPath("minecraft", formattedId));
        }
        return Optional.empty();
    }
}
