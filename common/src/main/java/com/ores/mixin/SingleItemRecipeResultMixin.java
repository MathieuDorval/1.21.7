package com.ores.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.ores.config.ModConfig;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SingleItemRecipe;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;


@Mixin(SingleItemRecipe.class)
public abstract class SingleItemRecipeResultMixin {

    @ModifyReturnValue(
            method = "assemble(Lnet/minecraft/world/item/crafting/SingleRecipeInput;Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/world/item/ItemStack;",
            at = @At("RETURN")
    )
    private ItemStack ores$modifyResultOnAssemble(ItemStack original, SingleRecipeInput input, HolderLookup.Provider provider) {
        if (original.isEmpty()) return original;
        Item originalItem = original.getItem();
        ResourceLocation originalId = BuiltInRegistries.ITEM.getKey(originalItem);
        if (originalId.getNamespace().equals("ores")) return original;
        ResourceLocation oresId = ResourceLocation.fromNamespaceAndPath("ores", originalId.getPath());
        return BuiltInRegistries.ITEM.getOptional(oresId)
                .filter(item -> ModConfig.isVariantEnabled(oresId.toString()))
                .map(original::transmuteCopy)
                .orElse(original);
    }
}
