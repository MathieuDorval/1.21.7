/**
 * ORES MOD | __mathieu
 * A custom falling block that can be configured to drop as an item when it breaks from falling.
 */
package com.ores.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class CustomFallingBlock extends FallingBlock {

    // -=-=-=- CONSTANTS -=-=-=-
    public static final MapCodec<CustomFallingBlock> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    propertiesCodec(),
                    Codec.BOOL.fieldOf("drops_on_break").forGetter(block -> block.dropsOnBreak)
            ).apply(instance, CustomFallingBlock::new)
    );

    // -=-=-=- FIELDS -=-=-=-
    private final boolean dropsOnBreak;

    // -=-=-=- CONSTRUCTOR -=-=-=-
    public CustomFallingBlock(BlockBehaviour.Properties properties, boolean dropsOnBreak) {
        super(properties);
        this.dropsOnBreak = dropsOnBreak;
    }

    // -=-=-=- OVERRIDES -=-=-=-
    @Override
    protected @NotNull MapCodec<? extends FallingBlock> codec() {
        return CODEC;
    }

    @Override
    protected void falling(FallingBlockEntity entity) {
        entity.dropItem = this.dropsOnBreak;
    }

    @Override
    public int getDustColor(BlockState state, @NotNull BlockGetter getter, @NotNull BlockPos pos) {
        return state.getMapColor(getter, pos).col;
    }
}
