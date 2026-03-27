package org.Enderfan.vivarium.client.renderers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Mod.EventBusSubscriber(modid = "vivarium", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class BloodArrowRenderer
{
    private static final ResourceLocation ARROW_TEXTURE = new ResourceLocation("vivarium", "textures/misc/blood_arrow.png");
    private static final ResourceLocation ARROW_PERP_TEXTURE = new ResourceLocation("vivarium", "textures/misc/blood_arrow_perp.png");

    private static final List<ActiveArrow> ARROWS = new ArrayList<>();

    private static class ActiveArrow
    {
        BlockPos pos;
        Direction face;
        float angle;
        float alpha = 1.0f;
        boolean isPerp;

        public ActiveArrow(BlockPos pos, Direction face, float angle, boolean isPerp)
        {
            this.pos = pos;
            this.face = face;
            this.angle = angle;
            this.isPerp = isPerp;
        }
    }

    public static void addArrow(BlockPos hitPos, Direction face, BlockPos targetPos)
    {
        float dx = targetPos.getX() - hitPos.getX();
        float dy = targetPos.getY() - hitPos.getY();
        float dz = targetPos.getZ() - hitPos.getZ();

        // calculate the total straight-line distance
        float dist = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        boolean isPerp = false;

        // prevent dividing by zero if they somehow click a block from inside the exact center of the target
        if (dist > 0.1f)
        {
            // normalize the vector so the values are between -1.0 and 1.0
            float nx = dx / dist;
            float ny = dy / dist;
            float nz = dz / dist;

            // if 85% or more of the distance is along the normal of the face they clicked,
            // it means the target is mostly straight through or straight away from the block.
            float threshold = 0.85f;

            switch (face)
            {
                case UP:
                case DOWN:
                    if (Math.abs(ny) > threshold) isPerp = true;
                    break;
                case NORTH:
                case SOUTH:
                    if (Math.abs(nz) > threshold) isPerp = true;
                    break;
                case EAST:
                case WEST:
                    if (Math.abs(nx) > threshold) isPerp = true;
                    break;
            }
        }

        float angle = calculateRotation(dx, dy, dz, face);
        ARROWS.add(new ActiveArrow(hitPos, face, angle, isPerp));
    }

    private static float calculateRotation(float dx, float dy, float dz, Direction face)
    {
        switch (face)
        {
            case UP:
                return (float) Math.toDegrees(Math.atan2(-dx, -dz));
            case DOWN:
                return (float) Math.toDegrees(Math.atan2(-dx, dz));
            case NORTH:
                return (float) Math.toDegrees(Math.atan2(dx, dy));
            case SOUTH:
                return (float) Math.toDegrees(Math.atan2(-dx, dy));
            case EAST:
                return (float) Math.toDegrees(Math.atan2(dz, dy));
            case WEST:
                return (float) Math.toDegrees(Math.atan2(-dz, dy));
            default:
                return 0f;
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event)
    {
        if (event.phase != TickEvent.Phase.END) return;

        Iterator<ActiveArrow> iterator = ARROWS.iterator();
        while (iterator.hasNext())
        {
            ActiveArrow arrow = iterator.next();
            arrow.alpha -= 0.02f;

            if (arrow.alpha <= 0)
            {
                iterator.remove();
            }
        }
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event)
    {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS || ARROWS.isEmpty()) return;

        PoseStack poseStack = event.getPoseStack();
        Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        RenderSystem.enablePolygonOffset();
        RenderSystem.polygonOffset(-1.0f, -1.0f);
        RenderSystem.depthMask(false);

        Tesselator tesselator = Tesselator.getInstance();

        for (ActiveArrow arrow : ARROWS)
        {
            // Pick the right texture for this specific arrow
            ResourceLocation texture = arrow.isPerp ? ARROW_PERP_TEXTURE : ARROW_TEXTURE;
            RenderSystem.setShaderTexture(0, texture);

            // We have to begin and end the buffer inside the loop now so the texture swap actually applies
            BufferBuilder buffer = tesselator.getBuilder();
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

            poseStack.pushPose();
            poseStack.translate(arrow.pos.getX() - camera.x, arrow.pos.getY() - camera.y, arrow.pos.getZ() - camera.z);

            applyFaceRotation(poseStack, arrow.face);

            // If it's the perp texture, you might not want it to spin randomly,
            // but keeping the rotation usually makes it look more organic anyway.
            poseStack.mulPose(Axis.ZP.rotationDegrees(arrow.angle));

            Matrix4f matrix = poseStack.last().pose();
            buffer.vertex(matrix, -0.5f, -0.5f, 0).uv(0, 1).color(1f, 1f, 1f, arrow.alpha).endVertex();
            buffer.vertex(matrix, 0.5f, -0.5f, 0).uv(1, 1).color(1f, 1f, 1f, arrow.alpha).endVertex();
            buffer.vertex(matrix, 0.5f, 0.5f, 0).uv(1, 0).color(1f, 1f, 1f, arrow.alpha).endVertex();
            buffer.vertex(matrix, -0.5f, 0.5f, 0).uv(0, 0).color(1f, 1f, 1f, arrow.alpha).endVertex();

            poseStack.popPose();
            tesselator.end();
        }

        RenderSystem.disablePolygonOffset();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }

    private static void applyFaceRotation(PoseStack stack, Direction face)
    {
        stack.translate(0.5, 0.5, 0.5);

        if (face == Direction.NORTH) stack.mulPose(Axis.YP.rotationDegrees(180));
        else if (face == Direction.EAST) stack.mulPose(Axis.YP.rotationDegrees(90));
        else if (face == Direction.WEST) stack.mulPose(Axis.YP.rotationDegrees(270));
        else if (face == Direction.UP) stack.mulPose(Axis.XP.rotationDegrees(-90));
        else if (face == Direction.DOWN) stack.mulPose(Axis.XP.rotationDegrees(90));

        stack.translate(0, 0, 0.501);
    }
}