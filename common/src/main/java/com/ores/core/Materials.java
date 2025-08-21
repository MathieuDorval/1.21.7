/**
 * ORES MOD | __mathieu
 * Defines all base materials, their properties, and associated data.
 */
package com.ores.core;

import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import org.jetbrains.annotations.Nullable;

public enum Materials {

    // -=-=-=- ENUM VALUES -=-=-=-
    COAL("coal", "minecraft:coal",
            new ItemProps(64, Rarity.COMMON, false, null, 1600),
            new BlockProps(0.0f, 0.0f, SoundType.METAL, MapColor.TERRACOTTA_BLACK, NoteBlockInstrument.BASEDRUM, true, 0, 0.6f, 1.0f, 1.0f, 0, null, 0, Tools.PICKAXE),
            new OreProps(0.0f, 0.0f, 0, 2, false, 0, 0, 1, 1, "minecraft:coal"),
            new Tags(false, false, false, false, false)
    ),
    COPPER("copper", "minecraft:copper_ingot",
            new ItemProps(64, Rarity.COMMON, false, 11823181, null),
            new BlockProps(0.0f, 0.0f, SoundType.COPPER, MapColor.COLOR_ORANGE, NoteBlockInstrument.BIT, true, 0, 0.6f, 1.0f, 1.0f, 0, null, 1, Tools.PICKAXE),
            new OreProps(0.0f, 0.0f, 0, 0, false, 0, 1, 2, 5, "minecraft:raw_copper"),
            new Tags(false, false, false, false, true)
    ),
    IRON("iron", "minecraft:iron_ingot",
            new ItemProps(64, Rarity.COMMON, false, 15527148, null),
            new BlockProps(0.0f, 0.0f, SoundType.METAL, MapColor.METAL, NoteBlockInstrument.IRON_XYLOPHONE, true, 0, 0.6f, 1.0f, 1.0f, 0, null, 1, Tools.PICKAXE),
            new OreProps(0.0f, 0.0f, 0, 0, false, 0, 1, 1, 1, "minecraft:raw_iron"),
            new Tags(true, false, false, false, true)
    ),
    LAPIS("lapis", "minecraft:lapis_lazuli",
            new ItemProps(64, Rarity.COMMON, null, 4288151, null),
            new BlockProps(0.0f, 0.0f, SoundType.STONE, MapColor.LAPIS, NoteBlockInstrument.BASEDRUM, true, 0, 0.6f, 1.0f, 1.0f, 0, null, 1, Tools.PICKAXE),
            new OreProps(0.0f, 0.0f, 2, 5, false, 0, 1, 4, 9, "minecraft:lapis_lazuli"),
            new Tags(false, false, false, false, true)
    ),
    GOLD("gold", "minecraft:gold_ingot",
            new ItemProps(64, Rarity.COMMON, false, 14594349, null),
            new BlockProps(0.5f, 0.0f, SoundType.METAL, MapColor.GOLD, NoteBlockInstrument.BELL, true, 0, 0.6f, 1.0f, 1.0f, 0, null, 2, Tools.PICKAXE),
            new OreProps(0.5f, 0.0f, 0, 0, false, 0, 2, 1, 1, "minecraft:raw_gold"),
            new Tags(true, false, true, true, true)
    ),
    REDSTONE("redstone", "minecraft:redstone",
            new ItemProps(64, Rarity.COMMON, false, 9901575, null),
            new BlockProps(0.0f, 0.0f, SoundType.STONE, MapColor.COLOR_RED, NoteBlockInstrument.BASEDRUM, true, 0, 0.6f, 1.0f, 1.0f, 15, null, 2, Tools.PICKAXE),
            new OreProps(0.0f, 0.0f, 1, 5, true, 0, 2, 4, 5, "minecraft:redstone"),
            new Tags(false, false, false, false, true)
    ),
    EMERALD("emerald", "minecraft:emerald",
            new ItemProps(64, Rarity.RARE, false, 1155126, null),
            new BlockProps(1.0f, 0.0f, SoundType.STONE, MapColor.EMERALD, NoteBlockInstrument.BASEDRUM, true, 0, 0.6f, 1.0f, 1.0f, 0, null, 2, Tools.PICKAXE),
            new OreProps(1.0f, 0.0f, 3, 7, false, 0, 2, 1, 1, "minecraft:emerald"),
            new Tags(true, false, false, false, true)
    ),
    DIAMOND("diamond", "minecraft:diamond",
            new ItemProps(64, Rarity.RARE, false, 7269586, null),
            new BlockProps(1.0f, 0.0f, SoundType.STONE, MapColor.DIAMOND, NoteBlockInstrument.BASEDRUM, true, 0, 0.6f, 1.0f, 1.0f, 0, null, 2, Tools.PICKAXE),
            new OreProps(1.0f, 0.0f, 3, 7, false, 0, 2, 1, 1, "minecraft:diamond"),
            new Tags(true, false, false, false, true)
    ),
    QUARTZ("quartz", "minecraft:quartz",
            new ItemProps(64, Rarity.COMMON, false, 14931140, null),
            new BlockProps(0.0f, 0.0f, SoundType.STONE, MapColor.QUARTZ, NoteBlockInstrument.BASEDRUM, true, 0, 0.6f, 1.0f, 1.0f, 0, null, 1, Tools.PICKAXE),
            new OreProps(0.0f, 0.0f, 2, 5, false, 0, 0, 1, 1, "minecraft:quartz"),
            new Tags(false, false, false, false, true)
    ),
    NETHERITE("netherite", "minecraft:netherite_ingot",
            new ItemProps(64, Rarity.EPIC, true, 6445145, null),
            new BlockProps(50.0f, 1200.0f, SoundType.NETHERITE_BLOCK, MapColor.COLOR_BLACK, NoteBlockInstrument.BASEDRUM, true, 0, 0.6f, 1.0f, 1.0f, 0, null, 3, Tools.PICKAXE),
            null,
            new Tags(true, false, false, false, true)
    ),
    TIN("tin", "ores:tin_ingot",
            new ItemProps(64, Rarity.EPIC, true, 12895428, null),
            new BlockProps(0.0f, 0.0f, SoundType.METAL, MapColor.RAW_IRON, NoteBlockInstrument.BASEDRUM, true, 0, 0.6f, 1.0f, 1.0f, 0, null, 1, Tools.PICKAXE),
            new OreProps(0.0f, 0.0f, 0, 0, false, 0, 1, 1, 1, "ores:raw_tin"),
            new Tags(true, false, false, false, true)
    );

