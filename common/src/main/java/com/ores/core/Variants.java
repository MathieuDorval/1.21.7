package com.ores.core;

import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum Variants {
    // -=-=-=- ITEMS -=-=-=-
    // === VANILLA ===
    SELF("%s", Category.ITEM, new ItemProps(64, Rarity.COMMON, false, false,100, true, ColorType.BASE)),
    INGOT("%s_ingot", Category.ITEM, new ItemProps(64, Rarity.COMMON, false, true, 100, true, ColorType.BASE)),
    RAW("raw_%s", Category.ITEM, new ItemProps(null, null, null, false, null, false, ColorType.RAW)),
    NUGGET("%s_nugget", Category.ITEM, new ItemProps(16, Rarity.RARE, false, false, null, false, ColorType.BASE)),
    SCRAP("%s_scrap", Category.ITEM, new ItemProps(64, Rarity.COMMON, false, false, null, false, ColorType.RAW)),
    // -=-=-=- BLOCKS -=-=-=-
    // === VANILLA ===
    BLOCK("%s_block", Category.BLOCK, new BlockProps(5.0f, 6.0f, true, 0, null, null, null, null, null, null, 64, 0, Materials.Tools.PICKAXE, 900, true, ColorType.BASE)),
    RAW_BLOCK("raw_%s_block", Category.BLOCK, new BlockProps(5.0f, 6.0f, true, null, null, null, null, null, null, null, null, 0, Materials.Tools.PICKAXE, null, false, ColorType.RAW)),
    // -=-=-=- ORES -=-=-=-
    // === VANILLA ===
    STONE_ORE("%s_ore", Category.ORE, new OreProps(3.0f, 3.0f, SoundType.STONE, MapColor.STONE, NoteBlockInstrument.BASEDRUM, null, null, null, null, null, 0, Materials.Tools.PICKAXE, "minecraft:stone")),
    DEEPSLATE_ORE("deepslate_%s_ore", Category.ORE, new OreProps(4.5f, 3.0f, SoundType.DEEPSLATE, MapColor.DEEPSLATE, NoteBlockInstrument.BASEDRUM, null, null, null, null, null, 0, Materials.Tools.PICKAXE, "minecraft:deepslate")),
    GRANITE_ORE("granite_%s_ore", Category.ORE, new OreProps(3.0f, 3.0f, SoundType.STONE, MapColor.DIRT, NoteBlockInstrument.BASEDRUM, null, null, null, null, null, 0, Materials.Tools.PICKAXE, "minecraft:granite")),
    DIORITE_ORE("diorite_%s_ore", Category.ORE, new OreProps(3.0f, 3.0f, SoundType.STONE, MapColor.QUARTZ, NoteBlockInstrument.BASEDRUM, null, null, null, null, null, 0, Materials.Tools.PICKAXE, "minecraft:diorite")),
    ANDESITE_ORE("andesite_%s_ore", Category.ORE, new OreProps(3.0f, 3.0f, SoundType.STONE, MapColor.STONE, NoteBlockInstrument.BASEDRUM, null, null, null, null, null, 0, Materials.Tools.PICKAXE, "minecraft:andesite")),
    TUFF_ORE("tuff_%s_ore", Category.ORE, new OreProps(3.0f, 3.0f, SoundType.TUFF, MapColor.TERRACOTTA_GRAY, NoteBlockInstrument.BASEDRUM, null, null, null, null, null, 0, Materials.Tools.PICKAXE, "minecraft:tuff")),
    CALCITE_ORE("calcite_%s_ore", Category.ORE, new OreProps(3.0f, 3.0f, SoundType.CALCITE, MapColor.TERRACOTTA_WHITE, NoteBlockInstrument.BASEDRUM, null, null, null, null, null, 0, Materials.Tools.PICKAXE, "minecraft:calcite")),
    DRIPSTONE_ORE("dripstone_%s_ore", Category.ORE, new OreProps(3.0f, 3.0f, SoundType.DRIPSTONE_BLOCK, MapColor.DIRT, NoteBlockInstrument.BASEDRUM, null, null, null, null, null, 0, Materials.Tools.PICKAXE, "minecraft:dripstone_block"));


    public enum Category { ITEM, BLOCK, FALLING_BLOCK, GLASS, ORE, FALLING_ORE }
    public enum ColorType { BASE, RAW }

    public record ItemProps(
            @Nullable Integer maxStackSize, @Nullable Rarity rarity, @Nullable Boolean isFireResistant,
            Boolean trimable, @Nullable Integer burnTime, Boolean beacon, ColorType color
    ) {}

    public record BlockProps(
            Float destroyTime, Float explosionResistance, @Nullable Boolean requiresCorrectToolForDrops,
            @Nullable Integer lightLevel, @Nullable Float friction, @Nullable Float jumpFactor, @Nullable Float speedFactor,
            @Nullable Integer redstonePower, @Nullable PushReaction pushReaction, @Nullable Boolean dropsOnFalling,
            @Nullable Integer maxStackSize, Integer toolLevel, Materials.Tools tool, @Nullable Integer burnTime, Boolean beacon, ColorType colorType
    ) {}

    public record OreProps(
            Float destroyTime, Float explosionResistance, SoundType soundType, MapColor mapColor,
            NoteBlockInstrument instrument, @Nullable Integer lightLevel, @Nullable Float friction, @Nullable Float jumpFactor,
            @Nullable Float speedFactor, @Nullable PushReaction pushReaction,
            Integer toolLevel, Materials.Tools tool, String idStone
    ) {}


    private final String idFormat;
    private final Category category;
    @Nullable private final ItemProps itemProps;
    @Nullable private final BlockProps blockProps;
    @Nullable private final OreProps oreProps;


    Variants(String idFormat, Category category, @Nullable ItemProps itemProps, @Nullable BlockProps blockProps, @Nullable OreProps oreProps) {
        this.idFormat = idFormat;
        this.category = category;
        this.itemProps = itemProps;
        this.blockProps = blockProps;
        this.oreProps = oreProps;
    }

    Variants(String idFormat, Category category, ItemProps itemProps) {
        this(idFormat, category, itemProps, null, null);
    }

    Variants(String idFormat, Category category, BlockProps blockProps) {
        this(idFormat, category, null, blockProps, null);
    }
    Variants(String idFormat, Category category, OreProps oreProps) {
        this(idFormat, category, null, null, oreProps);
    }


    public String getFormattedId(@NotNull String materialName) {
        return String.format(idFormat, materialName);
    }
    public Category getCategory() { return category; }
    @Nullable public ItemProps getItemProps() { return itemProps; }
    @Nullable public BlockProps getBlockProps() { return blockProps; }
    @Nullable public OreProps getOreProps() { return oreProps; }
}
