// Made with Blockbench 5.1.0
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports
package org.Enderfan.vivarium.entities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class ButterflyModel<T extends ButterflyEntity> extends HierarchicalModel<T> {
	// Note: I swapped "modid" back to org.Enderfan.vivarium.Vivarium.MODID so it registers correctly!
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(org.Enderfan.vivarium.Vivarium.MODID, "butterfly"), "main");

	// 1. Add a variable to hold the true global root
	private final ModelPart root;
	private final ModelPart body;
	private final ModelPart r_wing;
	private final ModelPart l_wing;


	public ButterflyModel(ModelPart root) {
		// 2. Save the global root
		this.root = root;
		this.body = root.getChild("body");
		this.r_wing = root.getChild("r_wing");
		this.l_wing = root.getChild("l_wing");
	}

	@Override
	public @NotNull ModelPart root()
	{
		// 3. Return the global root so the animator can see the wings!
		return this.root;
	}

	// Keep your exact createBodyLayer() and setupAnim() methods down here...

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 8).addBox(0.0F, -1.5F, -1.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition r_wing = partdefinition.addOrReplaceChild("r_wing", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, 0.0F, -1.0F, 3.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 22.5F, 1.0F));

		PartDefinition l_wing = partdefinition.addOrReplaceChild("l_wing", CubeListBuilder.create().texOffs(0, 4).addBox(0.0F, 0.0F, -1.0F, 3.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, 22.5F, 1.0F));

		return LayerDefinition.create(meshdefinition, 16, 16);
	}

	@Override
	public void setupAnim(@NotNull T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(net.minecraft.client.model.geom.ModelPart::resetPose);

		// Replace ButterflyAnimation.FLY with whatever your exported animation class/reference is named
		this.animate(entity.flyAnimationState, ButterflyAnimations.fly, ageInTicks, 1.0f);
		this.animate(entity.landAnimationState, ButterflyAnimations.land, ageInTicks, 1.0f);
	}
}