    // -=-=-=- RECORDS & PROPS -=-=-=-
    public enum Tools {PICKAXE, SHOVEL, AXE, HOE, SWORD}

    public record ItemProps(
            @Nullable Integer maxStackSize, @Nullable Rarity rarity, @Nullable Boolean isFireResistant,
            @Nullable Integer trimColor, @Nullable Integer burnTime
    ) {}

    public record BlockProps(
            Float destroyTime, Float explosionResistance, SoundType soundType, MapColor mapColor,
            NoteBlockInstrument instrument, @Nullable Boolean requiresCorrectToolForDrops, Integer lightLevel,
            Float friction, Float jumpFactor, Float speedFactor,
            Integer redstonePower, @Nullable PushReaction pushReaction,
            Integer toolLevel, Tools tool
    ) {}

    public record OreProps(
            Float destroyTime, Float explosionResistance, Integer minXp, Integer maxXp,
            Boolean isRedstoneLike, @Nullable Integer lightLevel,
            Integer toolLevel, Integer minDrop, Integer maxDrop, String idDrop
    ) {}

    public record Tags(
            Boolean beacon, Boolean piglinRepellents, Boolean piglinLoved, Boolean piglinFood, Boolean trimMaterial
    ) {}

    // -=-=-=- FIELDS & CONSTRUCTOR -=-=-=-
    private final String id;
    private final String idBase;
    private final ItemProps itemProps;
    private final BlockProps blockProps;
    private final OreProps oreProps;
    private final Tags tags;

    Materials(String id, String idBase, ItemProps itemProps, BlockProps blockProps, @Nullable OreProps oreProps, Tags tags) {
        this.id = id;
        this.idBase = idBase;
        this.itemProps = itemProps;
        this.blockProps = blockProps;
        this.oreProps = oreProps;
        this.tags = tags;
    }

    // -=-=-=- GETTERS -=-=-=-
    public String getId() { return id; }
    public String getIdBase() { return idBase; }
    public ItemProps getItemProps() { return itemProps; }
    public BlockProps getBlockProps() { return blockProps; }
    public OreProps getOreProps() { return oreProps; }
    public Tags getTags() { return tags; }
}
