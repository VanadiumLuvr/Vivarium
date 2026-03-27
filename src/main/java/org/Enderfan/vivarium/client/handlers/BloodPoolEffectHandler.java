package org.Enderfan.vivarium.client.handlers;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.Enderfan.vivarium.ModSounds;
import org.Enderfan.vivarium.Vivarium;
import org.Enderfan.vivarium.entities.BloodPoolEntity;

@Mod.EventBusSubscriber(modid = Vivarium.MODID, value = Dist.CLIENT)
public class BloodPoolEffectHandler
{
    private static final Minecraft MC = Minecraft.getInstance();
    private static float effectIntensity = 0.0f;
    private static int lookTicks = 0;
    private static final int MAX_LOOK_TICKS = 100;  // ~5 sec
    private static final float MIN_FOV = 65.0f;
    private static SimpleSoundInstance droneSound = null;
    private static final ResourceLocation VIGNETTE_TEXTURE = new ResourceLocation("minecraft", "textures/misc/vignette.png");

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event)
    {
        if (event.phase != TickEvent.Phase.END || MC.player == null || MC.level == null)
        {
            return;
        }

        LocalPlayer player = MC.player;
        Vec3 eyePos = player.getEyePosition();
        Vec3 viewVec = player.getViewVector(1.0f).normalize();

        BloodPoolEntity nearest = null;
        double closestDistSq = Double.MAX_VALUE;
        AABB searchBox = player.getBoundingBox().inflate(8.0);

        for (BloodPoolEntity pool : MC.level.getEntitiesOfClass(BloodPoolEntity.class, searchBox))
        {
            Vec3 toPool = pool.position().subtract(eyePos).normalize();
            double distSq = pool.distanceToSqr(player);
            if (toPool.dot(viewVec) > 0.8f && distSq < closestDistSq)
            {
                nearest = pool;
                closestDistSq = distSq;
            }
        }

        if (nearest != null)
        {
            lookTicks = Math.min(lookTicks + 1, MAX_LOOK_TICKS);
        }
        else
        {
            lookTicks = Math.max(lookTicks - 2, 0);
        }

        float targetIntensity = (float) lookTicks / MAX_LOOK_TICKS;
        effectIntensity = Mth.lerp(0.05f, effectIntensity, targetIntensity);

        if (effectIntensity > 0.1f)
        {
            if (droneSound == null || !MC.getSoundManager().isActive(droneSound))
            {
                droneSound = new SimpleSoundInstance(
                        ModSounds.BLOOD_DRONE.get().getLocation(),
                        SoundSource.AMBIENT,
                        effectIntensity,
                        1,
                        MC.level.random,
                        true,
                        0,
                        SoundInstance.Attenuation.NONE,
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        true
                );
                MC.getSoundManager().play(droneSound);
            }
        }
        else if (droneSound != null)
        {
            MC.getSoundManager().stop(droneSound);
            droneSound = null;
        }
    }

    @SubscribeEvent
    public static void onComputeFov(ViewportEvent.ComputeFov event)
    {
        if (effectIntensity < 0.01f)
        {
            return;
        }
        float fov = (float) event.getFOV();
        event.setFOV(Mth.lerp(effectIntensity, fov, MIN_FOV));
    }

    @SubscribeEvent
    public static void onRenderVignette(RenderGuiOverlayEvent.Post event)
    {
        if (event.getOverlay().id().equals(VanillaGuiOverlay.VIGNETTE.id()))
        {
            if (effectIntensity <= 0.01f)
            {
                return;
            }

            int width = event.getWindow().getGuiScaledWidth();
            int height = event.getWindow().getGuiScaledHeight();
            GuiGraphics guiGraphics = event.getGuiGraphics();

            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();

            // still using mojang's weird inverse blend. dont touch it.
            // it's held together by thoughts and prayers.
            RenderSystem.blendFuncSeparate(
                    com.mojang.blaze3d.platform.GlStateManager.SourceFactor.ZERO,
                    com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR,
                    com.mojang.blaze3d.platform.GlStateManager.SourceFactor.ONE,
                    com.mojang.blaze3d.platform.GlStateManager.DestFactor.ZERO
            );

            guiGraphics.setColor(effectIntensity, effectIntensity, effectIntensity, 1.0f);

            // look at this mess. we have to tell it the destination size (width, height),
            // then tell it to start at 0,0 in the source and take the full 256x256,
            // and THEN tell it the file itself is 256x256.
            // if we leave out one number it'll probably just render a single pixel.
            guiGraphics.blit(VIGNETTE_TEXTURE, 0, 0, width, height, 0.0f, 0.0f, 256, 256, 256, 256);

            // put the toys back in the box before the engine notices we touched anything
            guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
        }
    }
}