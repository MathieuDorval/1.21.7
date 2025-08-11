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
            // Création dynamique du tag pour le matériau (ex: ores:iron_items)
            TagKey<Item> materialItemsTag = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(ORESMod.MOD_ID, material.getId() + "_items"));
            // Création dynamique du tag pour les minerais du matériau (ex: ores:iron_ores)
            TagKey<Item> materialOresTag = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(ORESMod.MOD_ID, material.getId() + "_ores"));

            for (Variants variant : Variants.values()) {
                // On détermine la potentielle ResourceLocation de l'item
                String formattedId = variant.getFormattedId(material.getId());
                ResourceLocation itemLocation = getItemLocation(material, formattedId);

                // Si l'item n'a pas de location (n'est pas un item), on passe
                if (itemLocation == null) continue;

                // --- LOGIQUE DE TAGS PERSONNALISÉS (avec addOptional) ---
                Variants.Category category = variant.getCategory();
                if (category == Variants.Category.ORE || category == Variants.Category.FALLING_ORE || category == Variants.Category.INVERTED_FALLING_ORE) {
                    // CORRECTION: On crée une ResourceKey<Item> avant de l'ajouter
                    builder(materialOresTag).addOptional(ResourceKey.create(Registries.ITEM, itemLocation));
                } else {
                    // Crée le tag pour la variante
                    TagKey<Item> variantItemsTag = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(ORESMod.MOD_ID, variant.name().toLowerCase() + "_items"));
                    // CORRECTION: On crée une ResourceKey<Item> avant de l'ajouter
                    builder(materialItemsTag).addOptional(ResourceKey.create(Registries.ITEM, itemLocation));
                    builder(variantItemsTag).addOptional(ResourceKey.create(Registries.ITEM, itemLocation));
                }

                // --- LOGIQUE DE TAGS VANILLA (uniquement si l'item existe) ---
                findItemForRecipe(material, variant).ifPresent(item -> {
                    Materials.Tags materialTags = material.getTags();
                    Variants.ItemProps variantProps = variant.getItemProps();

                    if (materialTags != null && variantProps != null) {
                        if (materialTags.beacon() && variantProps.beacon()) {
                            builder(ItemTags.BEACON_PAYMENT_ITEMS).add(item.builtInRegistryHolder().key());
                        }
                    }
                });
            }
        }
    }

    /**
     * Détermine la ResourceLocation d'un item potentiel sans vérifier son existence.
     * @return La ResourceLocation, ou null si la variante ne correspond pas à un item.
     */
    private static ResourceLocation getItemLocation(Materials material, String formattedId) {
        // Vérifie si c'est une exclusion vanilla connue
        Materials.VanillaExclusions vanillaExclusions = material.getVanillaExclusions();
        if (vanillaExclusions != null) {
            List<String> exclusions = vanillaExclusions.excludedVariantIds();
            if (exclusions != null && exclusions.contains(formattedId)) {
                return ResourceLocation.fromNamespaceAndPath("minecraft", formattedId);
            }
        }

        // Sinon, c'est un item du mod
        return ResourceLocation.fromNamespaceAndPath(ORESMod.MOD_ID, formattedId);
    }

    /**
     * Trouve un item pour une recette, qu'il soit moddé ou vanilla (exclu).
     */
    private static Optional<Item> findItemForRecipe(Materials material, Variants variant) {
        String formattedId = variant.getFormattedId(material.getId());

        // 1. Cherche dans les items dynamiques du mod (inclut les BlockItems)
        RegistrySupplier<Item> modItemSupplier = ModItems.DYNAMIC_ITEMS.get(formattedId);
        if (modItemSupplier != null) {
            return Optional.of(modItemSupplier.get());
        }

        // 2. Si non trouvé, vérifie si c'est un item vanilla exclu
        Materials.VanillaExclusions vanillaExclusions = material.getVanillaExclusions();
        if (vanillaExclusions != null) {
            List<String> exclusions = vanillaExclusions.excludedVariantIds();
            if (exclusions != null && exclusions.contains(formattedId)) {
                // C'est un item vanilla, on le cherche dans le registre de Minecraft
                return BuiltInRegistries.ITEM.getOptional(ResourceLocation.fromNamespaceAndPath("minecraft", formattedId));
            }
        }

        return Optional.empty();
    }
}
