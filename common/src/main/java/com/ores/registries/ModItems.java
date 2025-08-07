package com.ores.registries;

import com.ores.ORESMod;
import com.ores.core.Materials;
import com.ores.core.Variants;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;


public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ORESMod.MOD_ID, Registries.ITEM);

    public static final Map<String, RegistrySupplier<Item>> DYNAMIC_ITEMS = new HashMap<>();


    public static void initItems() {
        for (Materials material : Materials.values()) {
            List<String> exclusions = material.getVanillaExclusions() != null ? material.getVanillaExclusions().excludedVariantIds() : null;
            for (Variants variant : Variants.values()) {
                String itemId = variant.getFormattedId(material.getId());

                if (exclusions != null && exclusions.contains(itemId)) continue;

                RegistrySupplier<Block> blockSupplier = ModBlocks.DYNAMIC_BLOCKS.get(itemId);

                switch (variant.getCategory()) {
                    case ITEM:
                        RegistrySupplier<Item> itemSupplier = registerItem(itemId, () -> new Item(applyCombinedProperties(ItemsProps(itemId), material, variant)));
                        DYNAMIC_ITEMS.put(itemId, itemSupplier);
                        ModFuels.addFuel(itemSupplier, material, variant);
                        break;

                    case BLOCK:
                    case FALLING_BLOCK:
                    case INVERTED_FALLING_BLOCK:
                        if (blockSupplier != null) {
                            RegistrySupplier<Item> blockItemSupplier = registerItem(itemId, () -> new BlockItem(blockSupplier.get(), applyCombinedProperties(BlocksProps(itemId), material, variant)));
                            DYNAMIC_ITEMS.put(itemId, blockItemSupplier);
                            ModFuels.addFuel(blockItemSupplier, material, variant);
                        }
                        break;

                    case ORE:
                    case FALLING_ORE:
                    case INVERTED_FALLING_ORE:
                        if (blockSupplier != null) {
                            RegistrySupplier<Item> oreItemSupplier = registerItem(itemId, () -> new BlockItem(blockSupplier.get(), OresProps(itemId)));
                            DYNAMIC_ITEMS.put(itemId, oreItemSupplier);
                            ModFuels.addFuel(oreItemSupplier, material, variant);
                        }
                        break;
                }
            }
        }

        ITEMS.register();
    }


    public static RegistrySupplier<Item> registerItem(String name, Supplier<Item> item){
        return ITEMS.register(ResourceLocation.fromNamespaceAndPath(ORESMod.MOD_ID, name), item);
    }

    public static Item.Properties ItemsProps(String name){
        return new Item.Properties().setId(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(ORESMod.MOD_ID, name))).arch$tab(ModCreativeTab.ITEMS_TAB);
    }
    public static Item.Properties BlocksProps(String name){
        return new Item.Properties().setId(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(ORESMod.MOD_ID, name))).arch$tab(ModCreativeTab.BLOCKS_TAB);
    }
    public static Item.Properties OresProps(String name){
        return new Item.Properties().setId(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(ORESMod.MOD_ID, name))).arch$tab(ModCreativeTab.ORES_TAB);
    }

    private static Item.Properties applyCombinedProperties(Item.Properties props, Materials material, Variants variant) {
        Materials.ItemProps materialProps = material.getItemProps();
        Variants.ItemProps variantProps = variant.getItemProps();

        if (materialProps == null || variantProps == null) return props;

        if (materialProps.maxStackSize() != null && variantProps.maxStackSize() != null) {
            int minStack = Math.min(materialProps.maxStackSize(), variantProps.maxStackSize());
            if (minStack != 64) {
                props.stacksTo(minStack);
            }
        }

        if (materialProps.rarity() != null && variantProps.rarity() != null) {
            Rarity higherRarity = materialProps.rarity().ordinal() > variantProps.rarity().ordinal() ? materialProps.rarity() : variantProps.rarity();
            if (higherRarity != Rarity.COMMON) {
                props.rarity(higherRarity);
            }
        }

        if (materialProps.isFireResistant() != null && variantProps.isFireResistant() != null) {
            if (materialProps.isFireResistant() || variantProps.isFireResistant()) {
                props.fireResistant();
            }
        }

        return props;
    }
}
