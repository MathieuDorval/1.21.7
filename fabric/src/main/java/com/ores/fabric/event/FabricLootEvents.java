package com.ores.fabric.event;

import com.ores.config.ModConfig;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.List;
import java.util.Optional;

/**
 * Handles all loot modification logic for Fabric.
 * This class registers a listener to modify loot tables and contains the logic
 * to replace vanilla item drops with custom ones from this mod.
 */
public class FabricLootEvents {

    /**
     * Registers the loot modification event listener.
     * This should be called once during mod initialization.
     */
    public static void initialize() {
        LootTableEvents.MODIFY_DROPS.register(FabricLootEvents::replaceDropsInLoot);
    }

    /**
     * This method is called whenever a loot table generates drops.
     * It iterates through the drops and replaces them if necessary.
     */
    private static void replaceDropsInLoot(net.minecraft.core.Holder<LootTable> entry, LootContext context, List<ItemStack> drops) {
        for (int i = 0; i < drops.size(); i++) {
            ItemStack stack = drops.get(i);
            Item replacementItem = getReplacement(stack.getItem());
            if (replacementItem != stack.getItem()) {
                drops.set(i, new ItemStack(replacementItem, stack.getCount()));
            }
        }
    }

    /**
     * Determines the replacement for a given item.
     * It checks if there is a corresponding item in the "ores" mod and if that variant is enabled in the config.
     * @param original The original item from the loot drop.
     * @return The replacement item, or the original item if no replacement is found or enabled.
     */
    private static Item getReplacement(Item original) {
        ResourceLocation originalId = BuiltInRegistries.ITEM.getKey(original);
        if (originalId == null) return original;

        ResourceLocation oresId = ResourceLocation.fromNamespaceAndPath("ores", originalId.getPath());

        Optional<Item> candidateOpt = BuiltInRegistries.ITEM.get(oresId).map(Holder.Reference::value);
        if (candidateOpt.isPresent()) {
            Item candidate = candidateOpt.get();

            if (ModConfig.isVariantEnabled(oresId.toString())) {
                return candidate;
            }
        }

        return original;
    }
}
