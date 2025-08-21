/**
 * ORES MOD | __mathieu
 * Handles the datagen registration of custom armor trim materials.
 */
package com.ores.registries;

import com.ores.core.Materials;
import net.minecraft.Util;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.MaterialAssetGroup;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ModTrimMaterials {

    // -=-=-=- CONSTANTS -=-=-=-
    private static final Set<String> VANILLA_TRIM_MATERIALS = Stream.of(
            "quartz", "iron", "netherite", "redstone", "copper",
            "gold", "emerald", "diamond", "lapis", "amethyst", "resin"
    ).collect(Collectors.toSet());

    // -=-=-=- DATAGEN -=-=-=-
    public static void bootstrap(BootstrapContext<TrimMaterial> context) {
        for (Materials material : Materials.values()) {
            Materials.ItemProps itemProps = material.getItemProps();

            if (itemProps != null && itemProps.trimColor() != null && !VANILLA_TRIM_MATERIALS.contains(material.getId())) {
                ResourceKey<TrimMaterial> materialKey = createRegistryKey(material.getId());
                Style style = Style.EMPTY.withColor(itemProps.trimColor());
                MaterialAssetGroup assetGroup = MaterialAssetGroup.create(material.getId());
                register(context, materialKey, style, assetGroup);
            }
        }
    }

    // -=-=-=- HELPERS -=-=-=-
    private static void register(BootstrapContext<TrimMaterial> context, ResourceKey<TrimMaterial> key, Style style, MaterialAssetGroup assetGroup) {
        Component description = Component.translatable(Util.makeDescriptionId("trim_material", key.location())).withStyle(style);
        context.register(key, new TrimMaterial(assetGroup, description));
    }

    private static ResourceKey<TrimMaterial> createRegistryKey(String name) {
        return ResourceKey.create(Registries.TRIM_MATERIAL, ResourceLocation.fromNamespaceAndPath("ores", name));
    }
}
