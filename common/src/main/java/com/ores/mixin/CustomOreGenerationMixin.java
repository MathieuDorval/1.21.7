package com.ores.mixin;

import com.ores.config.ModConfig;
import com.ores.config.ModOreGenConfig;
import com.ores.core.Materials;
import com.ores.core.Variants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.OreFeature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mixin(OreFeature.class)
public class CustomOreGenerationMixin {

    @Inject(method = "place", at = @At("HEAD"))
    private void onPlace(FeaturePlaceContext<OreConfiguration> context, CallbackInfoReturnable<Boolean> cir) {
        WorldGenLevel world = context.level();
        BlockPos origin = context.origin();

        for (Map.Entry<String, ModOreGenConfig.OreGenParams> entry : ModOreGenConfig.getOreGenerationConfigs().entrySet()) {
            ModOreGenConfig.OreGenParams params = entry.getValue();

            if (!"ore".equalsIgnoreCase(params.generationType())) {
                continue;
            }

            ResourceLocation currentDimension = world.getLevel().dimension().location();
            if (!currentDimension.toString().equals(params.dimension())) {
                continue;
            }

            if (params.biomes() != null && !params.biomes().isEmpty()) {
                ResourceLocation currentBiome = world.getBiome(origin).unwrapKey().map(ResourceKey::location).orElse(null);
                if (currentBiome == null || !params.biomes().contains(currentBiome.toString())) {
                    continue;
                }
            }

            Optional<Materials> materialToPlaceOpt = ores$findMaterialById(params.ore());
            if (materialToPlaceOpt.isEmpty()) {
                continue;
            }
            Materials materialToPlace = materialToPlaceOpt.get();

            boolean hasActiveOreVariant = false;
            for (Variants variant : Variants.values()) {
                if (variant.getCategory() == Variants.Category.ORE || variant.getCategory() == Variants.Category.FALLING_ORE || variant.getCategory() == Variants.Category.INVERTED_FALLING_ORE) {
                    if (ModConfig.isVariantEnabled(materialToPlace, variant)) {
                        hasActiveOreVariant = true;
                        break;
                    }
                }
            }
            if (!hasActiveOreVariant) {
                continue;
            }

            List<RuleTest> replaceableRules = new ArrayList<>();
            if (params.replaceableBlocks() == null || params.replaceableBlocks().isEmpty()) {
                for (Variants variant : Variants.values()) {
                    if (variant.getCategory() == Variants.Category.ORE || variant.getCategory() == Variants.Category.FALLING_ORE || variant.getCategory() == Variants.Category.INVERTED_FALLING_ORE) {
                        if (ModConfig.isVariantEnabled(materialToPlace, variant)) {
                            Variants.OreProps oreProps = variant.getOreProps();
                            if (oreProps != null) {
                                Optional<Block> stoneBlockOpt = BuiltInRegistries.BLOCK.getOptional(ResourceLocation.tryParse(oreProps.idStone()));
                                stoneBlockOpt.ifPresent(block -> replaceableRules.add(new net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest(block)));
                            }
                        }
                    }
                }
            } else {
                for (String stoneIdStr : params.replaceableBlocks()) {
                    for (Variants variant : Variants.values()) {
                        Variants.OreProps oreProps = variant.getOreProps();
                        if (oreProps != null && oreProps.idStone().equals(stoneIdStr)) {
                            if (ModConfig.isVariantEnabled(materialToPlace, variant)) {
                                String targetOreId = "ores:" + variant.getFormattedId(materialToPlace.getId());
                                Optional<Block> targetOreBlockOpt = BuiltInRegistries.BLOCK.getOptional(ResourceLocation.tryParse(targetOreId));
                                targetOreBlockOpt.ifPresent(block -> replaceableRules.add(new net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest(block)));
                            }
                            break;
                        }
                    }
                }
            }

            if (replaceableRules.isEmpty()) {
                continue;
            }

            // --- Logique de génération de minerai ---
            switch (params.generationShape().toLowerCase()) {
                case "uniform":
                    ores$generateUniform(context, params, materialToPlace, replaceableRules);
                    break;
                case "trapezoid":
                    ores$generateTrapezoid(context, params, materialToPlace, replaceableRules);
                    break;
                case "inverted_trapezoid":
                    ores$generateInvertedTrapezoid(context, params, materialToPlace, replaceableRules);
                    break;
                case "bottom_base":
                    ores$generateBottomBase(context, params, materialToPlace, replaceableRules);
                    break;
                case "top_base":
                    ores$generateTopBase(context, params, materialToPlace, replaceableRules);
                    break;
            }
        }
    }

