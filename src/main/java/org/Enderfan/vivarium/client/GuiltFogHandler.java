package org.Enderfan.vivarium.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.Enderfan.vivarium.Vivarium;
import org.Enderfan.vivarium.config.VivariumConfig;
import org.Enderfan.vivarium.server.GuiltProvider;
import org.Enderfan.vivarium.server.PlayerGuilt;

@Mod.EventBusSubscriber(modid = Vivarium.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class GuiltFogHandler
{
    @SubscribeEvent
    public static void onFogColor(ViewportEvent.ComputeFogColor event)
    {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // Reach into the player's capability backpack
        mc.player.getCapability(GuiltProvider.PLAYER_GUILT).ifPresent(cap ->
        {
            int guilt = cap.getGuilt();

            if (guilt > VivariumConfig.FOG_COLOR_THRESHOLD.get())
            {
                float factor = Math.min(1.0f, (guilt - 500) / 1000.0f);

                float targetR = 0.6f;
                float targetG = 0.0f;
                float targetB = 0.0f;

                float newR = event.getRed() + (targetR - event.getRed()) * factor;
                float newG = event.getGreen() + (targetG - event.getGreen()) * factor;
                float newB = event.getBlue() + (targetB - event.getBlue()) * factor;

                event.setRed(newR);
                event.setGreen(newG);
                event.setBlue(newB);
            }
        });
    }

    @SubscribeEvent
    public static void onFogDensity(ViewportEvent.RenderFog event)
    {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        mc.player.getCapability(GuiltProvider.PLAYER_GUILT).ifPresent(cap ->
        {
            int guilt = cap.getGuilt();

            if (guilt > 0) // Changed from 800 to 0 so the fade never snaps
            {
                // Math.max ensures the factor never goes negative if guilt drops under 800
                float factor = Math.max(0.0f, Math.min(1.0f, (float) (guilt - VivariumConfig.FOG_THICKNESS_MIN.get()) / VivariumConfig.FOG_THICKNESS_MAX.get()));

                float vanillaNear = event.getNearPlaneDistance();
                float vanillaFar = event.getFarPlaneDistance();

                float targetNear = 0.0f;
                float targetFar = 52.0f;

                float newNear = vanillaNear + (targetNear - vanillaNear) * factor;
                float newFar = vanillaFar + (targetFar - vanillaFar) * factor;

                event.setNearPlaneDistance(newNear);
                event.setFarPlaneDistance(newFar);

                // Keeps spectator fog disabled until the very end of the healing sequence
                event.setCanceled(true);
            }
        });
    }
}