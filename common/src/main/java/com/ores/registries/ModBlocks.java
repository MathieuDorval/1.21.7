/**
 * ORES MOD | __mathieu
 * Handles the dynamic registration of all mod blocks based on configuration.
 */
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
import net.minecraft.world.level.material.PushReaction;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ModBlocks {

    // -=-=-=- REGISTRY -=-=-=-
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ORESMod.MOD_ID, Registries.BLOCK);
    public static final Map<String, RegistrySupplier<Block>> DYNAMIC_BLOCKS = new HashMap<>();

    // -=-=-=- INITIALIZATION -=-=-=-
    public static void initialize() {
        ORESMod.LOGGER.info("Registering dynamic blocks...");

        for (Materials material : Materials.values()) {
            for (Variants variant : Variants.values()) {
                if (!ModConfig.isVariantEnabled(material, variant)) continue;

                switch (variant.getCategory()) {
                    case BLOCK -> registerSimpleBlock(material, variant);
                    case FALLING_BLOCK -> registerFallingBlock(material, variant);
                    case INVERTED_FALLING_BLOCK -> registerInvertedFallingBlock(material, variant);
                    case ORE -> registerOre(material, variant);
                    case FALLING_ORE -> registerFallingOre(material, variant);
                    case INVERTED_FALLING_ORE -> registerInvertedFallingOre(material, variant);
                }
            }
        }
        BLOCKS.register();
        ORESMod.LOGGER.info("Dynamic blocks registered successfully.");
    }

    // -=-=-=- REGISTRATION HELPERS -=-=-=-
    private static void registerSimpleBlock(Materials material, Variants variant) {
        Materials.BlockProps materialProps = material.getBlockProps();
        Variants.BlockProps variantProps = variant.getBlockProps();
        if (materialProps != null && variantProps != null) {
            String blockId = variant.getFormattedId(material.getId());
            register(blockId, () -> new Block(buildBlockProperties(blockId, materialProps, variantProps)));
        }
    }

    private static void registerFallingBlock(Materials material, Variants variant) {
        Materials.BlockProps materialProps = material.getBlockProps();
        Variants.BlockProps variantProps = variant.getBlockProps();
        if (materialProps != null && variantProps != null && variantProps.dropsOnFalling() != null) {
            String blockId = variant.getFormattedId(material.getId());
            register(blockId, () -> new CustomFallingBlock(buildBlockProperties(blockId, materialProps, variantProps), variantProps.dropsOnFalling()));
        }
    }

    private static void registerInvertedFallingBlock(Materials material, Variants variant) {
        Materials.BlockProps materialProps = material.getBlockProps();
        Variants.BlockProps variantProps = variant.getBlockProps();
        if (materialProps != null && variantProps != null && variantProps.dropsOnFalling() != null) {
            String blockId = variant.getFormattedId(material.getId());
            register(blockId, () -> new CustomInvertedFallingBlock(buildBlockProperties(blockId, materialProps, variantProps), variantProps.dropsOnFalling()));
        }
    }

    private static void registerOre(Materials material, Variants variant) {
        if (!ModConfig.isOreVariantEnabled(variant.name())) return;
        Materials.OreProps materialProps = material.getOreProps();
        Materials.BlockProps materialBlockProps = material.getBlockProps();
        Variants.OreProps variantProps = variant.getOreProps();
        if (materialProps != null && variantProps != null) {
            String blockId = variant.getFormattedId(material.getId());
            BlockBehaviour.Properties properties = buildOreProperties(blockId, materialProps, variantProps);
            UniformInt xpRange = UniformInt.of(materialProps.minXp(), materialProps.maxXp());

            if (materialProps.isRedstoneLike()) {
                register(blockId, () -> new CustomRedstoneOreBlock(properties, xpRange, materialBlockProps.mapColor().col));
            } else {
                register(blockId, () -> new DropExperienceBlock(xpRange, properties));
            }
        }
    }

    private static void registerFallingOre(Materials material, Variants variant) {
        if (!ModConfig.isOreVariantEnabled(variant.name())) return;
        Materials.OreProps materialProps = material.getOreProps();
        Materials.BlockProps materialBlockProps = material.getBlockProps();
        Variants.OreProps variantProps = variant.getOreProps();
        if (materialProps != null && variantProps != null) {
            String blockId = variant.getFormattedId(material.getId());
            BlockBehaviour.Properties properties = buildOreProperties(blockId, materialProps, variantProps);
            UniformInt xpRange = UniformInt.of(materialProps.minXp(), materialProps.maxXp());

            if (materialProps.isRedstoneLike()) {
                register(blockId, () -> new CustomFallingRedstoneOreBlock(properties, xpRange, materialBlockProps.mapColor().col));
            } else {
                register(blockId, () -> new CustomFallingOreBlock(properties, xpRange));
            }
        }
    }

    private static void registerInvertedFallingOre(Materials material, Variants variant) {
        if (!ModConfig.isOreVariantEnabled(variant.name())) return;
        Materials.OreProps materialProps = material.getOreProps();
        Variants.OreProps variantProps = variant.getOreProps();
        if (materialProps != null && variantProps != null) {
            String blockId = variant.getFormattedId(material.getId());
            BlockBehaviour.Properties properties = buildOreProperties(blockId, materialProps, variantProps);
            UniformInt xpRange = UniformInt.of(materialProps.minXp(), materialProps.maxXp());
            register(blockId, () -> new CustomInvertedFallingOreBlock(properties, xpRange));
        }
    }

    private static void register(String name, Supplier<Block> blockSupplier) {
        RegistrySupplier<Block> registeredBlock = BLOCKS.register(ResourceLocation.fromNamespaceAndPath(ORESMod.MOD_ID, name), blockSupplier);
        DYNAMIC_BLOCKS.put(name, registeredBlock);
    }

    // -=-=-=- PROPERTY BUILDERS -=-=-=-
    private static BlockBehaviour.Properties buildCommonProperties(String name, float destroyTime, float explosionResistance, int lightLevel) {
        BlockBehaviour.Properties properties = BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(ORESMod.MOD_ID, name)))
                .strength(destroyTime, explosionResistance);

        if (lightLevel > 0) {
            properties.lightLevel(state -> lightLevel);
        }
        return properties;
    }

    private static BlockBehaviour.Properties buildOreProperties(String name, Materials.OreProps materialProps, Variants.OreProps variantProps) {
        float destroyTime = materialProps.destroyTime() + variantProps.destroyTime();
        float explosionResistance = materialProps.explosionResistance() + variantProps.explosionResistance();
        int lightLevel = Math.max(
                materialProps.lightLevel() != null ? materialProps.lightLevel() : 0,
                variantProps.lightLevel() != null ? variantProps.lightLevel() : 0
        );

        BlockBehaviour.Properties properties = buildCommonProperties(name, destroyTime, explosionResistance, lightLevel)
                .requiresCorrectToolForDrops()
                .sound(variantProps.soundType())
                .mapColor(variantProps.mapColor())
                .instrument(variantProps.instrument());

        if (variantProps.friction() != null) properties.friction(variantProps.friction());
        if (variantProps.jumpFactor() != null) properties.jumpFactor(variantProps.jumpFactor());
        if (variantProps.speedFactor() != null) properties.speedFactor(variantProps.speedFactor());
        if (variantProps.pushReaction() != null) properties.pushReaction(variantProps.pushReaction());

        return properties;
    }

    private static BlockBehaviour.Properties buildBlockProperties(String name, Materials.BlockProps materialProps, Variants.BlockProps variantProps) {
        float destroyTime = materialProps.destroyTime() + variantProps.destroyTime();
        float explosionResistance = materialProps.explosionResistance() + variantProps.explosionResistance();
        int lightLevel = Math.max(
                materialProps.lightLevel() != null ? materialProps.lightLevel() : 0,
                variantProps.lightLevel() != null ? variantProps.lightLevel() : 0
        );

        BlockBehaviour.Properties properties = buildCommonProperties(name, destroyTime, explosionResistance, lightLevel)
                .sound(materialProps.soundType())
                .mapColor(materialProps.mapColor())
                .instrument(materialProps.instrument());

        if (Boolean.TRUE.equals(materialProps.requiresCorrectToolForDrops()) || Boolean.TRUE.equals(variantProps.requiresCorrectToolForDrops())) {
            properties.requiresCorrectToolForDrops();
        }

        if (variantProps.friction() != null && materialProps.friction() != null) {
            properties.friction((materialProps.friction() + variantProps.friction()) / 2.0f);
        }
        if (variantProps.jumpFactor() != null && materialProps.jumpFactor() != null) {
            properties.jumpFactor((materialProps.jumpFactor() + variantProps.jumpFactor()) / 2.0f);
        }
        if (variantProps.speedFactor() != null && materialProps.speedFactor() != null) {
            properties.speedFactor((materialProps.speedFactor() + variantProps.speedFactor()) / 2.0f);
        }

        PushReaction pushReaction = materialProps.pushReaction() != null ? materialProps.pushReaction() : variantProps.pushReaction();
        if (pushReaction != null) {
            properties.pushReaction(pushReaction);
        }

        return properties;
    }
}
