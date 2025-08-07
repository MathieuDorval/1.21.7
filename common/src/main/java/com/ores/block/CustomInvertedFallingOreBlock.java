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
    private final UniformInt xpRange;

    public static final MapCodec<CustomInvertedFallingOreBlock> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    propertiesCodec(),
                    UniformInt.CODEC.fieldOf("xp_range").forGetter(b -> b.xpRange)
            ).apply(instance, CustomInvertedFallingOreBlock::new)
    );

    public CustomInvertedFallingOreBlock(Properties properties, UniformInt xpRange) {
        // Correction ici : on passe 'false' pour le paramètre 'dropsOnFalling'
        super(properties, false);
        this.xpRange = xpRange;
    }

    @Override
    public @NotNull BlockState playerWillDestroy(Level pLevel, @NotNull BlockPos pPos, @NotNull BlockState pState, @NotNull Player pPlayer) {
        if (!pLevel.isClientSide && pLevel instanceof ServerLevel serverLevel) {
            if (!pPlayer.getAbilities().instabuild) {
                this.dropExperience(serverLevel, pPos, pPlayer.getMainHandItem());
            }
        }
        super.playerWillDestroy(pLevel, pPos, pState, pPlayer);
        return pState;
    }

    public void dropExperience(ServerLevel pLevel, BlockPos pPos, ItemStack pTool) {
        Optional<Holder.Reference<Enchantment>> silkTouchHolder = pLevel.registryAccess().lookup(Registries.ENCHANTMENT).flatMap(lookup -> lookup.get(Enchantments.SILK_TOUCH));
        int silkLevel = silkTouchHolder.map(holder -> EnchantmentHelper.getItemEnchantmentLevel(holder, pTool)).orElse(0);

        if (silkLevel == 0) {
            int i = this.xpRange.sample(pLevel.random);
            if (i > 0) {
                this.popExperience(pLevel, pPos, i);
            }
        }
    }
}