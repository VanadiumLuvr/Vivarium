package org.Enderfan.vivarium.events;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.Enderfan.vivarium.Vivarium;
import org.Enderfan.vivarium.config.VivariumConfig;
import org.Enderfan.vivarium.item.ModItems;
import org.Enderfan.vivarium.server.DreamFadePacket;
import org.Enderfan.vivarium.server.GuiltProvider;
import org.Enderfan.vivarium.server.ModMessages;

@Mod.EventBusSubscriber(modid = Vivarium.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DreamEvent {

    private static final String[] DREAM_MESSAGES = new String[]
            {
                    "You and The World cannot live in harmony. It simply isn't possible.",
                    "You can refrain from hurting Her, but ultimately you're only prolonging Her pain.",
                    "I'm not calling you a bad person; you just did what you had to in order to survive.",
                    "But if you want to end the cycle of torment, there is unfortunately only one option:",
                    "One of you must die.",
                    "You may travel to the World's Heart and put Her out of Her misery.",
                    "However, doing this will mean life will become unsustainable, and the resources you rely on will wither away.",
                    "Alternatively, you may take your own life, and let The World heal.",
                    "Of course, in Her kindness, She will bring you back, no matter how many times you die.",
                    "Unless you use this..."
            };

    @SubscribeEvent
    public static void onPlayerSleep(PlayerSleepInBedEvent event)
    {
        if (event.getEntity() instanceof ServerPlayer player && player.level() instanceof ServerLevel serverLevel)
        {

            player.getCapability(GuiltProvider.PLAYER_GUILT).ifPresent(cap ->
            {
                if (cap.getGuilt() >= VivariumConfig.DREAM_THRESHOLD.get() && !cap.hasDreamt())
                {
                    // 1. stop them from actually sleeping
                    event.setResult(Player.BedSleepingProblem.OTHER_PROBLEM);

                    // 2. mark the event as completed so they don't get trapped in a loop
                    cap.hasDreamt = true;

                    // 3. save their inventory to their persistent NBT, then wipe it
                    ListTag savedInv = new ListTag();
                    player.getInventory().save(savedInv);
                    player.getPersistentData().put("vivarium_saved_inv", savedInv);

                    // save their bed location so we know where to drop the sword later
                    player.getPersistentData().putLong("vivarium_bed_pos", event.getPos().asLong());

                    player.getPersistentData().putInt("vivarium_dream_start_z", player.getBlockZ());
                    player.getPersistentData().putInt("vivarium_dream_stage", 0);

                    player.getInventory().clearContent();

                    // 4. teleport them to the skybox (Y = 310)
                    BlockPos dreamStart = new BlockPos(player.getBlockX(), 310, player.getBlockZ());
                    player.teleportTo(dreamStart.getX() + 0.5, dreamStart.getY(), dreamStart.getZ() + 0.5);

                    // SAVE THE 3D POSITION HERE
                    player.getPersistentData().putLong("vivarium_dream_pos", dreamStart.asLong());

                    // sends the black fade packet
                    ModMessages.sendToPlayer(new DreamFadePacket(false), player);

                    // 5. generate the hallway
                    buildRibcageHallway(serverLevel, dreamStart);
                }
            });
        }
    }

    private static void buildRibcageHallway(ServerLevel level, BlockPos startPos)
    {
        int length = 160;

        // Expanded the loop to make the shell 2 blocks thick on all sides.
        // Interior remains X: -3 to 3, Y: 0 to 6, Z: 0 to length - 2
        for (int x = -5; x <= 5; x++)
        {
            for (int y = -2; y <= 8; y++)
            {
                // Z goes from -2 to length to give 2 blocks of thickness to the front and back walls
                for (int z = -2; z <= length; z++)
                {
                    BlockPos pos = startPos.offset(x, y, z);

                    // If it falls inside these bounds, it is empty walking space
                    boolean isInterior = (x >= -3 && x <= 3) && (y >= 0 && y <= 6) && (z >= 0 && z < length - 1);

                    if (isInterior)
                    {
                        level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                    }
                    else
                    {
                        // We are now generating the thick outer shell.

                        // Condition 1: The solid diorite spine on the ceiling
                        boolean isSpine = (x == 0 && y > 6 && z >= 0 && z < length - 1);

                        // Condition 2: The solid stone strip on the floor
                        boolean isFloorStrip = (x == 0 && y < 0 && z >= 0 && z < length - 1);

                        // Condition 3: The repeating ribs
                        boolean isRib = (z % 5 == 0 && z >= 0 && z < length - 1);

                        if (isSpine)
                        {
                            level.setBlockAndUpdate(pos, Blocks.DIORITE.defaultBlockState());
                        }
                        else if (isFloorStrip)
                        {
                            level.setBlockAndUpdate(pos, Blocks.STONE.defaultBlockState());
                        }
                        else if (isRib)
                        {
                            level.setBlockAndUpdate(pos, Blocks.DIORITE.defaultBlockState());
                        }
                        else
                        {
                            level.setBlockAndUpdate(pos, Blocks.STONE.defaultBlockState());
                        }
                    }
                }
            }
        }

        // The chest stays perfectly centered at x = 0 at the end of the hall
        BlockPos chestPos = startPos.offset(0, 0, length - 2);
        level.setBlockAndUpdate(chestPos, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.NORTH));

        if (level.getBlockEntity(chestPos) instanceof ChestBlockEntity chest)
        {
            chest.setItem(13, new ItemStack(ModItems.BLADE.get()));
        }
    }

    private static void destroyRibcageHallway(ServerLevel level, BlockPos startPos)
    {
        int length = 160;

        // Uses the exact same dimensions as the builder loop
        for (int x = -5; x <= 5; x++)
        {
            for (int y = -2; y <= 8; y++)
            {
                for (int z = -2; z <= length; z++)
                {
                    BlockPos pos = startPos.offset(x, y, z);
                    level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                }
            }
        }
    }

    @SubscribeEvent
    public static void onDreamSpawn(MobSpawnEvent.FinalizeSpawn event)
    {
        // Our dream sequence hardcodes the player at Y = 310.
        // If anything tries to spawn up here, we cancel it before it even exists.
        if (event.getY() >= 300)
        {
            event.setSpawnCancelled(true);
        }
    }

    @SubscribeEvent
    public static void onDreamBlockBreak(BlockEvent.BreakEvent event)
    {
        Player player = event.getPlayer();

        // if they have this tag, they are currently trapped in the skybox
        if (player.getPersistentData().contains("vivarium_saved_inv"))
        {
            // nice try, buddy.
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onDreamTick(TickEvent.PlayerTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END && event.player instanceof ServerPlayer player)
        {
            CompoundTag persistentData = player.getPersistentData();

            if (persistentData.contains("vivarium_saved_inv"))
            {
                //give them blindness that persists only for the duration of the dream
                event.player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.BLINDNESS, 40, 0,
                        true, false, false));

                // SAFELY stop music from the server using vanilla packets
                player.connection.send(new net.minecraft.network.protocol.game.ClientboundStopSoundPacket(null, SoundSource.MUSIC));
                player.connection.send(new net.minecraft.network.protocol.game.ClientboundStopSoundPacket(null, SoundSource.RECORDS));

                // --- THE EXPOSITION DUMP LOGIC ---
                int startZ = persistentData.getInt("vivarium_dream_start_z");
                int currentStage = persistentData.getInt("vivarium_dream_stage");

                int distanceWalked = player.getBlockZ() - startZ;

                if (distanceWalked >= (currentStage + 1) * 15 && currentStage < DREAM_MESSAGES.length)
                {
                    player.sendSystemMessage(Component.literal(DREAM_MESSAGES[currentStage]).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
                    persistentData.putInt("vivarium_dream_stage", currentStage + 1);
                }


                // --- THE WAKING UP LOGIC ---
                boolean hasBlade = player.getInventory().contains(new ItemStack(ModItems.BLADE.get()));

                if (hasBlade)
                {
                    ServerLevel serverLevel = (ServerLevel) player.level();

                    player.getInventory().clearContent();

                    ListTag savedInv = persistentData.getList("vivarium_saved_inv", 10);
                    player.getInventory().load(savedInv);

                    // ERASE THE HALLWAY FROM THE WORLD
                    BlockPos dreamStart = BlockPos.of(persistentData.getLong("vivarium_dream_pos"));
                    destroyRibcageHallway(serverLevel, dreamStart);

                    // Clean up NBT
                    persistentData.remove("vivarium_saved_inv");
                    persistentData.remove("vivarium_dream_start_z");
                    persistentData.remove("vivarium_dream_stage");
                    persistentData.remove("vivarium_dream_pos"); // remove the position tag too

                    BlockPos bedPos = BlockPos.of(persistentData.getLong("vivarium_bed_pos"));
                    player.teleportTo(bedPos.getX() + 0.5, bedPos.getY() + 1, bedPos.getZ() + 0.5);

                    ItemEntity bladeEntity = new ItemEntity(serverLevel, bedPos.getX() + 0.5, bedPos.getY() + 1.2, bedPos.getZ() + 0.5, new ItemStack(ModItems.BLADE.get()));
                    bladeEntity.setNoPickUpDelay();
                    serverLevel.addFreshEntity(bladeEntity);

                    ModMessages.sendToPlayer(new DreamFadePacket(true), player);
                }
            }
        }
    }
}
