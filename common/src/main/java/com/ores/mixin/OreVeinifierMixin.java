package com.ores.mixin;

import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.OreVeinifier;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(OreVeinifier.class)
public abstract class OreVeinifierMixin {

    @Inject(
            method = "create",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void ores$disableBigVeins(
            DensityFunction densityFunction,
            DensityFunction densityFunction2,
            DensityFunction densityFunction3,
            PositionalRandomFactory positionalRandomFactory,
            CallbackInfoReturnable<NoiseChunk.BlockStateFiller> cir
    ) {
        cir.setReturnValue((ctx) -> {
            return null;
        });
    }
}
