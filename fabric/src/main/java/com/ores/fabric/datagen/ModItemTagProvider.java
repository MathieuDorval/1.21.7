/**
 * ORES MOD | __mathieu
 * ITEMS TAGS DATAGEN
 */
package com.ores.fabric.datagen;

import com.ores.ORESMod;
import com.ores.core.Materials;
import com.ores.core.Variants;
import com.ores.registries.ModItems;
import dev.architectury.registry.registries.RegistrySupplier;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ModItemTagProvider extends FabricTagProvider<Item> {
    public ModItemTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, Registries.ITEM, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        for (Materials material : Materials.values()) {
            TagKey<Item> materialItemsTag = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(ORESMod.MOD_ID, material.getId() + "_items"));
            TagKey<Item> materialOresTag = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(ORESMod.MOD_ID, material.getId() + "_ores"));

            for (Variants variant : Variants.values()) {
                String formattedId = variant.getFormattedId(material.getId());
                ResourceLocation itemLocation = getItemLocation(material, formattedId);
                Variants.Category category = variant.getCategory();
                if (category == Variants.Category.ORE || category == Variants.Category.FALLING_ORE || category == Variants.Category.INVERTED_FALLING_ORE) {
                    builder(materialOresTag).addOptional(ResourceKey.create(Registries.ITEM, itemLocation));
                } else {
                    TagKey<Item> variantItemsTag = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(ORESMod.MOD_ID, variant.name().toLowerCase() + "_items"));
                    builder(materialItemsTag).addOptional(ResourceKey.create(Registries.ITEM, itemLocation));
                    builder(variantItemsTag).addOptional(ResourceKey.create(Registries.ITEM, itemLocation));
                }
                findItemForRecipe(material, variant).ifPresent(item -> {
                    Materials.Tags materialTags = material.getTags();
                    Variants.ItemProps variantProps = variant.getItemProps();

                    if (materialTags != null && variantProps != null) {
                        // -=-=-=- BEACON MATERIAL -=-=-=-
                        if (materialTags.beacon() && variantProps.beacon()) {
                            builder(ItemTags.BEACON_PAYMENT_ITEMS).add(item.builtInRegistryHolder().key());
                        }
                        // -=-=-=- PIGLIN REPELLENTS -=-=-=-
                        if (materialTags.piglinRepellents() && variantProps.piglinRepellents()) {
                            builder(ItemTags.PIGLIN_REPELLENTS).add(item.builtInRegistryHolder().key());
                        }
                        // -=-=-=- PIGLIN LOVED -=-=-=-
                        if (materialTags.piglinLoved() && variantProps.piglinLoved()) {
                            builder(ItemTags.PIGLIN_LOVED).add(item.builtInRegistryHolder().key());
                        }
                        // -=-=-=- PIGLIN FOOD -=-=-=-
                        if (materialTags.piglinFood() && variantProps.piglinFood()) {
                            builder(ItemTags.PIGLIN_FOOD).add(item.builtInRegistryHolder().key());
                        }
                    }
                });
            }
        }
    }

    private static ResourceLocation getItemLocation(Materials material, String formattedId) {
        Materials.VanillaExclusions vanillaExclusions = material.getVanillaExclusions();
        if (vanillaExclusions != null) {
            List<String> exclusions = vanillaExclusions.excludedVariantIds();
            if (exclusions != null && exclusions.contains(formattedId)) {
                return ResourceLocation.fromNamespaceAndPath("minecraft", formattedId);
            }
        }
        return ResourceLocation.fromNamespaceAndPath(ORESMod.MOD_ID, formattedId);
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
