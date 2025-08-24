package com.ores.mixin;

import com.ores.config.ModConfig;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;
import java.util.function.Consumer;

@Mixin(LootTable.class)
public class LootTableMixin {

    @Redirect(method = "getRandomItemsRaw(Lnet/minecraft/world/level/storage/loot/LootContext;Ljava/util/function/Consumer;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/loot/LootPool;addRandomItems(Ljava/util/function/Consumer;Lnet/minecraft/world/level/storage/loot/LootContext;)V"))
    private void modifyDrops(LootPool instance, Consumer<ItemStack> consumer, LootContext lootContext) {
        Consumer<ItemStack> newConsumer = (itemStack) -> {
            Item replacement = ores$getReplacement(itemStack.getItem());
            if (replacement != itemStack.getItem()) {
                consumer.accept(new ItemStack(replacement, itemStack.getCount()));
            } else {
                consumer.accept(itemStack);
            }
        };
        instance.addRandomItems(newConsumer, lootContext);
    }
    @Unique
    private Item ores$getReplacement(Item original) {
        ResourceLocation originalId = BuiltInRegistries.ITEM.getKey(original);
        if (originalId == null) return original;

        if (originalId.getNamespace().equals("ores")) {
            return original;
        }
        ResourceLocation oresId = ResourceLocation.fromNamespaceAndPath("ores", originalId.getPath());
        Optional<Holder.Reference<Item>> holderOpt = BuiltInRegistries.ITEM.get(oresId);
        if (holderOpt.isPresent()) {
            Item candidate = holderOpt.get().value();
            if (ModConfig.isVariantEnabled(oresId.toString())) {
                return candidate;
            }
        }
        return original;
    }
}
