package com.ores.registries;

import com.ores.ORESMod;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ORESMod.MOD_ID, Registries.BLOCK);









    public static RegistrySupplier<Block> TEST_BLOCK;
    public static RegistrySupplier<Block> TEST_ORE;

    public static void initBlocks(){
        TEST_BLOCK = registerBlock("test_block", () -> new Block(BlockProps("test_block").requiresCorrectToolForDrops().strength(3.5f)));
        TEST_ORE = registerBlock("test_ore", () -> new DropExperienceBlock(ConstantInt.of(0), OreProps("test_ore").strength(3.0f, 3.0f)));

        BLOCKS.register();
    }

    public static RegistrySupplier<Block> registerBlock(String name, Supplier<Block> block){
        return BLOCKS.register(ResourceLocation.fromNamespaceAndPath(ORESMod.MOD_ID, name), block);
    }

    public static BlockBehaviour.Properties BlockProps(String name){
        return BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(ORESMod.MOD_ID, name)));
    }
    public static BlockBehaviour.Properties OreProps(String name){
        return BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(ORESMod.MOD_ID, name))).requiresCorrectToolForDrops();
    }
}
