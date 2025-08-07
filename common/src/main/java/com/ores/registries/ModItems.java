package com.ores.registries;

import com.ores.ORESMod;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;


public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ORESMod.MOD_ID, Registries.ITEM);

    public static RegistrySupplier<Item> TEST_ITEM;
    public static RegistrySupplier<Item> TEST_BLOCK;
    public static RegistrySupplier<Item> TEST_ORE;


    public static void initItems() {

        TEST_ITEM = registerItem("test_item", () -> new Item(ItemsProps("test_item")));
        TEST_BLOCK = registerItem("test_block", () -> new BlockItem(ModBlocks.TEST_BLOCK.get(), BlocksProps("test_block")));
        TEST_ORE = registerItem("test_ore", () -> new BlockItem(ModBlocks.TEST_ORE.get(), OresProps("test_ore")));


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
}
