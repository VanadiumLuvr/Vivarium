package org.Enderfan.vivarium.client.sounds;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import org.Enderfan.vivarium.ModSounds;
import org.Enderfan.vivarium.entities.GrasshopperEntity;

public class GrasshopperFlySoundInstance extends AbstractTickableSoundInstance
{
    private final GrasshopperEntity grasshopper;

    public GrasshopperFlySoundInstance(GrasshopperEntity grasshopper)
    {
        super(ModSounds.GRASSHOPPER_FLY.get(), SoundSource.NEUTRAL, SoundInstance.createUnseededRandom());
        this.grasshopper = grasshopper;

        // This tells the engine to loop the sound seamlessly until we explicitly say stop
        this.looping = true;
        this.delay = 0;
        this.volume = 0.2f; // Adjust volume to your liking
        this.pitch = 1.0f;

        this.x = grasshopper.getX();
        this.y = grasshopper.getY();
        this.z = grasshopper.getZ();
    }

    @Override
    public void tick()
    {
        // The kill switch: If the bug dies, despawns, or lands on the ground, instantly kill the sound
        if (this.grasshopper.isRemoved() || this.grasshopper.onGround())
        {
            this.stop();
        }
        else
        {
            // Continuously update the audio coordinates so the 3D spatial sound tracks the bug perfectly
            this.x = this.grasshopper.getX();
            this.y = this.grasshopper.getY();
            this.z = this.grasshopper.getZ();
        }
    }

    // A safe helper method to trigger the sound without crashing dedicated servers
    public static void play(GrasshopperEntity entity)
    {
        Minecraft.getInstance().getSoundManager().play(new GrasshopperFlySoundInstance(entity));
    }
}