package com.ores.registries;

import com.ores.ORESMod;
import com.ores.block.*;
import com.ores.config.ModConfig;
import com.ores.core.Materials;
import com.ores.core.Variants;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ORESMod.MOD_ID, Registries.BLOCK);

    public static final Map<String, RegistrySupplier<Block>> DYNAMIC_BLOCKS = new HashMap<>();

    public static void initBlocks(){
        for (Materials material : Materials.values()) {
            List<String> exclusions = material.getVanillaExclusions().excludedVariantIds();

            for (Variants variant : Variants.values()) {
                String blockId = variant.getFormattedId(material.getId());
                if (exclusions != null && exclusions.contains(blockId)) continue;

                Materials.BlockProps materialBlockProps = material.getBlockProps();
                Variants.BlockProps variantBlockProps = variant.getBlockProps();
                Materials.OreProps materialOreProps = material.getOreProps();
                Variants.OreProps variantOreProps = variant.getOreProps();

                switch (variant.getCategory()) {
                    case BLOCK:
                        if (materialBlockProps != null && variantBlockProps != null) {
                            RegistrySupplier<Block> blockSupplier = registerBlock(blockId, () -> new Block(buildBlockProperties(blockId, materialBlockProps, variantBlockProps)));
                            DYNAMIC_BLOCKS.put(blockId, blockSupplier);
                        }
                        break;
                    case FALLING_BLOCK:
                        if (materialBlockProps != null && variantBlockProps != null && variantBlockProps.dropsOnFalling() != null) {
                            RegistrySupplier<Block> fallingBlockSupplier = registerBlock(blockId, () -> new CustomFallingBlock(buildBlockProperties(blockId, materialBlockProps, variantBlockProps), variantBlockProps.dropsOnFalling()));
                            DYNAMIC_BLOCKS.put(blockId, fallingBlockSupplier);
                        }
                        break;
                    case INVERTED_FALLING_BLOCK:
                        if (materialBlockProps != null && variantBlockProps != null && variantBlockProps.dropsOnFalling() != null) {
                            RegistrySupplier<Block> invertedFallingBlock = registerBlock(blockId, () -> new CustomInvertedFallingBlock(buildBlockProperties(blockId, materialBlockProps, variantBlockProps), variantBlockProps.dropsOnFalling()));
                            DYNAMIC_BLOCKS.put(blockId, invertedFallingBlock);
                        }
                        break;
                    case ORE:
                        if (!ModConfig.isOreVariantEnabled(variant.name())) continue;
                        if (materialOreProps != null && variantOreProps != null) {
                            BlockBehaviour.Properties oreProps = buildOreProperties(blockId, materialOreProps, variantOreProps);
                            RegistrySupplier<Block> oreSupplier;
                            if (materialOreProps.isRedstoneLike()) {
                                oreSupplier = registerBlock(blockId, () -> new CustomRedstoneOreBlock(oreProps, UniformInt.of(materialOreProps.minXp(), materialOreProps.maxXp()), variantOreProps.mapColor().col));
                            } else {
                                oreSupplier = registerBlock(blockId, () -> new DropExperienceBlock(UniformInt.of(materialOreProps.minXp(), materialOreProps.maxXp()), oreProps));
                            }
                            DYNAMIC_BLOCKS.put(blockId, oreSupplier);
                        }
                        break;
                    case FALLING_ORE:
                        if (!ModConfig.isOreVariantEnabled(variant.name())) continue;
                        if (materialOreProps != null && variantOreProps != null) {
                            BlockBehaviour.Properties fallingOreProps = buildOreProperties(blockId, materialOreProps, variantOreProps);
                            RegistrySupplier<Block> fallingOreSupplier;
                            if (materialOreProps.isRedstoneLike()) {
                                fallingOreSupplier = registerBlock(blockId, () -> new CustomFallingRedstoneOreBlock(fallingOreProps, UniformInt.of(materialOreProps.minXp(), materialOreProps.maxXp()), variantOreProps.mapColor().col));
                            } else {
                                fallingOreSupplier = registerBlock(blockId, () -> new CustomFallingOreBlock(fallingOreProps, UniformInt.of(materialOreProps.minXp(), materialOreProps.maxXp())));
                            }
                            DYNAMIC_BLOCKS.put(blockId, fallingOreSupplier);
                        }
                        break;
                    case INVERTED_FALLING_ORE:
                        if (!ModConfig.isOreVariantEnabled(variant.name())) continue;
                        if (materialOreProps != null && variantOreProps != null) {
                            RegistrySupplier<Block> invertedFallingOre = registerBlock(blockId, () -> new CustomInvertedFallingOreBlock(buildOreProperties(blockId, materialOreProps, variantOreProps), UniformInt.of(materialOreProps.minXp(), materialOreProps.maxXp())));
                            DYNAMIC_BLOCKS.put(blockId, invertedFallingOre);
                        }
                        break;
                }
            }
        }
        BLOCKS.register();
    }

    public static RegistrySupplier<Block> registerBlock(String name, Supplier<Block> block){
        return BLOCKS.register(ResourceLocation.fromNamespaceAndPath(ORESMod.MOD_ID, name), block);
    }

    private static BlockBehaviour.Properties buildOreProperties(String name, Materials.OreProps matProps, Variants.OreProps varProps) {
        BlockBehaviour.Properties props = BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(ORESMod.MOD_ID, name)))
                .requiresCorrectToolForDrops();

        props.sound(varProps.soundType())
                .mapColor(varProps.mapColor())
                .instrument(varProps.instrument());

        float destroyTime = matProps.destroyTime() + varProps.destroyTime();
        float explosionResistance = matProps.explosionResistance() + varProps.explosionResistance();
        props.strength(destroyTime, explosionResistance);

        Integer matLight = matProps.lightLevel();
        Integer varLight = varProps.lightLevel();
        if (matLight != null && varLight != null) {
            int maxLight = Math.max(varLight, matLight);
            if (maxLight > 0) props.lightLevel(state -> maxLight);
        }

        if (varProps.friction() != null) props.friction(varProps.friction());
        if (varProps.jumpFactor() != null) props.jumpFactor(varProps.jumpFactor());
        if (varProps.speedFactor() != null) props.speedFactor(varProps.speedFactor());
        if (varProps.pushReaction() != null) props.pushReaction(varProps.pushReaction());

        return props;
    }

    private static BlockBehaviour.Properties buildBlockProperties(String name, Materials.BlockProps matProps, Variants.BlockProps varProps) {
        BlockBehaviour.Properties props = BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(ORESMod.MOD_ID, name)));

        props.sound(matProps.soundType())
                .mapColor(matProps.mapColor())
                .instrument(matProps.instrument());

        float destroyTime = matProps.destroyTime() + varProps.destroyTime();
        float explosionResistance = matProps.explosionResistance() + varProps.explosionResistance();
        props.strength(destroyTime, explosionResistance);

        Integer matLight = matProps.lightLevel();
        Integer varLight = varProps.lightLevel();
        if (matLight != null && varLight != null) {
            int maxLight = Math.max(varLight, matLight);
            if (maxLight > 0) props.lightLevel(state -> maxLight);
        }

        if (matProps.requiresCorrectToolForDrops() != null && varProps.requiresCorrectToolForDrops() != null) {
            if (matProps.requiresCorrectToolForDrops() || varProps.requiresCorrectToolForDrops()) {
                props.requiresCorrectToolForDrops();
            }
        }

        if (varProps.friction() != null && matProps.friction() != null) {
            props.friction((matProps.friction() + varProps.friction()) / 2.0f);
        }
        if (varProps.jumpFactor() != null && matProps.jumpFactor() != null) {
            props.jumpFactor((matProps.jumpFactor() + varProps.jumpFactor()) / 2.0f);
        }
        if (varProps.speedFactor() != null && matProps.speedFactor() != null) {
            props.speedFactor((matProps.speedFactor() + varProps.speedFactor()) / 2.0f);
        }

        if (matProps.pushReaction() != null) {
            props.pushReaction(matProps.pushReaction());
        } else if (varProps.pushReaction() != null) {
            props.pushReaction(varProps.pushReaction());
        }

        return props;
    }
}
