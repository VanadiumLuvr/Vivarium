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

    public static void triggerRupture(ServerLevel level, ServerPlayer player)
    {
        int searchRadius = 60;
        int minRadiusSq = 30 * 30; // Keeps the explosion at least 30 blocks away from the player
        int maxRadiusSq = searchRadius * searchRadius;

        int highestY = Integer.MIN_VALUE;
        BlockPos center = null;

        // 1. Scan a donut-shaped area around the player to find the absolute highest natural peak
        for (int dx = -searchRadius; dx <= searchRadius; dx += 2)
        {
            for (int dz = -searchRadius; dz <= searchRadius; dz += 2)
            {
                int distSq = dx * dx + dz * dz;
                if (distSq >= minRadiusSq && distSq <= maxRadiusSq)
                {
                    int x = player.getBlockX() + dx;
                    int z = player.getBlockZ() + dz;

                    // Switch to NO_LEAVES so the invisible laser passes right through tree canopies
                    int y = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);

                    // Grab the actual solid block immediately beneath the air (y - 1)
                    BlockState surfaceBlock = level.getBlockState(new BlockPos(x, y - 1, z));

                    // Only accept the coordinate if the block belongs to a natural terrain category
                    boolean isNaturalTerrain = surfaceBlock.is(net.minecraft.tags.BlockTags.DIRT) ||
                            surfaceBlock.is(net.minecraft.tags.BlockTags.SAND) ||
                            surfaceBlock.is(net.minecraft.tags.BlockTags.BASE_STONE_OVERWORLD) ||
                            surfaceBlock.is(net.minecraft.tags.BlockTags.TERRACOTTA) ||
                            surfaceBlock.is(net.minecraft.world.level.block.Blocks.GRAVEL) ||
                            surfaceBlock.is(net.minecraft.world.level.block.Blocks.SNOW_BLOCK);

                    if (isNaturalTerrain && y > highestY)
                    {
                        highestY = y;
                        center = new BlockPos(x, y, z);
                    }
                }
            }
        }

        // Fallback safeguard in case they are standing in a void world or over an infinite ocean of glass
        if (center == null)
        {
            center = player.blockPosition().offset(40, 0, 40);
            center = new BlockPos(center.getX(), level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, center.getX(), center.getZ()), center.getZ());
        }

        // 2. Launch the Debris
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
                        double my = 1.0 + level.random.nextDouble() * 0.8;
                        double mz = (level.random.nextDouble() - 0.5) * 0.8;

                        flyingBlock.setDeltaMovement(mx, my, mz);
                        level.addFreshEntity(flyingBlock);
                    }
                }
            }
        }

        // ... Keep the debris launch code exactly the same ...
        // 2. Launch the Debris
        // ... [Your existing debris loop here] ...

        // 3. The Massive Explosion
        level.explode(null, center.getX() + 0.5, center.getY(), center.getZ() + 0.5, 7.0f, Level.ExplosionInteraction.MOB);

        // 4. The Submerged Plug (The Ramp)
        // We build a smaller, shallower cone that acts as a structural floor for the crater,
        // but it is mathematically capped below the lava line so it is completely swallowed.
        int ventRadius = 5;
        for (int dx = -ventRadius; dx <= ventRadius; dx++)
        {
            for (int dz = -ventRadius; dz <= ventRadius; dz++)
            {
                double distanceSq = dx * dx + dz * dz;
                if (distanceSq <= ventRadius * ventRadius)
                {
                    double distance = Math.sqrt(distanceSq);

                    // Center peaks at -1 (underground). Edges slope down gently to -3.
                    int topY = (int) (-1.0 - (distance * 0.4));

                    for (int yOffset = -8; yOffset <= topY; yOffset++)
                    {
                        BlockPos ventPos = center.offset(dx, yOffset, dz);

                        BlockState crust = level.random.nextBoolean() ?
                                net.minecraft.world.level.block.Blocks.MAGMA_BLOCK.defaultBlockState() :
                                net.minecraft.world.level.block.Blocks.COBBLESTONE.defaultBlockState();

                        level.setBlockAndUpdate(ventPos, crust);
                    }
                }
            }
        }

        // 5. The Overflowing Lava Vent
        // We drop a thick, solid cylinder of lava directly onto the submerged plug.
        // Because it sits at Y=0 and Y=1, it covers the cobble completely and breaches the crater lip.
        int lavaRadius = 3;
        for (int yOffset = -1; yOffset <= 1; yOffset++)
        {
            for (int dx = -lavaRadius; dx <= lavaRadius; dx++)
            {
                for (int dz = -lavaRadius; dz <= lavaRadius; dz++)
                {
                    if (dx * dx + dz * dz <= lavaRadius * lavaRadius)
                    {
                        BlockPos lavaPos = center.offset(dx, yOffset, dz);

                        // Only place lava in empty space so we don't accidentally overwrite our own rock plug
                        if (level.isEmptyBlock(lavaPos) || level.getBlockState(lavaPos).canBeReplaced())
                        {
                            level.setBlockAndUpdate(lavaPos, net.minecraft.world.level.block.Blocks.LAVA.defaultBlockState());
                        }
                    }
                }
            }
        }

        // 6. The Initial Smoke Burst
        for (int i = 0; i < 300; i++)
        {
            double px = center.getX() + (level.random.nextDouble() - 0.5) * 4.0;
            double py = center.getY() + level.random.nextDouble() * 10.0;
            double pz = center.getZ() + (level.random.nextDouble() - 0.5) * 4.0;

            level.sendParticles(net.minecraft.core.particles.ParticleTypes.CAMPFIRE_SIGNAL_SMOKE,
                    px, py, pz,
                    1, 0.2, 0.5, 0.2, 0.05);
        }

        // 7. Save the crater's coordinates to keep the smoke alive
        player.getPersistentData().putIntArray("vivarium_rupture_pos", new int[]{center.getX(), center.getY() + 3, center.getZ()});
    }
}
