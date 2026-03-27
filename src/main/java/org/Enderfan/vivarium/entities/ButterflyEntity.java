package org.Enderfan.vivarium.entities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import org.Enderfan.vivarium.config.VivariumConfig;
import org.jetbrains.annotations.NotNull;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.Mob;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;

public class ButterflyEntity extends PathfinderMob
{
    // 12000 ticks = half an in-game day.
    // If they can't find a healthy flower for 5 real-world minutes, they die.
    private int ticksSinceLastMeal = 0;
    private net.minecraft.core.BlockPos lastFlowerPos = null;
    private int flowerCooldown = 0;
    // Put this near your other variables (like flowerCooldown)
    private static final EntityDataAccessor<Boolean> FEEDING = SynchedEntityData.defineId(ButterflyEntity.class, EntityDataSerializers.BOOLEAN);

    public final net.minecraft.world.entity.AnimationState flyAnimationState = new net.minecraft.world.entity.AnimationState();
    public final net.minecraft.world.entity.AnimationState landAnimationState = new net.minecraft.world.entity.AnimationState();

    @Override
    protected void defineSynchedData()
    {
        super.defineSynchedData();
        this.entityData.define(FEEDING, false);
    }

    public boolean isFeeding()
    {
        return this.entityData.get(FEEDING);
    }

    public void setFeeding(boolean feeding)
    {
        this.entityData.set(FEEDING, feeding);
    }

    public ButterflyEntity(EntityType<? extends PathfinderMob> type, Level level)
    {
        super(type, level);
        this.moveControl = new FlyingMoveControl(this, 20, true);
    }

    public static AttributeSupplier.Builder createAttributes()
    {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 4.0D) // 2 hearts of health
                .add(Attributes.FLYING_SPEED, 0.6F)
                .add(Attributes.MOVEMENT_SPEED, 0.3F);
    }

    @Override
    protected @NotNull PathNavigation createNavigation(Level level)
    {
        FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
        nav.setCanOpenDoors(false);
        nav.setCanFloat(true);
        return nav;
    }

    @Override
    protected void registerGoals()
    {
        // Priority 1: Find food if hungry
        this.goalSelector.addGoal(1, new FeedOnVitaflowerGoal(this, 1.0, 16));

        // Priority 2: Wander around the sky
        this.goalSelector.addGoal(2, new WaterAvoidingRandomFlyingGoal(this, 1.0));
    }

    @Override
    public void tick()
    {
        super.tick();

        // Server-side: Handle starvation, memory, and logic
        if (!this.level().isClientSide)
        {
            // Cooldown logic for flower memory
            if (this.flowerCooldown > 0)
            {
                this.flowerCooldown--;
                if (this.flowerCooldown <= 0)
                {
                    this.lastFlowerPos = null;
                }
            }

            this.ticksSinceLastMeal++;

            if (this.ticksSinceLastMeal > VivariumConfig.BUTTERFLY_STARVATION_TIME.get())
            {
                if (this.tickCount % 20 == 0)
                {
                    this.hurt(this.damageSources().starve(), 1.0f);
                }
            }
        }
        // Client-side: Handle visuals and animations
        else
        {
            this.setupAnimationStates();
        }
    }

    private void setupAnimationStates()
    {
        // 1. Manually check if the block directly below them is solid
        boolean blockBelowIsSolid = !this.level().getBlockState(this.blockPosition().below()).isAir();

        // 2. Check if their Y coordinate is practically flush with the top of that block
        boolean isTouchingFloor = blockBelowIsSolid && (this.getY() - Math.floor(this.getY())) < 0.05;

        // Now it checks our custom floor math, OR the feeding flag, OR vanilla's onGround (just in case)
        boolean isResting = isTouchingFloor || this.isFeeding() || this.onGround();

        if (isResting)
        {
            this.flyAnimationState.stop();
            this.landAnimationState.startIfStopped(this.tickCount);
        }
        else
        {
            this.landAnimationState.stop();
            this.flyAnimationState.startIfStopped(this.tickCount);
        }
    }

    public void resetHunger()
    {
        this.ticksSinceLastMeal = 0;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound)
    {
        super.addAdditionalSaveData(compound);
        compound.putInt("VivariumTicksSinceLastMeal", this.ticksSinceLastMeal);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound)
    {
        super.readAdditionalSaveData(compound);
        if (compound.contains("VivariumTicksSinceLastMeal"))
        {
            this.ticksSinceLastMeal = compound.getInt("VivariumTicksSinceLastMeal");
        }
    }

    // 1. Prevents the actual health deduction
    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, net.minecraft.world.damagesource.DamageSource source)
    {
        return false;
    }

    // 2. Prevents the engine from playing the heavy "thud" sound and spawning dust particles
    @Override
    protected void checkFallDamage(double y, boolean onGround, net.minecraft.world.level.block.state.BlockState state, net.minecraft.core.BlockPos pos)
    {
        // We leave this completely empty so the butterfly lands in total silence
    }

    public net.minecraft.core.BlockPos getLastFlowerPos()
    {
        return this.lastFlowerPos;
    }

    public void setLastFlowerPos(net.minecraft.core.BlockPos pos)
    {
        this.lastFlowerPos = pos;
        this.flowerCooldown = 1200; // 1200 ticks = 60 seconds
    }

    public static boolean checkButterflySpawnRules(net.minecraft.world.entity.EntityType<ButterflyEntity> type, net.minecraft.world.level.LevelAccessor level, net.minecraft.world.entity.MobSpawnType spawnType, net.minecraft.core.BlockPos pos, net.minecraft.util.RandomSource random)
    {
        // Must spawn on grass/dirt/sand, and it must be daytime/bright enough
        return level.getBlockState(pos.below()).is(net.minecraft.tags.BlockTags.ANIMALS_SPAWNABLE_ON)
                && level.getRawBrightness(pos, 0) > 8;
    }
}