package org.Enderfan.vivarium.client.handlers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.Enderfan.vivarium.Vivarium;
import org.Enderfan.vivarium.config.VivariumConfig;
import org.Enderfan.vivarium.server.GuiltProvider;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

@Mod.EventBusSubscriber(modid = Vivarium.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class HallucinationHandler
{
    private static BlockPos lastPos = null;
    private static Direction lastFace = null;
    private static float alpha = 0.0f;
    private static int textureIndex = 1; // 1-4
    private static int clientGuilt = 0; // Sync this from server!

    private static final ResourceLocation[] CARVINGS = new ResourceLocation[]
            {
                    new ResourceLocation(Vivarium.MODID, "textures/misc/carving1.png"),
                    new ResourceLocation(Vivarium.MODID, "textures/misc/carving2.png"),
                    new ResourceLocation(Vivarium.MODID, "textures/misc/carving3.png"),
                    new ResourceLocation(Vivarium.MODID, "textures/misc/carving4.png")
            };

    @SubscribeEvent
    public static void onClientTick(TickEvent.PlayerTickEvent event)
    {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();

        // 1. Leave it as a generic Player! No ServerPlayer cast needed.
        Player player = event.player;

        // 2. Ensure we are only calculating hallucinations for YOUR eyeballs, not other players
        if (player != mc.player) return;

        player.getCapability(GuiltProvider.PLAYER_GUILT).ifPresent(guilt ->
        {
            if (guilt.getGuilt() < VivariumConfig.CARVING_THRESHOLD.get() || player.getRandom().nextFloat() > VivariumConfig.CARVING_CHANCE.get()) return;

            HitResult hit = mc.player.pick(4.5D, 0.0F, false);

            if (hit.getType() == HitResult.Type.BLOCK)
            {
                BlockHitResult blockHit = (BlockHitResult) hit;
                BlockPos currentPos = blockHit.getBlockPos();

                if (currentPos.equals(lastPos))
                {
                    // Increasing by 0.005 every single tick means it takes exactly 3 seconds
                    // of staring at a block for the carving to slowly materialize.
                    alpha = Math.min(0.3f, alpha + 0.005f);
                }
                else
                {
                    lastPos = currentPos;
                    lastFace = blockHit.getDirection();
                    alpha = 0.0f;
                    assert mc.level != null;
                    textureIndex = mc.level.random.nextInt(4); // Pick 1 of 4
                }
            }
            else
            {
                // If they look away into the air, fade it out
                alpha = Math.max(0.0f, alpha - 0.02f);
            }
        });
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event)
    {
        // Only render after all blocks are drawn
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        if (alpha <= 0 || lastPos == null || lastFace == null) return;

        PoseStack poseStack = event.getPoseStack();
        Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();

        poseStack.pushPose();

        // Move to the block position relative to the camera
        poseStack.translate(lastPos.getX() - camera.x, lastPos.getY() - camera.y, lastPos.getZ() - camera.z);

        // Position the decal slightly in front of the face to prevent Z-fighting
        float offset = 1.005f;
        applyFaceRotation(poseStack, lastFace, offset);

        renderDecal(poseStack, CARVINGS[textureIndex], alpha);

        poseStack.popPose();
    }

    private static void applyFaceRotation(PoseStack stack, Direction face, float offset)
    {
        // Move to the center of the block face
        stack.translate(0.5, 0.5, 0.5);

        // Rotate based on the face the player is looking at
        if (face == Direction.NORTH) stack.mulPose(Axis.YP.rotationDegrees(180));
        else if (face == Direction.EAST) stack.mulPose(Axis.YP.rotationDegrees(90));
        else if (face == Direction.WEST) stack.mulPose(Axis.YP.rotationDegrees(270));
        else if (face == Direction.UP) stack.mulPose(Axis.XP.rotationDegrees(-90));
        else if (face == Direction.DOWN) stack.mulPose(Axis.XP.rotationDegrees(90));

        // Move the plane slightly "out" from the surface
        stack.translate(0, 0, 0.501);
    }

    private static void renderDecal(PoseStack stack, ResourceLocation texture, float alpha)
    {
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, texture);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // 1. Enable Depth Test but allow it to "equal" the block's depth
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GL11.GL_LEQUAL);

        // 2. Use Polygon Offset to "push" the decal toward the camera
        // This prevents the decal from disappearing into the block
        RenderSystem.enablePolygonOffset();
        RenderSystem.polygonOffset(-1.0f, -1.0f);

        RenderSystem.depthMask(false); // Keep this false so we see the block behind

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

        Matrix4f matrix = stack.last().pose();

        buffer.vertex(matrix, -0.5f, -0.5f, 0).uv(0, 1).color(1f, 1f, 1f, alpha).endVertex();
        buffer.vertex(matrix, 0.5f, -0.5f, 0).uv(1, 1).color(1f, 1f, 1f, alpha).endVertex();
        buffer.vertex(matrix, 0.5f, 0.5f, 0).uv(1, 0).color(1f, 1f, 1f, alpha).endVertex();
        buffer.vertex(matrix, -0.5f, 0.5f, 0).uv(0, 0).color(1f, 1f, 1f, alpha).endVertex();

        tesselator.end();

        // 3. CLEANUP: Very important to reset these states
        RenderSystem.disablePolygonOffset();
        RenderSystem.depthMask(true);
        RenderSystem.depthFunc(GL11.GL_LEQUAL); // Reset to default
        RenderSystem.disableBlend();
    }


}

