package org.Enderfan.vivarium.client.renderers;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.Enderfan.vivarium.Vivarium;

@Mod.EventBusSubscriber(modid = Vivarium.MODID, value = Dist.CLIENT)
public class BlinkOverlayRenderer
{
    private static int blinkTimer = 0;

    public static void triggerBlink()
    {
        blinkTimer = 40; // 2 seconds total (1 second solid, 1 second fade)
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END && blinkTimer > 0)
        {
            blinkTimer--;
        }
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event)
    {
        // Render over the vignette so it covers everything
        if (blinkTimer > 0 && event.getOverlay().id().equals(VanillaGuiOverlay.VIGNETTE.id()))
        {
            GuiGraphics guiGraphics = event.getGuiGraphics();
            int width = event.getWindow().getGuiScaledWidth();
            int height = event.getWindow().getGuiScaledHeight();

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            // Hold solid black for the first 20 ticks, then fade out over the last 20 ticks
            float alpha = blinkTimer > 20 ? 1.0f : blinkTimer / 20.0f;

            // Convert alpha float (0.0 - 1.0) to an ARGB integer
            int alphaInt = (int)(alpha * 255.0f);
            int color = (alphaInt << 24) | 0x000000; // Black with calculated opacity

            guiGraphics.fill(0, 0, width, height, color);

            RenderSystem.disableBlend();
        }
    }
}