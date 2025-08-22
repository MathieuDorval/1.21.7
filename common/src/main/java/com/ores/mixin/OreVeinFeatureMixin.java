package com.ores.mixin;

import com.ores.worldgen.OreNameFilter;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.world.level.levelgen.feature.OreVeinFeature")
public abstract class OreVeinFeatureMixin {

    @Inject(method = "place", at = @At("HEAD"), cancellable = true)
    private void ores$cancelVeins(FeaturePlaceContext<? extends FeatureConfiguration> ctx, CallbackInfoReturnable<Boolean> cir) {
        Object config = ctx.config();
        if (config == null) return;

        try {
            for (var field : config.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                Object val = field.get(config);
                if (val == null) continue;

                if (val instanceof net.minecraft.world.level.block.state.BlockState state) {
                    if (OreNameFilter.shouldBlock(state.getBlock())) {
                        cir.setReturnValue(false);
                        return;
                    }
                } else if (val instanceof java.util.List list) {
                    for (Object o : list) {
                        if (o instanceof net.minecraft.world.level.block.state.BlockState s && OreNameFilter.shouldBlock(s.getBlock())) {
                            cir.setReturnValue(false);
                            return;
                        }
                    }
                }
            }
        } catch (Throwable ignored) {}
    }
}
