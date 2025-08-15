/**
 * ORES MOD | __mathieu
 * BLOCKS LOOT TABLE DATAGEN
 */
package com.ores.fabric.datagen;

import com.ores.core.Materials;
import com.ores.core.Variants;
import com.ores.registries.ModBlocks;
import dev.architectury.registry.registries.RegistrySupplier;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.concurrent.CompletableFuture;


public class ModLootTableProvider extends FabricBlockLootTableProvider {
    protected ModLootTableProvider(FabricDataOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generate() {
        for (RegistrySupplier<Block> blockSupplier : ModBlocks.DYNAMIC_BLOCKS.values()) {
            String blockId = blockSupplier.getId().getPath();

            for (Materials material : Materials.values()) {
                for (Variants variant : Variants.values()) {
                    String combinedId = variant.getFormattedId(material.getId());

                    if (combinedId.equals(blockId)) {
                        Variants.Category category = variant.getCategory();
                        switch (category) {
                            case BLOCK:
                            case FALLING_BLOCK:
                            case INVERTED_FALLING_BLOCK:
                                dropSelf(blockSupplier.get());
                                break;
                            case ORE:
                            case FALLING_ORE:
                            case INVERTED_FALLING_ORE:
                                Materials.OreProps oreProps = material.getOreProps();
                                if (oreProps != null) {
                                    Item dropItem = BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(oreProps.idDrop()))
                                            .orElseThrow(() -> new IllegalStateException("Item '" + oreProps.idDrop() + "' not found in registry."));
                                    add(blockSupplier.get(), oreDrops(
                                            blockSupplier.get(),
                                            dropItem,
                                            UniformGenerator.between(oreProps.minDrop(), oreProps.maxDrop())
                                    ));
                                }
                                break;
                        }
                    }
                }
            }
        }
    }

    public LootTable.Builder oreDrops(Block oreBlock, Item drop, NumberProvider dropCount) {
        HolderLookup.RegistryLookup<Enchantment> registryLookup = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
        return createSilkTouchDispatchTable(oreBlock, this.applyExplosionDecay(oreBlock,
                LootItem.lootTableItem(drop)
                        .apply(SetItemCountFunction.setCount(dropCount))
                        .apply(ApplyBonusCount.addOreBonusCount(registryLookup.getOrThrow(Enchantments.FORTUNE)))
        ));
    }
}
