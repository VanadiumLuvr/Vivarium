package org.Enderfan.vivarium.world;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class WorldHeartStructure extends Structure
{
    // i'm assuming you have a codec registered somewhere. if not, have fun with that.
    public static final Codec<WorldHeartStructure> CODEC = simpleCodec(WorldHeartStructure::new);

    public WorldHeartStructure(Structure.StructureSettings settings)
    {
        super(settings);
    }

    @Override
    public @NotNull Optional<GenerationStub> findGenerationPoint(Structure.GenerationContext context)
    {
        // put it deep underground. like y -40.
        // i'm not doing the math to find a "perfect" spot.
        BlockPos pos = new BlockPos(context.chunkPos().getMiddleBlockX(), -40, context.chunkPos().getMiddleBlockZ());

        return Optional.of(new Structure.GenerationStub(pos, (builder) ->
        {
            builder.addPiece(new WorldHeartPiece(pos));
        }));
    }

    @Override
    public @NotNull StructureType<?> type()
    {
        return ModStructures.WORLD_HEART.get(); // go register this. or don't.
    }
}
