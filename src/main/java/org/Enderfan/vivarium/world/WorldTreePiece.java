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
import org.Enderfan.vivarium.fluid.ModFluids;

import java.util.ArrayList;
import java.util.List;

public class WorldTreePiece extends StructurePiece
{
    protected final BlockPos templatePosition;

    private static class BranchData
    {
        BlockPos start; float angle; float pitch; int length; int startRadius;
    }

    public WorldTreePiece(BlockPos pos)
    {
        // Massively inflated so the deep roots and wide canopy all safely generate across chunks
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
        int treeHeight = 225;

        int absoluteMaxY = level.getMaxBuildHeight() - center.getY() - 1;
        treeHeight = Math.min(treeHeight, absoluteMaxY - 30);

        BlockState log = Blocks.DARK_OAK_LOG.defaultBlockState();
        BlockState leaves = ModBlocks.BLOOD_LEAVES.get().defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
        BlockState blood = ModFluids.SOURCE_BLOOD.get().defaultFluidState().createLegacyBlock();

        RandomSource treeRandom = RandomSource.create(center.asLong());

        List<BranchData> branches = new ArrayList<>();
        List<BranchData> roots = new ArrayList<>();
        List<BlockPos> leafClouds = new ArrayList<>();

        // --- PHASE 1: PRE-CALCULATE THE SKELETON ---

        float driftX = 0, driftZ = 0;
        float driftVelX = 0, driftVelZ = 0;

        // Start 25 blocks deep to anchor the massive base into uneven terrain
        for (int y = -25; y < treeHeight; y++)
        {
            // Only apply the sway above ground so the anchor stays solid
            if (y >= 0)
            {
                driftVelX += (treeRandom.nextFloat() - 0.5f) * 0.4f;
                driftVelZ += (treeRandom.nextFloat() - 0.5f) * 0.4f;
                driftVelX *= 0.9f;
                driftVelZ *= 0.9f;
                driftX += driftVelX;
                driftZ += driftVelZ;
            }

            // Clamp progress at 0 for negative Y values so the underground base doesn't taper in reverse
            float progress = Math.max(0, (float) y / treeHeight);
            float taper = (float) Math.pow(1.0f - progress, 3.0);
            float baseRadius = 24.0f;
            float currentRadius = Math.max(2.0f, baseRadius * taper);

            int cx = center.getX() + (int)driftX;
            int cy = center.getY() + y;
            int cz = center.getZ() + (int)driftZ;

            int rInt = (int) Math.ceil(currentRadius) + 4;
            for (int x = -rInt; x <= rInt; x++)
            {
                for (int z = -rInt; z <= rInt; z++)
                {
                    double angle = Math.atan2(z, x);
                    double dist = Math.sqrt(x*x + z*z);

                    double ribs = Math.sin(angle * 5 + cy * 0.05) * (currentRadius * 0.3);
                    double roughness = Math.cos(angle * 13 - cy * 0.2) * (currentRadius * 0.05);

                    if (dist <= currentRadius + ribs + roughness)
                    {
                        double veinNoise = Math.sin(x * 0.4) * Math.cos(z * 0.4) + Math.sin(cy * 0.15);
                        boolean isVein = (dist < currentRadius * 0.35) && (veinNoise > 0.6);

                        if (isVein)
                        {
                            placeBlock(level, box, new BlockPos(cx+x, cy, cz+z), blood);
                        }
                        else
                        {
                            placeBlock(level, box, new BlockPos(cx+x, cy, cz+z), log);
                        }
                    }
                }
            }

            // Only generate branches above ground
            if (y > treeHeight * 0.3f && treeRandom.nextInt(6) == 0)
            {
                BranchData b = new BranchData();
                b.start = new BlockPos(cx, cy, cz);
                b.angle = treeRandom.nextFloat() * (float)Math.PI * 2f;
                b.pitch = 0.2f + treeRandom.nextFloat() * 0.6f;
                b.length = (int)((40 + treeRandom.nextInt(50)) * (1.0f - progress));
                b.startRadius = Math.max(1, (int)(currentRadius * 0.6f));
                branches.add(b);
            }
        }

        // Seed Roots
        int numRoots = 12 + treeRandom.nextInt(6);
        for (int i = 0; i < numRoots; i++)
        {
            BranchData r = new BranchData();
            r.angle = ((float)i / numRoots) * (float)Math.PI * 2f + (treeRandom.nextFloat() * 0.5f);
            r.startRadius = 5 + treeRandom.nextInt(4);
            roots.add(r);
        }

        // --- PHASE 2: DRAW THE SKELETON INTO THE CHUNKS ---

        // DRAW PARASITE ROOTS
        float targetY = -42; // Go slightly past the heart to cradle it
        float startY = center.getY() + 4;
        float totalY = startY - targetY;
        int rootSteps = (int)(totalY * 2.0f); // High resolution to prevent gaps while tunneling

        for (BranchData r : roots)
        {
            for (int step = 0; step <= rootSteps; step++)
            {
                float progress = (float)step / rootSteps;

                // Parametric Cage: Bow outwards up to 30 blocks away, then squeeze back to 0 exactly at the heart
                float spread = 4.0f + (float)Math.sin(progress * Math.PI) * 30.0f;

                // Slowly spiral as they plunge into the earth
                float currentAngle = r.angle + (progress * 4.0f);

                float rx = center.getX() + (float)Math.cos(currentAngle) * spread;
                float ry = startY - (progress * totalY);
                float rz = center.getZ() + (float)Math.sin(currentAngle) * spread;

                int radius = Math.max(2, Math.round(r.startRadius * (1.0f - progress)));
                fillSphere(level, box, new BlockPos((int)rx, (int)ry, (int)rz), radius, log);
            }
        }

        // DRAW BRANCHES
        for (BranchData b : branches)
        {
            float bx = b.start.getX(), by = b.start.getY(), bz = b.start.getZ();
            float currentPitch = b.pitch;

            for (int step = 0; step < b.length; step++)
            {
                bx += Math.cos(b.angle) * Math.cos(currentPitch);
                by += Math.sin(currentPitch);
                bz += Math.sin(b.angle) * Math.cos(currentPitch);
                currentPitch -= 0.02f;

                int radius = Math.max(1, Math.round(b.startRadius * (1.0f - (float)step/b.length)));
                BlockPos bPos = new BlockPos((int)bx, (int)by, (int)bz);

                fillSphere(level, box, bPos, radius, log);

                if (step == b.length - 1) leafClouds.add(bPos);
            }
        }

        // DRAW ORGANIC LEAF CLOUDS
        for (BlockPos cloudCenter : leafClouds)
        {
            RandomSource cloudRandom = RandomSource.create(cloudCenter.asLong());
            int clusterRadius = 12 + cloudRandom.nextInt(8);

            BoundingBox cloudBox = new BoundingBox(
                    cloudCenter.getX() - clusterRadius, cloudCenter.getY() - clusterRadius, cloudCenter.getZ() - clusterRadius,
                    cloudCenter.getX() + clusterRadius, cloudCenter.getY() + clusterRadius, cloudCenter.getZ() + clusterRadius
            );
            if (!box.intersects(cloudBox)) continue;

            int size = clusterRadius * 2 + 1;
            boolean[][][] virtualCloud = new boolean[size][size][size];

            // 1. Generate the raw, noisy cloud strictly in local memory (extremely fast)
            for (int lx = -clusterRadius; lx <= clusterRadius; lx++)
            {
                for (int ly = -clusterRadius; ly <= clusterRadius; ly++)
                {
                    for (int lz = -clusterRadius; lz <= clusterRadius; lz++)
                    {
                        double dist = Math.sqrt(lx*lx + ly*ly + lz*lz);
                        double distortion = Math.sin(lx * 0.6) * Math.cos(ly * 0.6) * Math.sin(lz * 0.6) * 3.5;

                        if (dist + distortion <= clusterRadius && cloudRandom.nextFloat() < 0.85f)
                        {
                            virtualCloud[lx + clusterRadius][ly + clusterRadius][lz + clusterRadius] = true;
                        }
                    }
                }
            }

            // Force the absolute center to exist so our flood-fill always has a starting anchor
            virtualCloud[clusterRadius][clusterRadius][clusterRadius] = true;

            // 2. Virtual Flood-Fill: Isolate the main contiguous body of the cloud
            boolean[][][] connectedBody = new boolean[size][size][size];
            java.util.Queue<int[]> queue = new java.util.LinkedList<>();

            queue.add(new int[]{clusterRadius, clusterRadius, clusterRadius});
            connectedBody[clusterRadius][clusterRadius][clusterRadius] = true;

            int[][] dirs = {{1,0,0}, {-1,0,0}, {0,1,0}, {0,-1,0}, {0,0,1}, {0,0,-1}};

            while (!queue.isEmpty())
            {
                int[] curr = queue.poll();
                for (int[] dir : dirs)
                {
                    int nx = curr[0] + dir[0];
                    int ny = curr[1] + dir[1];
                    int nz = curr[2] + dir[2];

                    // If the neighbor is inside the array, is a leaf, and hasn't been checked yet
                    if (nx >= 0 && nx < size && ny >= 0 && ny < size && nz >= 0 && nz < size)
                    {
                        if (virtualCloud[nx][ny][nz] && !connectedBody[nx][ny][nz])
                        {
                            connectedBody[nx][ny][nz] = true;
                            queue.add(new int[]{nx, ny, nz});
                        }
                    }
                }
            }

            // 3. Paste ONLY the connected body into the physical world!
            for (int lx = -clusterRadius; lx <= clusterRadius; lx++)
            {
                for (int ly = -clusterRadius; ly <= clusterRadius; ly++)
                {
                    for (int lz = -clusterRadius; lz <= clusterRadius; lz++)
                    {
                        // Any floaters generated by the noise were left as 'false' in connectedBody, effectively deleting them
                        if (connectedBody[lx + clusterRadius][ly + clusterRadius][lz + clusterRadius])
                        {
                            BlockPos lPos = cloudCenter.offset(lx, ly, lz);

                            // The chunk border safety check happens AFTER the cloud is fully calculated
                            if (box.isInside(lPos) && level.getBlockState(lPos).isAir())
                            {
                                level.setBlock(lPos, leaves, 2);
                            }
                        }
                    }
                }
            }
        }
    }

    private void fillSphere(WorldGenLevel level, BoundingBox box, BlockPos center, int radius, BlockState state)
    {
        for (int x = -radius; x <= radius; x++)
        {
            for (int y = -radius; y <= radius; y++)
            {
                for (int z = -radius; z <= radius; z++)
                {
                    if (x*x + y*y + z*z <= radius*radius)
                    {
                        placeBlock(level, box, center.offset(x, y, z), state);
                    }
                }
            }
        }
    }

    private void placeBlock(WorldGenLevel level, BoundingBox box, BlockPos pos, BlockState state)
    {
        if (box.isInside(pos))
        {
            level.setBlock(pos, state, 2);
        }
    }
}