    @Unique
    private void ores$generateUniform(FeaturePlaceContext<OreConfiguration> context, ModOreGenConfig.OreGenParams params, Materials materialToPlace, List<RuleTest> replaceableRules) {
        BlockPos origin = context.origin();
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        int effectiveSize = (params.options() != null && params.options().contains("generate_as_single_block")) ? 1 : params.size();

        for (int i = 0; i < effectiveSize; ++i) {
            int xOffset = context.random().nextInt(params.size() / 2 + 1) - context.random().nextInt(params.size() / 2 + 1);
            int zOffset = context.random().nextInt(params.size() / 2 + 1) - context.random().nextInt(params.size() / 2 + 1);
            int y = context.random().nextInt(params.maxHeight() - params.minHeight() + 1) + params.minHeight();

            mutablePos.set(origin.getX() + xOffset, y, origin.getZ() + zOffset);

            if (context.random().nextFloat() >= params.density()) {
                continue;
            }

            ores$placeBlock(context, params, materialToPlace, replaceableRules, mutablePos);
        }
    }

    @Unique
    private void ores$generateTrapezoid(FeaturePlaceContext<OreConfiguration> context, ModOreGenConfig.OreGenParams params, Materials materialToPlace, List<RuleTest> replaceableRules) {
        BlockPos origin = context.origin();
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        int effectiveSize = (params.options() != null && params.options().contains("generate_as_single_block")) ? 1 : params.size();

        double midHeight = params.minHeight() + (double) (params.maxHeight() - params.minHeight()) / 2.0;
        double heightRangeHalf = (double) (params.maxHeight() - params.minHeight()) / 2.0;

        for (int i = 0; i < effectiveSize; ++i) {
            int xOffset = context.random().nextInt(params.size() / 2 + 1) - context.random().nextInt(params.size() / 2 + 1);
            int zOffset = context.random().nextInt(params.size() / 2 + 1) - context.random().nextInt(params.size() / 2 + 1);
            int y = context.random().nextInt(params.maxHeight() - params.minHeight() + 1) + params.minHeight();

            mutablePos.set(origin.getX() + xOffset, y, origin.getZ() + zOffset);

            double distanceFromCenter = Math.abs(y - midHeight);
            double heightModifier = heightRangeHalf > 0 ? (1.0 - (distanceFromCenter / heightRangeHalf)) : 1.0;

            if (context.random().nextFloat() >= params.density() * heightModifier) {
                continue;
            }

            ores$placeBlock(context, params, materialToPlace, replaceableRules, mutablePos);
        }
    }

    @Unique
    private void ores$generateInvertedTrapezoid(FeaturePlaceContext<OreConfiguration> context, ModOreGenConfig.OreGenParams params, Materials materialToPlace, List<RuleTest> replaceableRules) {
        BlockPos origin = context.origin();
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        int effectiveSize = (params.options() != null && params.options().contains("generate_as_single_block")) ? 1 : params.size();

        double midHeight = params.minHeight() + (double) (params.maxHeight() - params.minHeight()) / 2.0;
        double heightRangeHalf = (double) (params.maxHeight() - params.minHeight()) / 2.0;

        for (int i = 0; i < effectiveSize; ++i) {
            int xOffset = context.random().nextInt(params.size() / 2 + 1) - context.random().nextInt(params.size() / 2 + 1);
            int zOffset = context.random().nextInt(params.size() / 2 + 1) - context.random().nextInt(params.size() / 2 + 1);
            int y = context.random().nextInt(params.maxHeight() - params.minHeight() + 1) + params.minHeight();

            mutablePos.set(origin.getX() + xOffset, y, origin.getZ() + zOffset);

            double distanceFromCenter = Math.abs(y - midHeight);
            double heightModifier = heightRangeHalf > 0 ? (distanceFromCenter / heightRangeHalf) : 0.0;

            if (context.random().nextFloat() >= params.density() * heightModifier) {
                continue;
            }

            ores$placeBlock(context, params, materialToPlace, replaceableRules, mutablePos);
        }
    }

    @Unique
    private void ores$generateBottomBase(FeaturePlaceContext<OreConfiguration> context, ModOreGenConfig.OreGenParams params, Materials materialToPlace, List<RuleTest> replaceableRules) {
        BlockPos origin = context.origin();
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        int effectiveSize = (params.options() != null && params.options().contains("generate_as_single_block")) ? 1 : params.size();
        double heightRange = params.maxHeight() - params.minHeight();

        for (int i = 0; i < effectiveSize; ++i) {
            int xOffset = context.random().nextInt(params.size() / 2 + 1) - context.random().nextInt(params.size() / 2 + 1);
            int zOffset = context.random().nextInt(params.size() / 2 + 1) - context.random().nextInt(params.size() / 2 + 1);
            int y = context.random().nextInt(params.maxHeight() - params.minHeight() + 1) + params.minHeight();
            mutablePos.set(origin.getX() + xOffset, y, origin.getZ() + zOffset);

            double heightModifier = heightRange > 0 ? (1.0 - ((double)(y - params.minHeight()) / heightRange)) : 1.0;

            if (context.random().nextFloat() >= params.density() * heightModifier) {
                continue;
            }
            ores$placeBlock(context, params, materialToPlace, replaceableRules, mutablePos);
        }
    }

