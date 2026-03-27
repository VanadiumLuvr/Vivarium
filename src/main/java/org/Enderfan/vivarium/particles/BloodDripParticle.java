package org.Enderfan.vivarium.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class BloodDripParticle extends TextureSheetParticle {

    // 1. Updated Constructor to accept SpriteSet
    protected BloodDripParticle(ClientLevel level, double x, double y, double z,
                                double vx, double vy, double vz, SpriteSet spriteSet) {
        super(level, x, y, z, vx, vy, vz);
        this.gravity = 1F;
        this.lifetime = 20 + this.random.nextInt(10);
        this.rCol = 0.8F;
        this.gCol = 0.1F;
        this.bCol = 0.1F;
        this.quadSize = 0.01F + this.random.nextFloat() * 0.1F;

        // Pick the sprite immediately upon creation
        this.pickSprite(spriteSet);
    }

    @Override
    public void tick() {
        super.tick();
        // Note: super.tick() already handles this.yo = this.y and move() logic
        // for TextureSheetParticle, but your custom gravity is fine.
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        // 2. FIXED: Changed from null to PARTICLE_SHEET_OPAQUE
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Factory(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        @Override
        public Particle createParticle(@NotNull SimpleParticleType type, @NotNull ClientLevel level,
                                       double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            // Pass the sprites into the new constructor
            return new BloodDripParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites);
        }
    }
}
