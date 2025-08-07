package com.ores.registries;

import com.ores.ORESMod;
import dev.architectury.registry.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;

public class ModItems {
    public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ORESMod.MOD_ID, Registries.ITEM);

    public static final Item.Properties DEFAULT_PROPS = new Item.Properties().arch$tab(ModCreativeTab.TAB);

}