    @Unique
    private void ores$generateTopBase(FeaturePlaceContext<OreConfiguration> context, ModOreGenConfig.OreGenParams params, Materials materialToPlace, List<RuleTest> replaceableRules) {
        BlockPos origin = context.origin();
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        int effectiveSize = (params.options() != null && params.options().contains("generate_as_single_block")) ? 1 : params.size();
        double heightRange = params.maxHeight() - params.minHeight();

        for (int i = 0; i < effectiveSize; ++i) {
            int xOffset = context.random().nextInt(params.size() / 2 + 1) - context.random().nextInt(params.size() / 2 + 1);
            int zOffset = context.random().nextInt(params.size() / 2 + 1) - context.random().nextInt(params.size() / 2 + 1);
            int y = context.random().nextInt(params.maxHeight() - params.minHeight() + 1) + params.minHeight();
            mutablePos.set(origin.getX() + xOffset, y, origin.getZ() + zOffset);

            double heightModifier = heightRange > 0 ? ((double)(y - params.minHeight()) / heightRange) : 1.0;

            if (context.random().nextFloat() >= params.density() * heightModifier) {
                continue;
            }
            ores$placeBlock(context, params, materialToPlace, replaceableRules, mutablePos);
        }
    }

    @Unique
    private void ores$placeBlock(FeaturePlaceContext<OreConfiguration> context, ModOreGenConfig.OreGenParams params, Materials materialToPlace, List<RuleTest> replaceableRules, BlockPos.MutableBlockPos mutablePos) {
        BlockState existingState = context.level().getBlockState(mutablePos);
        boolean canBeReplaced = false;
        for (RuleTest rule : replaceableRules) {
            if (rule.test(existingState, context.random())) {
                canBeReplaced = true;
                break;
            }
        }

        if (canBeReplaced) {
            BlockState oreStateToPlace = ores$getOreStateForReplacedBlock(existingState.getBlock(), materialToPlace);
            if (oreStateToPlace == null) return;

            if (params.options() != null) {
                if (params.options().contains("generate_only_on_air_contact")) {
                    boolean hasAirContact = false;
                    for (Direction dir : Direction.values()) {
                        if (context.level().getBlockState(mutablePos.relative(dir)).isAir()) {
                            hasAirContact = true;
                            break;
                        }
                    }
                    if (!hasAirContact) return;
                }

                if (params.options().contains("generate_only_on_liquid_contact")) {
                    boolean hasLiquidContact = false;
                    for (Direction dir : Direction.values()) {
                        if (!context.level().getFluidState(mutablePos.relative(dir)).isEmpty()) {
                            hasLiquidContact = true;
                            break;
                        }
                    }
                    if (!hasLiquidContact) return;
                }
            }

            boolean discard = false;
            if (params.discardChanceOnAirExposure() > 0) {
                for (Direction dir : Direction.values()) {
                    if (context.level().getBlockState(mutablePos.relative(dir)).isAir()) {
                        if (context.random().nextFloat() < params.discardChanceOnAirExposure()) {
                            discard = true;
                            break;
                        }
                    }
                }
            }
            if (discard) return;

            if (params.discardChanceOnLiquidExposure() > 0) {
                for (Direction dir : Direction.values()) {
                    if (!context.level().getFluidState(mutablePos.relative(dir)).isEmpty()) {
                        if (context.random().nextFloat() < params.discardChanceOnLiquidExposure()) {
                            discard = true;
                            break;
                        }
                    }
                }
            }
            if (discard) return;

            context.level().setBlock(mutablePos, oreStateToPlace, 2);
        }
    }

    @Unique
    private BlockState ores$getOreStateForReplacedBlock(Block replacedBlock, Materials material) {
        for (Variants variant : Variants.values()) {
            if (variant.getCategory() == Variants.Category.ORE || variant.getCategory() == Variants.Category.FALLING_ORE || variant.getCategory() == Variants.Category.INVERTED_FALLING_ORE) {
                if (ModConfig.isVariantEnabled(material, variant)) {
                    Variants.OreProps oreProps = variant.getOreProps();
                    if (oreProps != null) {
                        Optional<Block> stoneBlockOpt = BuiltInRegistries.BLOCK.getOptional(ResourceLocation.tryParse(oreProps.idStone()));
                        if (stoneBlockOpt.isPresent() && stoneBlockOpt.get() == replacedBlock) {
                            String oreId = "ores:" + variant.getFormattedId(material.getId());
                            Optional<Block> oreBlockOpt = BuiltInRegistries.BLOCK.getOptional(ResourceLocation.tryParse(oreId));
                            return oreBlockOpt.map(Block::defaultBlockState).orElse(null);
                        }
                    }
                }
            }
        }
        return null;
    }

    @Unique
    private Optional<Materials> ores$findMaterialById(String materialId) {
        for (Materials material : Materials.values()) {
            if (material.getId().equalsIgnoreCase(materialId)) {
                return Optional.of(material);
            }
        }
        return Optional.empty();
    }
}
