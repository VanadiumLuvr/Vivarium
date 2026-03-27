package org.Enderfan.vivarium.server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class GuiltSyncPacket
{
    private final int guilt;

    public GuiltSyncPacket(int guilt)
    {
        this.guilt = guilt;
    }

    public GuiltSyncPacket(FriendlyByteBuf buffer)
    {
        this.guilt = buffer.readInt();
    }

    public void toBytes(FriendlyByteBuf buffer)
    {
        buffer.writeInt(this.guilt);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() ->
            net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT, () -> () ->
            {
                // Notice there are no imports at the top of the file for this.
                // We type the whole path out so the server JVM completely ignores it.
                net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();

                if (mc.player != null)
                {
                    // Open the client player's backpack and shove the new guilt number in
                    mc.player.getCapability(GuiltProvider.PLAYER_GUILT).ifPresent(cap ->
                    {
                        cap.setGuilt(this.guilt);
                    });
                }
            }));
        context.setPacketHandled(true);
    }
}

