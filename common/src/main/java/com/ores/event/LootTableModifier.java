package com.ores.event;

import com.ores.config.ModConfig;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

import java.util.Optional;

public class LootTableModifier {

    public static Item getReplacement(Item original) {
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
