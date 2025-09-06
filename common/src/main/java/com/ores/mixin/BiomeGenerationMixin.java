package com.ores.mixin;

import com.ores.ORESMod;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// On cible PlainBuilder car c'est la classe parente qui contient la méthode build().
// Cela garantit que nous interceptons la construction de TOUS les biomes, quelle que soit la dimension.
@Mixin(BiomeGenerationSettings.PlainBuilder.class)
public class BiomeGenerationMixin {

    @Inject(method = "build", at = @At("HEAD"))
    private void ores$onBuildBiomeFeatures(CallbackInfoReturnable<BiomeGenerationSettings> cir) {
        // C'est le point d'injection idéal pour ajouter vos "features" personnalisées
        // aux paramètres de génération de n'importe quel biome, quelle que soit la dimension.
        // Lorsque cette méthode est appelée, la plupart des features de vanilla et des autres mods ont déjà été ajoutées.

        // PROBLÈME : À ce stade, nous ne savons pas quel biome est en cours de construction.
        // La solution consiste généralement à utiliser un autre Mixin (par exemple, sur RegistryDataLoader ou BuiltinRegistries)
        // pour capturer la ResourceKey du biome en cours de chargement et la stocker dans une variable ThreadLocal.
        // Nous pourrions alors accéder à ce ThreadLocal ici pour obtenir le contexte.

        // PROCHAINES ÉTAPES :
        // 1. Créer un système pour enregistrer dynamiquement une PlacedFeature pour chaque configuration de minerai.
        // 2. Implémenter l'approche ThreadLocal pour obtenir la clé du biome actuel.
        // 3. Ici, récupérer la clé du biome et parcourir vos minerais configurés.
        // 4. Pour chaque minerai, vérifier s'il doit être généré dans ce biome (dimension, listes de biomes, etc.).
        // 5. Si c'est le cas, récupérer le Holder de sa PlacedFeature pré-enregistrée et l'ajouter à ce builder, comme ceci :
        //    ((BiomeGenerationSettings.PlainBuilder)(Object)this).addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, votrePlacedFeatureHolder);

        ORESMod.LOGGER.info("ORES Mixin: Interception de la construction des features de biome. Prêt à ajouter des minerais personnalisés.");
    }
}

