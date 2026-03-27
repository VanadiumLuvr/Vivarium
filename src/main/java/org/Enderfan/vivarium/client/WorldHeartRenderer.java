package org.Enderfan.vivarium.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.Enderfan.vivarium.entities.WorldHeartEntity;
import org.Enderfan.vivarium.entities.WorldHeartModel;

public class WorldHeartRenderer extends MobRenderer<WorldHeartEntity, WorldHeartModel<WorldHeartEntity>>
{
    private static final ResourceLocation HEART_TEXTURE = new ResourceLocation("vivarium", "textures/entity/world_heart.png");

    public WorldHeartRenderer(EntityRendererProvider.Context context)
    {
        // 1.5f is the shadow size. make it bigger if you want a massive shadow.
        super(context, new WorldHeartModel<>(context.bakeLayer(ModModelLayers.WORLD_HEART_LAYER)), 1.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(WorldHeartEntity entity)
    {
        return HEART_TEXTURE;
    }

    @Override
    protected void scale(WorldHeartEntity entity, PoseStack poseStack, float partialTickTime)
    {
        float scaleMultiplier = 40.0f;

        poseStack.scale(scaleMultiplier, scaleMultiplier, scaleMultiplier);

        super.scale(entity, poseStack, partialTickTime);
    }
}
