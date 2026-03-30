package org.Enderfan.vivarium.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.*;

// TWEAK
@Mod.EventBusSubscriber(modid = "vivarium", value = Dist.CLIENT)
public class MetaStateManager
{
    private static final File CACHE_FILE = new File("config/vivarium_meta.txt");

    public static void saveLastGuilt(int guilt)
    {
        try (FileWriter writer = new FileWriter(CACHE_FILE))
        {
            writer.write(String.valueOf(guilt));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static int getLastGuilt()
    {
        if (!CACHE_FILE.exists()) return 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(CACHE_FILE)))
        {
            return Integer.parseInt(reader.readLine().trim());
        }
        catch (Exception e)
        {
            return 0;
        }
    }

    @SubscribeEvent
    public static void onLogOut(ClientPlayerNetworkEvent.LoggingOut event)
    {
        // When the player leaves the world, save their current guilt to the physical file
        saveLastGuilt(WaterColorReloader.currentClientGuilt);
    }
}