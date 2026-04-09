package org.Enderfan.vivarium.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.Enderfan.vivarium.Vivarium;
import org.Enderfan.vivarium.client.renderers.BirdRenderer;
import org.Enderfan.vivarium.client.renderers.GrasshopperRenderer;
import org.Enderfan.vivarium.entities.*;
import org.Enderfan.vivarium.client.renderers.ButterflyRenderer;
// Replace this with wherever your EntityType DeferredRegister is:


@Mod.EventBusSubscriber(modid = Vivarium.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModClientEvents
{
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event)
    {
        // Tells the game "When you see a ButterflyEntity, use the ButterflyRenderer to draw it"
        event.registerEntityRenderer(ModEntities.BUTTERFLY.get(), ButterflyRenderer::new);

        event.registerEntityRenderer(ModEntities.GRASSHOPPER.get(), GrasshopperRenderer::new);

        event.registerEntityRenderer(ModEntities.BIRD.get(), BirdRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event)
    {
        // Tells the game how to physically construct the 3D geometry of the model
        event.registerLayerDefinition(ButterflyModel.LAYER_LOCATION, ButterflyModel::createBodyLayer);

        event.registerLayerDefinition(GrasshopperModel.LAYER_LOCATION, GrasshopperModel::createBodyLayer);

        event.registerLayerDefinition(BirdModel.LAYER_LOCATION, BirdModel::createBodyLayer);
    }



}