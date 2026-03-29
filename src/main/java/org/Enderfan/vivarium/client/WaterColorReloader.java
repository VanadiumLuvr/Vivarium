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
    // A globally accessible variable that the background chunk threads can safely read
    public static int currentClientGuilt = 0;
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
                    if (lastUpdatedGuilt == -1)
                    {
                        lastUpdatedGuilt = currentClientGuilt;
                    }

                    if (Math.abs(currentClientGuilt - lastUpdatedGuilt) >= 50)
                    {
                        lastUpdatedGuilt = currentClientGuilt;

                        // 1. Nuke the vanilla color memory so it forgets the old blue tint instantly
                        mc.level.clearTintCaches();

                        BlockPos pos = mc.player.blockPosition();

                        // 2. ONLY queue the chunks we can actually see right now.
                        // (Distant chunks will pull the correct static guilt variable natively when they load)
                        int renderDistance = mc.options.getEffectiveRenderDistance();
                        int radius = renderDistance * 16;

                        mc.levelRenderer.setBlocksDirty(
                                pos.getX() - radius, -64, pos.getZ() - radius,
                                pos.getX() + radius, 320, pos.getZ() + radius
                        );
                    }
                }
                else if (lastUpdatedGuilt != -1)
                {
                    lastUpdatedGuilt = -1;
                    mc.level.clearTintCaches();
                    mc.levelRenderer.allChanged();
                }
            });
        }
    }
}

// My name is Caine! I am your bitch.