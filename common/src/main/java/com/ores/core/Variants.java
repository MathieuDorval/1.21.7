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
    SELF("%s", Category.ITEM, new ItemProps(64, Rarity.COMMON, false, true,100, true, ColorType.BASE, true, true, true)),
    INGOT("%s_ingot", Category.ITEM, new ItemProps(64, Rarity.COMMON, false, true, 100, true, ColorType.BASE, true, true, true)),
    RAW("raw_%s", Category.ITEM, new ItemProps(null, null, null, false, null, false, ColorType.RAW, false, false, false)),
    NUGGET("%s_nugget", Category.ITEM, new ItemProps(16, Rarity.COMMON, false, false, 12, false, ColorType.BASE, false, false, false)),
    SCRAP("%s_scrap", Category.ITEM, new ItemProps(64, Rarity.COMMON, false, false, null, false, ColorType.RAW, false, false, false)),
    // -=-=-=- BLOCKS -=-=-=-
    // === VANILLA ===
    BLOCK("%s_block", Category.BLOCK, new BlockProps(5.0f, 6.0f, true, 0, null, null, null, null, null, null, 64, 0, Materials.Tools.PICKAXE, 900, true, ColorType.BASE)),
    RAW_BLOCK("raw_%s_block", Category.BLOCK, new BlockProps(5.0f, 6.0f, true, null, null, null, null, null, null, null, null, 0, Materials.Tools.PICKAXE, null, false, ColorType.RAW)),
    // === COMPRESSED ===

    // -=-=-=- ORES -=-=-=-
    // === VANILLA ===
    STONE_ORE("%s_ore", Category.ORE, new OreProps(3.0f, 3.0f, SoundType.STONE, MapColor.STONE, NoteBlockInstrument.BASEDRUM, null, null, null, null, null, 0, Materials.Tools.PICKAXE, "minecraft:stone")),
    DEEPSLATE_ORE("deepslate_%s_ore", Category.ORE, new OreProps(4.5f, 3.0f, SoundType.DEEPSLATE, MapColor.DEEPSLATE, NoteBlockInstrument.BASEDRUM, null, null, null, null, null, 0, Materials.Tools.PICKAXE, "minecraft:deepslate")),
    GRANITE_ORE("granite_%s_ore", Category.ORE, new OreProps(3.0f, 3.0f, SoundType.STONE, MapColor.DIRT, NoteBlockInstrument.BASEDRUM, null, null, null, null, null, 0, Materials.Tools.PICKAXE, "minecraft:granite")),
    DIORITE_ORE("diorite_%s_ore", Category.ORE, new OreProps(3.0f, 3.0f, SoundType.STONE, MapColor.QUARTZ, NoteBlockInstrument.BASEDRUM, null, null, null, null, null, 0, Materials.Tools.PICKAXE, "minecraft:diorite")),
    ANDESITE_ORE("andesite_%s_ore", Category.ORE, new OreProps(3.0f, 3.0f, SoundType.STONE, MapColor.STONE, NoteBlockInstrument.BASEDRUM, null, null, null, null, null, 0, Materials.Tools.PICKAXE, "minecraft:andesite")),
    TUFF_ORE("tuff_%s_ore", Category.ORE, new OreProps(3.0f, 3.0f, SoundType.TUFF, MapColor.TERRACOTTA_GRAY, NoteBlockInstrument.BASEDRUM, null, null, null, null, null, 0, Materials.Tools.PICKAXE, "minecraft:tuff")),
    CALCITE_ORE("calcite_%s_ore", Category.ORE, new OreProps(3.0f, 3.0f, SoundType.CALCITE, MapColor.TERRACOTTA_WHITE, NoteBlockInstrument.BASEDRUM, null, null, null, null, null, 0, Materials.Tools.PICKAXE, "minecraft:calcite")),
    DRIPSTONE_ORE("dripstone_%s_ore", Category.ORE, new OreProps(3.0f, 3.0f, SoundType.DRIPSTONE_BLOCK, MapColor.DIRT, NoteBlockInstrument.BASEDRUM, null, null, null, null, null, 0, Materials.Tools.PICKAXE, "minecraft:dripstone_block")),
    NETHERRACK_ORE("netherrack_%s_ore", Category.ORE, new OreProps(0.4f, 0.4f, SoundType.NETHERRACK, MapColor.NETHER, NoteBlockInstrument.BASEDRUM, null, null, null, null, null, 0, Materials.Tools.PICKAXE, "minecraft:netherrack")),
    BASALT_ORE("basalt_%s_ore", Category.ORE, new OreProps(1.25f, 4.2f, SoundType.BASALT, MapColor.COLOR_BLACK, NoteBlockInstrument.BASEDRUM, null, null, null, null, null, 0, Materials.Tools.PICKAXE, "minecraft:basalt")),
    SMOOTH_BASALT_ORE("smooth_basalt_%s_ore", Category.ORE, new OreProps(1.25f, 4.2f, SoundType.BASALT, MapColor.COLOR_BLACK, NoteBlockInstrument.BASEDRUM, null, null, null, null, null, 0, Materials.Tools.PICKAXE, "minecraft:smooth_basalt")),
    DIRT_ORE("dirt_%s_ore", Category.ORE, new OreProps(0.5f, 0.5f, SoundType.GRAVEL, MapColor.DIRT, NoteBlockInstrument.BASEDRUM, null, null, null, null, null, 0, Materials.Tools.SHOVEL, "minecraft:dirt")),
    SOUL_SAND_ORE("soul_sand_%s_ore", Category.ORE, new OreProps(0.5f, 0.5f, SoundType.SOUL_SAND, MapColor.COLOR_BROWN, NoteBlockInstrument.SNARE, null, null, null, null, null, 0, Materials.Tools.SHOVEL, "minecraft:soul_sand")),
    SOUL_SOIL_ORE("soul_soil_%s_ore", Category.ORE, new OreProps(0.5f, 0.5f, SoundType.SOUL_SOIL, MapColor.COLOR_BROWN, NoteBlockInstrument.BASEDRUM, null, null, null, null, null, 0, Materials.Tools.SHOVEL, "minecraft:soul_soil")),
    SCULK_ORE("sculk_%s_ore", Category.ORE, new OreProps(0.2f, 0.2f, SoundType.SCULK, MapColor.COLOR_BLACK, NoteBlockInstrument.BASEDRUM, null, null, null, null, null, 0, Materials.Tools.HOE, "minecraft:sculk")),
    GRAVEL_ORE("gravel_%s_ore", Category.FALLING_ORE, new OreProps(0.6f, 0.6f, SoundType.GRAVEL, MapColor.STONE, NoteBlockInstrument.BASEDRUM, null, null, null, null, null, 0, Materials.Tools.SHOVEL, "minecraft:gravel")),
    SAND_ORE("sand_%s_ore", Category.FALLING_ORE, new OreProps(0.5f, 0.5f, SoundType.SAND, MapColor.SAND, NoteBlockInstrument.SNARE, null, null, null, null, null, 0, Materials.Tools.SHOVEL, "minecraft:sand")),
    RED_SAND_ORE("red_sand_%s_ore", Category.FALLING_ORE, new OreProps(0.5f, 0.5f, SoundType.SAND, MapColor.COLOR_ORANGE, NoteBlockInstrument.SNARE, null, null, null, null, null, 0, Materials.Tools.SHOVEL, "minecraft:red_sand")),
    SANDSTONE_ORE("sandstone_%s_ore", Category.ORE, new OreProps(0.8f, 0.8f, SoundType.STONE, MapColor.SAND, NoteBlockInstrument.BASEDRUM, null, null, null, null, null, 0, Materials.Tools.PICKAXE, "minecraft:sandstone")),
    RED_SANDSTONE_ORE("red_sandstone_%s_ore", Category.ORE, new OreProps(0.8f, 0.8f, SoundType.STONE, MapColor.COLOR_ORANGE, NoteBlockInstrument.BASEDRUM, null, null, null, null, null, 0, Materials.Tools.PICKAXE, "minecraft:red_sandstone")),
    TERRACOTTA_ORE("terracotta_%s_ore", Category.ORE, new OreProps(1.25f, 4.2f, SoundType.STONE, MapColor.COLOR_ORANGE, NoteBlockInstrument.BASEDRUM, null, null, null, null, null, 0, Materials.Tools.PICKAXE, "minecraft:terracotta")),
    WHITE_TERRACOTTA_ORE("white_terracotta_%s_ore", Category.ORE, new OreProps(1.25f, 4.2f, SoundType.STONE, MapColor.TERRACOTTA_WHITE, NoteBlockInstrument.BASEDRUM, null, null, null, null, null, 0, Materials.Tools.PICKAXE, "minecraft:white_terracotta")),
    ORANGE_TERRACOTTA_ORE("orange_terracotta_%s_ore", Category.ORE, new OreProps(1.25f, 4.2f, SoundType.STONE, MapColor.COLOR_ORANGE, NoteBlockInstrument.BASEDRUM, null, null, null, null, null, 0, Materials.Tools.PICKAXE, "minecraft:orange_terracotta")),
    MAGENTA_TERRACOTTA_ORE("magenta_terracotta_%s_ore", Category.ORE, new OreProps(1.25f, 4.2f, SoundType.STONE, MapColor.COLOR_MAGENTA, NoteBlockInstrument.BASEDRUM, null, null, null, null, null, 0, Materials.Tools.PICKAXE, "minecraft:magenta_terracotta")),
    LIGHT_BLUE_TERRACOTTA_ORE("light_blue_terracotta_%s_ore", Category.ORE, new OreProps(1.25f, 4.2f, SoundType.STONE, MapColor.COLOR_LIGHT_BLUE, NoteBlockInstrument.BASEDRUM, null, null, null, null, null, 0, Materials.Tools.PICKAXE, "minecraft:light_blue_terracotta")),
    YELLOW_TERRACOTTA_ORE("yellow_terracotta_%s_ore", Category.ORE, new OreProps(1.25f, 4.2f, SoundType.STONE, MapColor.COLOR_YELLOW, NoteBlockInstrument.BASEDRUM, null, null, null, null, null, 0, Materials.Tools.PICKAXE, "minecraft:yellow_terracotta")),
    LIME_TERRACOTTA_ORE("lime_terracotta_%s_ore", Category.ORE, new OreProps(1.25f, 4.2f, SoundType.STONE, MapColor.COLOR_LIGHT_GREEN, NoteBlockInstrument.BASEDRUM, null, null, null, null, null, 0, Materials.Tools.PICKAXE, "minecraft:lime_terracotta")),
    PINK_TERRACOTTA_ORE("pink_terracotta_%s_ore", Category.ORE, new OreProps(1.25f, 4.2f, SoundType.STONE, MapColor.COLOR_PINK, NoteBlockInstrument.BASEDRUM, null, null, null, null, null, 0, Materials.Tools.PICKAXE, "minecraft:pink_terracotta")),
    GRAY_TERRACOTTA_ORE("gray_terracotta_%s_ore", Category.ORE, new OreProps(1.25f, 4.2f, SoundType.STONE, MapColor.TERRACOTTA_GRAY, NoteBlockInstrument.BASEDRUM, null, null, null, null, null, 0, Materials.Tools.PICKAXE, "minecraft:gray_terracotta")),
    LIGHT_GRAY_TERRACOTTA_ORE("light_gray_terracotta_%s_ore", Category.ORE, new OreProps(1.25f, 4.2f, SoundType.STONE, MapColor.TERRACOTTA_LIGHT_GRAY, NoteBlockInstrument.BASEDRUM, null, null, null, null, null, 0, Materials.Tools.PICKAXE, "minecraft:light_gray_terracotta")),
    CYAN_TERRACOTTA_ORE("cyan_terracotta_%s_ore", Category.ORE, new OreProps(1.25f, 4.2f, SoundType.STONE, MapColor.COLOR_CYAN, NoteBlockInstrument.BASEDRUM, null, null, null, null, null, 0, Materials.Tools.PICKAXE, "minecraft:cyan_terracotta")),
    PURPLE_TERRACOTTA_ORE("purple_terracotta_%s_ore", Category.ORE, new OreProps(1.25f, 4.2f, SoundType.STONE, MapColor.COLOR_PURPLE, NoteBlockInstrument.BASEDRUM, null, null, null, null, null, 0, Materials.Tools.PICKAXE, "minecraft:purple_terracotta")),
    BLUE_TERRACOTTA_ORE("blue_terracotta_%s_ore", Category.ORE, new OreProps(1.25f, 4.2f, SoundType.STONE, MapColor.COLOR_BLUE, NoteBlockInstrument.BASEDRUM, null, null, null, null, null, 0, Materials.Tools.PICKAXE, "minecraft:blue_terracotta")),
    BROWN_TERRACOTTA_ORE("brown_terracotta_%s_ore", Category.ORE, new OreProps(1.25f, 4.2f, SoundType.STONE, MapColor.TERRACOTTA_BROWN, NoteBlockInstrument.BASEDRUM, null, null, null, null, null, 0, Materials.Tools.PICKAXE, "minecraft:brown_terracotta")),
    GREEN_TERRACOTTA_ORE("green_terracotta_%s_ore", Category.ORE, new OreProps(1.25f, 4.2f, SoundType.STONE, MapColor.COLOR_GREEN, NoteBlockInstrument.BASEDRUM, null, null, null, null, null, 0, Materials.Tools.PICKAXE, "minecraft:green_terracotta")),
    RED_TERRACOTTA_ORE("red_terracotta_%s_ore", Category.ORE, new OreProps(1.25f, 4.2f, SoundType.STONE, MapColor.COLOR_RED, NoteBlockInstrument.BASEDRUM, null, null, null, null, null, 0, Materials.Tools.PICKAXE, "minecraft:red_terracotta")),
    BLACK_TERRACOTTA_ORE("black_terracotta_%s_ore", Category.ORE, new OreProps(1.25f, 4.2f, SoundType.STONE, MapColor.TERRACOTTA_BLACK, NoteBlockInstrument.BASEDRUM, null, null, null, null, null, 0, Materials.Tools.PICKAXE, "minecraft:black_terracotta"));


    public enum Category { ITEM, BLOCK, FALLING_BLOCK, INVERTED_FALLING_BLOCK, GLASS, ORE, FALLING_ORE, INVERTED_FALLING_ORE }
    public enum ColorType { BASE, RAW }

    public record ItemProps(
            @Nullable Integer maxStackSize, @Nullable Rarity rarity, @Nullable Boolean isFireResistant,
            Boolean trimable, @Nullable Integer burnTime, Boolean beacon, ColorType color, Boolean piglinRepellents, Boolean piglinLoved, Boolean piglinFood
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
