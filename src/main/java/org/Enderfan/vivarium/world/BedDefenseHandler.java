package org.Enderfan.vivarium.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.Enderfan.vivarium.server.GuiltProvider;

@Mod.EventBusSubscriber(modid = "vivarium")
public class BedDefenseHandler
{
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END && event.player instanceof ServerPlayer player)
        {
            // every 5 minutes
            if (player.tickCount % 6000 == 0)
            {
                BlockPos spawnPos = player.getRespawnPosition();

                if (spawnPos != null && player.level().dimension() == player.getRespawnDimension())
                {
                    player.getCapability(GuiltProvider.PLAYER_GUILT).ifPresent(guilt ->
                    {
                        if (guilt.getGuilt() > 100)
                        {
                            Level level = player.level();
                            int radius = 15; // How far out the thorns can spawn

                            // Try 10 random spots in the radius.
                            // If it finds a valid one, it places the thorn and stops.
                            for (int i = 0; i < 10; i++)
                            {
                                int offsetX = player.getRandom().nextInt(radius * 2 + 1) - radius;
                                int offsetZ = player.getRandom().nextInt(radius * 2 + 1) - radius;
                                // Give a slight Y leeway in case their bedroom floor is uneven
                                int offsetY = player.getRandom().nextInt(3) - 1;

                                BlockPos targetPos = spawnPos.offset(offsetX, offsetY, offsetZ);
                                BlockState targetState = level.getBlockState(targetPos);
                                BlockState bushState = Blocks.SWEET_BERRY_BUSH.defaultBlockState().setValue(SweetBerryBushBlock.AGE, 3);

                                // Ask the engine directly: "Is this air, AND is a bush allowed to survive here?"
                                if (targetState.isAir() && bushState.canSurvive(level, targetPos))
                                {
                                    level.setBlock(targetPos, bushState, 3);
                                    break;
                                }
                            }
                        }
                    });
                }
            }
        }
    }
}