package org.Enderfan.vivarium.events;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.Enderfan.vivarium.config.VivariumConfig;
import org.Enderfan.vivarium.server.ModMessages;
import org.Enderfan.vivarium.server.packets.TriggerCreditsPacket;
import org.Enderfan.vivarium.server.WorldHeartState;

@Mod.EventBusSubscriber(modid = "vivarium", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ApocalypseEvents
{
    // stops entities from naturally spawning
    @SubscribeEvent
    public static void onMobSpawn(MobSpawnEvent.FinalizeSpawn event)
    {
        ServerLevel serverLevel = event.getLevel().getLevel();

        if (WorldHeartState.get(serverLevel).isWorldDead())
        {
            event.setSpawnCancelled(true);
        }
    }

    // stops wheat, carrots, trees, etc from growing
    @SubscribeEvent
    public static void onCropGrow(BlockEvent.CropGrowEvent.Pre event)
    {
        if (event.getLevel() instanceof ServerLevel serverLevel)
        {
            if (WorldHeartState.get(serverLevel).isWorldDead())
            {
                event.setResult(Event.Result.DENY);
            }
        }
    }

    // the hardcore respawn block
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event)
    {
        if (event.getEntity().level() instanceof ServerLevel serverLevel)
        {
            if (WorldHeartState.get(serverLevel).isWorldDead())
            {
                // if they died (not just moving dimensions), force spectator mode
                if (event.isWasDeath())
                {
                    ServerPlayer player = (ServerPlayer) event.getEntity();
                    player.setGameMode(GameType.SPECTATOR);
                    if( VivariumConfig.DO_CREDITS.get()) credits(player);
                }
            }
        }
    }

    public static void credits(ServerPlayer player)
    {
        // Tell the client to handle its own audio and visuals
        ModMessages.sendToPlayer(new TriggerCreditsPacket(), player);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        // phase.END makes sure it only runs once per tick instead of twice.
        // here, event.player.level() returns a generic Level, so we DO need the instanceof check.
        if (event.phase == TickEvent.Phase.END && event.player.level() instanceof ServerLevel serverLevel)
        {
            ServerPlayer serverPlayer = (ServerPlayer) event.player;
            boolean isSpectator = serverPlayer.gameMode.getGameModeForPlayer() == GameType.SPECTATOR;

            if (WorldHeartState.get(serverLevel).isWorldDead() && !isSpectator) {
                if (serverPlayer.tickCount % 20 == 0)
                {
                    BlockPos pos = event.player.blockPosition();
                    int radius = 4; // how far out the rot spreads around them

                    // scan a box around the player
                    for (int x = -radius; x <= radius; x++) {
                        for (int y = -2; y <= 2; y++) // check slightly above and below their feet
                        {
                            for (int z = -radius; z <= radius; z++) {
                                BlockPos targetPos = pos.offset(x, y, z);

                                // is it grass?
                                if (serverLevel.getBlockState(targetPos).is(Blocks.GRASS_BLOCK)) {
                                    // 1% chance to decay per tick so it looks like it's spreading naturally
                                    // instead of just deleting the chunk instantly
                                    if (serverLevel.random.nextFloat() < 0.2f) {
                                        serverLevel.setBlockAndUpdate(targetPos, Blocks.DIRT.defaultBlockState());
                                    }
                                }

                                //destroys leaves and grass & shi
                                if (serverLevel.getBlockState(targetPos).is(BlockTags.LEAVES) ||
                                        serverLevel.getBlockState(targetPos).is(Blocks.TALL_GRASS) ||
                                        serverLevel.getBlockState(targetPos).is(Blocks.GRASS) ||
                                        serverLevel.getBlockState(targetPos).is(Blocks.FERN) ||
                                        serverLevel.getBlockState(targetPos).is(Blocks.LARGE_FERN) ||
                                        serverLevel.getBlockState(targetPos).is(Blocks.KELP_PLANT)) {
                                    if (serverLevel.random.nextFloat() < 0.2f) {
                                        serverLevel.setBlockAndUpdate(targetPos, Blocks.AIR.defaultBlockState());
                                    }
                                }

                                if (serverLevel.getBlockState(targetPos).is(BlockTags.FLOWERS)) {
                                    if (serverLevel.random.nextFloat() < 0.2f) {
                                        serverLevel.destroyBlock(targetPos, false);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
