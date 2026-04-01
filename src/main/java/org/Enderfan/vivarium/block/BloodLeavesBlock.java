package org.Enderfan.vivarium.block;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
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

        // 1 in 15 chance per tick to drop a particle per leaf block
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
}