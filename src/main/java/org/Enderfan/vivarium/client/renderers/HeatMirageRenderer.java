package org.Enderfan.vivarium.client.renderers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

@Mod.EventBusSubscriber(modid = "vivarium", value = Dist.CLIENT)
public class HeatMirageRenderer
{
    private static final ResourceLocation MIRAGE_TEXTURE = new ResourceLocation("vivarium", "textures/misc/mirage.png");
    private static final List<Mirage> mirages = new ArrayList<>();
    private static final Random random = new Random();

    // A tiny data class to track where the ghosts are and how long they have been alive
    private static class Mirage
    {
        double x, y, z;
        int age;
        int maxAge;

        Mirage(double x, double y, double z)
        {
            this.x = x;
            this.y = y;
            this.z = z;
            this.age = 0;
            // They stick around for 3 to 7 seconds before vanishing
            this.maxAge = 60 + random.nextInt(80);
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event)
    {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        // 1. Tick existing mirages and kill old ones
        Iterator<Mirage> iterator = mirages.iterator();
        while (iterator.hasNext())
        {
            Mirage m = iterator.next();
            m.age++;
            if (m.age >= m.maxAge)
            {
                iterator.remove();
            }
        }


        if (mirages.size() < 15 && random.nextInt(10) == 0)
        {
            // Pick a random spot 15 to 30 blocks away from the player
            double angle = random.nextDouble() * Math.PI * 2;
            double distance = 15 + random.nextDouble() * 15;
            double targetX = mc.player.getX() + Math.cos(angle) * distance;
            double targetZ = mc.player.getZ() + Math.sin(angle) * distance;

            // Start 10 blocks in the air and drop down until we find the solid ground
            BlockPos pos = BlockPos.containing(targetX, mc.player.getY() + 10, targetZ);
            while (pos.getY() > mc.level.getMinBuildHeight() && mc.level.isEmptyBlock(pos))
            {
                pos = pos.below();
            }

            // Only spawn if it's outside under the open sky
            if (mc.level.canSeeSky(pos.above()))
            {
                mirages.add(new Mirage(targetX, pos.getY() + 1.5, targetZ));
            }
        }
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event)
    {
        // Inject right after translucent blocks (water/glass) are drawn so it layers correctly
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        if (mirages.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        Vec3 cameraPos = event.getCamera().getPosition();

        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, MIRAGE_TEXTURE);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        // Disable depth writing so the transparent edges don't glitch out the skybox
        RenderSystem.depthMask(false);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();

        for (Mirage m : mirages)
        {
            // If the player walks within 5 blocks of the mirage, instantly set its age to max so it vanishes
            double distToPlayerSqr = mc.player.distanceToSqr(m.x, m.y, m.z);
            if (distToPlayerSqr < 25)
            {
                m.age = m.maxAge;
                continue;
            }

            // Smooth out the age using partial ticks so the movement never stutters
            float smoothAge = m.age + event.getPartialTick();

            float lifeRatio = (float) m.age / m.maxAge;
            float alpha = (float) Math.sin(lifeRatio * Math.PI);

            // The wave rises steadily at 0.05 blocks per tick (about 3 blocks total over its lifespan)
            float rise = smoothAge * 0.05f;

            // A very subtle horizontal drift to make the air look like it's rippling
            float swayX = (float) Math.sin(smoothAge * 0.1) * 0.15f;

            poseStack.pushPose();
            // Apply the new sway and continuous upward rise
            poseStack.translate((m.x + swayX) - cameraPos.x, (m.y + rise) - cameraPos.y, m.z - cameraPos.z);

            poseStack.mulPose(event.getCamera().rotation());
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));

            float scale = 3.0f;
            poseStack.scale(scale, scale, scale);

            Matrix4f matrix4f = poseStack.last().pose();
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

            // Draw the four corners of the texture
            buffer.vertex(matrix4f, -0.5F, -0.5F, 0.0F).uv(1.0F, 1.0F).color(1.0F, 1.0F, 1.0F, alpha * 0.7f).endVertex();
            buffer.vertex(matrix4f, 0.5F, -0.5F, 0.0F).uv(0.0F, 1.0F).color(1.0F, 1.0F, 1.0F, alpha * 0.7f).endVertex();
            buffer.vertex(matrix4f, 0.5F, 0.5F, 0.0F).uv(0.0F, 0.0F).color(1.0F, 1.0F, 1.0F, alpha * 0.7f).endVertex();
            buffer.vertex(matrix4f, -0.5F, 0.5F, 0.0F).uv(1.0F, 0.0F).color(1.0F, 1.0F, 1.0F, alpha * 0.7f).endVertex();

            tesselator.end();
            poseStack.popPose();
        }

        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        poseStack.popPose();
    }
}