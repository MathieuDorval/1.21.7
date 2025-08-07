package com.ores.core;

import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Définit tous les matériaux à ajouter par le mod.
 */
public enum Materials {

    COAL("coal", "minecraft:coal",
            new ItemProps(64, Rarity.COMMON, false, true, 1600, false),
            new BlockProps(0.0f, 0.0f, SoundType.METAL, MapColor.TERRACOTTA_BLACK, NoteBlockInstrument.BASEDRUM, true, 0, null, null, null, null, null, 0, Tools.PICKAXE),
            new OreProps(0.0f, 0.0f, 0, 2, false, 0, 0, 1, 1, "minecraft:coal"),
            new VanillaExclusions(List.of("coal","coal_block"))
    ),
    COPPER("copper", "minecraft:copper_ingot",
            new ItemProps(64, Rarity.COMMON, false, true, null, false),
            new BlockProps(0.0f, 0.0f, SoundType.COPPER, MapColor.COLOR_ORANGE, NoteBlockInstrument.BIT, true, 0, null, null, null, null, null, 1, Tools.PICKAXE),
            new OreProps(0.0f, 0.0f, 0, 0, false, 0, 1, 2, 5, "minecraft:raw_copper"),
            new VanillaExclusions(List.of("copper_ingot", "raw_copper", "copper_block", "raw_copper_block"))
    ),
    IRON("iron", "minecraft:iron_ingot",
            new ItemProps(64, Rarity.COMMON, false, true, null, true),
            new BlockProps(0.0f, 0.0f, SoundType.METAL, MapColor.METAL, NoteBlockInstrument.IRON_XYLOPHONE, true, 0, null, null, null, null, null, 1, Tools.PICKAXE),
            new OreProps(0.0f, 0.0f, 0, 0, false, 0, 1, 1, 1, "minecraft:raw_iron"),
            new VanillaExclusions(List.of("iron_ingot", "iron_nugget", "raw_iron", "iron_block", "raw_iron_block"))
    ),
    LAPIS("lapis", "minecraft:lapis_lazuli",
            new ItemProps(64, Rarity.COMMON, null, true, null, false),
            new BlockProps(0.0f, 0.0f, SoundType.STONE, MapColor.LAPIS, NoteBlockInstrument.BASEDRUM, true, 0, null, null, null, null, null, 1, Tools.PICKAXE),
            new OreProps(0.0f, 0.0f, 2, 5, false, 0, 1, 4, 9, "minecraft:lapis_lazuli"),
            new VanillaExclusions(List.of("lapis", "lapis_block"))
    ),
    GOLD("gold", "minecraft:gold_ingot",
            new ItemProps(64, Rarity.COMMON, false, true, null, true),
            new BlockProps(0.5f, 0.0f, SoundType.METAL, MapColor.GOLD, NoteBlockInstrument.BELL, true, 0, null, null, null, null, null, 2, Tools.PICKAXE),
            new OreProps(0.5f, 0.0f, 0, 0, false, 0, 2, 1, 1, "minecraft:raw_gold"),
            new VanillaExclusions(List.of("gold_ingot", "gold_nugget", "raw_gold", "gold_block", "raw_gold_block"))
    ),
    REDSTONE("redstone", "minecraft:redstone",
            new ItemProps(64, Rarity.COMMON, false, true, null, false),
            new BlockProps(0.0f, 0.0f, SoundType.STONE, MapColor.COLOR_RED, NoteBlockInstrument.BASEDRUM, true, 0, null, null, null, 15, null, 2, Tools.PICKAXE),
            new OreProps(0.0f, 0.0f, 1, 5, true, 0, 2, 4, 5, "minecraft:redstone"),
            new VanillaExclusions(List.of("redstone", "redstone_block"))
    ),
    EMERALD("emerald", "minecraft:emerald",
            new ItemProps(64, Rarity.RARE, false, true, null, true),
            new BlockProps(1.0f, 0.0f, SoundType.STONE, MapColor.EMERALD, NoteBlockInstrument.BASEDRUM, true, 0, null, null, null, null, null, 2, Tools.PICKAXE),
            new OreProps(1.0f, 0.0f, 3, 7, false, 0, 2, 1, 1, "minecraft:emerald"),
            new VanillaExclusions(List.of("emerald", "emerald_block"))
    ),
    DIAMOND("diamond",  "minecraft:diamond",
            new ItemProps(64, Rarity.RARE, false, true, null, true),
            new BlockProps(1.0f, 0.0f, SoundType.STONE, MapColor.DIAMOND, NoteBlockInstrument.BASEDRUM, true, 0, null, null, null, null, null, 2, Tools.PICKAXE),
            new OreProps(1.0f, 0.0f, 3, 7, false, 0, 2, 1, 1, "minecraft:diamond"),
            new VanillaExclusions(List.of("diamond", "diamond_block"))
    ),
    QUARTZ("quartz",  "minecraft:quartz",
            new ItemProps(64, Rarity.COMMON, false, true, null, false),
            new BlockProps(0.0f, 0.0f, SoundType.STONE, MapColor.QUARTZ, NoteBlockInstrument.BASEDRUM, true, 0, null, null, null, null, null, 1, Tools.PICKAXE),
            new OreProps(0.0f, 0.0f, 2, 5, false, 0, 0, 1, 1, "minecraft:quartz"),
            new VanillaExclusions(List.of("quartz", "quartz_block"))
    ),
    NETHERITE("netherite",  "minecraft:netherite_ingot",
            new ItemProps(64, Rarity.EPIC, true, true, null, true),
            new BlockProps(50.0f, 1200.0f, SoundType.NETHERITE_BLOCK, MapColor.COLOR_BLACK, NoteBlockInstrument.BASEDRUM, true, 0, null, null, null, null, null, 3, Tools.PICKAXE),
            null,
            new VanillaExclusions(List.of("netherite_ingot", "netherite_block", "netherite_scrap"))
    );

