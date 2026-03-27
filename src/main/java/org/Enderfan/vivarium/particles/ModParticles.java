package org.Enderfan.vivarium.particles;

import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.Enderfan.vivarium.Vivarium;

public class ModParticles
{
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(Registries.PARTICLE_TYPE, Vivarium.MODID);

    public static final RegistryObject<SimpleParticleType> BLOOD_DRIP = PARTICLES.register("blood_drip",
            () -> new SimpleParticleType(true));
}
