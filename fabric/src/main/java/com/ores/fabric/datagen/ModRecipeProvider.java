package com.ores.fabric.datagen;

import com.ores.ORESMod;
import com.ores.core.Materials;
import com.ores.core.Variants;
import com.ores.registries.ModItems;
import dev.architectury.registry.registries.RegistrySupplier;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.data.recipes.RecipeProvider.*;

public class ModRecipeProvider extends RecipeProvider.Runner {

    public ModRecipeProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    public @NotNull String getName() {
        return "ORES Mod Recipes";
    }

    @Override
    protected @NotNull RecipeProvider createRecipeProvider(HolderLookup.Provider registryLookup, RecipeOutput exporter) {
        HolderGetter<Item> itemHolderGetter = registryLookup.lookupOrThrow(Registries.ITEM);

        return new RecipeProvider(registryLookup, exporter) {
            @Override
            public void buildRecipes() {
                for (Materials material : Materials.values()) {
                    // --- LOGIQUE DE COMPRESSION/DÉCOMPRESSION ---
                    // (Code inchangé)
                    findItemFromIdBase(material.getIdBase()).ifPresent(baseItem -> {
                        findItemForRecipe(material, Variants.NUGGET).ifPresent(nuggetItem -> {
                            addStorageRecipes(this.output, RecipeCategory.MISC, nuggetItem, baseItem, itemHolderGetter);
                        });
                    });
                    findItemForRecipe(material, Variants.RAW).ifPresent(rawItem -> {
                        findItemForRecipe(material, Variants.RAW_BLOCK).ifPresent(rawBlockItem -> {
                            addStorageRecipes(this.output, RecipeCategory.BUILDING_BLOCKS, rawItem, rawBlockItem, itemHolderGetter);
                        });
                    });
                    findItemFromIdBase(material.getIdBase()).ifPresent(baseItem -> {
                        findItemForRecipe(material, Variants.BLOCK).ifPresent(blockItem -> {
                            addStorageRecipes(this.output, RecipeCategory.BUILDING_BLOCKS, baseItem, blockItem, itemHolderGetter);
                        });
                    });

                    // --- LOGIQUE DE CUISSON ---

                    // MODIFICATION : Utilisation du tag pour les minerais
                    TagKey<Item> oreTag = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(ORESMod.MOD_ID, material.getId() + "_ores"));
                    findItemFromIdBase(material.getIdBase()).ifPresent(outputItem -> {
                        // CORRECTION : Utilisation de la méthode 'has' pour créer le critère de déverrouillage
                        var unlockCriterion = this.has(oreTag);
                        // CORRECTION : Création de l'ingrédient via un HolderSet pour éviter les problèmes de surcharge de méthode
                        HolderSet.Named<Item> oreTagHolderSet = itemHolderGetter.getOrThrow(oreTag);
                        Ingredient oreIngredient = Ingredient.of(oreTagHolderSet);
                        addCookingRecipesByTag(this.output, material.getId() + "_ores", oreIngredient, outputItem, 200, 100, unlockCriterion);
                    });


                    // Logique de cuisson pour les items spécifiques (RAW, RAW_BLOCK)
                    for (Variants variant : List.of(Variants.RAW, Variants.RAW_BLOCK)) {
                        findItemForRecipe(material, variant).ifPresent(inputItem -> {
                            var unlockCriterion = inventoryTrigger(ItemPredicate.Builder.item().of(itemHolderGetter, inputItem).build());

                            if (variant == Variants.RAW) {
                                findItemFromIdBase(material.getIdBase()).ifPresent(outputItem -> {
                                    addCookingRecipes(this.output, variant.getFormattedId(material.getId()), inputItem, outputItem, 200, 100, unlockCriterion);
                                });
                            } else if (variant == Variants.RAW_BLOCK) {
                                findItemForRecipe(material, Variants.BLOCK).ifPresent(outputItem -> {
                                    addCookingRecipes(this.output, variant.getFormattedId(material.getId()), inputItem, outputItem, 1800, 900, unlockCriterion);
                                });
                            }
                        });
                    }
                }
            }
        };
    }


    private void addStorageRecipes(RecipeOutput exporter, RecipeCategory category, Item smallItem, Item largeItem, HolderGetter<Item> itemHolderGetter) {
        ShapelessRecipeBuilder.shapeless(itemHolderGetter, category, smallItem, 9)
                .requires(largeItem)
                .unlockedBy(getHasName(largeItem), inventoryTrigger(ItemPredicate.Builder.item().of(itemHolderGetter, largeItem).build()))
                .save(exporter, ORESMod.MOD_ID + ":" + getItemName(smallItem) + "_from_" + getItemName(largeItem));

        ShapedRecipeBuilder.shaped(itemHolderGetter, category, largeItem)
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#', smallItem)
                .unlockedBy(getHasName(smallItem), inventoryTrigger(ItemPredicate.Builder.item().of(itemHolderGetter, smallItem).build()))
                .save(exporter, ORESMod.MOD_ID + ":" + getItemName(largeItem) + "_from_" + getItemName(smallItem));
    }

    // Recettes pour des items uniques
    private void addCookingRecipes(RecipeOutput exporter, String inputId, Item input, Item output, int smeltingTime, int blastingTime, net.minecraft.advancements.Criterion<?> unlockCriterion) {
        String smeltingPath = getItemName(output) + "_from_smelting_" + inputId;
        String blastingPath = getItemName(output) + "_from_blasting_" + inputId;

        SimpleCookingRecipeBuilder.smelting(Ingredient.of(input), RecipeCategory.MISC, output, 0.7f, smeltingTime)
                .unlockedBy(getHasName(input), unlockCriterion)
                .save(exporter, ORESMod.MOD_ID + ":" + smeltingPath);

        SimpleCookingRecipeBuilder.blasting(Ingredient.of(input), RecipeCategory.MISC, output, 0.7f, blastingTime)
                .unlockedBy(getHasName(input), unlockCriterion)
                .save(exporter, ORESMod.MOD_ID + ":" + blastingPath);
    }

    // Nouvelle méthode pour les recettes basées sur des tags
    private void addCookingRecipesByTag(RecipeOutput exporter, String inputId, Ingredient input, Item output, int smeltingTime, int blastingTime, net.minecraft.advancements.Criterion<?> unlockCriterion) {
        String smeltingPath = getItemName(output) + "_from_smelting_" + inputId;
        String blastingPath = getItemName(output) + "_from_blasting_" + inputId;

        SimpleCookingRecipeBuilder.smelting(input, RecipeCategory.MISC, output, 0.7f, smeltingTime)
                .unlockedBy("has_" + inputId, unlockCriterion)
                .save(exporter, ORESMod.MOD_ID + ":" + smeltingPath);

        SimpleCookingRecipeBuilder.blasting(input, RecipeCategory.MISC, output, 0.7f, blastingTime)
                .unlockedBy("has_" + inputId, unlockCriterion)
                .save(exporter, ORESMod.MOD_ID + ":" + blastingPath);
    }


    private static Optional<Item> findItemFromIdBase(String idBase) {
        ResourceLocation resourceLocation = ResourceLocation.tryParse(idBase);
        if (resourceLocation == null) {
            return Optional.empty();
        }

        if (resourceLocation.getNamespace().equals(ORESMod.MOD_ID)) {
            RegistrySupplier<Item> modItem = ModItems.DYNAMIC_ITEMS.get(resourceLocation.getPath());
            if (modItem != null) {
                return Optional.of(modItem.get());
            }
        }

        return BuiltInRegistries.ITEM.getOptional(resourceLocation);
    }

    private static Optional<Item> findItemForRecipe(Materials material, Variants variant) {
        String formattedId = variant.getFormattedId(material.getId());

        RegistrySupplier<Item> modItemSupplier = ModItems.DYNAMIC_ITEMS.get(formattedId);
        if (modItemSupplier != null) {
            return Optional.of(modItemSupplier.get());
        }

        Materials.VanillaExclusions vanillaExclusions = material.getVanillaExclusions();
        if (vanillaExclusions != null) {
            List<String> exclusions = vanillaExclusions.excludedVariantIds();
            if (exclusions != null && exclusions.contains(formattedId)) {
                return BuiltInRegistries.ITEM.getOptional(ResourceLocation.fromNamespaceAndPath("minecraft", formattedId));
            }
        }

        return Optional.empty();
    }
}
