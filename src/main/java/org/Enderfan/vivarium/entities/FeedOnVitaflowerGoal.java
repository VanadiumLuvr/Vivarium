package org.Enderfan.vivarium.entities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import org.Enderfan.vivarium.block.VitaflowerBlock;

public class FeedOnVitaflowerGoal extends MoveToBlockGoal
{
    private final ButterflyEntity butterfly;
    private int eatingTimer = 0;

    public FeedOnVitaflowerGoal(ButterflyEntity butterfly, double speedModifier, int searchRange)
    {
        super(butterfly, speedModifier, searchRange);
        this.butterfly = butterfly;
    }

    @Override
    protected boolean isValidTarget(LevelReader level, BlockPos pos)
    {
        // 1. Completely ignore the flower if it is the exact same one we just ate from
        if (pos.equals(this.butterfly.getLastFlowerPos()))
        {
            return false;
        }

        BlockState state = level.getBlockState(pos);

        // 2. Only target open Vitaflowers
        if (state.getBlock() instanceof VitaflowerBlock)
        {
            return state.getValue(VitaflowerBlock.STATE) == 0;
        }

        return false;
    }

    @Override
    public void start()
    {
        super.start();
        this.eatingTimer = 0;
        this.butterfly.setFeeding(false); // Reset just in case
    }

    @Override
    public void stop()
    {
        super.stop();
        this.butterfly.setFeeding(false); // Make sure it starts flying again when done
    }

    @Override
    public void tick()
    {
        super.tick();

        // Vanilla says we are in the general vicinity of the flower
        if (this.isReachedTarget())
        {
            // Calculate the absolute dead-center of the block
            // (You might need to adjust the Y value depending on how tall your flower model is)
            double centerX = this.blockPos.getX() + 0.5;
            double centerY = this.blockPos.getY() + 0.8;
            double centerZ = this.blockPos.getZ() + 0.5;

            net.minecraft.world.phys.Vec3 centerPos = new net.minecraft.world.phys.Vec3(centerX, centerY, centerZ);
            net.minecraft.world.phys.Vec3 currentPos = this.butterfly.position();
            net.minecraft.world.phys.Vec3 moveVector = centerPos.subtract(currentPos);

            // If we are more than 0.1 blocks away from the exact center, gently glide towards it
            if (moveVector.length() > 0.1)
            {
                this.butterfly.setDeltaMovement(moveVector.normalize().scale(0.05));
            }
            else
            {
                // We hit dead center. Lock it down.
                this.butterfly.setDeltaMovement(0, 0, 0);
                this.butterfly.setPos(centerX, centerY, centerZ);

                // Trigger the landing animation via the synched data
                this.butterfly.setFeeding(true);

                this.eatingTimer++;

                if (this.eatingTimer >= 60)
                {
                    this.butterfly.resetHunger();
                    this.butterfly.setLastFlowerPos(this.blockPos);
                    this.stop();
                }
            }
        }
    }
}