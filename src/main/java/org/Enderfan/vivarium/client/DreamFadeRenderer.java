package org.Enderfan.vivarium.client;

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
public class DreamFadeRenderer
{
    private static float fadeAlpha = 0.0f;
    private static boolean isWhiteFade = false;

    public static void triggerFade(boolean white)
    {
        // 1.0 is completely opaque (blind)
        fadeAlpha = 1.0f;
        isWhiteFade = white;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END && fadeAlpha > 0.0f)
        {
            // Subtracting 0.015 per tick means the fade takes exactly 3.3 seconds to clear.
            // Change this number if you want it faster or slower.
            fadeAlpha -= 0.015f;
            if (fadeAlpha < 0.0f)
            {
                fadeAlpha = 0.0f;
            }
        }
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event)
    {
        // We hijack the CHAT_PANEL overlay because it is usually one of the very last
        // things rendered to the screen. This ensures our solid color covers the UI too.
        if (event.getOverlay().id().equals(VanillaGuiOverlay.CHAT_PANEL.id()))
        {
            if (fadeAlpha > 0.0f)
            {
                // Convert the 0.0-1.0 float into a 0-255 byte for the hex color code
                int alphaInt = (int) (fadeAlpha * 255.0f);
                alphaInt = Math.max(0, Math.min(255, alphaInt));

                // 0xFFFFFF is pure white, 0x000000 is pitch black
                int colorBase = isWhiteFade ? 0xFFFFFF : 0x000000;

                // Bitwise shift magic to combine the alpha channel with the color channel
                int finalColor = (alphaInt << 24) | colorBase;

                GuiGraphics guiGraphics = event.getGuiGraphics();
                int width = event.getWindow().getGuiScaledWidth();
                int height = event.getWindow().getGuiScaledHeight();

                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.disableDepthTest();

                // Draws the massive colored rectangle over everything
                guiGraphics.fill(0, 0, width, height, finalColor);

                RenderSystem.enableDepthTest();
                RenderSystem.disableBlend();
            }
        }
    }
}