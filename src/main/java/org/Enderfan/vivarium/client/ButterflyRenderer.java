package org.Enderfan.vivarium.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.Enderfan.vivarium.Vivarium;
import org.Enderfan.vivarium.entities.ButterflyEntity;
import org.Enderfan.vivarium.entities.ButterflyModel;

public class ButterflyRenderer extends MobRenderer<ButterflyEntity, ButterflyModel<ButterflyEntity>>
{
    // Make sure your texture PNG is actually saved at this exact file path!
    private static final ResourceLocation TEXTURE = new ResourceLocation(Vivarium.MODID, "textures/entity/butterfly.png");

    public ButterflyRenderer(EntityRendererProvider.Context context)
    {
        // The 0.3f at the end is the size of the shadow block on the ground.
        super(context, new ButterflyModel<>(context.bakeLayer(ButterflyModel.LAYER_LOCATION)), 0.3f);
    }

    @Override
    public ResourceLocation getTextureLocation(ButterflyEntity entity)
    {
        return TEXTURE;
    }
}