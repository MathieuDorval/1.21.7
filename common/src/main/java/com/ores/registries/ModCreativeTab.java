package com.ores.registries;

import com.ores.ORESMod;
import com.ores.core.Materials;
import com.ores.core.Variants;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class ModCreativeTab {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(ORESMod.MOD_ID, Registries.CREATIVE_MODE_TAB);

    public static RegistrySupplier<CreativeModeTab> ITEMS_TAB;
    public static RegistrySupplier<CreativeModeTab> BLOCKS_TAB;
    public static RegistrySupplier<CreativeModeTab> ORES_TAB;

    public static void initTabs(){
        ITEMS_TAB = TABS.register("items_tab", () -> CreativeTabRegistry.create(Component.translatable("ores.items_tab"),
                findFirstIcon(new ItemStack(Items.IRON_INGOT), Variants.Category.ITEM)));

        BLOCKS_TAB = TABS.register("blocks_tab", () -> CreativeTabRegistry.create(Component.translatable("ores.blocks_tab"),
                findFirstIcon(new ItemStack(Items.IRON_BLOCK), Variants.Category.BLOCK, Variants.Category.FALLING_BLOCK, Variants.Category.INVERTED_FALLING_BLOCK)));

        ORES_TAB = TABS.register("ores_tab", () -> CreativeTabRegistry.create(Component.translatable("ores.ores_tab"),
                findFirstIcon(new ItemStack(Items.IRON_ORE), Variants.Category.ORE, Variants.Category.FALLING_ORE, Variants.Category.INVERTED_FALLING_ORE)));

        TABS.register();
    }

    private static Supplier<ItemStack> findFirstIcon(ItemStack fallbackStack, Variants.Category... categories) {
        return () -> {
            List<Variants.Category> categoryList = Arrays.asList(categories);
            for (Materials material : Materials.values()) {
                for (Variants variant : Variants.values()) {
                    if (categoryList.contains(variant.getCategory())) {
                        String itemId = variant.getFormattedId(material.getId());
                        if (ModItems.DYNAMIC_ITEMS.containsKey(itemId)) {
                            return new ItemStack(ModItems.DYNAMIC_ITEMS.get(itemId).get());
                        }
                    }
                }
            }
            return fallbackStack;
        };
    }
}
