package org.Enderfan.vivarium.entities;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.level.LevelReader;

// TWEAK
public class BirdPerchAndSingGoal extends MoveToBlockGoal
{
    private final BirdEntity bird;
    private int singCooldown = 0;
    private int perchTime = 0;

    public BirdPerchAndSingGoal(BirdEntity bird, double speed, int searchRange)
    {
        super(bird, speed, searchRange);
        this.bird = bird;
    }

    @Override
    public boolean canUse()
    {
        // Only try to find a tree occasionally, otherwise it will relentlessly snap to leaves
        return this.bird.getRandom().nextInt(50) == 0 && super.canUse();
    }

    @Override
    public void start()
    {
        super.start();
        this.perchTime = 0;
        this.singCooldown = 10; // Sing almost immediately after landing
    }

    @Override
    protected boolean isValidTarget(LevelReader level, BlockPos pos)
    {
        // Target any block tagged as leaves, but ONLY if the block above it is air so we can stand on it
        return level.getBlockState(pos).is(net.minecraft.tags.BlockTags.LEAVES)
                && level.getBlockState(pos.above()).isAir();
    }

    @Override
    public void tick()
    {
        super.tick();

        // Vanilla logic says we have arrived at the leaf block
        if (this.isReachedTarget())
        {
            this.perchTime++;
            this.singCooldown--;

            // 1. Play the song!
            if (this.singCooldown <= 0)
            {
                this.bird.level().playSound(null, this.bird.blockPosition(),
                        org.Enderfan.vivarium.ModSounds.BIRD_SONG.get(),
                        SoundSource.AMBIENT,
                        1.0F,
                        this.bird.getVoicePitch());

                // Wait 3 to 6 seconds before singing again
                this.singCooldown = 120 + this.bird.getRandom().nextInt(60);
            }

            // 2. Stop perching after about 15 seconds so it can go hunt or stroll
            if (this.perchTime > 300)
            {
                this.stop();
            }
        }
    }
}