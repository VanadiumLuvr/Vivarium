package org.Enderfan.vivarium.server.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.Enderfan.vivarium.client.renderers.BlinkOverlayRenderer;

import java.util.function.Supplier;

public class CaveInBlinkPacket
{
    public CaveInBlinkPacket() {}

    public CaveInBlinkPacket(FriendlyByteBuf buffer) {}

    public void toBytes(FriendlyByteBuf buffer) {}

    public void handle(Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() ->
        {
            if (context.getDirection().getReceptionSide().isClient())
            {
                BlinkOverlayRenderer.triggerBlink();
            }
        });
        context.setPacketHandled(true);
    }
}