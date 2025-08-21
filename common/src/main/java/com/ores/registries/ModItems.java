/**
 * ORES MOD | __mathieu
 * Handles the dynamic registration of all mod items and block items.
 */
package com.ores.registries;

import com.ores.ORESMod;
import com.ores.config.ModConfig;
import com.ores.core.Materials;
import com.ores.core.Variants;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ModItems {

    // -=-=-=- REGISTRY -=-=-=-
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ORESMod.MOD_ID, Registries.ITEM);
    public static final Map<String, RegistrySupplier<Item>> DYNAMIC_ITEMS = new HashMap<>();

    // -=-=-=- INITIALIZATION -=-=-=-
    public static void initialize() {
        ORESMod.LOGGER.info("Registering dynamic items...");

        for (Materials material : Materials.values()) {
            for (Variants variant : Variants.values()) {
                if (!ModConfig.isVariantEnabled(material, variant)) continue;

                String itemId = variant.getFormattedId(material.getId());
                registerItemForVariant(itemId, material, variant);
            }
        }

        ITEMS.register();
        ORESMod.LOGGER.info("Dynamic items registered successfully.");
    }

    // -=-=-=- REGISTRATION LOGIC -=-=-=-
    private static void registerItemForVariant(String itemId, Materials material, Variants variant) {
        RegistrySupplier<Block> blockSupplier = ModBlocks.DYNAMIC_BLOCKS.get(itemId);

        switch (variant.getCategory()) {
            case ITEM -> {
                Item.Properties properties = ItemsProps(itemId);
                applyCombinedProperties(properties, material, variant);
                registerAndProcessItem(itemId, () -> new Item(properties), material, variant);
            }
            case BLOCK, FALLING_BLOCK, INVERTED_FALLING_BLOCK -> {
                if (blockSupplier != null) {
                    Item.Properties properties = BlocksProps(itemId);
                    applyCombinedProperties(properties, material, variant);
                    registerAndProcessItem(itemId, () -> new BlockItem(blockSupplier.get(), properties), material, variant);
                }
            }
            case ORE, FALLING_ORE, INVERTED_FALLING_ORE -> {
                if (blockSupplier != null) {
                    Item.Properties properties = OresProps(itemId);
                    registerAndProcessItem(itemId, () -> new BlockItem(blockSupplier.get(), properties), material, variant);
                }
            }
        }
    }

    private static void registerAndProcessItem(String name, Supplier<Item> itemSupplier, Materials material, Variants variant) {
        RegistrySupplier<Item> registeredItem = ITEMS.register(ResourceLocation.fromNamespaceAndPath(ORESMod.MOD_ID, name), itemSupplier);
        DYNAMIC_ITEMS.put(name, registeredItem);
        ModFuels.addFuel(registeredItem, material, variant);
    }

    // -=-=-=- PROPERTY BUILDERS -=-=-=-
    public static Item.Properties ItemsProps(String name) {
        return new Item.Properties().setId(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(ORESMod.MOD_ID, name))).arch$tab(ModCreativeTab.ITEMS_TAB);
    }

    public static Item.Properties BlocksProps(String name) {
        return new Item.Properties().setId(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(ORESMod.MOD_ID, name))).arch$tab(ModCreativeTab.BLOCKS_TAB);
    }

    public static Item.Properties OresProps(String name) {
        return new Item.Properties().setId(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(ORESMod.MOD_ID, name))).arch$tab(ModCreativeTab.ORES_TAB);
    }

    private static void applyCombinedProperties(Item.Properties properties, Materials material, Variants variant) {
        Materials.ItemProps materialProps = material.getItemProps();
        Variants.ItemProps variantProps = variant.getItemProps();

        if (materialProps == null || variantProps == null) return;

        // === Max Stack Size ===
        int minStack = Math.min(
                materialProps.maxStackSize() != null ? materialProps.maxStackSize() : 64,
                variantProps.maxStackSize() != null ? variantProps.maxStackSize() : 64
        );
        if (minStack != 64) {
            properties.stacksTo(minStack);
        }

        // === Rarity ===
        if (materialProps.rarity() != null && variantProps.rarity() != null) {
            Rarity higherRarity = materialProps.rarity().ordinal() > variantProps.rarity().ordinal() ? materialProps.rarity() : variantProps.rarity();
            if (higherRarity != Rarity.COMMON) {
                properties.rarity(higherRarity);
            }
        }

        // === Fire Resistant ===
        if (Boolean.TRUE.equals(materialProps.isFireResistant()) || Boolean.TRUE.equals(variantProps.isFireResistant())) {
            properties.fireResistant();
        }

        // === Trim Material ===
        if (variantProps.trimable() && materialProps.trimColor() != null) {
            ResourceLocation baseIdLocation = ResourceLocation.parse(material.getIdBase());
            ResourceLocation trimLocation = ResourceLocation.fromNamespaceAndPath(baseIdLocation.getNamespace(), material.getId());
            ResourceKey<TrimMaterial> materialKey = ResourceKey.create(Registries.TRIM_MATERIAL, trimLocation);
            properties.trimMaterial(materialKey);
        }
    }
}
