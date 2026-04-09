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

    public float flightPitch = 0.0f;

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
        this.goalSelector.addGoal(1, new net.minecraft.world.entity.ai.goal.MeleeAttackGoal(this, 2.5D, false));
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
            this.peckTick = 10; // Locks the bird in place briefly
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

        // 1. Only glide if we are NOT hunting. If we have an attack target, let gravity take over!
        if (!this.onGround() && this.getDeltaMovement().y < 0.0D && this.getTarget() == null)
        {
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0D, 0.6D, 1.0D));
        }

        // 2. Anti-Hover: If it finished moving but is still stuck in the air, pull it to the dirt
        if (!this.onGround() && this.getNavigation().isDone() && this.getTarget() == null)
        {
            this.setDeltaMovement(this.getDeltaMovement().add(0, -0.02D, 0));
        }

        // Random idle pecking logic
        if (this.onGround() && this.peckTick <= 0 && this.random.nextInt(200) == 0)
        {
            this.peckTick = 20;
            this.level().broadcastEntityEvent(this, (byte) 10);
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

        // Dynamic Pitch Tracking (Runs on client to keep the model smooth)
        if (this.level().isClientSide())
        {
            if (!this.onGround() && this.getDeltaMovement().horizontalDistanceSqr() > 0.0001D)
            {
                double horizSpeed = this.getDeltaMovement().horizontalDistance();
                float targetPitch = (float) (-(Math.atan2(this.getDeltaMovement().y, horizSpeed) * (180D / Math.PI)));

                this.flightPitch = this.flightPitch + (targetPitch - this.flightPitch) * 0.2f;
            }
            else
            {
                this.flightPitch = this.flightPitch + (0 - this.flightPitch) * 0.2f;
            }

            // --- ANIMATION CONTROLLER ---

            // 1. Determine if we are pecking (Overrides all movement)
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
            // 2. Determine Airborne States (Flap vs Glide)
            else if (!this.onGround())
            {
                // Calculate physical movement
                double yMotion = this.getY() - this.yOld;
                double hMotion = this.getDeltaMovement().horizontalDistanceSqr();

                // Floor Check: Are we in or directly above a non-air block?
                boolean nearGround = !this.level().getBlockState(this.blockPosition().below()).isAir()
                        || !this.level().getBlockState(this.blockPosition()).isAir();

                // THE JITTER FILTER (Restored with Floor Check)
                // Prevents the "single-frame glide" glitch while resting, but allows mid-air flapping if stuck high up.
                if (nearGround && Math.abs(yMotion) < 0.05D && hMotion < 0.0001D)
                {
                    this.hopAnimationState.stop();
                    this.glideAnimationState.stop();
                    this.flapAnimationState.stop();
                    this.peckAnimationState.stop();
                    this.peckFlyAnimationState.stop();
                }
                else
                {
                    this.hopAnimationState.stop();
                    this.peckAnimationState.stop();
                    this.peckFlyAnimationState.stop();

                    // If the bird's raw Y-velocity is negative, it is actively falling. GLIDE!
                    if (this.getDeltaMovement().y < -0.01D)
                    {
                        this.flapAnimationState.stop();
                        this.glideAnimationState.startIfStopped(this.tickCount);
                    }
                    // Otherwise, it is fighting to go up (or hold altitude). FLAP!
                    else
                    {
                        this.glideAnimationState.stop();
                        this.flapAnimationState.startIfStopped(this.tickCount);
                    }
                }
            }
            // 3. Determine if we are on the ground and moving (Hop)
            else if (this.getDeltaMovement().horizontalDistanceSqr() > 0.0001D)
            {
                this.glideAnimationState.stop();
                this.flapAnimationState.stop();
                this.peckAnimationState.stop();
                this.peckFlyAnimationState.stop();
                this.hopAnimationState.startIfStopped(this.tickCount);
            }
            // 4. Idle (Standing still on the ground)
            else
            {
                this.hopAnimationState.stop();
                this.glideAnimationState.stop();
                this.flapAnimationState.stop();
                this.peckAnimationState.stop();
                this.peckFlyAnimationState.stop();
            }
        }
    }

    // Handles the server telling the client "Hey, play the peck animation!"
    @Override
    public void handleEntityEvent(byte id)
    {
        if (id == 10)
        {
            this.peckTick = 10;
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