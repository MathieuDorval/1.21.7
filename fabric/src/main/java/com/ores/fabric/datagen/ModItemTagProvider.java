package com.ores.fabric.datagen;

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
            for (Variants variant : Variants.values()) {
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
