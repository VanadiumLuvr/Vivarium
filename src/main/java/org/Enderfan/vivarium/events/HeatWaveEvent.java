package org.Enderfan.vivarium.events;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.Enderfan.vivarium.config.VivariumConfig;
import org.Enderfan.vivarium.server.GuiltProvider;
import org.Enderfan.vivarium.server.ModMessages;
import org.Enderfan.vivarium.server.packets.HeatSyncPacket;

@Mod.EventBusSubscriber(modid = "vivarium")
public class HeatWaveEvent
{
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;

        Level level = player.level();
        CompoundTag data = player.getPersistentData();

        player.getCapability(GuiltProvider.PLAYER_GUILT).ifPresent(guilt ->
        {
            if (guilt.getGuilt() >= VivariumConfig.HEAT_WAVE_THRESHOLD.get() && !data.contains("vivarium_heat_wave_done"))
            {
                if (!data.contains("vivarium_heat_wave_start"))
                {
                    data.putLong("vivarium_heat_wave_start", level.getGameTime());
                }

                long elapsed = level.getGameTime() - data.getLong("vivarium_heat_wave_start");

                if (elapsed >= 24000)
                {
                    data.putBoolean("vivarium_heat_wave_done", true);
                    data.remove("vivarium_heat_wave_start");
                    data.remove("vivarium_heat_level");
                    // Force the UI to instantly clear when the day is over
                    ModMessages.sendToPlayer(new HeatSyncPacket(0), player);
                }
                else
                {
                    if (!data.contains("vivarium_saved_inv"))
                    {
                        if (level.isDay() && level.canSeeSky(player.blockPosition()) && player.getArmorValue() > 0)
                        {
                            int heat = data.getInt("vivarium_heat_level");
                            int newHeat = Math.min(heat + 1, 400);

                            // If the heat changed, sync it to the UI!
                            if (heat != newHeat)
                            {
                                data.putInt("vivarium_heat_level", newHeat);
                                ModMessages.sendToPlayer(new HeatSyncPacket(newHeat), player);
                            }

                            if (newHeat >= 400 && player.tickCount % 60 == 0)
                            {
                                player.hurt(level.damageSources().onFire(), 1.0f);
                            }
                        }
                        else
                        {
                            // Notice we now pass the 'player' variable into the coolDown method
                            coolDown(data, player);
                        }
                    }
                }
            }
            else
            {
                coolDown(data, player);
            }
        });
    }

    private static void coolDown(CompoundTag data, ServerPlayer player)
    {
        if (data.contains("vivarium_heat_level"))
        {
            int heat = data.getInt("vivarium_heat_level");
            if (heat > 0)
            {
                int newHeat = Math.max(heat - 2, 0);
                data.putInt("vivarium_heat_level", newHeat);
                // Sync the cooling effect to the UI so it smoothly fades out
                ModMessages.sendToPlayer(new HeatSyncPacket(newHeat), player);
            }
        }
    }
}