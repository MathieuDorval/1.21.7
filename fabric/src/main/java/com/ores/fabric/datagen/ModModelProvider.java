package com.ores.fabric.datagen;

import com.ores.registries.ModBlocks;
import com.ores.registries.ModItems;
import dev.architectury.registry.registries.RegistrySupplier;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ModModelProvider extends FabricModelProvider {
    public ModModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators blockStateModelGenerator) {
        for (RegistrySupplier<Block> blockSupplier : ModBlocks.DYNAMIC_BLOCKS.values()) {
            blockStateModelGenerator.createTrivialCube(blockSupplier.get());
        }
    }

    @Override
    public void generateItemModels(ItemModelGenerators itemModelGenerator) {
        for (RegistrySupplier<Item> itemSupplier : ModItems.DYNAMIC_ITEMS.values()) {
            Item item = itemSupplier.get();
            if (!(item instanceof BlockItem)) {
                itemModelGenerator.generateFlatItem(item, ModelTemplates.FLAT_ITEM);
            }
        }
    }
}
