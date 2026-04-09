package org.Enderfan.vivarium.entities;// Made with Blockbench 5.1.3
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class BirdModel<T extends Entity> extends HierarchicalModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "birdmodel"), "main");
	private final ModelPart root;
	private final ModelPart body;
	private final ModelPart head;
	private final ModelPart R_leg;
	private final ModelPart L_leg;
	private final ModelPart R_wing;
	private final ModelPart L_wing;
	private final ModelPart R_tail;
	private final ModelPart L_tail;
	private final ModelPart body2;
	private final ModelPart head2;

	public BirdModel(ModelPart root) {
		this.root = root;
		this.body = root.getChild("body");
		this.head = this.body.getChild("head");
		this.R_leg = this.body.getChild("R_leg");
		this.L_leg = this.body.getChild("L_leg");
		this.R_wing = this.body.getChild("R_wing");
		this.L_wing = this.body.getChild("L_wing");
		this.R_tail = this.body.getChild("R_tail");
		this.L_tail = this.body.getChild("L_tail");
		this.body2 = root.getChild("body2");
		this.head2 = this.body2.getChild("head2");
	}

	@Override
	public ModelPart root()
	{
		return this.root;
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -5.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 22.0F, 0.0F));

		PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(16, 0).addBox(-2.0F, -3.0F, -1.0F, 4.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(16, 6).addBox(-1.0F, -2.0F, -2.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -5.0F, -2.0F));

		PartDefinition R_leg = body.addOrReplaceChild("R_leg", CubeListBuilder.create().texOffs(0, 18).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.5F, 0.0F, 0.5F));

		PartDefinition L_leg = body.addOrReplaceChild("L_leg", CubeListBuilder.create().texOffs(4, 18).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(1.5F, 0.0F, 0.5F));

		PartDefinition R_wing = body.addOrReplaceChild("R_wing", CubeListBuilder.create(), PartPose.offset(-2.0F, -3.0F, -1.9F));

		PartDefinition R_wing_r1 = R_wing.addOrReplaceChild("R_wing_r1", CubeListBuilder.create().texOffs(10, 7).mirror().addBox(-1.0F, -0.1206F, 4.684F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(11, 8).mirror().addBox(-1.0F, 1.8794F, 4.684F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(0, 9).addBox(-1.0F, -0.1206F, -0.316F, 1.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.0F, 0.9F, -0.3491F, 0.0F, 0.0F));

		PartDefinition L_wing = body.addOrReplaceChild("L_wing", CubeListBuilder.create(), PartPose.offset(2.0F, -3.0F, -1.9F));

		PartDefinition L_wing_r1 = L_wing.addOrReplaceChild("L_wing_r1", CubeListBuilder.create().texOffs(11, 8).addBox(0.0F, -0.1206F, 4.684F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(11, 8).addBox(0.0F, 1.8794F, 4.684F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(11, 8).addBox(0.0F, -0.1206F, -0.316F, 1.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.0F, 0.9F, -0.3491F, 0.0F, 0.0F));

		PartDefinition R_tail = body.addOrReplaceChild("R_tail", CubeListBuilder.create(), PartPose.offset(0.0F, -1.0F, 2.0F));

		PartDefinition R_tail_r1 = R_tail.addOrReplaceChild("R_tail_r1", CubeListBuilder.create().texOffs(-3, -2).addBox(-2.0F, -0.1874F, -0.1548F, 2.0F, 0.005F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4367F, -0.0395F, 0.0184F));

		PartDefinition L_tail = body.addOrReplaceChild("L_tail", CubeListBuilder.create(), PartPose.offset(0.0F, -1.0F, 2.0F));

		PartDefinition L_tail_r1 = L_tail.addOrReplaceChild("L_tail_r1", CubeListBuilder.create().texOffs(-2, -2).mirror().addBox(0.0F, -0.1874F, -0.1548F, 2.0F, 0.005F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4367F, 0.0395F, -0.0184F));

		PartDefinition body2 = partdefinition.addOrReplaceChild("body2", CubeListBuilder.create(), PartPose.offset(1.0F, 22.0F, 0.0F));

		PartDefinition head2 = body2.addOrReplaceChild("head2", CubeListBuilder.create(), PartPose.offset(-1.0F, -5.0F, -2.0F));

		return LayerDefinition.create(meshdefinition, 32, 32);
	}

	@Override
	public void setupAnim(@NotNull T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
	{
		this.root().getAllParts().forEach(net.minecraft.client.model.geom.ModelPart::resetPose);

		// Head tracking uses the native parameters
		this.head.yRot = netHeadYaw * ((float)Math.PI / 180F);
		this.head.xRot = headPitch * ((float)Math.PI / 180F);

		if (entity instanceof BirdEntity bird)
		{
			// The body uses our custom flight math!
			this.body.xRot = bird.flightPitch * ((float)Math.PI / 180F);

			this.animate(bird.hopAnimationState, BirdModelAnimation.hop, ageInTicks, 0.75f);
			this.animate(bird.flapAnimationState, BirdModelAnimation.flap, ageInTicks, 0.8f);
			this.animate(bird.glideAnimationState, BirdModelAnimation.glide, ageInTicks, 1.0f);
			this.animate(bird.peckAnimationState, BirdModelAnimation.peck, ageInTicks, 0.7f);
			this.animate(bird.peckFlyAnimationState, BirdModelAnimation.peck_fly, ageInTicks, 1.0f);
		}
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		body.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		body2.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}