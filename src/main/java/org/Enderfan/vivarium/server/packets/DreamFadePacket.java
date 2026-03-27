package org.Enderfan.vivarium.server.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.Enderfan.vivarium.client.renderers.DreamFadeRenderer;

import java.util.function.Supplier;

public class DreamFadePacket
{
    private final boolean isWhite;

    public DreamFadePacket(boolean isWhite)
    {
        this.isWhite = isWhite;
    }

    public DreamFadePacket(FriendlyByteBuf buffer)
    {
        this.isWhite = buffer.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buffer)
    {
        buffer.writeBoolean(this.isWhite);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() ->
        {
            net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT, () -> () ->
            {
                // Tells the client renderer to start the effect
                DreamFadeRenderer.triggerFade(this.isWhite);
            });
        });
        context.setPacketHandled(true);
    }
}