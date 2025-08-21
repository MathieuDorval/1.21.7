/**
 * ORES MOD | __mathieu
 * Handles the datagen for item tags.
 */
package com.ores.fabric.datagen;

import com.ores.ORESMod;
import com.ores.config.ModConfig;
import com.ores.core.Materials;
import com.ores.core.Variants;
import com.ores.registries.ModItems;
import dev.architectury.registry.registries.RegistrySupplier;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ModItemTagProvider extends FabricTagProvider<Item> {

    // -=-=-=- CONSTRUCTOR -=-=-=-
    public ModItemTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, Registries.ITEM, registriesFuture);
    }

    // -=-=-=- TAG GENERATION -=-=-=-
    @Override
    protected void addTags(HolderLookup.Provider provider) {
        for (Materials material : Materials.values()) {
            TagKey<Item> materialItemsTag = createModTag(material.getId() + "_items");
            TagKey<Item> materialOresTag = createModTag(material.getId() + "_ores");

            for (Variants variant : Variants.values()) {
                findItem(material, variant).ifPresent(item -> {
                    applyCategoryTags(item, material, variant, materialItemsTag, materialOresTag);
                    applyPropertyTags(item, material, variant);
                });
            }
        }
    }

    // -=-=-=- HELPERS -=-=-=-
    private void applyCategoryTags(Item item, Materials material, Variants variant, TagKey<Item> materialItemsTag, TagKey<Item> materialOresTag) {
        Variants.Category category = variant.getCategory();

        if (category == Variants.Category.ORE || category == Variants.Category.FALLING_ORE || category == Variants.Category.INVERTED_FALLING_ORE) {
            builder(materialOresTag).addOptional(item.builtInRegistryHolder().key());
        } else {
            TagKey<Item> variantItemsTag = createModTag(variant.name().toLowerCase() + "_items");
            builder(materialItemsTag).addOptional(item.builtInRegistryHolder().key());
            builder(variantItemsTag).addOptional(item.builtInRegistryHolder().key());
        }
    }

    private void applyPropertyTags(Item item, Materials material, Variants variant) {
        Materials.Tags materialTags = material.getTags();
        Variants.ItemProps variantProps = variant.getItemProps();

        if (materialTags == null || variantProps == null) return;

        // === BEACON MATERIAL ===
        if (materialTags.beacon() && variantProps.beacon()) {
            builder(ItemTags.BEACON_PAYMENT_ITEMS).addOptional(item.builtInRegistryHolder().key());
        }
        // === PIGLIN REPELLENTS ===
        if (materialTags.piglinRepellents() && variantProps.piglinRepellents()) {
            builder(ItemTags.PIGLIN_REPELLENTS).addOptional(item.builtInRegistryHolder().key());
        }
        // === PIGLIN LOVED ===
        if (materialTags.piglinLoved() && variantProps.piglinLoved()) {
            builder(ItemTags.PIGLIN_LOVED).addOptional(item.builtInRegistryHolder().key());
        }
        // === PIGLIN FOOD ===
        if (materialTags.piglinFood() && variantProps.piglinFood()) {
            builder(ItemTags.PIGLIN_FOOD).addOptional(item.builtInRegistryHolder().key());
        }
        // === TRIM MATERIALS ===
        if (materialTags.trimMaterial() && variantProps.trimable()) {
            builder(ItemTags.TRIM_MATERIALS).addOptional(item.builtInRegistryHolder().key());
        }
    }

    private Optional<Item> findItem(Materials material, Variants variant) {
        String formattedId = variant.getFormattedId(material.getId());
        RegistrySupplier<Item> modItemSupplier = ModItems.DYNAMIC_ITEMS.get(formattedId);
        if (modItemSupplier != null) {
            return Optional.of(modItemSupplier.get());
        }
        if (ModConfig.VANILLA_EXCLUSIONS.contains(formattedId)) {
            return BuiltInRegistries.ITEM.getOptional(ResourceLocation.fromNamespaceAndPath("minecraft", formattedId));
        }
        return Optional.empty();
    }

    private TagKey<Item> createModTag(String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(ORESMod.MOD_ID, path));
    }
}
