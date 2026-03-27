package org.Enderfan.vivarium.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import org.Enderfan.vivarium.config.VivariumConfig;
import org.Enderfan.vivarium.server.GuiltProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ParticleEngine.class)
public class ParticleEngineMixin
{
    // We inject right AFTER the engine successfully creates the vanilla particle
    @Inject(
            method = "createParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)Lnet/minecraft/client/particle/Particle;",
            at = @At("RETURN")
    )
    private void infectWaterParticles(ParticleOptions options, double x, double y, double z, double dx, double dy, double dz, CallbackInfoReturnable<Particle> cir)
    {
        Particle particle = cir.getReturnValue();

        // If the particle successfully spawned
        if (particle != null)
        {
            if (options.getType() == ParticleTypes.DRIPPING_WATER ||
                    options.getType() == ParticleTypes.FALLING_WATER ||
                    options.getType() == ParticleTypes.SPLASH )
            {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null)
                {
                    var guiltCap = mc.player.getCapability(GuiltProvider.PLAYER_GUILT).resolve();

                    if (guiltCap.isPresent() && guiltCap.get().getGuilt() >= VivariumConfig.WATER_DRIP_THRESHOLD.get())
                    {
                        // Keep all the exact vanilla physics, models, and lifecycles...
                        // Just forcefully dye the particle dark red before it renders.
                        // (1.0f, 0.0f, 0.0f is bright red. 0.6f is a darker, dried blood color).
                        particle.setColor(0.6f, 0.0f, 0.0f);
                    }
                }
            }
        }
    }
}