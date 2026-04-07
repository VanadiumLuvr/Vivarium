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

public class GrasshopperModel<T extends Entity> extends HierarchicalModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "grasshopper"), "main");
	private final ModelPart root;
	private final ModelPart thorax;
	private final ModelPart head;
	private final ModelPart R_antenna;
	private final ModelPart L_antenna;
	private final ModelPart abdomen;
	private final ModelPart R2_leg;
	private final ModelPart L1_leg;
	private final ModelPart R1_leg;
	private final ModelPart L2_leg;
	private final ModelPart L3_tibia;
	private final ModelPart R3_tibia;
	private final ModelPart L3_femur;
	private final ModelPart R3_femur;
	private final ModelPart L_wing;
	private final ModelPart R_wing;

	public GrasshopperModel(ModelPart root) {
		this.root = root;
		this.thorax = root.getChild("thorax");
		this.head = this.thorax.getChild("head");
		this.R_antenna = this.head.getChild("R_antenna");
		this.L_antenna = this.head.getChild("L_antenna");
		this.abdomen = root.getChild("abdomen");
		this.R2_leg = root.getChild("R2_leg");
		this.L1_leg = root.getChild("L1_leg");
		this.R1_leg = root.getChild("R1_leg");
		this.L2_leg = root.getChild("L2_leg");
		this.L3_tibia = root.getChild("L3_tibia");
		this.R3_tibia = root.getChild("R3_tibia");
		this.L3_femur = root.getChild("L3_femur");
		this.R3_femur = root.getChild("R3_femur");
		this.L_wing = root.getChild("L_wing");
		this.R_wing = root.getChild("R_wing");
	}

	@Override
	public ModelPart root()
	{
		return this.root;
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition thorax = partdefinition.addOrReplaceChild("thorax", CubeListBuilder.create().texOffs(36, 16).addBox(-1.0F, -5.0F, -1.0F, 2.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition head = thorax.addOrReplaceChild("head", CubeListBuilder.create().texOffs(36, 25).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -4.0F, 5.0F));

		PartDefinition R_antenna = head.addOrReplaceChild("R_antenna", CubeListBuilder.create(), PartPose.offset(-0.5F, -1.0F, 1.4F));

		PartDefinition R_antenna_r1 = R_antenna.addOrReplaceChild("R_antenna_r1", CubeListBuilder.create().texOffs(3, 1).addBox(1.0F, -3.0F, -1.0F, 0.001F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1719F, -0.7703F, 0.2444F));

		PartDefinition L_antenna = head.addOrReplaceChild("L_antenna", CubeListBuilder.create(), PartPose.offset(0.5F, -1.0F, 1.4F));

		PartDefinition L_antenna_r1 = L_antenna.addOrReplaceChild("L_antenna_r1", CubeListBuilder.create().texOffs(1, 0).mirror().addBox(-1.0F, -3.0F, -1.0F, 0.001F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1719F, 0.7703F, -0.2444F));

		PartDefinition abdomen = partdefinition.addOrReplaceChild("abdomen", CubeListBuilder.create().texOffs(0, 34).addBox(-1.0F, -1.0F, -14.0F, 2.0F, 2.0F, 15.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 20.0F, -2.0F));

		PartDefinition R2_leg = partdefinition.addOrReplaceChild("R2_leg", CubeListBuilder.create(), PartPose.offset(1.0F, 20.9F, 2.5F));

		PartDefinition R2_r1 = R2_leg.addOrReplaceChild("R2_r1", CubeListBuilder.create().texOffs(36, 30).addBox(0.0F, -2.0F, 0.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.0F, -1.0F, -0.1745F, 0.0F, 0.0F));

		PartDefinition L1_leg = partdefinition.addOrReplaceChild("L1_leg", CubeListBuilder.create(), PartPose.offset(-1.0F, 21.1F, 4.0F));

		PartDefinition L1_r1 = L1_leg.addOrReplaceChild("L1_r1", CubeListBuilder.create().texOffs(34, 50).addBox(0.0F, -3.0F, 0.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 3.0F, 0.0F, 0.2182F, 0.0F, 0.0F));

		PartDefinition R1_leg = partdefinition.addOrReplaceChild("R1_leg", CubeListBuilder.create(), PartPose.offset(1.0F, 21.1F, 4.0F));

		PartDefinition R1_r1 = R1_leg.addOrReplaceChild("R1_r1", CubeListBuilder.create().texOffs(40, 30).addBox(-1.0F, -3.0F, 0.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, 3.0F, 0.0F, 0.2182F, 0.0F, 0.0F));

		PartDefinition L2_leg = partdefinition.addOrReplaceChild("L2_leg", CubeListBuilder.create(), PartPose.offset(-1.0F, 20.9F, 2.5F));

		PartDefinition L2_r1 = L2_leg.addOrReplaceChild("L2_r1", CubeListBuilder.create().texOffs(38, 50).addBox(-1.0F, -2.0F, 0.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.0F, -1.0F, -0.1745F, 0.0F, 0.0F));

		PartDefinition L3_tibia = partdefinition.addOrReplaceChild("L3_tibia", CubeListBuilder.create(), PartPose.offset(-2.0F, 16.25F, -11.75F));

		PartDefinition L3_tibia_r1 = L3_tibia.addOrReplaceChild("L3_tibia_r1", CubeListBuilder.create().texOffs(48, 25).addBox(-1.0F, -2.0F, 0.0F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, 2.0F, -1.0F, -0.4363F, 0.0F, 0.0F));

		PartDefinition R3_tibia = partdefinition.addOrReplaceChild("R3_tibia", CubeListBuilder.create(), PartPose.offset(2.0F, 16.25F, -11.75F));

		PartDefinition R3_tibia_r1 = R3_tibia.addOrReplaceChild("R3_tibia_r1", CubeListBuilder.create().texOffs(44, 25).addBox(0.0F, -2.0F, 0.0F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 2.0F, -1.0F, -0.4363F, 0.0F, 0.0F));

		PartDefinition L3_femur = partdefinition.addOrReplaceChild("L3_femur", CubeListBuilder.create(), PartPose.offset(-1.0F, 21.0F, 0.0F));

		PartDefinition L3_femur_r1 = L3_femur.addOrReplaceChild("L3_femur_r1", CubeListBuilder.create().texOffs(36, 0).addBox(-1.0F, -2.0F, -13.0F, 2.0F, 2.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 1.0F, 0.0F, -0.4363F, 0.0F, 0.0F));

		PartDefinition R3_femur = partdefinition.addOrReplaceChild("R3_femur", CubeListBuilder.create(), PartPose.offset(1.0F, 21.0F, 0.0F));

		PartDefinition R3_femur_r1 = R3_femur.addOrReplaceChild("R3_femur_r1", CubeListBuilder.create().texOffs(34, 34).addBox(-1.0F, -2.0F, -13.0F, 2.0F, 2.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, 1.0F, 0.0F, -0.4363F, 0.0F, 0.0F));

		PartDefinition L_wing = partdefinition.addOrReplaceChild("L_wing", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, 0.0F, -16.0F, 1.0F, 0.001F, 17.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 19.0F, -2.0F));

		PartDefinition R_wing = partdefinition.addOrReplaceChild("R_wing", CubeListBuilder.create().texOffs(0, 17).addBox(0.0F, 0.0F, -16.0F, 1.0F, 0.001F, 17.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 19.0F, -2.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
	{
		// 1. Reset everything to the default Blockbench pose
		this.root().getAllParts().forEach(ModelPart::resetPose);

		// 2. Apply the head tracking math (converting degrees to radians)
		this.head.yRot = netHeadYaw * ((float)Math.PI / 180F);
		this.head.xRot = headPitch * ((float)Math.PI / 180F);

		// 3. Play the animations over the top of the base pose
		if (entity instanceof GrasshopperEntity grasshopper)
		{
			this.animate(grasshopper.idleAnimationState, GrasshopperAnimations.idle, ageInTicks, 1.0F);
			this.animateWalk(GrasshopperAnimations.walk_loop, limbSwing, limbSwingAmount, 2.0F, 2.5F);
			this.animate(grasshopper.flyAnimationState, GrasshopperAnimations.fly_loop, ageInTicks, 1.0F);
		}
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		thorax.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		abdomen.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		R2_leg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		L1_leg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		R1_leg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		L2_leg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		L3_tibia.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		R3_tibia.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		L3_femur.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		R3_femur.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		L_wing.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		R_wing.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}