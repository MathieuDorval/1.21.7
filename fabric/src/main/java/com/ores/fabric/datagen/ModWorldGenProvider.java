/**
 * ORES MOD | __mathieu
 * Handles the datagen for dynamic registries like trim materials.
 */
package com.ores.fabric.datagen;

import com.mojang.serialization.Lifecycle;
import com.ores.registries.ModTrimMaterials;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ModWorldGenProvider extends FabricDynamicRegistryProvider {

    // -=-=-=- CONSTRUCTOR -=-=-=-
    public ModWorldGenProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    // -=-=-=- CONFIGURATION -=-=-=-
    @Override
    protected void configure(HolderLookup.Provider registries, Entries entries) {
        BootstrapContext<TrimMaterial> context = new BootstrapContext<>() {
            @Override
            public Holder.@NotNull Reference<TrimMaterial> register(ResourceKey<TrimMaterial> key, TrimMaterial value, Lifecycle lifecycle) {
                return (Holder.Reference<TrimMaterial>) entries.add(key, value);
            }

            @Override
            public <S> @NotNull HolderGetter<S> lookup(ResourceKey<? extends Registry<? extends S>> registryKey) {
                return registries.lookupOrThrow(registryKey);
            }
        };

        ModTrimMaterials.bootstrap(context);
    }

    @Override
    public @NotNull String getName() {
        return "World Gen";
    }
}
