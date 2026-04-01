package org.Enderfan.vivarium.block;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.Enderfan.vivarium.particles.ModParticles;

public class BloodLeavesBlock extends LeavesBlock
{
    public BloodLeavesBlock(Properties properties)
    {
        super(properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random)
    {
        super.animateTick(state, level, pos, random);

        if (random.nextInt(15) == 0)
        {
            BlockPos below = pos.below();
            if (level.isEmptyBlock(below))
            {
                double d0 = pos.getX() + random.nextDouble();
                double d1 = pos.getY() - 0.05D;
                double d2 = pos.getZ() + random.nextDouble();
                level.addParticle(ModParticles.BLOOD_DRIP.get(), d0, d1, d2, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    // Tells the engine this block reduces passing light by exactly 0 levels (like Glass)
    @Override
    public int getLightBlock(BlockState state, BlockGetter level, BlockPos pos)
    {
        return 0;
    }

    // Forces skylight to fall straight through the block without stopping
    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos)
    {
        return true;
    }
}