package org.Enderfan.vivarium.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.Enderfan.vivarium.Vivarium;
import org.Enderfan.vivarium.config.VivariumConfig;
import org.Enderfan.vivarium.server.GuiltProvider;
import org.jetbrains.annotations.NotNull;

@Mod.EventBusSubscriber(modid = Vivarium.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientMusicHandler
{
    // The switch for your credits sequence
    public static boolean suppressVanillaMusic = false;

    @SubscribeEvent
    public static void onPlaySound(PlaySoundEvent event)
    {
        SoundInstance sound = event.getSound();
        if (sound == null) return;

        // 1. The Credits Sequence Suppression
        if (suppressVanillaMusic && sound.getSource() == SoundSource.MUSIC)
        {
            event.setSound(null);
            return;
        }

        // 2. The Atmospheric Pitch Shift (Affects Music and Jukebox Records)
        if (sound.getSource() == SoundSource.MUSIC || sound.getSource() == SoundSource.RECORDS)
        {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null)
            {
                mc.player.getCapability(GuiltProvider.PLAYER_GUILT).ifPresent(guilt ->
                {
                    int currentGuilt = guilt.getGuilt();

                    if (currentGuilt > VivariumConfig.MUSIC_PITCH_MIN.get())
                    {
                        // Cap the math at 2500 so the pitch doesn't continue dropping into negative numbers
                        float effectiveGuilt = Math.min(VivariumConfig.MUSIC_PITCH_MAX.get(), currentGuilt);

                        // Calculates how far along the 1400-2500 spectrum the player is (0.0 to 1.0)
                        float dropFactor = (effectiveGuilt - VivariumConfig.MUSIC_PITCH_MIN.get()) / 1100.0f;

                        // Drops the pitch by a maximum of 30% (down to 0.7f)
                        double pitchMultiplier = 1.0 - (dropFactor * VivariumConfig.MUSIC_PITCH_FACTOR.get());

                        // Ensure we don't accidentally double-wrap the sound if the engine fires the event twice
                        if (!(sound instanceof GuiltMusicWrapper))
                        {
                            event.setSound(new GuiltMusicWrapper(sound, (float) pitchMultiplier));
                        }
                    }
                });
            }
        }
    }

    // --- THE INTERCEPTOR WRAPPER ---
    // By implementing TickableSoundInstance, we ensure vanilla music still correctly fades in and out when changing biomes.
    public static class GuiltMusicWrapper implements TickableSoundInstance
    {
        private final SoundInstance original;
        private final float pitchMultiplier;

        public GuiltMusicWrapper(SoundInstance original, float pitchMultiplier)
        {
            this.original = original;
            this.pitchMultiplier = pitchMultiplier;
        }

        // --- Our Intercepted Method ---
        @Override
        public float getPitch()
        {
            return original.getPitch() * pitchMultiplier;
        }

        // --- Delegated Methods (We just pass these straight through to the original sound) ---
        @Override
        public boolean isStopped()
        {
            return original instanceof TickableSoundInstance t && t.isStopped();
        }

        @Override
        public void tick()
        {
            if (original instanceof TickableSoundInstance t) t.tick();
        }

        @Override public @NotNull ResourceLocation getLocation() { return original.getLocation(); }
        @Override public WeighedSoundEvents resolve(@NotNull SoundManager manager) { return original.resolve(manager); }
        @Override public @NotNull Sound getSound() { return original.getSound(); }
        @Override public @NotNull SoundSource getSource() { return original.getSource(); }
        @Override public boolean isLooping() { return original.isLooping(); }
        @Override public boolean isRelative() { return original.isRelative(); }
        @Override public int getDelay() { return original.getDelay(); }
        @Override public float getVolume() { return original.getVolume(); }
        @Override public double getX() { return original.getX(); }
        @Override public double getY() { return original.getY(); }
        @Override public double getZ() { return original.getZ(); }
        @Override public @NotNull Attenuation getAttenuation() { return original.getAttenuation(); }
    }
}