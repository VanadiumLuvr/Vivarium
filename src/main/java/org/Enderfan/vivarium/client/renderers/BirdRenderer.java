package org.Enderfan.vivarium.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.Enderfan.vivarium.Vivarium;
import org.Enderfan.vivarium.entities.BirdEntity;
import org.Enderfan.vivarium.entities.BirdModel;
import org.Enderfan.vivarium.entities.ButterflyEntity;
import org.Enderfan.vivarium.entities.ButterflyModel;

public class BirdRenderer extends MobRenderer<BirdEntity, BirdModel<BirdEntity>>
{
    // Make sure your texture PNG is actually saved at this exact file path!
    private static final ResourceLocation TEXTURE = new ResourceLocation(Vivarium.MODID, "textures/entity/bird.png");

    public BirdRenderer(EntityRendererProvider.Context context)
    {
        super(context, new BirdModel<>(context.bakeLayer(BirdModel.LAYER_LOCATION)), 0.4f);
    }

    @Override
    protected void scale(BirdEntity entity, PoseStack poseStack, float partialTickTime)
    {
        poseStack.scale(0.9f, 0.9f, 0.9f);
    }
    @Override
    public ResourceLocation getTextureLocation(BirdEntity entity)
    {
        return TEXTURE;
    }
}