package org.Enderfan.vivarium.client.handlers;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "vivarium", value = Dist.CLIENT)
public class HeatWaveOverlay
{
    private static final ResourceLocation SUN_HEART = new ResourceLocation("vivarium", "textures/gui/sun_heart.png");
    private static final ResourceLocation ARMOR_FIRE = new ResourceLocation("vivarium", "textures/gui/armor_fire.png");

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Post event)
    {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        CompoundTag data = mc.player.getPersistentData();
        if (!data.contains("vivarium_heat_level")) return;

        int heat = data.getInt("vivarium_heat_level");
        if (heat <= 0) return;

        GuiGraphics graphics = event.getGuiGraphics();
        int width = event.getWindow().getGuiScaledWidth();
        int height = event.getWindow().getGuiScaledHeight();

        RenderSystem.enableBlend();

        // 1. Paint the Sun Hearts accurately with a smooth fade
        if (event.getOverlay().id().equals(VanillaGuiOverlay.PLAYER_HEALTH.id()))
        {
            // Calculate the fade based on how close they are to taking damage
            float alpha = Math.min(1.0f, heat / 400.0f);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);

            int healthY = height - 39;
            int left = width / 2 - 91;

            int health = Mth.ceil(mc.player.getHealth());
            int maxHealth = Mth.ceil(mc.player.getMaxHealth());

            for (int i = 0; i < maxHealth / 2; i++)
            {
                int heartValue = i * 2;

                if (heartValue >= health) continue;

                int row = i / 10;
                int x = left + (i % 10) * 8;
                int y = healthY - (row * 10);

                if (health <= 4 && mc.player.tickCount % 20 < 10)
                {
                    y += mc.player.getRandom().nextInt(2);
                }

                if (heartValue + 1 == health)
                {
                    graphics.blit(SUN_HEART, x, y, 0, 0, 5, 9, 9, 9);
                }
                else
                {
                    graphics.blit(SUN_HEART, x, y, 0, 0, 9, 9, 9, 9);
                }
            }
        }

        // 2. Paint the Animated Armor Fire at 100% opacity instantly
        if (event.getOverlay().id().equals(VanillaGuiOverlay.ARMOR_LEVEL.id()))
        {
            // Snap the opacity to full right before rendering the fire
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

            int armor = mc.player.getArmorValue();
            if (armor > 0)
            {
                int healthRows = (int) Math.ceil(mc.player.getMaxHealth() / 20.0f);
                int armorY = height - 39 - (10 * healthRows);
                int left = width / 2 - 91;

                int frameOffset = (mc.player.tickCount % 10 < 5) ? 0 : 9;

                for (int i = 0; i < armor / 2; i++)
                {
                    int x = left + i * 8;
                    graphics.blit(ARMOR_FIRE, x, armorY, 0, frameOffset, 9, 9, 9, 18);
                }
            }
        }

        // Clean up and restore vanilla rendering color
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
    }
}