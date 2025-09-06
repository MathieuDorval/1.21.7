package com.ores.mixin;

import com.ores.ORESMod;
import com.ores.worldgen.BiomeContext;
import com.ores.worldgen.OreGenerationManager;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

// On cible PlainBuilder car c'est la classe parente qui contient la méthode build().
// Cela garantit que nous interceptons la construction de TOUS les biomes, quelle que soit la dimension.
@Mixin(BiomeGenerationSettings.PlainBuilder.class)
public class BiomeGenerationMixin {

    @Inject(method = "build", at = @At("HEAD"))
    private void ores$onBuildBiomeFeatures(CallbackInfoReturnable<BiomeGenerationSettings> cir) {
        // On récupère la clé du biome depuis notre contexte
        ResourceKey<Biome> biomeKey = BiomeContext.CURRENT_BIOME_KEY.get();

        // Si la clé est nulle, on ne fait rien (sécurité pour les biomes non chargés via datapacks)
        if (biomeKey == null) {
            return;
        }

        var configuredOres = OreGenerationManager.getConfiguredOres();
        ResourceLocation biomeLocation = biomeKey.location();

        for (OreGenerationManager.ConfiguredOre ore : configuredOres.values()) {
            boolean shouldGenerate = true;

            // 1. Vérifier la liste noire des biomes
            List<ResourceLocation> blacklistedBiomes = ore.blacklistedBiomes();
            if (blacklistedBiomes != null && blacklistedBiomes.contains(biomeLocation)) {
                shouldGenerate = false;
            }

            // 2. Si non banni, vérifier la liste blanche (si elle existe)
            if (shouldGenerate) {
                List<ResourceLocation> whitelistedBiomes = ore.biomes();
                if (whitelistedBiomes != null && !whitelistedBiomes.isEmpty()) {
                    // Si une whitelist existe, le biome DOIT y être.
                    if (!whitelistedBiomes.contains(biomeLocation)) {
                        shouldGenerate = false;
                    }
                }
            }

            if (shouldGenerate) {
                // TODO: Obtenir la PlacedFeature pour ce minerai et l'ajouter au builder.
                // Exemple:
                // Holder<PlacedFeature> featureHolder = OreFeatureManager.getPlacedFeatureHolder(ore.name());
                // if (featureHolder != null) {
                //     ((BiomeGenerationSettings.PlainBuilder)(Object)this).addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, featureHolder);
                // }
                ORESMod.LOGGER.info("ORES Mod: Ajout potentiel du minerai '{}' au biome '{}'", ore.name(), biomeLocation);
            }
        }
    }
}

