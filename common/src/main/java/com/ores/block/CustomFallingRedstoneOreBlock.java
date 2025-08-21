/**
 * ORES MOD | __mathieu
 * A custom falling redstone ore block that lights up and creates particles on interaction.
 */
package com.ores.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.RedstoneTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class CustomFallingRedstoneOreBlock extends FallingBlock {

    // -=-=-=- CONSTANTS & PROPERTIES -=-=-=-
    public static final BooleanProperty LIT = RedstoneTorchBlock.LIT;
    public static final MapCodec<CustomFallingRedstoneOreBlock> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    propertiesCodec(),
                    UniformInt.CODEC.fieldOf("xp_range").forGetter(block -> block.xpRange),
                    Codec.INT.fieldOf("particle_color").forGetter(block -> block.particleColor)
            ).apply(instance, CustomFallingRedstoneOreBlock::new)
    );

    // -=-=-=- FIELDS -=-=-=-
    private final UniformInt xpRange;
    private final int particleColor;

    // -=-=-=- CONSTRUCTOR -=-=-=-
    public CustomFallingRedstoneOreBlock(Properties properties, UniformInt xpRange, int particleColor) {
        super(properties);
        this.xpRange = xpRange;
        this.particleColor = particleColor;
        this.registerDefaultState(this.defaultBlockState().setValue(LIT, false));
    }

    // -=-=-=- OVERRIDES -=-=-=-
    @Override
    protected @NotNull MapCodec<? extends FallingBlock> codec() {
        return CODEC;
    }

    @Override
    public void attack(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player) {
        interact(state, level, pos);
        super.attack(state, level, pos, player);
    }

    @Override
    public void stepOn(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull Entity entity) {
        if (!entity.isSteppingCarefully()) {
            interact(state, level, pos);
        }
        super.stepOn(level, pos, state, entity);
    }

    @Override
    public @NotNull InteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hitResult) {
        if (level.isClientSide) {
            spawnParticles(level, pos);
        } else {
            interact(state, level, pos);
        }
        boolean canPlace = stack.getItem() instanceof BlockItem && (new BlockPlaceContext(player, hand, stack, hitResult)).canPlace();
        return canPlace ? InteractionResult.PASS : InteractionResult.SUCCESS;
    }

    @Override
    public boolean isRandomlyTicking(@NotNull BlockState state) {
        return state.getValue(LIT);
    }

    @Override
    public void randomTick(@NotNull BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        if (state.getValue(LIT)) {
            level.setBlock(pos, state.setValue(LIT, false), 3);
        }
    }

    @Override
    protected void falling(FallingBlockEntity entity) {
        entity.dropItem = false;
    }

    @Override
    public @NotNull BlockState playerWillDestroy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull Player player) {
        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            if (!player.getAbilities().instabuild) {
                this.dropExperience(serverLevel, pos, player.getMainHandItem());
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void animateTick(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        if (state.getValue(LIT)) {
            spawnParticles(level, pos);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }

    @Override
    public int getDustColor(@NotNull BlockState state, @NotNull BlockGetter getter, @NotNull BlockPos pos) {
        return this.particleColor;
    }

    // -=-=-=- HELPERS -=-=-=-
    private void interact(BlockState state, Level level, BlockPos pos) {
        spawnParticles(level, pos);
        if (!state.getValue(LIT)) {
            level.setBlock(pos, state.setValue(LIT, true), 3);
        }
    }

    public void dropExperience(ServerLevel level, BlockPos pos, ItemStack tool) {
        Optional<Holder.Reference<Enchantment>> silkTouchHolder = level.registryAccess().lookup(Registries.ENCHANTMENT).flatMap(lookup -> lookup.get(Enchantments.SILK_TOUCH));
        int silkLevel = silkTouchHolder.map(holder -> EnchantmentHelper.getItemEnchantmentLevel(holder, tool)).orElse(0);
        if (silkLevel == 0) {
            int experience = this.xpRange.sample(level.random);
            if (experience > 0) {
                this.popExperience(level, pos, experience);
            }
        }
    }

    private void spawnParticles(Level level, BlockPos pos) {
        DustParticleOptions particleOptions = new DustParticleOptions(this.particleColor, 1.0F);
        RandomSource random = level.random;
        for (Direction direction : Direction.values()) {
            BlockPos adjacentPos = pos.relative(direction);
            if (!level.getBlockState(adjacentPos).isSolidRender()) {
                Direction.Axis axis = direction.getAxis();
                double xOffset = axis == Direction.Axis.X ? 0.5 + 0.5625 * direction.getStepX() : random.nextDouble();
                double yOffset = axis == Direction.Axis.Y ? 0.5 + 0.5625 * direction.getStepY() : random.nextDouble();
                double zOffset = axis == Direction.Axis.Z ? 0.5 + 0.5625 * direction.getStepZ() : random.nextDouble();
                level.addParticle(particleOptions, pos.getX() + xOffset, pos.getY() + yOffset, pos.getZ() + zOffset, 0.0, 0.0, 0.0);
            }
        }
    }
}
