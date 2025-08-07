package com.ores.registries;

import com.ores.ORESMod;
import dev.architectury.registry.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class ModBlocks {
    public static final DeferredRegister<Block> REGISTRY = DeferredRegister.create(ORESMod.MOD_ID, Registries.BLOCK);

    public static final Block.Properties DEFAULT_PROPS = Block.Properties.ofFullCopy(Blocks.STONE);
}
