package com.ores.mixin;

import com.ores.worldgen.OreNameFilter;
import net.minecraft.world.level.levelgen.feature.OreFeature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(OreFeature.class)
public abstract class OreFeatureMixin {

    @Inject(method = "place", at = @At("HEAD"), cancellable = true)
    private void ores$blockMatchingOres(FeaturePlaceContext<OreConfiguration> ctx, CallbackInfoReturnable<Boolean> cir) {
        for (OreConfiguration.TargetBlockState t : ctx.config().targetStates) {
            if (OreNameFilter.shouldBlock(t.state.getBlock())) {
                cir.setReturnValue(false);
                return;
            }
        }
    }
}