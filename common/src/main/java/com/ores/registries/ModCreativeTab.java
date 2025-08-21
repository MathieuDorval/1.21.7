/**
 * ORES MOD | __mathieu
 * Handles the registration of creative mode tabs for the mod.
 */
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

    // -=-=-=- REGISTRY -=-=-=-
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(ORESMod.MOD_ID, Registries.CREATIVE_MODE_TAB);

    // -=-=-=- TABS -=-=-=-
    public static RegistrySupplier<CreativeModeTab> ITEMS_TAB;
    public static RegistrySupplier<CreativeModeTab> BLOCKS_TAB;
    public static RegistrySupplier<CreativeModeTab> ORES_TAB;

    // -=-=-=- INITIALIZATION -=-=-=-
    public static void initialize() {
        ORESMod.LOGGER.info("Registering creative mode tabs...");

        ITEMS_TAB = TABS.register("items_tab", () -> CreativeTabRegistry.create(
                Component.translatable("itemGroup.ores.items_tab"),
                findFirstIcon(new ItemStack(Items.IRON_INGOT), Variants.Category.ITEM)
        ));

        BLOCKS_TAB = TABS.register("blocks_tab", () -> CreativeTabRegistry.create(
                Component.translatable("itemGroup.ores.blocks_tab"),
                findFirstIcon(new ItemStack(Items.IRON_BLOCK), Variants.Category.BLOCK, Variants.Category.FALLING_BLOCK, Variants.Category.INVERTED_FALLING_BLOCK)
        ));

        ORES_TAB = TABS.register("ores_tab", () -> CreativeTabRegistry.create(
                Component.translatable("itemGroup.ores.ores_tab"),
                findFirstIcon(new ItemStack(Items.IRON_ORE), Variants.Category.ORE, Variants.Category.FALLING_ORE, Variants.Category.INVERTED_FALLING_ORE)
        ));

        TABS.register();
        ORESMod.LOGGER.info("Creative mode tabs registered successfully.");
    }

    // -=-=-=- HELPERS -=-=-=-
    private static Supplier<ItemStack> findFirstIcon(ItemStack fallbackStack, Variants.Category... categories) {
        return () -> {
            List<Variants.Category> categoryList = Arrays.asList(categories);
            for (Materials material : Materials.values()) {
                for (Variants variant : Variants.values()) {
                    if (categoryList.contains(variant.getCategory())) {
                        String itemId = variant.getFormattedId(material.getId());
                        RegistrySupplier<net.minecraft.world.item.Item> itemSupplier = ModItems.DYNAMIC_ITEMS.get(itemId);
                        if (itemSupplier != null) {
                            return new ItemStack(itemSupplier.get());
                        }
                    }
                }
            }
            return fallbackStack;
        };
    }
}
