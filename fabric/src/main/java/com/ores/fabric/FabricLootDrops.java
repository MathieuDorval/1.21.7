package com.ores.fabric;

import com.ores.event.LootTableModifier;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.List;

public class FabricLootDrops {

    public static void register() {
        LootTableEvents.MODIFY_DROPS.register(FabricLootDrops::replaceDrops);
    }

    private static void replaceDrops(net.minecraft.core.Holder<LootTable> entry, LootContext context, List<ItemStack> drops) {
        for (int i = 0; i < drops.size(); i++) {
            ItemStack stack = drops.get(i);
            Item replacementItem = LootTableModifier.getReplacement(stack.getItem());
            if (replacementItem != stack.getItem()) {
                drops.set(i, new ItemStack(replacementItem, stack.getCount()));
            }
        }
    }
}
