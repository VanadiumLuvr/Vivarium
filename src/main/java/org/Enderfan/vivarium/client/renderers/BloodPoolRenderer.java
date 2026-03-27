package org.Enderfan.vivarium.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.Enderfan.vivarium.Vivarium;
import org.Enderfan.vivarium.entities.BloodPoolEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Matrix3f;

public class BloodPoolRenderer extends EntityRenderer<BloodPoolEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Vivarium.MODID, "textures/entity/blood_pool.png");

    public BloodPoolRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(BloodPoolEntity entity, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int light) {
        poseStack.pushPose();
        poseStack.translate(0, 0.01, 0);
        float radius = entity.getRadius();
        poseStack.scale(radius, 0.125f, radius * 0.8f);  // Scale circle

        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucentEmissive(TEXTURE));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();

        // Flat horizontal quad (texture alpha = circle shape)
        float half = 0.5f;
        consumer.vertex(matrix, -half, 0, -half).color(1.0f, 0.2f, 0.2f, 0.7f).uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normalMatrix, 0, 1, 0).endVertex();
        consumer.vertex(matrix,  half, 0, -half).color(1.0f, 0.2f, 0.2f, 0.7f).uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normalMatrix, 0, 1, 0).endVertex();
        consumer.vertex(matrix,  half, 0,  half).color(1.0f, 0.2f, 0.2f, 0.7f).uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normalMatrix, 0, 1, 0).endVertex();
        consumer.vertex(matrix, -half, 0,  half).color(1.0f, 0.2f, 0.2f, 0.7f).uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normalMatrix, 0, 1, 0).endVertex();

        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffer, light);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull BloodPoolEntity pEntity) {
        return TEXTURE;
    }
}
