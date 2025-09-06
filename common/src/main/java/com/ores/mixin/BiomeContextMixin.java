package com.ores.mixin;

import com.google.gson.JsonElement;
import com.mojang.serialization.Decoder;
import com.ores.worldgen.BiomeContext;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Cible la classe qui gère le chargement des données depuis les fichiers
@Mixin(RegistryDataLoader.class)
public class BiomeContextMixin {

    // S'injecte au début de la méthode qui charge un élément de registre (comme un biome) depuis sa ressource.
    // C'est le point idéal pour capturer la clé du registre avant son traitement.
    @Inject(
            method = "loadElementFromResource(Lnet/minecraft/core/WritableRegistry;Lcom/mojang/serialization/Decoder;Lnet/minecraft/resources/RegistryOps;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/server/packs/resources/Resource;Lnet/minecraft/core/RegistrationInfo;)V",
            at = @At("HEAD")
    )
    private static <E> void ores$onBeforeParse(WritableRegistry<E> writableRegistry, Decoder<E> decoder, RegistryOps<JsonElement> registryOps, ResourceKey<E> resourceKey, Resource resource, RegistrationInfo registrationInfo, CallbackInfo ci) {
        // On vérifie qu'on traite bien un biome
        if (writableRegistry.key().equals(Registries.BIOME)) {
            BiomeContext.CURRENT_BIOME_KEY.set((ResourceKey<Biome>) resourceKey);
        }
    }

    // S'injecte à la fin de la même méthode pour nettoyer le contexte.
    @Inject(
            method = "loadElementFromResource(Lnet/minecraft/core/WritableRegistry;Lcom/mojang/serialization/Decoder;Lnet/minecraft/resources/RegistryOps;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/server/packs/resources/Resource;Lnet/minecraft/core/RegistrationInfo;)V",
            at = @At("TAIL")
    )
    private static <E> void ores$onAfterParse(WritableRegistry<E> writableRegistry, Decoder<E> decoder, RegistryOps<JsonElement> registryOps, ResourceKey<E> resourceKey, Resource resource, RegistrationInfo registrationInfo, CallbackInfo ci) {
        // On nettoie la variable ThreadLocal pour éviter les fuites de mémoire et les erreurs de contexte
        if (writableRegistry.key().equals(Registries.BIOME)) {
            BiomeContext.CURRENT_BIOME_KEY.remove();
        }
    }
}

