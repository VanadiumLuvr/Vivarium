package org.Enderfan.vivarium.entities;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class GrasshopperEntity extends Animal
{
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState flyAnimationState = new AnimationState();
    public boolean isFlyingSoundActive = false;

    public GrasshopperEntity(EntityType<? extends Animal> type, Level level)
    {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes()
    {
        return Animal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 6.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.FOLLOW_RANGE, 12.0D);
    }

    @Override
    protected void registerGoals()
    {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        // If a player gets within 8 blocks, speed up to 1.5x and run away
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Player.class, 8.0F, 1.2D, 1.5D));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
    }

    @Override
    public void tick()
    {
        super.tick();

        if (this.level().isClientSide())
        {
            this.setupAnimationStates();

            // --- THE NEW SOUND LOGIC ---
            // If the bug is in the air and moving
            if (!this.onGround() && this.getDeltaMovement().horizontalDistanceSqr() > 0.0001D)
            {
                // If we haven't started the sound yet, start it now
                if (!this.isFlyingSoundActive)
                {
                    org.Enderfan.vivarium.client.sounds.GrasshopperFlySoundInstance.play(this);
                    this.isFlyingSoundActive = true;
                }
            }
            else
            {
                // The moment it lands, reset the flag so it can play again on the next jump.
                // (The sound instance will automatically read onGround() and kill itself).
                this.isFlyingSoundActive = false;
            }
        }
        else
        {
            if (!this.getNavigation().isDone() && this.onGround())
            {
                if (this.getDeltaMovement().horizontalDistanceSqr() > 0.005D)
                {
                    if (this.random.nextFloat() < 0.15F)
                    {
                        this.jumpFromGround();

                        net.minecraft.world.phys.Vec3 motion = this.getDeltaMovement();
                        this.setDeltaMovement(motion.x * 2.5D, 0.45D, motion.z * 2.5D);
                    }
                }
            }
        }
    }

    private void setupAnimationStates()
    {
        if (!this.onGround())
        {
            this.idleAnimationState.stop();
            this.walkAnimationState.stop();
            this.flyAnimationState.startIfStopped(this.tickCount);
        }
        else if (this.getDeltaMovement().horizontalDistanceSqr() > 0.0001D)
        {
            this.idleAnimationState.stop();
            this.flyAnimationState.stop();
            this.walkAnimationState.startIfStopped(this.tickCount);
        }
        else
        {
            this.walkAnimationState.stop();
            this.flyAnimationState.stop();
            this.idleAnimationState.startIfStopped(this.tickCount);
        }
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob parent)
    {
        return null; // Update this if you want grasshoppers to be breedable
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, net.minecraft.world.damagesource.DamageSource source)
    {
        return false; // Complete immunity to fall damage
    }

    @Override
    public void aiStep()
    {
        super.aiStep();

        // If in the air and falling downward, resist gravity
        if (!this.onGround() && this.getDeltaMovement().y < 0.0D)
        {
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0D, 0.8D, 1.0D));
        }
    }
}