package com.ores.mixin;

import com.ores.config.ModConfig;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Mixin(Ingredient.class)
public abstract class RecipeIngredientMixin {

    @ModifyVariable(
            method = "<init>(Lnet/minecraft/core/HolderSet;)V",
            at = @At("HEAD"),
            argsOnly = true
    )
    private static HolderSet<Item> ores$replaceIngredientsInConstructor(HolderSet<Item> originalHolderSet) {
        return originalHolderSet.unwrap().map(
                tag -> originalHolderSet,
                list -> {
                    List<Holder<Item>> modifiedList = list.stream().map(holder -> {
                        Optional<ResourceKey<Item>> resourceKeyOpt = holder.unwrapKey();
                        if (resourceKeyOpt.isEmpty()) {
                            return holder;
                        }

                        ResourceLocation originalId = resourceKeyOpt.get().location();

                        if (originalId.getNamespace().equals("ores")) {
                            return holder;
                        }

                        ResourceLocation oresId = ResourceLocation.fromNamespaceAndPath("ores", originalId.getPath());

                        return BuiltInRegistries.ITEM.getOptional(oresId)
                                .filter(item -> ModConfig.isVariantEnabled(oresId.toString()))
                                .map(Item::builtInRegistryHolder)
                                .orElse((Holder.Reference<Item>) holder);

                    }).collect(Collectors.toList());

                    return HolderSet.direct(modifiedList);
                }
        );
    }
}