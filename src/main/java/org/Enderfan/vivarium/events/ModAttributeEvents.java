package org.Enderfan.vivarium.events;

import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.Enderfan.vivarium.Vivarium;
import org.Enderfan.vivarium.entities.ButterflyEntity;
import org.Enderfan.vivarium.entities.GrasshopperEntity;
import org.Enderfan.vivarium.entities.ModEntities;
import org.Enderfan.vivarium.entities.WorldHeartEntity;

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
    }
}
