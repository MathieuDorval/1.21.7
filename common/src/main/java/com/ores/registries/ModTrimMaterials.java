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

    private static final Set<String> VANILLA_TRIM_MATERIALS = Stream.of(
            "quartz", "iron", "netherite", "redstone", "copper",
            "gold", "emerald", "diamond", "lapis", "amethyst", "resin"
    ).collect(Collectors.toSet());

    public static void bootstrap(BootstrapContext<TrimMaterial> context) {
        for (Materials material : Materials.values()) {
            Materials.ItemProps itemProps = material.getItemProps();

            if (itemProps.trimColor() != null && !VANILLA_TRIM_MATERIALS.contains(material.getId())) {
                MaterialAssetGroup assetGroup = MaterialAssetGroup.create(material.getId());
                ResourceKey<TrimMaterial> materialKey = registryKey(material.getId());
                Style style = Style.EMPTY.withColor(itemProps.trimColor());
                register(context, materialKey, style, assetGroup);
            }
        }
    }

    private static void register(BootstrapContext<TrimMaterial> bootstrapContext, ResourceKey<TrimMaterial> resourceKey, Style style, MaterialAssetGroup materialAssetGroup) {
        Component component = Component.translatable(Util.makeDescriptionId("trim_material", resourceKey.location())).withStyle(style);
        bootstrapContext.register(resourceKey, new TrimMaterial(materialAssetGroup, component));
    }

    private static ResourceKey<TrimMaterial> registryKey(String name) {
        return ResourceKey.create(Registries.TRIM_MATERIAL, ResourceLocation.fromNamespaceAndPath("ores", name));
    }
}
