package org.Enderfan.vivarium.events;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.Enderfan.vivarium.Vivarium;
import org.Enderfan.vivarium.config.VivariumConfig;
import org.Enderfan.vivarium.server.GuiltProvider;

@Mod.EventBusSubscriber(modid = Vivarium.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class VolcanoEvent
{
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;

        ServerPlayer player = (ServerPlayer) event.player;
        Level level = player.level();
        CompoundTag persistentData = player.getPersistentData();

        player.getCapability(GuiltProvider.PLAYER_GUILT).ifPresent(guilt ->
        {
            // The 2500 Guilt Milestone: The Rupture
            if (guilt.getGuilt() >= VivariumConfig.VOLCANO_THRESHOLD.get() && !persistentData.getBoolean("vivarium_rupture_triggered")
                    && level.canSeeSky(player.blockPosition()))
            {
                if (level instanceof ServerLevel serverLevel)
                {
                    persistentData.putBoolean("vivarium_rupture_triggered", true);
                    triggerRupture(serverLevel, player);
                }
            }
        });

        // --- The Persistent Rupture Smoke ---
        if (persistentData.contains("vivarium_rupture_pos"))
        {
            int[] pos = persistentData.getIntArray("vivarium_rupture_pos");

            if (player.distanceToSqr(pos[0], pos[1], pos[2]) < 22500)
            {
                if (level instanceof ServerLevel serverLevel)
                {
                    // Swapped to SIGNAL_SMOKE, tighter spread (0.1 on X and Z), slightly faster upward drift
                    serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.CAMPFIRE_SIGNAL_SMOKE,
                            pos[0] + 0.5,
                            pos[1] + 2.0, // Start slightly above the lava
                            pos[2] + 0.5,
                            4, // Spawn 4 particles per tick
                            0.01, 1, 0.01, // Tight X/Z spread to keep it pillared
                            0.02);
                }
            }
        }
    }

    private static void triggerRupture(ServerLevel level, ServerPlayer player)
    {
        double distance = 50.0 + level.random.nextDouble() * 10.0;
        double angle = level.random.nextDouble() * Math.PI * 2;

        int x = net.minecraft.util.Mth.floor(player.getX() + Math.cos(angle) * distance);
        int z = net.minecraft.util.Mth.floor(player.getZ() + Math.sin(angle) * distance);

        int y = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, x, z);
        BlockPos center = new BlockPos(x, y, z);

        // 1. Launch the Debris (Expanded to a 5-block radius circle)
        int debrisRadius = 5;
        for (int dx = -debrisRadius; dx <= debrisRadius; dx++)
        {
            for (int dz = -debrisRadius; dz <= debrisRadius; dz++)
            {
                if (dx * dx + dz * dz <= debrisRadius * debrisRadius)
                {
                    BlockPos targetPos = center.offset(dx, -1, dz);
                    BlockState state = level.getBlockState(targetPos);

                    if (!state.isAir() && state.getDestroySpeed(level, targetPos) >= 0)
                    {
                        net.minecraft.world.entity.item.FallingBlockEntity flyingBlock = net.minecraft.world.entity.item.FallingBlockEntity.fall(level, targetPos, state);
                        flyingBlock.time = 1;

                        double mx = (level.random.nextDouble() - 0.5) * 0.8;
                        double my = 1.0 + level.random.nextDouble() * 0.8; // Shoots much higher
                        double mz = (level.random.nextDouble() - 0.5) * 0.8;

                        flyingBlock.setDeltaMovement(mx, my, mz);
                        level.addFreshEntity(flyingBlock);
                    }
                }
            }
        }

        // 2. The Massive Explosion (7.0f is massive)
        level.explode(null, center.getX() + 0.5, center.getY(), center.getZ() + 0.5, 7.0f, Level.ExplosionInteraction.MOB);

        // 3. The Boiling Lava Lake (Expanded to a 4-block radius circle, 3 blocks deep)
        int lavaRadius = 4;
        for (int yOffset = -4; yOffset <= -2; yOffset++)
        {
            for (int dx = -lavaRadius; dx <= lavaRadius; dx++)
            {
                for (int dz = -lavaRadius; dz <= lavaRadius; dz++)
                {
                    if (dx * dx + dz * dz <= lavaRadius * lavaRadius)
                    {
                        BlockPos lavaPos = center.offset(dx, yOffset, dz);
                        level.setBlockAndUpdate(lavaPos, net.minecraft.world.level.block.Blocks.LAVA.defaultBlockState());
                    }
                }
            }
        }

        // 4. The Initial Smoke Burst (Swapped to SIGNAL_SMOKE)
        for (int i = 0; i < 300; i++)
        {
            double px = center.getX() + (level.random.nextDouble() - 0.5) * 4.0;
            double py = center.getY() + level.random.nextDouble() * 10.0;
            double pz = center.getZ() + (level.random.nextDouble() - 0.5) * 4.0;

            level.sendParticles(net.minecraft.core.particles.ParticleTypes.CAMPFIRE_SIGNAL_SMOKE,
                    px, py, pz,
                    1, 0.2, 0.5, 0.2, 0.05);
        }

        // 5. Save the crater's coordinates to keep the smoke alive
        player.getPersistentData().putIntArray("vivarium_rupture_pos", new int[]{center.getX(), center.getY() - 2, center.getZ()});
    }
}
