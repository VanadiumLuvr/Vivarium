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

    // Ripple Effect Variables to prevent CPU stutter
    private static int rippleRadius = -1;
    private static int maxRippleRadius = 0;
    private static BlockPos rippleCenter = null;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event)
    {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        // 1. Process the expanding visual ripple if it is active
        // This runs every single frame, pushing the update wave out by 1 chunk per tick
        if (rippleRadius >= 0 && rippleCenter != null)
        {
            processRippleRing(mc);
        }

        // 2. Check guilt every 20 ticks (1 second)
        if (mc.player.tickCount % 20 == 0)
        {
            mc.player.getCapability(GuiltProvider.PLAYER_GUILT).ifPresent(cap ->
            {
                currentClientGuilt = cap.getGuilt();
                int threshold = VivariumConfig.FOG_COLOR_MIN.get();

                if (currentClientGuilt > threshold)
                {
                    if (lastUpdatedGuilt == -1 || Math.abs(currentClientGuilt - lastUpdatedGuilt) >= 50)
                    {
                        lastUpdatedGuilt = currentClientGuilt;
                        startRipple(mc);
                    }
                }
                else if (lastUpdatedGuilt != -1)
                {
                    lastUpdatedGuilt = -1;
                    startRipple(mc);
                }
            });
        }
    }

    private static void startRipple(Minecraft mc)
    {
        // Instantly dump the old blue color from memory
        mc.level.clearTintCaches();

        // Lock in the epicenter of the guilt spike
        rippleCenter = mc.player.blockPosition();
        rippleRadius = 0;

        // Grab the exact edge of their vision
        maxRippleRadius = mc.options.getEffectiveRenderDistance();
    }

    private static void processRippleRing(Minecraft mc)
    {
        int cx = rippleCenter.getX() >> 4;
        int cz = rippleCenter.getZ() >> 4;
        int minY = mc.level.getMinBuildHeight();
        int maxY = mc.level.getMaxBuildHeight();

        // ONLY mark the chunks sitting exactly on the perimeter of the current radius
        for (int x = -rippleRadius; x <= rippleRadius; x++)
        {
            for (int z = -rippleRadius; z <= rippleRadius; z++)
            {
                if (Math.abs(x) == rippleRadius || Math.abs(z) == rippleRadius)
                {
                    int blockX = (cx + x) << 4;
                    int blockZ = (cz + z) << 4;

                    mc.levelRenderer.setBlocksDirty(
                            blockX, minY, blockZ,
                            blockX + 15, maxY, blockZ + 15
                    );
                }
            }
        }

        // Expand the ring outward for the next tick
        rippleRadius++;

        // Stop the engine once we reach the edge of the loaded world
        if (rippleRadius > maxRippleRadius)
        {
            rippleRadius = -1;
        }
    }
}