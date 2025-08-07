package com.ores.registries;

import com.ores.ORESMod;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.DeferredSupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ModCreativeTab {
    public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(ORESMod.MOD_ID, Registries.CREATIVE_MODE_TAB);

    public static final DeferredSupplier<CreativeModeTab> TAB = REGISTRY.register("tab", () -> CreativeTabRegistry.create(
                    Component.translatable("itemGroup.ores.ores_main"),
                    () -> new ItemStack(Items.IRON_BLOCK)
            )
    );
}