    // --- Enums Internes ---
    public enum Tools { PICKAXE, SHOVEL, AXE, HOE, SWORD }

    public record ItemProps(
            @Nullable Integer maxStackSize, @Nullable Rarity rarity, @Nullable Boolean isFireResistant,
            Boolean trim, @Nullable Integer burnTime, Boolean beacon
    ) {}

    public record BlockProps(
            Float destroyTime, Float explosionResistance, SoundType soundType, MapColor mapColor,
            NoteBlockInstrument instrument, @Nullable Boolean requiresCorrectToolForDrops, @Nullable Integer lightLevel,
            @Nullable Float friction, @Nullable Float jumpFactor, @Nullable Float speedFactor,
            @Nullable Integer redstonePower, @Nullable PushReaction pushReaction,
            Integer toolLevel, Tools tool
    ) {}

    public record OreProps(
            Float destroyTime, Float explosionResistance, Integer minXp, Integer maxXp,
            Boolean isRedstoneLike, @Nullable Integer lightLevel,
            Integer toolLevel, Integer minDrop, Integer maxDrop, String idDrop
    ) {}
    public record VanillaExclusions(
            @Nullable List<String> excludedVariantIds
    ) {}

    private final String id;
    private final String idBase;
    private final ItemProps itemProps;
    private final BlockProps blockProps;
    private final OreProps oreProps;
    private final VanillaExclusions vanillaExclusions;

    Materials(String id, String idBase, ItemProps itemProps, BlockProps blockProps, OreProps oreProps, VanillaExclusions vanillaExclusions) {
        this.id = id;
        this.idBase = idBase;
        this.itemProps = itemProps;
        this.blockProps = blockProps;
        this.oreProps = oreProps;
        this.vanillaExclusions = vanillaExclusions;
    }

    public String getId() { return id; }
    public String getIdBase() { return idBase; }
    public ItemProps getItemProps() { return itemProps; }
    public BlockProps getBlockProps() { return blockProps; }
    public OreProps getOreProps() { return oreProps; }
    public VanillaExclusions getVanillaExclusions() { return vanillaExclusions; }

}
