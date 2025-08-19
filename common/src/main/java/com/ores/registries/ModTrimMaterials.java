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

/**
 * Gère la création et l'enregistrement dynamique des matériaux de trim pour le mod.
 * Se base sur l'enum Materials pour générer les trims qui ne sont pas déjà présents dans Minecraft vanilla.
 */
public class ModTrimMaterials {

    // Un ensemble contenant les noms des matériaux de trim vanilla pour une vérification rapide.
    private static final Set<String> VANILLA_TRIM_MATERIALS = Stream.of(
            "quartz", "iron", "netherite", "redstone", "copper",
            "gold", "emerald", "diamond", "lapis", "amethyst", "resin"
    ).collect(Collectors.toSet());

    /**
     * Méthode principale appelée au démarrage pour enregistrer les trims.
     * Elle parcourt l'enum Materials et crée dynamiquement un TrimMaterial pour chaque
     * matériau non-vanilla qui possède une couleur de trim.
     * @param context Le contexte de bootstrap fourni par Minecraft.
     */
    public static void bootstrap(BootstrapContext<TrimMaterial> context) {
        for (Materials material : Materials.values()) {
            Materials.ItemProps itemProps = material.getItemProps();

            // On vérifie si le matériau a une couleur de trim et s'il n'est pas un matériau vanilla
            if (itemProps.trimColor() != null && !VANILLA_TRIM_MATERIALS.contains(material.getId())) {

                // Création dynamique du groupe d'assets en utilisant l'ID du matériau comme suffixe.
                MaterialAssetGroup assetGroup = MaterialAssetGroup.create(material.getId());

                // Création de la clé de ressource pour notre nouveau trim.
                ResourceKey<TrimMaterial> materialKey = registryKey(material.getId());

                // Création du style avec la couleur définie.
                Style style = Style.EMPTY.withColor(itemProps.trimColor());

                // Enregistrement du nouveau matériau de trim.
                register(context, materialKey, style, assetGroup);
            }
        }
    }

    /**
     * Méthode utilitaire pour enregistrer un nouveau TrimMaterial.
     */
    private static void register(BootstrapContext<TrimMaterial> bootstrapContext, ResourceKey<TrimMaterial> resourceKey, Style style, MaterialAssetGroup materialAssetGroup) {
        Component component = Component.translatable(Util.makeDescriptionId("trim_material", resourceKey.location())).withStyle(style);
        bootstrapContext.register(resourceKey, new TrimMaterial(materialAssetGroup, component));
    }

    /**
     * Méthode utilitaire pour créer une ResourceKey dans le namespace du mod.
     */
    private static ResourceKey<TrimMaterial> registryKey(String name) {
        return ResourceKey.create(Registries.TRIM_MATERIAL, ResourceLocation.fromNamespaceAndPath("ores", name));
    }
}
