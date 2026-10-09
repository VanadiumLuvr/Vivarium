package org.Enderfan.vivarium.server;

import net.minecraft.nbt.CompoundTag;
import org.Enderfan.vivarium.config.VivariumConfig;

public class PlayerGuilt
{
    private int guilt = 0;
    private int logsBroken = 0;
    private boolean hasTriggeredFirstBleed = false;
    private boolean hasTriggeredCaveIn = false;
    public boolean hasDreamt = false;

    public int getGuilt()
    {
        return guilt;
    }

    public boolean hasTriggeredCaveIn() { return hasTriggeredCaveIn; }

    public void setTriggeredCaveIn(boolean value) { this.hasTriggeredCaveIn = value; }

    public boolean hasDreamt()
    {
        return hasDreamt;
    }

    public void hasDreamt(boolean nbtDreamt) { hasDreamt = nbtDreamt; }

    public void setGuilt(int value)
    {
        this.guilt = value;
    }

    public void addGuilt(int amount)
    {
        this.guilt += (int) (amount * VivariumConfig.PACE.get());
    }

    public void saveNBTData(CompoundTag nbt) {
        nbt.putInt("guilt", this.guilt);
        nbt.putInt("logsBroken", this.logsBroken);
        nbt.putBoolean("firstBleed", this.hasTriggeredFirstBleed);
        nbt.putBoolean("hasDreamt", this.hasDreamt);
        nbt.putBoolean("caveInTriggered", this.hasTriggeredCaveIn);
    }

    public void loadNBTData(CompoundTag nbt)
    {
        this.guilt = nbt.getInt("guilt");
        this.logsBroken = nbt.getInt("logsBroken");
        this.hasTriggeredFirstBleed = nbt.getBoolean("firstBleed");
        this.hasDreamt = nbt.getBoolean("hasDreamt");
        this.hasTriggeredCaveIn = nbt.getBoolean("caveInTriggered");
    }

    public int getLogsBroken()
    {
        return logsBroken;
    }

    public void setLogsBroken(int value)
    {
        this.logsBroken = value;
    }

    public void incrementLogsBroken()
    {
        this.logsBroken++;
    }

    public boolean hasTriggeredFirstBleed()
    {
        return hasTriggeredFirstBleed;
    }

    public void setTriggeredFirstBleed(boolean value)
    {
        this.hasTriggeredFirstBleed = value;
    }

}
