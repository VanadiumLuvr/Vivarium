package org.Enderfan.vivarium;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, Vivarium.MODID);

    public static final RegistryObject<SoundEvent> BLOOD_DRONE = SOUNDS.register("blood_drone",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Vivarium.MODID, "blood_drone")));

    public static final RegistryObject<SoundEvent> GORE1 = SOUNDS.register("gore1",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Vivarium.MODID, "gore1")));

    public static final RegistryObject<SoundEvent> HEARTBEAT = SOUNDS.register("heartbeat",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("vivarium", "heartbeat")));

    public static final RegistryObject<SoundEvent> SONG1 = SOUNDS.register("song1",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("vivarium", "sacrifice")));

    public static final RegistryObject<SoundEvent> CRYING = SOUNDS.register("crying",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("vivarium", "crying")));
}