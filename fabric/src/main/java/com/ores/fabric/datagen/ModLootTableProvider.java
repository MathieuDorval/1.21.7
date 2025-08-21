/**
 * ORES MOD | __mathieu
 * Handles the datagen for block loot tables.
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

import java.util.Optional;
import java.util.concurrent.CompletableFuture;


public class ModLootTableProvider extends FabricBlockLootTableProvider {

    // -=-=-=- CONSTRUCTOR -=-=-=-
    public ModLootTableProvider(FabricDataOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(dataOutput, registryLookup);
    }

    // -=-=-=- LOOT TABLE GENERATION -=-=-=-
    @Override
    public void generate() {
        for (RegistrySupplier<Block> blockSupplier : ModBlocks.DYNAMIC_BLOCKS.values()) {
            Block block = blockSupplier.get();
            String blockId = blockSupplier.getId().getPath();

            findMaterialAndVariant(blockId).ifPresent(pair -> {
                generateLootTable(block, pair.material(), pair.variant());
            });
        }
    }

    private void generateLootTable(Block block, Materials material, Variants variant) {
        switch (variant.getCategory()) {
            case BLOCK, FALLING_BLOCK, INVERTED_FALLING_BLOCK -> dropSelf(block);
            case ORE, FALLING_ORE, INVERTED_FALLING_ORE -> {
                Materials.OreProps oreProps = material.getOreProps();
                if (oreProps != null) {
                    Item dropItem = BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(oreProps.idDrop()))
                            .orElseThrow(() -> new IllegalStateException("Item '" + oreProps.idDrop() + "' not found in registry."));
                    add(block, createOreDrop(
                            block,
                            dropItem,
                            UniformGenerator.between(oreProps.minDrop(), oreProps.maxDrop())
                    ));
                }
            }
        }
    }

    // -=-=-=- HELPERS -=-=-=-
    private record MaterialVariantPair(Materials material, Variants variant) {}

    private static Optional<MaterialVariantPair> findMaterialAndVariant(String blockId) {
        for (Materials material : Materials.values()) {
            for (Variants variant : Variants.values()) {
                if (variant.getFormattedId(material.getId()).equals(blockId)) {
                    return Optional.of(new MaterialVariantPair(material, variant));
                }
            }
        }
        return Optional.empty();
    }

    public LootTable.Builder createOreDrop(Block oreBlock, Item drop, NumberProvider dropCount) {
        HolderLookup.RegistryLookup<Enchantment> registryLookup = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
        return createSilkTouchDispatchTable(oreBlock, this.applyExplosionDecay(oreBlock,
                LootItem.lootTableItem(drop)
                        .apply(SetItemCountFunction.setCount(dropCount))
                        .apply(ApplyBonusCount.addOreBonusCount(registryLookup.getOrThrow(Enchantments.FORTUNE)))
        ));
    }
}
