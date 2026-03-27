package org.Enderfan.vivarium.entities;// Made with Blockbench 5.0.7
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

public class WorldHeartModel<T extends WorldHeartEntity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "world_heart"), "main");
	private final ModelPart mainbody;
	private final ModelPart arteries;
	private final ModelPart artery1;
	private final ModelPart artery2;
	private final ModelPart atery3;
	public float throb;

	public WorldHeartModel(ModelPart root) {
		this.mainbody = root.getChild("mainbody");
		this.arteries = root.getChild("arteries");
		this.artery1 = this.arteries.getChild("artery1");
		this.artery2 = this.arteries.getChild("artery2");
		this.atery3 = this.arteries.getChild("atery3");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition mainbody = partdefinition.addOrReplaceChild("mainbody", CubeListBuilder.create().texOffs(20, 14).addBox(2.0F, 11.0F, -5.0F, 4.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(0, 12).addBox(3.0F, 14.0F, -6.0F, 5.0F, 6.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(24, 20).addBox(1.0F, 14.0F, -5.0F, 2.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.0F, 7.0F, 2.0F));

		PartDefinition arteries = partdefinition.addOrReplaceChild("arteries", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition artery1 = arteries.addOrReplaceChild("artery1", CubeListBuilder.create().texOffs(0, 25).addBox(-1.0F, -11.0F, -1.0F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(16, 23).addBox(-2.0F, -18.0F, 0.0F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(20, 20).addBox(-3.0F, -30.0F, 1.0F, 1.0F, 13.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition artery2 = arteries.addOrReplaceChild("artery2", CubeListBuilder.create().texOffs(4, 25).addBox(2.0F, -7.0F, -3.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 23).addBox(4.0F, -8.0F, -4.0F, 7.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(20, 12).addBox(10.0F, -9.0F, -5.0F, 11.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition atery3 = arteries.addOrReplaceChild("atery3", CubeListBuilder.create().texOffs(12, 25).addBox(2.0F, -8.0F, -1.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(24, 0).addBox(3.0F, -9.0F, -1.0F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(24, 6).addBox(4.0F, -10.0F, 3.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(5.0F, -11.0F, 6.0F, 1.0F, 1.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
	{
		// ageInTicks is just a counter that goes up forever while the entity exists.
		// we use Math.sin to turn that ever-increasing number into a wave that goes up and down.

		float beatSpeed = 0.1f; // how fast it pumps. higher = heart attack.
		float beatSize = 0.15f;  // how much it bulges out.

		// using Math.abs so it only bulges OUTWARDS instead of shrinking inwards too.
		// looks slightly more like a real heartbeat.
		throb = Math.abs((float) Math.sin(ageInTicks * beatSpeed)) * beatSize;

		// blockbench makes everything a ModelPart.
		this.mainbody.xScale = 1.0f + throb;
		this.mainbody.yScale = 1.0f + throb;
		this.mainbody.zScale = 1.0f + throb;
		float xoffset = -5f;
		float yoffset = 0.7f;
		this.arteries.x = xoffset * 0.3f;
		this.mainbody.x = xoffset;
		//this.mainbody.y = 5f; //this is just shih to make the model render right b/c the animation and bounding box are stupid
		this.arteries.y = 26f + yoffset*(-8.5f); //why does the offset affect the model parts different amounts?!?!?!?!?!!??
		this.mainbody.y = yoffset;	//this bs proves to me there is no god.
		this.mainbody.z = 4f; //maybe i should have just modeled it correctly atp
		this.arteries.z = 2f;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		mainbody.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		arteries.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}

}