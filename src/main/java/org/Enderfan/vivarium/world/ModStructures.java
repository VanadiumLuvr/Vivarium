package org.Enderfan.vivarium.world;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModStructures
{
    public static final DeferredRegister<StructureType<?>> STRUCTURES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, "vivarium");

    // the piece registry is different because mojang likes to make things complicated
    public static final DeferredRegister<StructurePieceType> PIECES =
            DeferredRegister.create(Registries.STRUCTURE_PIECE, "vivarium");

    public static final RegistryObject<StructureType<WorldHeartStructure>> WORLD_HEART =
            STRUCTURES.register("world_heart", () -> () -> WorldHeartStructure.CODEC);

    // this thing is what actually lets the game load the piece from your save file
    public static final RegistryObject<StructurePieceType> WORLD_HEART_PIECE =
            PIECES.register("world_heart_piece", () -> WorldHeartPiece::new);

    public static final RegistryObject<StructurePieceType> WORLD_TREE_PIECE =
            PIECES.register("world_tree_piece", () -> WorldTreePiece::new);

    public static void register(IEventBus eventBus)
    {
        STRUCTURES.register(eventBus);
        PIECES.register(eventBus);
    }
}
