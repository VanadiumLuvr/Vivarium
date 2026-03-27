package org.Enderfan.vivarium.server;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class GuiltProvider implements ICapabilitySerializable<CompoundTag>
{
    public static Capability<PlayerGuilt> PLAYER_GUILT = CapabilityManager.get(new CapabilityToken<>() { });

    private PlayerGuilt guilt = null;
    private final LazyOptional<PlayerGuilt> optional = LazyOptional.of(this::createPlayerGuilt);

    private PlayerGuilt createPlayerGuilt()
    {
        if (this.guilt == null)
        {
            this.guilt = new PlayerGuilt();
        }
        return this.guilt;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side)
    {
        if (cap == PLAYER_GUILT)
        {
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT()
    {
        CompoundTag nbt = new CompoundTag();
        PlayerGuilt data = createPlayerGuilt();

        nbt.putInt("guilt", data.getGuilt());
        nbt.putInt("logsBroken", data.getLogsBroken());
        nbt.putBoolean("hasTriggeredFirstBleed", data.hasTriggeredFirstBleed());

        // This actually saves it to the hard drive when they log out
        nbt.putBoolean("hasDreamt", data.hasDreamt());

        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt)
    {
        PlayerGuilt data = createPlayerGuilt();

        data.setGuilt(nbt.getInt("guilt"));

        if (nbt.contains("logsBroken"))
        {
            for(int i = 0; i < nbt.getInt("logsBroken"); i++) data.incrementLogsBroken();
        }

        data.setTriggeredFirstBleed(nbt.getBoolean("hasTriggeredFirstBleed"));

        // This pulls it back out of the hard drive when they log in
        data.hasDreamt(nbt.getBoolean("hasDreamt"));
    }
}
