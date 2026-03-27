package org.Enderfan.vivarium.server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public class TriggerCreditsPacket
{
    public TriggerCreditsPacket() {}

    public TriggerCreditsPacket(FriendlyByteBuf buffer) {}

    public void toBytes(FriendlyByteBuf buffer) {}

    public void handle(Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() ->
                net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT, () -> () ->
                {
                    net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();

                    // 1. Stop whatever is currently playing
                    mc.getSoundManager().stop(null, net.minecraft.sounds.SoundSource.MUSIC);
                    mc.getSoundManager().stop(null, net.minecraft.sounds.SoundSource.RECORDS);

                    // 2. TURN ON THE SUPPRESSOR
                    org.Enderfan.vivarium.client.ClientMusicHandler.suppressVanillaMusic = true;

                    // 3. Play your song on the UI/Master channel
                    mc.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(org.Enderfan.vivarium.ModSounds.SONG1.get(), 1.0f, 0.2f));

                    // 4. Schedule the GUI titles
                    org.Enderfan.vivarium.client.ClientScheduler.schedule(200, () ->
                    {
                        mc.gui.setTimes(20, 350, 0);
                        mc.gui.setTitle(net.minecraft.network.chat.Component.literal("Vivarium").withStyle(net.minecraft.ChatFormatting.DARK_AQUA));
                    });

                    org.Enderfan.vivarium.client.ClientScheduler.schedule(325, () ->
                            mc.gui.setSubtitle(net.minecraft.network.chat.Component.literal("Made by Enderfan Gamer").withStyle(net.minecraft.ChatFormatting.AQUA)));

                    org.Enderfan.vivarium.client.ClientScheduler.schedule(445, () ->
                            mc.gui.setSubtitle(net.minecraft.network.chat.Component.literal("Song by @TastyHotCoca").withStyle(net.minecraft.ChatFormatting.GOLD)));

                    // 5. TURN OFF THE SUPPRESSOR WHEN THE SONG ENDS
                    org.Enderfan.vivarium.client.ClientScheduler.schedule(2400, () ->
                    {
                        org.Enderfan.vivarium.client.ClientMusicHandler.suppressVanillaMusic = false;
                    });
                }));
        context.setPacketHandled(true);
    }
}