/**
 * ORES MOD | __mathieu
 * A custom block that "falls" upwards when the space above it is empty.
 */
package com.ores.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class CustomInvertedFallingBlock extends Block {

    // -=-=-=- CONSTANTS & PROPERTIES -=-=-=-
    public static final MapCodec<CustomInvertedFallingBlock> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    propertiesCodec(),
                    Codec.BOOL.fieldOf("drops_on_falling").forGetter(block -> block.dropsOnFalling)
            ).apply(instance, CustomInvertedFallingBlock::new)
    );

    // -=-=-=- FIELDS -=-=-=-
    private final boolean dropsOnFalling;

    // -=-=-=- CONSTRUCTOR -=-=-=-
    public CustomInvertedFallingBlock(Properties properties, boolean dropsOnFalling) {
        super(properties);
        this.dropsOnFalling = dropsOnFalling;
    }

    // -=-=-=- OVERRIDES -=-=-=-
    @Override
    protected @NotNull MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public void onPlace(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState oldState, boolean isMoving) {
        if (oldState.is(this)) {
            return;
        }
        level.scheduleTick(pos, this, 2);
    }

    @Override
    public void tick(@NotNull BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        BlockPos posAbove = pos.above();

        if (canRise(level.getBlockState(posAbove)) && pos.getY() < level.getMaxY() - 1) {
            level.removeBlock(pos, false);
            level.setBlock(posAbove, state, 3);
        } else {
            level.removeBlock(pos, false);
            if (this.dropsOnFalling) {
                Block.popResource(level, pos, new ItemStack(this));
            }
        }
    }

    @Override
    public void animateTick(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        if (random.nextInt(16) == 0) {
            BlockPos posAbove = pos.above();
            if (canRise(level.getBlockState(posAbove))) {
                double x = (double) pos.getX() + random.nextDouble();
                double y = (double) pos.getY() + 1.05D;
                double z = (double) pos.getZ() + random.nextDouble();
                level.addParticle(new BlockParticleOption(ParticleTypes.FALLING_DUST, state), x, y, z, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    // -=-=-=- HELPERS -=-=-=-
    public static boolean canRise(BlockState state) {
        return state.isAir() || state.is(BlockTags.FIRE) || state.liquid() || state.canBeReplaced();
    }
}
