/**
 * ORES MOD | __mathieu
 * A custom inverted falling ore block that drops experience when mined.
 */
package com.ores.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class CustomInvertedFallingOreBlock extends CustomInvertedFallingBlock {

    // -=-=-=- CONSTANTS & PROPERTIES -=-=-=-
    public static final MapCodec<CustomInvertedFallingOreBlock> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    propertiesCodec(),
                    UniformInt.CODEC.fieldOf("xp_range").forGetter(block -> block.xpRange)
            ).apply(instance, CustomInvertedFallingOreBlock::new)
    );

    // -=-=-=- FIELDS -=-=-=-
    private final UniformInt xpRange;

    // -=-=-=- CONSTRUCTOR -=-=-=-
    public CustomInvertedFallingOreBlock(Properties properties, UniformInt xpRange) {
        super(properties, false);
        this.xpRange = xpRange;
    }

    // -=-=-=- OVERRIDES -=-=-=-
    @Override
    public @NotNull BlockState playerWillDestroy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull Player player) {
        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            if (!player.getAbilities().instabuild) {
                this.dropExperience(serverLevel, pos, player.getMainHandItem());
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    // -=-=-=- HELPERS -=-=-=-
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
}
