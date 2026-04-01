package org.Enderfan.vivarium.world;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import org.Enderfan.vivarium.block.ModBlocks;

public class WorldTreePiece extends StructurePiece
{
    protected final BlockPos templatePosition;

    public WorldTreePiece(BlockPos pos)
    {
        // Massively inflated to accommodate the new 180-block wide canopy
        super(ModStructures.WORLD_TREE_PIECE.get(), 0, new BoundingBox(pos).inflatedBy(120));
        this.templatePosition = pos;
    }

    public WorldTreePiece(CompoundTag tag)
    {
        super(ModStructures.WORLD_TREE_PIECE.get(), tag);
        this.templatePosition = new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"));
    }

    public WorldTreePiece(StructurePieceSerializationContext context, CompoundTag tag)
    {
        super(ModStructures.WORLD_TREE_PIECE.get(), tag);
        this.templatePosition = new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"));
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag)
    {
        tag.putInt("x", this.templatePosition.getX());
        tag.putInt("y", this.templatePosition.getY());
        tag.putInt("z", this.templatePosition.getZ());
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager manager, ChunkGenerator generator, RandomSource random, BoundingBox box, ChunkPos chunkPos, BlockPos pos)
    {
        BlockPos center = this.templatePosition;

        // 5x Scale Dimensions
        int treeHeight = 225;

        BlockState log = Blocks.DARK_OAK_LOG.defaultBlockState();

        // Setting PERSISTENT to true permanently disables vanilla leaf decay
        BlockState leaves = ModBlocks.BLOOD_LEAVES.get().defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);

        // 1. Build the colossal tapering trunk
        for (int y = 0; y < treeHeight; y++)
        {
            // Stop building if we hit the top of the world
            if (center.getY() + y >= level.getMaxBuildHeight()) break;

            float progress = (float) y / treeHeight;
            // Base radius is now 20 blocks thick, tapering to 1 at the top
            int currentRadius = Math.max(1, Math.round(20 * (1.0f - progress)));

            for (int x = -currentRadius; x <= currentRadius; x++)
            {
                for (int z = -currentRadius; z <= currentRadius; z++)
                {
                    if (x * x + z * z <= currentRadius * currentRadius + 1)
                    {
                        BlockPos logPos = center.offset(x, y, z);
                        if (box.isInside(logPos))
                        {
                            level.setBlock(logPos, log, 2);
                        }
                    }
                }
            }
        }

        // 2. Build the sky-blotting canopy
        int canopyStart = treeHeight - 100;
        int canopyRadiusXZ = 90;
        int canopyRadiusY = 60;

        // Calculate maximum safe height so the canopy doesn't clip out of the world bounds
        int absoluteMaxY = level.getMaxBuildHeight() - center.getY() - 1;
        int canopyEnd = Math.min(treeHeight + 20, absoluteMaxY);

        for (int y = canopyStart; y <= canopyEnd; y++)
        {
            for (int x = -canopyRadiusXZ; x <= canopyRadiusXZ; x++)
            {
                for (int z = -canopyRadiusXZ; z <= canopyRadiusXZ; z++)
                {
                    double dy = y - (canopyStart + canopyRadiusY);
                    double dist = (x * x) / (double)(canopyRadiusXZ * canopyRadiusXZ)
                            + (dy * dy) / (double)(canopyRadiusY * canopyRadiusY)
                            + (z * z) / (double)(canopyRadiusXZ * canopyRadiusXZ);

                    if (dist <= 1.0 && random.nextFloat() < 0.8f)
                    {
                        BlockPos leafPos = center.offset(x, y, z);
                        if (box.isInside(leafPos) && level.getBlockState(leafPos).isAir())
                        {
                            level.setBlock(leafPos, leaves, 2);
                        }
                    }
                }
            }
        }
    }
}