package org.Enderfan.vivarium.events;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import org.Enderfan.vivarium.ModSounds;
import org.Enderfan.vivarium.Vivarium;
import org.Enderfan.vivarium.config.VivariumConfig;
import org.Enderfan.vivarium.item.ModItems;
import org.Enderfan.vivarium.particles.ModParticles;
import org.Enderfan.vivarium.server.*;
import org.Enderfan.vivarium.server.packets.BloodArrowPacket;
import org.Enderfan.vivarium.server.packets.GuiltSyncPacket;
import org.Enderfan.vivarium.server.packets.StrikeHeartPacket;

import static org.Enderfan.vivarium.ModSounds.GORE1;
import static org.Enderfan.vivarium.Vivarium.INSTANCE;
import static org.Enderfan.vivarium.Vivarium.PLAYER_GUILT;

@Mod.EventBusSubscriber(modid = Vivarium.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class Events
{

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event)
    {
        event.getDispatcher().register(Commands.literal("guilt")
                .requires(source -> source.hasPermission(2)) // Requires cheats (level 2)
                .then(Commands.literal("get")
                        .then(Commands.argument("target", EntityArgument.player())
                                .executes(context ->
                                {
                                    return getGuilt(context.getSource(), EntityArgument.getPlayer(context, "target"));
                                })))
                .then(Commands.literal("set")
                        .then(Commands.argument("target", EntityArgument.player())
                                .then(Commands.argument("value", IntegerArgumentType.integer(0))
                                        .executes(context ->
                                        {
                                            return setGuilt(context.getSource(), EntityArgument.getPlayer(context, "target"), IntegerArgumentType.getInteger(context, "value"));
                                        }))))
        );

        event.getDispatcher().register(Commands.literal("vivarium")
                .requires(source -> source.hasPermission(2)) // Requires OP / Cheats

                .then(Commands.literal("forceTreeBleed")
                        .executes(context ->
                        {
                            ServerPlayer player = context.getSource().getPlayerOrException();

                            player.getCapability(GuiltProvider.PLAYER_GUILT).ifPresent(cap ->
                            {
                                cap.setLogsBroken(VivariumConfig.LOG_THRESHOLD.get());
                                cap.setTriggeredFirstBleed(false);
                            });

                            context.getSource().sendSuccess(() -> Component.literal("Primed Tree Bleed. Break 1 more log."), true);
                            return 1;
                        }))

                .then(Commands.literal("forceCaveIn")
                        .executes(context ->
                        {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            ServerLevel level = context.getSource().getLevel();

                            // Manually simulate the cinematic triggers
                            level.playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.WITHER_BREAK_BLOCK, SoundSource.BLOCKS, 2.0f, 0.5f);
                            TreeBleedEvent.treeBleed(level, player.blockPosition(), false);
                            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.BLINDNESS, 100, 0, true, false, false));

                            CaveInEvent.triggerCaveIn(level, player.blockPosition());
                            player.getPersistentData().putInt("vivarium_collapse_timer", 30);

                            context.getSource().sendSuccess(() -> Component.literal("Forced Cinematic Cave In"), true);
                            return 1;
                        }))

                .then(Commands.literal("forceEruption")
                        .executes(context ->
                        {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            VolcanoEvent.triggerRupture(context.getSource().getLevel(), player);
                            context.getSource().sendSuccess(() -> Component.literal("Forced Volcano Eruption"), true);
                            return 1;
                        }))

                .then(Commands.literal("forceDream")
                        .executes(context ->
                        {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            // Uses their current standing position as the "bed" to return to
                            DreamEvent.forceDreamSequence(player, player.blockPosition());
                            context.getSource().sendSuccess(() -> Component.literal("Forced Dream Sequence"), true);
                            return 1;
                        }))

                .then(Commands.literal("worldHeartState")
                        .then(Commands.literal("alive")
                                .executes(context ->
                                {
                                    WorldHeartState.get(context.getSource().getLevel()).setWorldDead(false);
                                    context.getSource().sendSuccess(() -> Component.literal("World Heart is now ALIVE"), true);
                                    return 1;
                                }))
                        .then(Commands.literal("dead")
                                .executes(context ->
                                {
                                    WorldHeartState.get(context.getSource().getLevel()).setWorldDead(true);
                                    context.getSource().sendSuccess(() -> Component.literal("World Heart is now DEAD"), true);
                                    return 1;
                                })))
        );
    }

    private static int getGuilt(CommandSourceStack source, Player player)
    {
        player.getCapability(PLAYER_GUILT).ifPresent(guilt ->
        {
            // Parameter 'false' prevents the message from being sent to all admins/logs
            source.sendSuccess(() -> Component.literal(player.getName().getString() + " has " + guilt.getGuilt() + " guilt."), false);
        });

        return 1;
    }

    private static int setGuilt(CommandSourceStack source, Player player, int value)
    {
        player.getCapability(PLAYER_GUILT).ifPresent(guilt ->
        {
            guilt.setGuilt(value);
            // Parameter 'true' means it will be logged and shown to admins
            source.sendSuccess(() -> Component.literal("Set guilt of " + player.getName().getString() + " to " + value), true);
            INSTANCE.send(PacketDistributor.PLAYER.with(() -> player.createCommandSourceStack().getPlayer()), new GuiltSyncPacket(value));
        });

        return 1;
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event)
    {
        if (event.getEntity() instanceof ServerPlayer player)
        {
            player.getCapability(GuiltProvider.PLAYER_GUILT).ifPresent(guilt ->
            {
                ModMessages.sendToPlayer(new GuiltSyncPacket(guilt.getGuilt()), player);
            });
        }
    }

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getObject() instanceof Player)
        {
            event.addCapability(new ResourceLocation(Vivarium.MODID, "guilt"), new GuiltProvider());
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(net.minecraftforge.event.entity.player.PlayerEvent.Clone event)
    {
        // 1. Resuscitate the dead body's capabilities so we can read them
        event.getOriginal().reviveCaps();

        event.getOriginal().getCapability(GuiltProvider.PLAYER_GUILT).ifPresent(oldCap ->
        {
            event.getEntity().getCapability(GuiltProvider.PLAYER_GUILT).ifPresent(newCap ->
            {
                newCap.setGuilt(oldCap.getGuilt());

                for(int i = 0; i < oldCap.getLogsBroken(); i++)
                {
                    newCap.incrementLogsBroken();
                }

                newCap.setTriggeredFirstBleed(oldCap.hasTriggeredFirstBleed());

                newCap.hasDreamt(oldCap.hasDreamt());
            });
        });

        // 2. Put the dead capabilities back to sleep
        event.getOriginal().invalidateCaps();

        // 3. Rescue the healing tag from the dead body so the loop doesn't break
        if (event.getOriginal().getPersistentData().contains("vivarium_cleansing_guilt"))
        {
            event.getEntity().getPersistentData().putBoolean("vivarium_cleansing_guilt", true);
        }

        // Rescue the cleansing tag...
        if (event.getOriginal().getPersistentData().contains("vivarium_cleansing_guilt"))
        {
            event.getEntity().getPersistentData().putBoolean("vivarium_cleansing_guilt", true);
        }

        // ADD THIS: Rescue the Dream Sequence tags so their inventory isn't deleted if they die in the skybox!
        CompoundTag oldData = event.getOriginal().getPersistentData();
        CompoundTag newData = event.getEntity().getPersistentData();

        if (oldData.contains("vivarium_saved_inv"))
        {
            newData.put("vivarium_saved_inv", oldData.get("vivarium_saved_inv"));
            newData.putLong("vivarium_bed_pos", oldData.getLong("vivarium_bed_pos"));
            newData.putInt("vivarium_dream_start_z", oldData.getInt("vivarium_dream_start_z"));
            newData.putInt("vivarium_dream_stage", oldData.getInt("vivarium_dream_stage"));
            newData.putLong("vivarium_dream_pos", oldData.getLong("vivarium_dream_pos"));
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent event)
    {
        if (event.getEntity() instanceof ServerPlayer player)
        {
            player.getCapability(GuiltProvider.PLAYER_GUILT).ifPresent(guilt ->
                    ModMessages.sendToPlayer(new GuiltSyncPacket(guilt.getGuilt()), player));
        }
    }

    @SubscribeEvent
    public static void onAnimalKilled(LivingDeathEvent event)
    {
        if(event.getEntity() instanceof Animal animal)
        {
            if(event.getSource().getEntity() instanceof ServerPlayer player)
            {
                player.getCapability(GuiltProvider.PLAYER_GUILT).ifPresent(guilt ->
                {
                    guilt.addGuilt(VivariumConfig.GUILT_INC_KILL.get());
                    ModMessages.sendToPlayer(new GuiltSyncPacket(guilt.getGuilt()), player);

                    // If guilt is over 300, 5% chance for the animal to violently burst blood
                    if (guilt.getGuilt() > VivariumConfig.ANIMAL_BLEED_THRESHOLD.get() && player.getRandom().nextFloat() < 0.05f)
                    {
                        if (animal.level() instanceof ServerLevel serverLevel)
                        {
                            // A wet, fleshy sound
                            serverLevel.playSound(null, animal.blockPosition(), GORE1.get(), SoundSource.NEUTRAL, 1.2f, 0.8f);

                            // A massive burst of your blood particles
                            serverLevel.sendParticles(ModParticles.BLOOD_DRIP.get(),
                                    animal.getX(), animal.getY() + 0.5, animal.getZ(),
                                    60, 0.4, 0.4, 0.4, 0.1);
                        }
                    }
                });
            }
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event)
    {
        if (event.getLevel().isClientSide()) return;

        Player player = event.getPlayer();
        BlockState state = event.getState();

        player.getCapability(GuiltProvider.PLAYER_GUILT).ifPresent(guilt ->
        {
            int startingGuilt = guilt.getGuilt();

            if(state.is(Blocks.GRASS_BLOCK))
            {
                guilt.addGuilt(VivariumConfig.GUILT_INC_GRASS.get());
            }

            if (state.is(BlockTags.LEAVES))
            {
                guilt.addGuilt(VivariumConfig.GUILT_INC_LEAVES.get());
            }

            //this should cover a good few blocks i didnt think of
            if(state.is(BlockTags.ENDERMAN_HOLDABLE) && !state.is(Blocks.STONE)) // reject stone bc CaveInEvent handles adding guilt for that already
            {
                guilt.addGuilt(VivariumConfig.GUILT_INC_MISC.get());
            }

            if (guilt.getGuilt() > startingGuilt && player instanceof ServerPlayer serverPlayer)
            {
                ModMessages.sendToPlayer(new GuiltSyncPacket(guilt.getGuilt()), serverPlayer);
            }
        });
    }

    @SubscribeEvent
    public static void onBlockHit(PlayerInteractEvent.LeftClickBlock event)
    {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        Player player = event.getEntity();

        if (!level.isClientSide() && level instanceof ServerLevel serverLevel)
        {
            if (player.getMainHandItem().getItem() == ModItems.BLADE.get())
            {
                // Moajng's awful structure lookup code from the compass
                ResourceKey<Structure> structureKey = ResourceKey.create(Registries.STRUCTURE,
                        new ResourceLocation("vivarium", "world_heart"));
                var holderSet = serverLevel.registryAccess().registryOrThrow(Registries.STRUCTURE).
                        getHolder(structureKey).map(HolderSet::direct).orElse(null);

                if (holderSet != null)
                {
                    var result = serverLevel.getChunkSource().getGenerator().findNearestMapStructure(serverLevel, holderSet, player.blockPosition(), 100, false);

                    if (result != null)
                    {
                        BlockPos fakeTargetPos = result.getFirst();

                        // override mojang's lazy Y coordinate with the actual depth of the boss room
                        BlockPos trueTargetPos = new BlockPos(fakeTargetPos.getX(), -40, fakeTargetPos.getZ());

                        // send the true target in the packet
                        ModMessages.sendToPlayer(new BloodArrowPacket(pos, event.getFace(), trueTargetPos), (ServerPlayer) player);

                        event.setCanceled(true);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        // Only run on the server to handle the logic, or client for pure effect.
        // Let's do server-side to ensure capability sync is respected.
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;

        ServerPlayer player = (ServerPlayer) event.player;
        Level level = player.level();

        // Check every 5 seconds or so
        if (level.getGameTime() % 100 == 0)
        {
            // Low light and low altitude check
            boolean isUnderground = player.getY() < 50 && !level.canSeeSky(player.blockPosition());

            if (isUnderground)
            {
                player.getCapability(GuiltProvider.PLAYER_GUILT).ifPresent(guilt ->
                {
                    float guiltFactor = guilt.getGuilt() / 22000.0f;
                    if (player.getRandom().nextFloat() < guiltFactor && guilt.getGuilt() > VivariumConfig.HEARTBEAT_THRESHOLD.get())
                    {
                        // block radius
                        double r = 10.0;

                        // Random spherical angles
                        double theta = player.getRandom().nextDouble() * 2.0 * Math.PI;
                        double phi = Math.acos(2.0 * player.getRandom().nextDouble() - 1.0);

                        // Offset from the player's eye position so it feels directional
                        double dx = r * Math.sin(phi) * Math.cos(theta);
                        double dy = r * Math.cos(phi);
                        double dz = r * Math.sin(phi) * Math.sin(theta);

                        double finalX = player.getX() + dx;
                        double finalY = player.getEyeY() + dy;
                        double finalZ = player.getZ() + dz;

                        // This specific overload sends the sound to the player at those coords
                        level.playSound(null, finalX, finalY, finalZ,
                                ModSounds.HEARTBEAT.get(),
                                SoundSource.AMBIENT,
                                1.5f, // Volume
                                0.8f + player.getRandom().nextFloat() * 0.4f); // Randomized pitch
                    }
                });
            }
        }

        // Check every 20 seconds for the crying event
        if (level.getGameTime() % 400 == 0)
        {
            player.getCapability(GuiltProvider.PLAYER_GUILT).ifPresent(guilt ->
            {
                // Only starts happening once things are getting bad (Guilt 1000+)
                if (guilt.getGuilt() > VivariumConfig.CRY_THRESHOLD.get())
                {
                    // 5% chance to trigger
                    if (player.getRandom().nextFloat() < VivariumConfig.CRY_CHANCE.get())
                    {
                        // Calculate a position slightly behind the player
                        net.minecraft.world.phys.Vec3 lookAngle = player.getLookAngle();
                        double dx = player.getX() - (lookAngle.x * 5.0);
                        double dy = player.getY();
                        double dz = player.getZ() - (lookAngle.z * 5.0);

                        // Play it as an ambient sound so it feels disembodied
                        level.playSound(null, dx, dy, dz,
                                ModSounds.CRYING.get(), // Make sure you register this!
                                SoundSource.AMBIENT,
                                0.8f, // Slightly quieter than the heartbeat
                                0.9f + player.getRandom().nextFloat() * 0.2f);
                    }
                }
            });
        }

        // inside your player tick event, running every 10 ticks (half second)
        if (player.tickCount % 10 == 0)
        {
            player.getCapability(GuiltProvider.PLAYER_GUILT).ifPresent(guilt ->
            {
                int currentGuilt = guilt.getGuilt();

                if (currentGuilt >= VivariumConfig.FLOWER_CLOSE_THRESHOLD.get())
                {
                    // 1300 guilt gets a massive permanent kill radius.
                    // 700 guilt gets a small 5-block personal space radius.
                    int radius = currentGuilt >= VivariumConfig.FLOWER_WILT_THRESHOLD.get() ? 16 : 5;
                    BlockPos playerPos = player.blockPosition();

                    // scan the blocks around the player
                    for (int x = -radius; x <= radius; x++)
                    {
                        // only check a few blocks vertically so we don't scan deep underground
                        for (int y = -2; y <= 2; y++)
                        {
                            for (int z = -radius; z <= radius; z++)
                            {
                                BlockPos targetPos = playerPos.offset(x, y, z);
                                BlockState state = level.getBlockState(targetPos);

                                if (state.getBlock() instanceof org.Enderfan.vivarium.block.VitaflowerBlock)
                                {
                                    int flowerState = state.getValue(org.Enderfan.vivarium.block.VitaflowerBlock.STATE);

                                    // The 1300+ Wilt (Permanent Death)
                                    if (currentGuilt >= VivariumConfig.FLOWER_WILT_THRESHOLD.get() && flowerState != 2)
                                    {
                                        level.setBlockAndUpdate(targetPos, state.setValue(org.Enderfan.vivarium.block.VitaflowerBlock.STATE, 2));
                                    }
                                    // The 700+ Close (Temporary Fear)
                                    else if (currentGuilt < VivariumConfig.FLOWER_WILT_THRESHOLD.get() && flowerState == 0)
                                    {
                                        level.setBlockAndUpdate(targetPos, state.setValue(org.Enderfan.vivarium.block.VitaflowerBlock.STATE, 1));

                                        // tell the block to try and reopen in 3 seconds (60 ticks)
                                        level.scheduleTick(targetPos, state.getBlock(), 60);
                                    }
                                }
                            }
                        }
                    }
                }

                // The 1200 Guilt Milestone: Nature's Rejection (Hostile Wildlife)
                if (currentGuilt >= VivariumConfig.HOSTILE_WILDLIFE_THRESHOLD.get())
                {
                    // Scan a 24-block radius (standard aggro distance for most mobs)
                    net.minecraft.world.phys.AABB searchBox = player.getBoundingBox().inflate(24.0);

                    // Grab every generic Mob in that radius
                    java.util.List<net.minecraft.world.entity.Mob> nearbyMobs = level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class, searchBox);

                    for (net.minecraft.world.entity.Mob mob : nearbyMobs)
                    {
                        // Filter for anything that uses the vanilla 'NeutralMob' logic
                        // (Bees, Wolves, Golems, Endermen, Zombified Piglins, etc.)
                        if (mob instanceof net.minecraft.world.entity.NeutralMob neutralMob)
                        {
                            // If they aren't already trying to kill the player, force them to.
                            if (mob.getTarget() != player)
                            {
                                mob.setTarget(player);
                                neutralMob.setPersistentAngerTarget(player.getUUID());
                                neutralMob.startPersistentAngerTimer();
                            }
                        }
                    }
                }
            });
        }
    }


    @SubscribeEvent
    public static void onPassiveMobJoin(net.minecraftforge.event.entity.EntityJoinLevelEvent event)
    {
        // Only run this on the server, and only for mobs that actually use pathfinding
        if (event.getLevel() instanceof net.minecraft.server.level.ServerLevel && event.getEntity() instanceof net.minecraft.world.entity.PathfinderMob mob)
        {
            // We only want to traumatize animals and villagers
            if (mob instanceof net.minecraft.world.entity.animal.Animal || mob instanceof net.minecraft.world.entity.npc.AbstractVillager)
            {
                // Priority 1 is extremely high. It overrides wandering, looking at the player, and grazing.
                // It is only beaten by priority 0 (swimming so they don't drown).
                mob.goalSelector.addGoal(1, new net.minecraft.world.entity.ai.goal.AvoidEntityGoal<>(
                        mob,
                        net.minecraft.world.entity.player.Player.class,
                        16.0F, // Distance in blocks they will start running from
                        1.5D,  // Walk speed multiplier when you are far away
                        2.0D,  // Sprint speed multiplier when you get too close
                        (entity) ->
                        {
                            // This predicate is checked constantly by the AI.
                            // If it returns true, they panic. If false, they act completely normal.
                            if (entity instanceof net.minecraft.server.level.ServerPlayer player)
                            {
                                return player.getCapability(GuiltProvider.PLAYER_GUILT)
                                        .map(cap -> cap.getGuilt() >= VivariumConfig.ANIMAL_FLEE_THRESHOLD.get())
                                        .orElse(false);
                            }
                            return false;
                        }
                ));
            }
        }
    }

    @SubscribeEvent
    public static void onGuiltSpawns(TickEvent.PlayerTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END && event.player instanceof ServerPlayer player)
        {
            // Only run once a second to spare the server's CPU
            if (player.tickCount % 20 != 0) return;

            // Don't spawn creepers inside the dream sequence hallway!
            if (player.getPersistentData().contains("vivarium_saved_inv")) return;

            player.getCapability(GuiltProvider.PLAYER_GUILT).ifPresent(cap ->
            {
                int guilt = cap.getGuilt();
                if (guilt > VivariumConfig.MONSTER_SPAWN_MIN.get())
                {
                    // factor scales from 0.0 (at 150) to 1.0 (at 500+)
                    float factor = Math.min(1.0f, (float) (guilt - VivariumConfig.MONSTER_SPAWN_MIN.get()) / VivariumConfig.MONSTER_SPAWN_MAX.get());

                    // At 500 guilt, there is a 5% chance every second to force an extra spawn.
                    // This averages out to about 1 extra monster every 20 seconds, which
                    // perfectly simulates a heavy increase in natural spawns.
                    if (player.getRandom().nextFloat() < (VivariumConfig.MONSTER_SPAWN_FACTOR.get() * factor))
                    {
                        forceExtraSpawn(player);
                    }
                }
            });
        }
    }

    private static void forceExtraSpawn(ServerPlayer player)
    {
        ServerLevel level = (ServerLevel) player.level();
        net.minecraft.util.RandomSource random = player.getRandom();

        // pick a random spot 15 to 30 blocks away
        double distance = 15.0 + random.nextDouble() * 15.0;
        double angle = random.nextDouble() * Math.PI * 2;

        int x = net.minecraft.util.Mth.floor(player.getX() + Math.cos(angle) * distance);
        int z = net.minecraft.util.Mth.floor(player.getZ() + Math.sin(angle) * distance);

        // gets the highest solid block at those coordinates
        int y = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        BlockPos spawnPos = new BlockPos(x, y, z);

        // a rough check to make sure it's actually dark enough to justify a monster
        if (level.isNight() || !level.canSeeSky(spawnPos))
        {
            // basic list of vanilla harassers. add your own modded entities to this array if you want.
            net.minecraft.world.entity.EntityType<?>[] mobs = new net.minecraft.world.entity.EntityType<?>[]
                    {
                            net.minecraft.world.entity.EntityType.ZOMBIE,
                            net.minecraft.world.entity.EntityType.SKELETON,
                            net.minecraft.world.entity.EntityType.CREEPER,
                            net.minecraft.world.entity.EntityType.SPIDER
                    };

            net.minecraft.world.entity.EntityType<?> type = mobs[random.nextInt(mobs.length)];

            net.minecraft.world.entity.Entity mob = type.create(level);

            if (mob instanceof net.minecraft.world.entity.Mob livingMob)
            {
                livingMob.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);

                // checkSpawnRules makes sure it isn't spawning on bedrock or inside a wall
                if (livingMob.checkSpawnRules(level, net.minecraft.world.entity.MobSpawnType.NATURAL) && livingMob.checkSpawnObstruction(level))
                {
                    level.addFreshEntity(livingMob);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onGuiltCleansing(TickEvent.PlayerTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END && event.player instanceof ServerPlayer player)
        {
            if (player.getPersistentData().getBoolean("vivarium_cleansing_guilt"))
            {
                // Run twice a second (every 10 ticks)
                player.getCapability(GuiltProvider.PLAYER_GUILT).ifPresent(guilt ->
                {
                    if (guilt.getGuilt() > 0) {
                        int newGuilt = Math.max(0, guilt.getGuilt() - 5);
                        guilt.setGuilt(newGuilt);
                        ModMessages.sendToPlayer(new GuiltSyncPacket(newGuilt), player);
                    } else {
                        // Sequence complete
                        player.getPersistentData().remove("vivarium_cleansing_guilt");
                    }
                });
            }
        }
    }

    @SubscribeEvent
    public static void onClientAttackHeart(net.minecraftforge.event.entity.player.AttackEntityEvent event)
    {
        // We strictly only want to do this on the Client side, because the server side
        // is the one broken by the anti-cheat!
        if (event.getEntity().level().isClientSide() && event.getTarget() instanceof org.Enderfan.vivarium.entities.WorldHeartEntity)
        {
            // We use ModMessages now, so it actually knows what a StrikeHeartPacket is!
            org.Enderfan.vivarium.server.ModMessages.sendToServer(new StrikeHeartPacket(event.getTarget().getId()));
        }
    }

    @SubscribeEvent
    public static void onEmptyClick(net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickEmpty event)
    {
        Player player = event.getEntity();
        Level level = player.level();

        // 1. Find any World Hearts within a massive 50-block radius
        java.util.List<org.Enderfan.vivarium.entities.WorldHeartEntity> hearts = level.getEntitiesOfClass(
                org.Enderfan.vivarium.entities.WorldHeartEntity.class,
                player.getBoundingBox().inflate(50.0D)
        );

        for (org.Enderfan.vivarium.entities.WorldHeartEntity heart : hearts)
        {
            // 2. Grab the giant physical hitbox and your exact camera angle
            net.minecraft.world.phys.AABB box = heart.getBoundingBox();
            net.minecraft.world.phys.Vec3 eyePos = player.getEyePosition();
            net.minecraft.world.phys.Vec3 lookVec = player.getViewVector(1.0f);

            // 3. Project a laser 6 blocks forward (covers both survival and creative reach)
            net.minecraft.world.phys.Vec3 endPos = eyePos.add(lookVec.x * 6.0D, lookVec.y * 6.0D, lookVec.z * 6.0D);

            // 4. If the laser intersects the giant box, force the strike packet!
            if (box.clip(eyePos, endPos).isPresent())
            {
                org.Enderfan.vivarium.server.ModMessages.sendToServer(new StrikeHeartPacket(heart.getId()));
                break;
            }
        }
    }
}


