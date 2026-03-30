package org.Enderfan.vivarium.server.packets;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class HeatSyncPacket
{
    private final int heatLevel;

    public HeatSyncPacket(int heatLevel)
    {
        this.heatLevel = heatLevel;
    }

    public HeatSyncPacket(FriendlyByteBuf buf)
    {
        this.heatLevel = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf)
    {
        buf.writeInt(heatLevel);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier)
    {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() ->
        {
            // We are now on the client side. Inject the number directly into the player's NBT!
            if (Minecraft.getInstance().player != null)
            {
                Minecraft.getInstance().player.getPersistentData().putInt("vivarium_heat_level", heatLevel);
            }
        });
        context.setPacketHandled(true);
        return true;
    }
}