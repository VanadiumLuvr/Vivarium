package org.Enderfan.vivarium.events;

import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.Enderfan.vivarium.Vivarium;
import org.Enderfan.vivarium.entities.*;

@Mod.EventBusSubscriber(modid = Vivarium.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModAttributeEvents
{
    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event)
    {
        // Bind the stats we just created to your registered Butterfly entity
        event.put(ModEntities.BUTTERFLY.get(), ButterflyEntity.createAttributes().build());

        event.put(ModEntities.GRASSHOPPER.get(), GrasshopperEntity.createAttributes().build());

        event.put(ModEntities.WORLD_HEART.get(), WorldHeartEntity.createAttributes().build());

        event.put(ModEntities.BIRD.get(), BirdEntity.createAttributes().build());
    }

    @SubscribeEvent
    public static void registerSpawnPlacements(net.minecraftforge.event.entity.SpawnPlacementRegisterEvent event)
    {
        event.register(
                org.Enderfan.vivarium.entities.ModEntities.BUTTERFLY.get(),
                net.minecraft.world.entity.SpawnPlacements.Type.ON_GROUND,
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                ButterflyEntity::checkButterflySpawnRules,
                net.minecraftforge.event.entity.SpawnPlacementRegisterEvent.Operation.REPLACE
        );

        event.register(
                org.Enderfan.vivarium.entities.ModEntities.GRASSHOPPER.get(),
                net.minecraft.world.entity.SpawnPlacements.Type.ON_GROUND,
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                net.minecraft.world.entity.animal.Animal::checkAnimalSpawnRules,
                net.minecraftforge.event.entity.SpawnPlacementRegisterEvent.Operation.REPLACE
        );

        event.register(
                org.Enderfan.vivarium.entities.ModEntities.BIRD.get(),
                net.minecraft.world.entity.SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING,
                BirdEntity::checkBirdSpawnRules,
                net.minecraftforge.event.entity.SpawnPlacementRegisterEvent.Operation.REPLACE
        );
    }
}
