package org.Enderfan.vivarium.server;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import org.Enderfan.vivarium.Vivarium;
import org.Enderfan.vivarium.events.CaveInEvent;

public class ModMessages
{
    private static SimpleChannel INSTANCE;
    private static int packetId = 0;
    private static int id() { return packetId++; }

    public static void register()
    {
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(Vivarium.MODID, "messages"))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        INSTANCE = net;

        net.messageBuilder(GuiltSyncPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(GuiltSyncPacket::new)
                .encoder(GuiltSyncPacket::toBytes)
                .consumerMainThread(GuiltSyncPacket::handle)
                .add();

        net.messageBuilder(DreamFadePacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(DreamFadePacket::new)
                .encoder(DreamFadePacket::toBytes)
                .consumerMainThread(DreamFadePacket::handle)
                .add();

        INSTANCE.messageBuilder(BloodArrowPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(BloodArrowPacket::new)
                .encoder(BloodArrowPacket::toBytes)
                .consumerMainThread(BloodArrowPacket::handle)
                .add();

        INSTANCE.messageBuilder(TriggerCreditsPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(TriggerCreditsPacket::new)
                .encoder(TriggerCreditsPacket::toBytes)
                .consumerMainThread(TriggerCreditsPacket::handle)
                .add();

        INSTANCE.messageBuilder(CaveInBlinkPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(CaveInBlinkPacket::new)
                .encoder(CaveInBlinkPacket::toBytes)
                .consumerMainThread(CaveInBlinkPacket::handle)
                .add();

        INSTANCE.messageBuilder(StrikeHeartPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(StrikeHeartPacket::new)
                .encoder(StrikeHeartPacket::toBytes)
                .consumerMainThread(StrikeHeartPacket::handle)
                .add();
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player)
    {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    // ADD THIS NEW METHOD:
    public static <MSG> void sendToServer(MSG message)
    {
        INSTANCE.sendToServer(message);
    }
}
