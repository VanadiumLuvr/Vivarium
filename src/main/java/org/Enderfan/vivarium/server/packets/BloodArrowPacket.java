package org.Enderfan.vivarium.server.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.Enderfan.vivarium.client.renderers.BloodArrowRenderer;

import java.util.function.Supplier;

public class BloodArrowPacket
{
    private final BlockPos hitPos;
    private final Direction face;
    private final BlockPos targetPos;

    public BloodArrowPacket(BlockPos hitPos, Direction face, BlockPos targetPos)
    {
        this.hitPos = hitPos;
        this.face = face;
        this.targetPos = targetPos;
    }

    // reads the data from the server into the client
    public BloodArrowPacket(FriendlyByteBuf buffer)
    {
        this.hitPos = buffer.readBlockPos();
        this.face = buffer.readEnum(Direction.class);
        this.targetPos = buffer.readBlockPos();
    }

    // writes the data from the server into the network pipe
    public void toBytes(FriendlyByteBuf buffer)
    {
        buffer.writeBlockPos(this.hitPos);
        buffer.writeEnum(this.face);
        buffer.writeBlockPos(this.targetPos);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() ->
        {
            // This runs on the Client and passes the data to the renderer we wrote earlier
            BloodArrowRenderer.addArrow(this.hitPos, this.face, this.targetPos);
        });
        context.setPacketHandled(true);
    }
}
