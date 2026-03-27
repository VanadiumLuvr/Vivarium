package org.Enderfan.vivarium.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class VitaflowerBlock extends FlowerBlock
{
    // 0 = Open, 1 = Closed, 2 = Wilted
    public static final IntegerProperty STATE = IntegerProperty.create("state", 0, 2);

    public VitaflowerBlock(Properties properties)
    {
        // the mob effect only matters if they try to craft suspicious stew out of it
        super(() -> MobEffects.HEAL, 5, properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(STATE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(STATE);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random)
    {
        if (state.getValue(STATE) == 1)
        {
            boolean playerNearby = false;

            // Check if any player is still within the 5-block radius (25 block distance squared)
            for (net.minecraft.world.entity.player.Player p : level.players())
            {
                if (p.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) <= 36.0)
                {
                    playerNearby = true;
                    break;
                }
            }

            if (playerNearby)
            {
                // You are still there. Stay closed and check again in 3 seconds.
                level.scheduleTick(pos, this, 60);
            }
            else
            {
                // The coast is clear. Reopen.
                level.setBlockAndUpdate(pos, state.setValue(STATE, 0));
            }
        }
    }
}