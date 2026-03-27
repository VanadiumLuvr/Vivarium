package org.Enderfan.vivarium.server;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

public class WorldHeartState extends SavedData
{
    private boolean isWorldDead = false;

    public boolean isWorldDead()
    {
        return this.isWorldDead;
    }

    public void killWorld()
    {
        this.isWorldDead = true;
        this.setDirty(); // tells the game to save this to the hard drive
    }

    public void setWorldDead(boolean dead)
    {
        this.isWorldDead = dead;
        this.setDirty(); // tells the game to save this to the hard drive
    }

    // reads the nbt file when the world loads
    public static WorldHeartState load(CompoundTag tag)
    {
        WorldHeartState state = new WorldHeartState();
        state.isWorldDead = tag.getBoolean("isWorldDead");
        return state;
    }

    // writes the nbt file when the world saves
    @Override
    public CompoundTag save(CompoundTag tag)
    {
        tag.putBoolean("isWorldDead", this.isWorldDead);
        return tag;
    }

    // magic boilerplate to get the data from the server
    public static WorldHeartState get(ServerLevel level)
    {
        DimensionDataStorage storage = level.getServer().overworld().getDataStorage();
        return storage.computeIfAbsent(WorldHeartState::load, WorldHeartState::new, "vivarium_world_state");
    }
}
