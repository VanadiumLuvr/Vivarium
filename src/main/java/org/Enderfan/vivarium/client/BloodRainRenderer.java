package org.Enderfan.vivarium.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.resources.ResourceLocation;
import org.Enderfan.vivarium.server.GuiltProvider;
import org.Enderfan.vivarium.Vivarium;
import org.Enderfan.vivarium.mixins.MixinLevelRenderer;

public class BloodRainRenderer
{
    private static final ResourceLocation RED_RAIN_LOCATION = new ResourceLocation(Vivarium.MODID, "textures/environment/red_rain.png");

    public static void render(ClientLevel level, int ticks, float partialTick, LightTexture lightTexture, double camX, double camY, double camZ)
    {
        float rainLevel = level.getRainLevel(partialTick);
        if (rainLevel <= 0.0F) return;

        // 1. Check the local player's guilt
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        player.getCapability(GuiltProvider.PLAYER_GUILT).ifPresent(guilt ->
        {
            // Only use red rain if guilt is high (e.g., 50+)
            ResourceLocation texture = (guilt.getGuilt() >= 50) ? RED_RAIN_LOCATION : MixinLevelRenderer.RAIN_LOCATION;

            // 2. Insert Vanilla Rain Rendering Logic here
            // (You must copy the loop from WorldRenderer#renderSnowAndRain and replace the texture)
            RenderSystem.setShaderTexture(0, texture);
            // ... [Vanilla rendering buffers and loops] ...
        });
    }
}
