package org.Enderfan.vivarium.client;

import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.Enderfan.vivarium.Vivarium;
import org.Enderfan.vivarium.client.renderers.BloodPoolRenderer;
import org.Enderfan.vivarium.client.renderers.WorldHeartRenderer;
import org.Enderfan.vivarium.entities.ModEntities;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.Enderfan.vivarium.entities.WorldHeartModel;
import org.Enderfan.vivarium.particles.BloodDripParticle;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import org.Enderfan.vivarium.particles.ModParticles;


@Mod.EventBusSubscriber(modid = Vivarium.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.BLOOD_POOL.get(), BloodPoolRenderer::new);
        event.registerEntityRenderer(ModEntities.WORLD_HEART.get(), WorldHeartRenderer::new);
    }

    @SubscribeEvent
    public static void registerParticleFactories(RegisterParticleProvidersEvent event)
    {
        event.registerSpriteSet(ModParticles.BLOOD_DRIP.get(), BloodDripParticle.Factory::new);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event)
    {

    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event)
    {
        // blockbench should have generated a method called createBodyLayer in your model class.
        // if they named it something else, change this to match.
        event.registerLayerDefinition(ModModelLayers.WORLD_HEART_LAYER, WorldHeartModel::createBodyLayer);
    }
}
