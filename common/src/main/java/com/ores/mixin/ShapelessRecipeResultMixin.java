package com.ores.mixin;

import com.ores.config.ModConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ShapelessRecipe.class)
public abstract class ShapelessRecipeResultMixin {

    @Shadow @Final @Mutable
    ItemStack result;

    @Inject(
            method = "<init>(Ljava/lang/String;Lnet/minecraft/world/item/crafting/CraftingBookCategory;Lnet/minecraft/world/item/ItemStack;Ljava/util/List;)V",
            at = @At("RETURN")
    )
    private void ores$modifyResult(String group, CraftingBookCategory category, ItemStack initialResult, List<Ingredient> ingredients, CallbackInfo ci) {
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