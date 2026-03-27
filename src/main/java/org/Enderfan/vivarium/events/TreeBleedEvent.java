package org.Enderfan.vivarium.events;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.Enderfan.vivarium.Vivarium;
import org.Enderfan.vivarium.config.VivariumConfig;
import org.Enderfan.vivarium.entities.BloodPoolEntity;
import org.Enderfan.vivarium.entities.ModEntities;
import org.Enderfan.vivarium.particles.ModParticles;
import org.Enderfan.vivarium.server.GuiltProvider;
import org.Enderfan.vivarium.server.GuiltSyncPacket;
import org.Enderfan.vivarium.server.ModMessages;

import static org.Enderfan.vivarium.ModSounds.GORE1;

@Mod.EventBusSubscriber(modid = Vivarium.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TreeBleedEvent
{
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event)
    {
        if (event.getLevel().isClientSide()) return;

        Player player = event.getPlayer();
        BlockState state = event.getState();
        Level level = (Level) event.getLevel();

        player.getCapability(GuiltProvider.PLAYER_GUILT).ifPresent(guilt ->
        {
            int startingGuilt = guilt.getGuilt();

            if (state.is(BlockTags.LOGS))
            {
                guilt.addGuilt(VivariumConfig.GUILT_INC_LOG.get());
                guilt.incrementLogsBroken();

                if (guilt.getLogsBroken() >= (VivariumConfig.LOG_THRESHOLD.get() / VivariumConfig.PACE.get()) && !guilt.hasTriggeredFirstBleed())
                {
                    treeBleed(level, event.getPos(), true);

                    // give them mining fatigue IV for about 10 seconds.
                    // 200 ticks = 10s. level 3 is technically Fatigue IV because computers start at 0.
                    if (player instanceof ServerPlayer serverPlayer) {
                        serverPlayer.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                net.minecraft.world.effect.MobEffects.DIG_SLOWDOWN, 200, 3,
                                true, false
                        ));
                    }

                    for (int i = 1; i < 5; i++) {
                        BlockPos above = event.getPos().above(i);
                        if (level.getBlockState(above).is(BlockTags.LOGS)) {
                            if (level instanceof ServerLevel serverLevel) {
                                serverLevel.sendParticles(ModParticles.BLOOD_DRIP.get(),
                                        above.getX() + 0.5, above.getY() + 0.5, above.getZ() + 0.5,
                                        20, 0.3, 0.3, 0.3, 0.1);
                            }
                        }
                    }

                    guilt.setTriggeredFirstBleed(true);
                }
            }
            else if (guilt.hasTriggeredFirstBleed())
            {
                // A 1% chance on every block broken thereafter to bleed without the pool
                if (level.random.nextFloat() < 0.01f)
                {
                    treeBleed(level, event.getPos(), false);
                }
            }

            if (guilt.getGuilt() > startingGuilt && player instanceof ServerPlayer serverPlayer)
            {
                ModMessages.sendToPlayer(new GuiltSyncPacket(guilt.getGuilt()), serverPlayer);
            }
        });
    }

    public static void treeBleed(Level level, BlockPos pos, boolean doPool)
    {
        // this handles the heavy lifting: sound, heavy particles, and ONE entity.
        level.playSound(null, pos, GORE1.get(), SoundSource.BLOCKS, 0.8f, 0.7f + level.random.nextFloat() * 0.2f);

        if (level instanceof ServerLevel serverLevel)
        {
            // heavy burst for the main break
            serverLevel.sendParticles(ModParticles.BLOOD_DRIP.get(),
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    100, 0.2, 0.2, 0.2, 0.1);

            // find the ground for the single pool entity
            BlockPos groundPos = pos.immutable();
            int searchLimit = 20;
            while (searchLimit > 0 && groundPos.getY() > level.getMinBuildHeight())
            {
                BlockState stateBelow = level.getBlockState(groundPos.below());
                if (stateBelow.isAir() || stateBelow.is(BlockTags.LOGS))
                {
                    groundPos = groundPos.below();
                    searchLimit--;
                }
                else break;
            }

            if(doPool)
            {
                // spawn the pool. just one. i'm not cleaning up a hundred of these.
                BloodPoolEntity entity = new BloodPoolEntity(ModEntities.BLOOD_POOL.get(), level);
                entity.moveTo(groundPos.getX() + 0.5, groundPos.getY() + 0.0625, groundPos.getZ() + 0.5, 0, 0);
                entity.setOriginY((float) pos.getY());

                level.addFreshEntity(entity);
            }
        }
    }
}
