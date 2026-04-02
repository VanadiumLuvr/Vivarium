package org.Enderfan.vivarium.server.packets;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class HeatWaveStatePacket
{
    private final boolean isActive;

    public HeatWaveStatePacket(boolean isActive)
    {
        this.isActive = isActive;
    }

    public HeatWaveStatePacket(FriendlyByteBuf buf)
    {
        this.isActive = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf)
    {
        buf.writeBoolean(isActive);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier)
    {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() ->
        {
            if (Minecraft.getInstance().player != null)
            {
                Minecraft.getInstance().player.getPersistentData().putBoolean("vivarium_heat_wave_active", isActive);
            }
        });
        context.setPacketHandled(true);
        return true;
    }
}