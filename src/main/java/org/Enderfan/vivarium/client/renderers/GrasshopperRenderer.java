package org.Enderfan.vivarium.client.renderers;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.Enderfan.vivarium.Vivarium;
import org.Enderfan.vivarium.entities.GrasshopperEntity;
import org.Enderfan.vivarium.entities.GrasshopperModel;

public class GrasshopperRenderer extends MobRenderer<GrasshopperEntity, GrasshopperModel<GrasshopperEntity>>
{
    private static final ResourceLocation GRASSHOPPER_TEXTURE = new ResourceLocation(Vivarium.MODID, "textures/entity/grasshopper.png");

    public GrasshopperRenderer(EntityRendererProvider.Context context)
    {
        super(context, new GrasshopperModel<>(context.bakeLayer(GrasshopperModel.LAYER_LOCATION)), 0.3f);
    }

    @Override
    public ResourceLocation getTextureLocation(GrasshopperEntity entity)
    {
        return GRASSHOPPER_TEXTURE;
    }

    @Override
    protected void scale(GrasshopperEntity entity, com.mojang.blaze3d.vertex.PoseStack poseStack, float partialTickTime)
    {
        // Shrinks the model
        poseStack.scale(0.3f, 0.3f, 0.3f);
        // Spins it 180 degrees so it stops moonwalking
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180.0f));
    }
}