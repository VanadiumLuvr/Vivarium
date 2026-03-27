package org.Enderfan.vivarium.entities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.NetworkHooks;
import org.Enderfan.vivarium.particles.ModParticles;

public class BloodPoolEntity extends Entity
{
    private static final EntityDataAccessor<Float> RADIUS = SynchedEntityData.defineId(BloodPoolEntity.class, EntityDataSerializers.FLOAT);
    // 1. Define the DataAccessor for originY
    private static final EntityDataAccessor<Float> ORIGIN_Y = SynchedEntityData.defineId(BloodPoolEntity.class, EntityDataSerializers.FLOAT);

    private int age;

    public BloodPoolEntity(EntityType<? extends BloodPoolEntity> entityType, Level level)
    {
        super(entityType, level);
        this.setNoGravity(true);
    }

    @Override
    protected void defineSynchedData()
    {
        this.entityData.define(RADIUS, 0.25f);
        // 2. Initialize with a default value
        this.entityData.define(ORIGIN_Y, 0.0f);
    }

    // 3. Updated Getter and Setter for Sync
    public void setOriginY(float y)
    {
        this.entityData.set(ORIGIN_Y, y);
    }

    public float getOriginY()
    {
        return this.entityData.get(ORIGIN_Y);
    }

    @Override
    public void tick()
    {
        super.tick();
        ++this.age;

        float targetRadius = Mth.lerp(0.005f * this.age, 0.25f, 3.4f);
        this.setRadius(targetRadius);

        if (this.level().isClientSide())
        {
            this.spawnFallingRain();
            this.spawnFallingRain();
            this.spawnFallingRain();
        }

        if (this.age > 800)
        {
            this.discard();
        }
    }

    private void spawnFallingRain()
    {
        float yCoord = this.getOriginY();

        // Increased frequency from 0.3f to 0.6f for a heavier flow
        if (yCoord > this.level().getMinBuildHeight())
        {
            // Randomize the angle and distance for a wider splash area
            double angle = this.random.nextDouble() * Math.PI * 2.0;

            // Increased the distribution radius slightly (0.45 instead of 0.4)
            double dist = this.random.nextDouble();

            // Add a tiny extra random "jitter" to X and Z for more organic dispersion
            double px = this.getX() + (Math.cos(angle) * dist) + (this.random.nextDouble() - 0.5) * 0.1;
            double pz = this.getZ() + (Math.sin(angle) * dist) + (this.random.nextDouble() - 0.5) * 0.1;

            this.level().addParticle(ModParticles.BLOOD_DRIP.get(),
                    px, (yCoord + 0.2), pz,
                    0.0, 0.5, 0.0);
        }
    }

    public void setRadius(float radius) {
        float clamped = Mth.clamp(radius, 0.25f, 6f);
        this.entityData.set(RADIUS, clamped);
        // Dynamic BB to match circle (for culling/collision if needed)
        double cx = this.getX(), cz = this.getZ();
        this.setBoundingBox(new AABB(cx - clamped, this.getY(), cz - clamped, cx + clamped, this.getY() + 0.125, cz + clamped));
    }

    public float getRadius() {
        return this.entityData.get(RADIUS);
    }

    // No pickup, no push, no save
    @Override
    public boolean isPickable() { return false; }
    @Override
    public boolean isPushable() { return false; }
    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {}
    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {}

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
