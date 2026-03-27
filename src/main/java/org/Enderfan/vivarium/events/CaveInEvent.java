package org.Enderfan.vivarium.events;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.Enderfan.vivarium.Vivarium;
import org.Enderfan.vivarium.config.VivariumConfig;
import org.Enderfan.vivarium.server.GuiltProvider;
import org.Enderfan.vivarium.server.GuiltSyncPacket;
import org.Enderfan.vivarium.server.ModMessages;

@Mod.EventBusSubscriber(modid = Vivarium.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CaveInEvent {

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event)
    {
        if (event.getLevel().isClientSide()) return;
        Player player = event.getPlayer();
        BlockState state = event.getState();
        Level level = (Level) event.getLevel();
        boolean isUnderground = player.getY() < 50 && !level.canSeeSky(player.blockPosition());

        player.getCapability(GuiltProvider.PLAYER_GUILT).ifPresent(guilt ->
        {
            int startingGuilt = guilt.getGuilt();

            if (state.is(Blocks.STONE) || state.is(Blocks.DEEPSLATE))
            {
                int increment = (int) (VivariumConfig.GUILT_INC_STONE.get() + VivariumConfig.PACE.get());
                int threshold = VivariumConfig.STONE_THRESHOLD.get();
                boolean doTP = VivariumConfig.CAVE_IN_TELEPORT.get();

                guilt.addGuilt(increment);

                CompoundTag persistentData = player.getPersistentData();
                int stoneMined = persistentData.getInt("vivarium_stone_mined") + 1;
                persistentData.putInt("vivarium_stone_mined", stoneMined);


                // The Day 2 Underground Event: The Collapse
                if (stoneMined >= threshold && isUnderground && !persistentData.getBoolean("cave_in_triggered"))
                {
                    if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer)
                    {
                        // make it so this only triggers once
                        persistentData.putBoolean("cave_in_triggered", true);

                        // 1. The deafening crack
                        serverLevel.playSound(null, event.getPos(), net.minecraft.sounds.SoundEvents.WITHER_BREAK_BLOCK, SoundSource.BLOCKS, 2.0f, 0.5f);
                        TreeBleedEvent.treeBleed(level, event.getPos(), false);

                        // 2. Give them blindness so the visual chaos is even more disorienting
                        serverPlayer.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                net.minecraft.world.effect.MobEffects.BLINDNESS, 100, 0, true, false, false));

                        // 3. Trigger the falling ceiling
                        CaveInEvent.triggerCaveIn(serverLevel, player.blockPosition());

                        // 4. Start the 30-tick (1.5 second) timer for the seamless teleport
                        if(doTP)
                        {
                            persistentData.putInt("vivarium_collapse_timer", 30);
                        }
                    }
                }
            }

            if (guilt.getGuilt() > startingGuilt && player instanceof ServerPlayer serverPlayer)
            {
                ModMessages.sendToPlayer(new GuiltSyncPacket(guilt.getGuilt()), serverPlayer);
            }
        });
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        // 1. ALWAYS use event.side to check physical/logical sides in a TickEvent
        if (event.phase != TickEvent.Phase.END || event.side.isClient()) return;

        // 2. Now that the event has guaranteed we are on the server, this cast is 100% safe
        ServerPlayer player = (ServerPlayer) event.player;
        Level level = player.level();
        CompoundTag persistentData = player.getPersistentData();

        // --- THE COLLAPSE TELEPORT LOGIC ---
        if (persistentData.contains("vivarium_collapse_timer"))
        {
            int timer = persistentData.getInt("vivarium_collapse_timer");
            timer--;

            if (timer <= 0)
            {
                persistentData.remove("vivarium_collapse_timer");
                CaveInEvent.executeSeamlessTeleport(player, (ServerLevel) level);
            }
            else
            {
                persistentData.putInt("vivarium_collapse_timer", timer);
            }
        }
    }

    public static void triggerCaveIn(ServerLevel level, BlockPos playerPos)
    {
        int radius = 5;

        // Scan a 10x10 area above the player
        for (int x = -radius; x <= radius; x++)
        {
            for (int z = -radius; z <= radius; z++)
            {
                // Protect a 3x3 column directly over their head from falling
                if (Math.abs(x) <= 1 && Math.abs(z) <= 1)
                {
                    continue;
                }

                // Look up to 10 blocks above them for loose ceiling
                for (int y = 3; y <= 10; y++)
                {
                    BlockPos targetPos = playerPos.offset(x, y, z);
                    BlockState targetState = level.getBlockState(targetPos);
                    BlockState belowState = level.getBlockState(targetPos.below());

                    // If the block is solid stone/dirt and has air beneath it, make it fall
                    if (!targetState.isAir() && targetState.getDestroySpeed(level, targetPos) >= 0 && belowState.isAir())
                    {
                        net.minecraft.world.entity.item.FallingBlockEntity fallingBlock = net.minecraft.world.entity.item.FallingBlockEntity.fall(level, targetPos, targetState);
                        fallingBlock.time = 1; // Forces it to start falling immediately
                        level.addFreshEntity(fallingBlock);
                    }
                }
            }
        }
    }

    public static void executeSeamlessTeleport(ServerPlayer player, ServerLevel level)
    {
        BlockPos currentPos = player.blockPosition();

        // 5 chunks (80 blocks) away on the X and Z axes, straight down to Y = 20
        BlockPos newPos = new BlockPos(currentPos.getX() + 80, 20, currentPos.getZ() + 80);

        int radius = 2; // 5x5x5 area (center + 2 on each side)

        // Array to temporarily hold the block states of the rubble around them
        BlockState[][][] savedBox = new BlockState[5][5][5];

        // 1. Copy the chaotic, caved-in area immediately around the player
        for (int x = -radius; x <= radius; x++)
        {
            for (int y = -radius; y <= radius; y++)
            {
                for (int z = -radius; z <= radius; z++)
                {
                    savedBox[x + radius][y + radius][z + radius] = level.getBlockState(currentPos.offset(x, y, z));
                }
            }
        }

        // 2. Force the new chunk to load instantly if it somehow isn't already
        level.getChunk(newPos);

        // 3. Paste the chaotic rubble box into the new destination
        for (int x = -radius; x <= radius; x++)
        {
            for (int y = -radius; y <= radius; y++)
            {
                for (int z = -radius; z <= radius; z++)
                {
                    BlockPos targetOffset = newPos.offset(x, y, z);
                    BlockState stateToPaste = savedBox[x + radius][y + radius][z + radius];
                    if (stateToPaste.getDestroySpeed(level, targetOffset) >= 0 || stateToPaste.isAir())
                    {
                        level.setBlockAndUpdate(targetOffset, stateToPaste);
                    }
                }
            }
        }

        // TRIGGER THE BLACKOUT MASK
        org.Enderfan.vivarium.server.ModMessages.sendToPlayer(new org.Enderfan.vivarium.server.CaveInBlinkPacket(), player);

        // 4. Snap the player to the exact center of the new box, keeping their camera angle exactly the same
        player.teleportTo(newPos.getX() + 0.5, newPos.getY(), newPos.getZ() + 0.5);
    }

}
