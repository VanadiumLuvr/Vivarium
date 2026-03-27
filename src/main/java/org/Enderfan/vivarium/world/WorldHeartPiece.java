package org.Enderfan.vivarium.world;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import org.Enderfan.vivarium.entities.ModEntities;
import org.Enderfan.vivarium.entities.WorldHeartEntity;

public class WorldHeartPiece extends StructurePiece
{
    // you actually have to tell java this exists or it panics. obviously.
    protected final BlockPos templatePosition;

    public WorldHeartPiece(BlockPos pos)
    {
        // change ModStructurePieces to ModStructures if thats where you put the registry
        super(ModStructures.WORLD_HEART_PIECE.get(), 0, new BoundingBox(pos).inflatedBy(10));
        this.templatePosition = pos;
    }

    public WorldHeartPiece(CompoundTag tag)
    {
        super(ModStructures.WORLD_HEART_PIECE.get(), tag);
        this.templatePosition = new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"));
    }

    // put the context parameter in here even if we literally don't use it
    public WorldHeartPiece(StructurePieceSerializationContext context, CompoundTag tag)
    {
        super(ModStructures.WORLD_HEART_PIECE.get(), tag);
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
        int radius = 20;
        BlockPos center = this.templatePosition;


        for (int x = -radius; x <= radius; x++)
        {
            for (int y = -radius; y <= radius; y++)
            {
                for (int z = -radius; z <= radius; z++)
                {
                    double distance = Math.sqrt(x*x + y*y + z*z);
                    if (distance < radius)
                    {
                        BlockPos current = center.offset(x, y, z);
                        // 2 is the flag for "don't update neighbors" so it doesn't lag out
                        level.setBlock(current, Blocks.WATER.defaultBlockState(), 2);
                    }
                }
            }
        }

        // check if the center block is inside the current chunk boundary.
        // if we don't do this, it might spawn 4 hearts as the surrounding chunks load.
        if (box.isInside(center))
        {
            // change ModEntities.WORLD_HEART to whatever your registry object is called
            WorldHeartEntity heart = ModEntities.WORLD_HEART.get().create(level.getLevel());

            if (heart != null)
            {
                // add 0.5 so it sits perfectly in the middle of the block instead of the corner
                heart.setPos(center.getX() + 0.5, center.getY()-10, center.getZ() + 0.5);
                heart.setPersistenceRequired();
                level.addFreshEntity(heart);
            }
        }
    }
}
