package org.Enderfan.vivarium.server; // Change to your actual package

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;
import org.Enderfan.vivarium.entities.WorldHeartEntity;

import java.util.function.Supplier;

public class StrikeHeartPacket
{
    private final int entityId;

    public StrikeHeartPacket(int entityId)
    {
        this.entityId = entityId;
    }

    public StrikeHeartPacket(FriendlyByteBuf buffer)
    {
        this.entityId = buffer.readInt();
    }

    public void toBytes(FriendlyByteBuf buffer)
    {
        buffer.writeInt(this.entityId);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() ->
        {
            ServerPlayer player = context.getSender();
            if (player != null)
            {
                Entity target = player.level().getEntity(this.entityId);

                if (target instanceof WorldHeartEntity heart)
                {
                    // 2500.0D is 50 blocks of distance squared.
                    // This easily covers you hovering 28 blocks in the air above its feet.
                    if (player.distanceToSqr(heart) < 2500.0D)
                    {
                        heart.hurt(player.damageSources().playerAttack(player), 1.0f);
                    }
                }
            }
        });
        context.setPacketHandled(true);
    }
}