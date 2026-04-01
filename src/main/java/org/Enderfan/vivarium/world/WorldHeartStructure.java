package org.Enderfan.vivarium.world;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class WorldHeartStructure extends Structure
{
    public static final Codec<WorldHeartStructure> CODEC = simpleCodec(WorldHeartStructure::new);

    public WorldHeartStructure(Structure.StructureSettings settings)
    {
        super(settings);
    }

    @Override
    public @NotNull Optional<GenerationStub> findGenerationPoint(Structure.GenerationContext context)
    {
        int x = context.chunkPos().getMiddleBlockX();
        int z = context.chunkPos().getMiddleBlockZ();

        BlockPos heartPos = new BlockPos(x, -40, z);

        // Ask the engine directly where the highest solid ground is at these coordinates
        int surfaceY = context.chunkGenerator().getBaseHeight(
                x, z,
                Heightmap.Types.WORLD_SURFACE_WG,
                context.heightAccessor(),
                context.randomState()
        );

        BlockPos treePos = new BlockPos(x, surfaceY, z);

        return Optional.of(new Structure.GenerationStub(heartPos, (builder) ->
        {
            // Spawn the bloody core deep underground
            builder.addPiece(new WorldHeartPiece(heartPos));

            // Spawn the ominous tree on the surface directly above it
            builder.addPiece(new WorldTreePiece(treePos));
        }));
    }

    @Override
    public @NotNull StructureType<?> type()
    {
        return ModStructures.WORLD_HEART.get();
    }
}