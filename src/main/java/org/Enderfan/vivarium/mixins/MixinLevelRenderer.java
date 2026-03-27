package org.Enderfan.vivarium.mixins;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import org.Enderfan.vivarium.config.VivariumConfig;
import org.Enderfan.vivarium.particles.ModParticles;
import org.Enderfan.vivarium.server.GuiltProvider;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LevelRenderer.class)
public abstract class MixinLevelRenderer {

    @Shadow
    @Final
    public static ResourceLocation RAIN_LOCATION;

    @Shadow @Final private static ResourceLocation SNOW_LOCATION;

    @Unique
    private static final ResourceLocation RED_RAIN = new ResourceLocation("vivarium", "textures/environment/red_rain.png");
    @Unique
    private static final ResourceLocation VIVARIUM_ALT_SUN = new ResourceLocation("vivarium", "textures/environment/alt_sun.png");
    @Unique
    private static final ResourceLocation VIVARIUM_ALT_SUN_FLIPPED = new ResourceLocation("vivarium", "textures/environment/alt_sun_flipped.png");

    @Redirect(
            method = "tickRain(Lnet/minecraft/client/Camera;)V",
            at = @At(value = "FIELD", target = "Lnet/minecraft/core/particles/ParticleTypes;RAIN:Lnet/minecraft/core/particles/SimpleParticleType;", opcode = Opcodes.GETSTATIC)
    )
    private SimpleParticleType vivarium$redirectRainSplash() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            return player.getCapability(GuiltProvider.PLAYER_GUILT)
                    .map(g -> g.getGuilt() >= 1000 ? ModParticles.BLOOD_DRIP.get() : ParticleTypes.RAIN)
                    .orElse(ParticleTypes.RAIN);
        }
        return ParticleTypes.RAIN;
    }

    @Redirect(
            method = "renderSnowAndRain",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(ILnet/minecraft/resources/ResourceLocation;)V")
    )
    private void vivarium$redirectRainTexture(int sampler, ResourceLocation texture) {
        LocalPlayer player = Minecraft.getInstance().player;
        // Use the shadowed RAIN_LOCATION here
        if (player != null && texture.equals(RAIN_LOCATION)) {
            ResourceLocation finalTexture = texture;
            texture = player.getCapability(GuiltProvider.PLAYER_GUILT)
                    .map(g -> g.getGuilt() >= VivariumConfig.BLOOD_RAIN_THRESHOLD.get() ? RED_RAIN : finalTexture)
                    .orElse(texture);
        }
        RenderSystem.setShaderTexture(sampler, texture);
    }

    @Redirect(
            method = "renderSky(Lcom/mojang/blaze3d/vertex/PoseStack;Lorg/joml/Matrix4f;FLnet/minecraft/client/Camera;ZLjava/lang/Runnable;)V",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(ILnet/minecraft/resources/ResourceLocation;)V")
    )
    private void vivarium$modifySkyTextures(int sampler, ResourceLocation texture) {
        if (texture.getPath().equals("textures/environment/sun.png")) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
                int guilt = player.getCapability(GuiltProvider.PLAYER_GUILT).map(g -> g.getGuilt()).orElse(0);
                if (guilt >= VivariumConfig.ALT_SUN_THRESHOLD.get()) {
                    long time = player.level().getDayTime() % 24000;
                    texture = (time > 6000 && time < 18000) ? VIVARIUM_ALT_SUN : VIVARIUM_ALT_SUN_FLIPPED;
                }
            }
        }
        RenderSystem.setShaderTexture(sampler, texture);
    }
}