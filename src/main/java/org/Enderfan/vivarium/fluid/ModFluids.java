package org.Enderfan.vivarium.fluid;

import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.Enderfan.vivarium.Vivarium;
import org.Enderfan.vivarium.block.ModBlocks;
import org.Enderfan.vivarium.item.ModItems;

import java.util.function.Consumer;

public class ModFluids
{
    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, Vivarium.MODID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(ForgeRegistries.FLUIDS, Vivarium.MODID);

    public static final RegistryObject<FluidType> BLOOD_TYPE = FLUID_TYPES.register("blood",
            () -> new FluidType(FluidType.Properties.create()
                    .descriptionId("block.vivarium.blood")
                    .density(2000)
                    .viscosity(2000)
                    .canSwim(false) // Makes it incredibly thick and hard to escape
                    .canDrown(true)
                    .pathType(BlockPathTypes.LAVA)
                    .adjacentPathType(null)
                    .sound(net.minecraftforge.common.SoundActions.BUCKET_FILL, net.minecraft.sounds.SoundEvents.BUCKET_FILL)
                    .sound(net.minecraftforge.common.SoundActions.BUCKET_EMPTY, net.minecraft.sounds.SoundEvents.BUCKET_EMPTY))
            {
                @Override
                public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer)
                {
                    consumer.accept(new IClientFluidTypeExtensions()
                    {
                        private static final net.minecraft.resources.ResourceLocation WATER_STILL = new net.minecraft.resources.ResourceLocation("block/water_still");
                        private static final net.minecraft.resources.ResourceLocation WATER_FLOW = new net.minecraft.resources.ResourceLocation("block/water_flow");

                        @Override
                        public net.minecraft.resources.ResourceLocation getStillTexture()
                        {
                            return WATER_STILL;
                        }

                        @Override
                        public net.minecraft.resources.ResourceLocation getFlowingTexture()
                        {
                            return WATER_FLOW;
                        }

                        @Override
                        public int getTintColor()
                        {
                            return 0xFF660000; // Deep, dark crimson ARGB
                        }
                    });
                }
            });

    public static final RegistryObject<ForgeFlowingFluid.Source> SOURCE_BLOOD = FLUIDS.register("blood",
            () -> new ForgeFlowingFluid.Source(ModFluids.BLOOD_PROPERTIES));

    public static final RegistryObject<ForgeFlowingFluid.Flowing> FLOWING_BLOOD = FLUIDS.register("flowing_blood",
            () -> new ForgeFlowingFluid.Flowing(ModFluids.BLOOD_PROPERTIES));

    public static final ForgeFlowingFluid.Properties BLOOD_PROPERTIES = new ForgeFlowingFluid.Properties(
            BLOOD_TYPE, SOURCE_BLOOD, FLOWING_BLOOD)
            .slopeFindDistance(2)
            .levelDecreasePerBlock(2)
            .block(ModBlocks.BLOOD_BLOCK)
            .bucket(ModItems.BLOOD_BUCKET);
}