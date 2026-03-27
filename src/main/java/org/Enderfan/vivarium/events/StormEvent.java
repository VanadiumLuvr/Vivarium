package org.Enderfan.vivarium.events;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.Enderfan.vivarium.Vivarium;
import org.Enderfan.vivarium.config.VivariumConfig;
import org.Enderfan.vivarium.server.GuiltProvider;

@Mod.EventBusSubscriber(modid = Vivarium.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class StormEvent
{
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide)
        {
            return;
        }

        ServerPlayer player = (ServerPlayer) event.player;
        Level level = player.level();
        CompoundTag persistentData = player.getPersistentData();

        // Check weather triggers once a second to save performance
        if (player.tickCount % 20 == 0)
        {
            player.getCapability(GuiltProvider.PLAYER_GUILT).ifPresent(guilt ->
            {
                int currentGuilt = guilt.getGuilt();

                // 1. The 2000 Guilt Milestone: The Wrath
                if (currentGuilt >= VivariumConfig.STORM_THRESHOLD.get() && !persistentData.getBoolean("vivarium_storm_triggered"))
                {
                    if (level instanceof ServerLevel serverLevel)
                    {
                        // setWeatherParameters(clearTime, rainTime, isRaining, isThundering)
                        // 24000 ticks = 20 minutes of relentless thunderstorm
                        serverLevel.setWeatherParameters(0, 24000, true, true);
                        persistentData.putBoolean("vivarium_storm_triggered", true);
                    }
                }

                // 2. The Barrage: Custom Lightning Frequency
                // If it is currently thundering and they have crossed the threshold, we call down the strikes manually.
                if (currentGuilt >= VivariumConfig.STORM_THRESHOLD.get() && level.isThundering() && level instanceof ServerLevel serverLevel)
                {
                    // 10% chance every second to spawn a lightning strike near the player.
                    // This is vastly more frequent than vanilla Minecraft.
                    if (player.getRandom().nextFloat() < 0.10f)
                    {
                        forceLightningStrike(serverLevel, player);
                    }
                }
            });
        }
    }

    private static void forceLightningStrike(ServerLevel level, ServerPlayer player)
    {
        // Pick a random distance between 15 and 45 blocks away
        double distance = 15.0 + level.random.nextDouble() * 30.0;
        double angle = level.random.nextDouble() * Math.PI * 2;

        int x = net.minecraft.util.Mth.floor(player.getX() + Math.cos(angle) * distance);
        int z = net.minecraft.util.Mth.floor(player.getZ() + Math.sin(angle) * distance);

        // Find the highest block exposed to the sky
        int y = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, x, z);
        BlockPos strikePos = new BlockPos(x, y, z);

        // Only strike if the block can actually see the sky (we don't want lightning spawning inside caves)
        if (level.canSeeSky(strikePos))
        {
            net.minecraft.world.entity.LightningBolt lightning = net.minecraft.world.entity.EntityType.LIGHTNING_BOLT.create(level);

            if (lightning != null)
            {
                lightning.moveTo(net.minecraft.world.phys.Vec3.atBottomCenterOf(strikePos));

                // If you want the lightning to be purely visual and NOT set the forest on fire,
                // uncomment the following line:
                // lightning.setVisualOnly(true);

                level.addFreshEntity(lightning);
            }
        }
    }
}
