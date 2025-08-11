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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.data.recipes.RecipeProvider.getHasName;
import static net.minecraft.data.recipes.RecipeProvider.getItemName;

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
                    for (Variants variant : Variants.values()) {
                        String inputId = variant.getFormattedId(material.getId());
                        RegistrySupplier<Item> inputSupplier = ModItems.DYNAMIC_ITEMS.get(inputId);

                        if (inputSupplier == null) {
                            continue;
                        }

                        var unlockCriterion = inventoryTrigger(ItemPredicate.Builder.item().of(itemHolderGetter, inputSupplier.get()).build());

                        // --- LOGIQUE DE CUISSON REFACTORISÉE ---
                        switch (variant.getCategory()) {
                            case ORE, FALLING_ORE, INVERTED_FALLING_ORE -> {
                                findItemFromIdBase(material.getIdBase()).ifPresent(outputItem -> {
                                    addCookingRecipes(this.output, inputId, inputSupplier.get(), outputItem, 200, 100, unlockCriterion);
                                });
                            }
                            case ITEM -> {
                                if (variant == Variants.RAW) {
                                    findItemFromIdBase(material.getIdBase()).ifPresent(outputItem -> {
                                        addCookingRecipes(this.output, inputId, inputSupplier.get(), outputItem, 200, 100, unlockCriterion);
                                    });
                                }
                            }
                            case BLOCK -> {
                                if (variant == Variants.RAW_BLOCK) {
                                    String outputId = Variants.BLOCK.getFormattedId(material.getId());
                                    RegistrySupplier<Item> outputSupplier = ModItems.DYNAMIC_ITEMS.get(outputId);
                                    if (outputSupplier != null) {
                                        addCookingRecipes(this.output, inputId, inputSupplier.get(), outputSupplier.get(), 1800, 900, unlockCriterion);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        };
    }

    /**
     * Ajoute les recettes de cuisson (fourneau et haut-fourneau) pour un item donné.
     */
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
}
