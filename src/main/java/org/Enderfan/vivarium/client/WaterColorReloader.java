package org.Enderfan.vivarium.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.Enderfan.vivarium.config.VivariumConfig;
import org.Enderfan.vivarium.server.GuiltProvider;

@Mod.EventBusSubscriber(modid = "vivarium", value = Dist.CLIENT)
public class WaterColorReloader
{
    // Ensure background threads always see the main thread's updates
    public static volatile int currentClientGuilt = 0;
    private static int lastUpdatedGuilt = -1;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event)
    {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        if (mc.player.tickCount % 20 == 0)
        {
            mc.player.getCapability(GuiltProvider.PLAYER_GUILT).ifPresent(cap ->
            {
                currentClientGuilt = cap.getGuilt();
                int threshold = VivariumConfig.FOG_COLOR_MIN.get();

                if (currentClientGuilt > threshold)
                {
                    // Trigger if it's the first time crossing the threshold OR if guilt changed by 50+
                    if (lastUpdatedGuilt == -1 || Math.abs(currentClientGuilt - lastUpdatedGuilt) >= 50)
                    {
                        lastUpdatedGuilt = currentClientGuilt;
                        reloadWaterChunks(mc);
                    }
                }
                else if (lastUpdatedGuilt != -1)
                {
                    // Reset when falling back below the threshold
                    lastUpdatedGuilt = -1;
                    reloadWaterChunks(mc);
                }
            });
        }
    }

    private static void reloadWaterChunks(Minecraft mc)
    {
        // 1. Clear the main thread's tint cache
        mc.level.clearTintCaches();

        // 2. Queue the chunks in render distance for a smooth background rebuild
        BlockPos pos = mc.player.blockPosition();
        int renderDistance = mc.options.getEffectiveRenderDistance();
        int radius = renderDistance * 16;

        // Dynamically grab the world's build height to support custom dimensions
        int minY = mc.level.getMinBuildHeight();
        int maxY = mc.level.getMaxBuildHeight();

        mc.levelRenderer.setBlocksDirty(
                pos.getX() - radius, minY, pos.getZ() - radius,
                pos.getX() + radius, maxY, pos.getZ() + radius
        );
    }
}