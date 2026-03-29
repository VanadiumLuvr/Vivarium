package org.Enderfan.vivarium.mixins;

import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import org.Enderfan.vivarium.client.WaterColorReloader;
import org.Enderfan.vivarium.config.VivariumConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BiomeColors.class)
public class MixinBiomeColors
{
    @Inject(method = "getAverageWaterColor", at = @At("RETURN"), cancellable = true)
    private static void onGetAverageWaterColor(BlockAndTintGetter level, BlockPos pos, CallbackInfoReturnable<Integer> cir)
    {
        // Safely grab the guilt from the main thread's cache
        int cachedGuilt = WaterColorReloader.currentClientGuilt;
        int threshold = VivariumConfig.FOG_COLOR_MIN.get();

        if (cachedGuilt > threshold)
        {
            float factor = Math.min(1.0f, (float) (cachedGuilt - threshold) / VivariumConfig.FOG_COLOR_MAX.get());

            int original = cir.getReturnValue();
            int r = (original >> 16) & 0xFF;
            int g = (original >> 8) & 0xFF;
            int b = original & 0xFF;

            int targetR = 140;
            int targetG = 0;
            int targetB = 0;

            int newR = (int) (r + (targetR - r) * factor);
            int newG = (int) (g + (targetG - g) * factor);
            int newB = (int) (b + (targetB - b) * factor);

            int finalColor = (newR << 16) | (newG << 8) | newB;
            cir.setReturnValue(finalColor);
        }
    }
}