package com.ores.mixin;

import com.ores.worldgen.OreNameFilter;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.world.level.levelgen.feature.OreVeinFeature")
public abstract class OreVeinFeatureMixin {

    @Inject(method = "place", at = @At("HEAD"), cancellable = true)
    private void ores$blockVeins(FeaturePlaceContext<?> ctx, CallbackInfoReturnable<Boolean> cir) {
        Object cfg = ctx.config();
        if (cfg == null) return;
        try {
            for (var f : cfg.getClass().getDeclaredFields()) {
                if (!f.getType().getName().equals("net.minecraft.world.level.block.state.BlockState")) continue;
                f.setAccessible(true);
                Object val = f.get(cfg);
                if (val == null) continue;
                net.minecraft.world.level.block.state.BlockState state = (net.minecraft.world.level.block.state.BlockState) val;
                if (OreNameFilter.shouldBlock(state.getBlock())) {
                    cir.setReturnValue(false);
                    return;
                }
            }
        } catch (Throwable ignored) {
        }
    }
}