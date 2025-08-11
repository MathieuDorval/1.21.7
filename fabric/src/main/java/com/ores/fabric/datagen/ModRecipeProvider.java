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
                    Optional<Item> baseItemOutputOpt = findItemFromIdBase(material.getIdBase());
                    if (baseItemOutputOpt.isEmpty()) {
                        continue;
                    }
                    Item baseItemOutput = baseItemOutputOpt.get();

                    for (Variants variant : Variants.values()) {
                        Variants.Category category = variant.getCategory();
                        if (category != Variants.Category.ORE &&
                                category != Variants.Category.FALLING_ORE &&
                                category != Variants.Category.INVERTED_FALLING_ORE)
                        {
                            continue;
                        }

                        String itemId = variant.getFormattedId(material.getId());
                        RegistrySupplier<Item> itemSupplier = ModItems.DYNAMIC_ITEMS.get(itemId);

                        if (itemSupplier == null) {
                            continue;
                        }

                        var unlockCriterion = inventoryTrigger(ItemPredicate.Builder.item().of(itemHolderGetter, itemSupplier.get()).build());

                        String smeltingPath = getItemName(baseItemOutput) + "_from_smelting_" + itemId;
                        String blastingPath = getItemName(baseItemOutput) + "_from_blasting_" + itemId;

                        SimpleCookingRecipeBuilder.smelting(Ingredient.of(itemSupplier.get()), RecipeCategory.MISC, baseItemOutput, 0.7f, 200)
                                .unlockedBy(getHasName(itemSupplier.get()), unlockCriterion)
                                .save(this.output, String.valueOf(ResourceLocation.fromNamespaceAndPath(ORESMod.MOD_ID, smeltingPath)));

                        SimpleCookingRecipeBuilder.blasting(Ingredient.of(itemSupplier.get()), RecipeCategory.MISC, baseItemOutput, 0.7f, 100)
                                .unlockedBy(getHasName(itemSupplier.get()), unlockCriterion)
                                .save(this.output, String.valueOf(ResourceLocation.fromNamespaceAndPath(ORESMod.MOD_ID, blastingPath)));
                    }
                }
            }
        };
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
