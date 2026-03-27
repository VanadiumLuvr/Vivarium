package org.Enderfan.vivarium.server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
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
                org.Enderfan.vivarium.client.BlinkOverlayRenderer.triggerBlink();
            }
        });
        context.setPacketHandled(true);
    }
}