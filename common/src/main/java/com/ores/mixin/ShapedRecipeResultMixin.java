package com.ores.mixin;

import com.ores.config.ModConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ShapedRecipe.class)
public abstract class ShapedRecipeResultMixin {

    @Shadow @Final @Mutable
    ItemStack result;

    @Inject(
            method = "<init>(Ljava/lang/String;Lnet/minecraft/world/item/crafting/CraftingBookCategory;Lnet/minecraft/world/item/crafting/ShapedRecipePattern;Lnet/minecraft/world/item/ItemStack;Z)V",
            at = @At("RETURN")
    )
    private void ores$modifyResult(String group, CraftingBookCategory category, ShapedRecipePattern pattern, ItemStack initialResult, boolean showNotification, CallbackInfo ci) {
        if (this.result.isEmpty()) return;
        Item originalItem = this.result.getItem();
        ResourceLocation originalId = BuiltInRegistries.ITEM.getKey(originalItem);
        if (originalId.getNamespace().equals("ores")) return;
        ResourceLocation oresId = ResourceLocation.fromNamespaceAndPath("ores", originalId.getPath());
        BuiltInRegistries.ITEM.getOptional(oresId)
                .filter(item -> ModConfig.isVariantEnabled(oresId.toString()))
                .ifPresent(newItem -> this.result = this.result.transmuteCopy(newItem));
    }
}