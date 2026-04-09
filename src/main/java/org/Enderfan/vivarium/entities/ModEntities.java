package org.Enderfan.vivarium.entities;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.Enderfan.vivarium.Vivarium;

public class ModEntities
{
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, Vivarium.MODID);

    public static final RegistryObject<EntityType<BloodPoolEntity>> BLOOD_POOL = ENTITIES.register("blood_pool",
            () -> EntityType.Builder.<BloodPoolEntity>of(BloodPoolEntity::new, MobCategory.MISC)
                    .sized(6.0f, 0.125f)  // Wide BB for spanning blocks, tiny height
                    .build("blood_pool"));

    public static final RegistryObject<EntityType<WorldHeartEntity>> WORLD_HEART =
            ENTITIES.register("world_heart", () -> EntityType.Builder.of(WorldHeartEntity::new, MobCategory.CREATURE)
                    .sized(15.0f, 28.0f) // make it huge
                    .clientTrackingRange(10)
                    .build("world_heart"));

    public static final RegistryObject<EntityType<ButterflyEntity>> BUTTERFLY =
            ENTITIES.register("butterfly", () -> EntityType.Builder.of(ButterflyEntity::new, MobCategory.AMBIENT)
                    .sized(0.5f, 0.3f) // make it tiny
                    .build("butterfly"));

    public static final RegistryObject<EntityType<GrasshopperEntity>> GRASSHOPPER =
            ENTITIES.register("grasshopper", () -> EntityType.Builder.of(GrasshopperEntity::new, MobCategory.AMBIENT)
                    .sized(0.4f, 0.4f) // Grasshoppers are small!
                    .clientTrackingRange(8)
                    .build("grasshopper"));

    public static final RegistryObject<EntityType<BirdEntity>> BIRD =
            ENTITIES.register("bird", () -> EntityType.Builder.of(BirdEntity::new, MobCategory.AMBIENT)
                    .sized(0.6f, 0.6f)
                    .clientTrackingRange(8)
                    .build("bird"));
}
