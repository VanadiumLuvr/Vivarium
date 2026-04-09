package org.Enderfan.vivarium.entities;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BirdEntity extends Animal
{
    // The 4 Animation States
    public final AnimationState hopAnimationState = new AnimationState();
    public final AnimationState glideAnimationState = new AnimationState();
    public final AnimationState flapAnimationState = new AnimationState();
    public final AnimationState peckAnimationState = new AnimationState();
    public final AnimationState peckFlyAnimationState = new AnimationState();

    // A timer to keep the bird still while the peck animation finishes
    private int peckTick = 0;

    public BirdEntity(EntityType<? extends Animal> type, Level level)
    {
        super(type, level);
        // Gives the bird the ability to pathfind through the air
        this.moveControl = new FlyingMoveControl(this, 20, true);
    }

    public static AttributeSupplier.Builder createAttributes()
    {
        return Animal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 8.0D)
                .add(Attributes.FLYING_SPEED, 0.4D)
                .add(Attributes.MOVEMENT_SPEED, 0.2D)
                .add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 0.5D);
    }

    @Override
    protected PathNavigation createNavigation(Level level)
    {
        FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
        nav.setCanOpenDoors(false);
        nav.setCanFloat(true);
        nav.setCanPassDoors(true);
        return nav;
    }

    @Override
    protected void registerGoals()
    {
        this.goalSelector.addGoal(0, new net.minecraft.world.entity.ai.goal.FloatGoal(this));
        this.goalSelector.addGoal(2, new net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new net.minecraft.world.entity.ai.goal.LookAtPlayerGoal(this, net.minecraft.world.entity.player.Player.class, 8.0F));
        this.goalSelector.addGoal(1, new net.minecraft.world.entity.ai.goal.MeleeAttackGoal(this, 1.2D, false));
        this.targetSelector.addGoal(1, new net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<>(this, GrasshopperEntity.class, true));
        this.targetSelector.addGoal(2, new net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<>(this, ButterflyEntity.class, true));
    }

    // When the bird successfully hits its target, force the peck animation to play
    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target)
    {
        boolean success = super.doHurtTarget(target);
        if (success)
        {
            this.peckTick = 20; // Locks the bird in place briefly
            this.level().broadcastEntityEvent(this, (byte) 10); // Tells the client to visually play the peck animation
        }
        return success;
    }

    // Prevents fall damage when it drops out of the sky
    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, net.minecraft.world.damagesource.DamageSource source) {
        return false;
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel p_146743_, AgeableMob p_146744_) {
        return null;
    }

    @Override
    public void aiStep()
    {
        super.aiStep();

        if (this.level().isClientSide()) return;

        // If in the air and falling, apply the "Glide" parachute effect
        if (!this.onGround() && this.getDeltaMovement().y < 0.0D)
        {
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0D, 0.6D, 1.0D));
        }

        // Random idle pecking logic (only on the ground, 1-in-200 chance per tick)
        if (this.onGround() && this.peckTick <= 0 && this.random.nextInt(200) == 0)
        {
            this.peckTick = 4;
            this.level().broadcastEntityEvent(this, (byte) 10); // Custom packet to tell client to peck
        }

        if (this.peckTick > 0)
        {
            this.peckTick--;
        }
    }

    @Override
    public void tick()
    {
        super.tick();

        if (this.level().isClientSide())
        {
            // 1. Determine if we are pecking
            if (this.peckTick > 0)
            {
                this.peckTick--;
                this.hopAnimationState.stop();
                this.glideAnimationState.stop();
                this.flapAnimationState.stop();

                if (this.onGround())
                {
                    this.peckFlyAnimationState.stop();
                    this.peckAnimationState.startIfStopped(this.tickCount);
                }
                else
                {
                    this.peckAnimationState.stop();
                    this.peckFlyAnimationState.startIfStopped(this.tickCount);
                }
            }
            // 2. Determine if we are on the ground and moving (Hop)
            else if (this.onGround() && this.getDeltaMovement().horizontalDistanceSqr() > 0.0001D)
            {
                this.glideAnimationState.stop();
                this.flapAnimationState.stop();
                this.peckAnimationState.stop();
                this.hopAnimationState.startIfStopped(this.tickCount);
            }
            // 3. Determine Airborne States (Flap vs Glide)
            else if (!this.onGround())
            {
                this.hopAnimationState.stop();
                this.peckAnimationState.stop();

                // Calculate exact physical movement between frames
                double yMotion = this.getY() - this.yOld;

                // If moving upwards (or holding altitude), FLAP
                if (yMotion > -0.05D)
                {
                    this.glideAnimationState.stop();
                    this.flapAnimationState.startIfStopped(this.tickCount);
                }
                // If losing altitude, GLIDE
                else
                {
                    this.flapAnimationState.stop();
                    this.glideAnimationState.startIfStopped(this.tickCount);
                }
            }
            // 4. Idle (Standing still on the ground)
            else
            {
                this.hopAnimationState.stop();
                this.glideAnimationState.stop();
                this.flapAnimationState.stop();
                // If you have a separate idle animation, start it here!
                // Otherwise, it will just freeze in its base pose.
            }
        }
    }

    // Handles the server telling the client "Hey, play the peck animation!"
    @Override
    public void handleEntityEvent(byte id)
    {
        if (id == 10)
        {
            this.peckTick = 20;
        }
        else
        {
            super.handleEntityEvent(id);
        }
    }

    // --- CUSTOM SPAWN LOGIC ---
    public static boolean checkBirdSpawnRules(EntityType<? extends Animal> type, net.minecraft.world.level.ServerLevelAccessor level, net.minecraft.world.entity.MobSpawnType spawnType, BlockPos pos, net.minecraft.util.RandomSource random)
    {
        // 1. Must pass vanilla animal checks first (spawn on grass, proper lighting)
        if (!Animal.checkAnimalSpawnRules(type, level, spawnType, pos, random)) return false;

        // 2. Find the nearest player to check their localized ecosystem guilt
        net.minecraft.world.entity.player.Player player = level.getNearestPlayer(pos.getX(), pos.getY(), pos.getZ(), 128.0D, false);

        if (player != null)
        {
            // Use AtomicBoolean so we can safely modify it inside the capability lambda
            java.util.concurrent.atomic.AtomicBoolean canSpawn = new java.util.concurrent.atomic.AtomicBoolean(true);

            player.getCapability(org.Enderfan.vivarium.server.GuiltProvider.PLAYER_GUILT).ifPresent(guiltCap -> {
                int guilt = guiltCap.getGuilt();
                double startFade = org.Enderfan.vivarium.config.VivariumConfig.FLOWER_CLOSE_THRESHOLD.get();
                double endFade = org.Enderfan.vivarium.config.VivariumConfig.FLOWER_WILT_THRESHOLD.get() * 1.25;

                if (guilt >= endFade)
                {
                    canSpawn.set(false); // 0% chance
                }
                else if (guilt > startFade)
                {
                    // Lerp from 1.0 (100%) at startFade to 0.0 (0%) at endFade
                    double chance = 1.0 - ((guilt - startFade) / (endFade - startFade));
                    if (random.nextDouble() > chance)
                    {
                        canSpawn.set(false);
                    }
                }
            });

            return canSpawn.get();
        }

        return true; // Default to spawning normally if no player is nearby
    }
}