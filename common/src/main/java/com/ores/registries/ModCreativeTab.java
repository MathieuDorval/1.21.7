package com.ores.registries;

import com.ores.ORESMod;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ModCreativeTab {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(ORESMod.MOD_ID, Registries.CREATIVE_MODE_TAB);

    public static RegistrySupplier<CreativeModeTab> ITEMS_TAB;
    public static RegistrySupplier<CreativeModeTab> BLOCKS_TAB;
    public static RegistrySupplier<CreativeModeTab> ORES_TAB;

    public static void initTabs(){
        ITEMS_TAB = TABS.register("items_tab", () -> CreativeTabRegistry.create(Component.translatable("ores.items_tab"), () -> new ItemStack(Items.PAPER)));
        BLOCKS_TAB = TABS.register("blocks_tab", () -> CreativeTabRegistry.create(Component.translatable("ores.blocks_tab"), () -> new ItemStack(Items.SUGAR)));
        ORES_TAB = TABS.register("ores_tab", () -> CreativeTabRegistry.create(Component.translatable("ores.ores_tab"), () -> new ItemStack(Items.SUGAR_CANE)));

        TABS.register();
    }
}
