package icu.azim.wagrapple.render;

import java.util.OptionalDouble;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import icu.azim.wagrapple.entity.GrappleLine;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.Matrix4f;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class GrappleLineRenderer extends EntityRenderer<GrappleLine>{
	
	public GrappleLineRenderer(EntityRenderDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	public Identifier getTexture(GrappleLine entity) {
		return new Identifier("wagrapple:textures/entity/scanner_line.png");
	}
	
	
	@Override
	public void render(GrappleLine entity, float yaw, float tickDelta, MatrixStack matrixStack,
			VertexConsumerProvider vertexConsumerProvider, int light) {
		PlayerEntity playerEntity = entity.getPlayer();
		if(playerEntity!=null) {
			matrixStack.push();
			matrixStack.push();
			matrixStack.multiply(this.renderManager.getRotation());
			matrixStack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(180.0F));
			matrixStack.pop();
			int j = playerEntity.getMainArm() == Arm.RIGHT ? 1 : -1;

	        float h = playerEntity.getHandSwingProgress(tickDelta);
	        float k = MathHelper.sin(MathHelper.sqrt(h) * 3.1415927F);
	        float l = MathHelper.lerp(tickDelta, playerEntity.prevBodyYaw, playerEntity.bodyYaw) * 0.017453292F;
	        double d = (double)MathHelper.sin(l);
	        double e = (double)MathHelper.cos(l);
	        double m = (double)j * 0.35D;
	        double n = 0.8D;
	        double t;
	        double u;
	        double v;
	        float w;
	        double x;
	        if ((this.renderManager.gameOptions == null || this.renderManager.gameOptions.perspective <= 0) && playerEntity == MinecraftClient.getInstance().player) {
	            x = this.renderManager.gameOptions.fov;
	            x /= 100.0D;
	            Vec3d vec3d = new Vec3d((double)j * -0.36D * x, -0.045D * x, 0.4D);
	            vec3d = vec3d.rotateX(-MathHelper.lerp(tickDelta, playerEntity.prevPitch, playerEntity.pitch) * 0.017453292F);
	            vec3d = vec3d.rotateY(-MathHelper.lerp(tickDelta, playerEntity.prevYaw, playerEntity.yaw) * 0.017453292F);
	            vec3d = vec3d.rotateY(k * 0.5F);
	            vec3d = vec3d.rotateX(-k * 0.7F);
	            t = MathHelper.lerp((double)tickDelta, playerEntity.prevX, playerEntity.getX()) + vec3d.x;
	            u = MathHelper.lerp((double)tickDelta, playerEntity.prevY, playerEntity.getY()) + vec3d.y;
	            v = MathHelper.lerp((double)tickDelta, playerEntity.prevZ, playerEntity.getZ()) + vec3d.z;
	            w = playerEntity.getStandingEyeHeight();
	         } else {
	            t = MathHelper.lerp((double)tickDelta, playerEntity.prevX, playerEntity.getX()) - e * m - d * 0.8D;
	            u = playerEntity.prevY + (double)playerEntity.getStandingEyeHeight() + (playerEntity.getY() - playerEntity.prevY) * (double)tickDelta - 0.45D;
	            v = MathHelper.lerp((double)tickDelta, playerEntity.prevZ, playerEntity.getZ()) - d * m + e * 0.8D;
	            w = playerEntity.isInSneakingPose() ? -0.1875F : 0.0F;
	         }
	        

	         x = MathHelper.lerp((double)tickDelta, entity.prevX, entity.getX());
	         double y = MathHelper.lerp((double)tickDelta, entity.prevY, entity.getY()) + 0.25D;
	         double z = MathHelper.lerp((double)tickDelta, entity.prevZ, entity.getZ());
	         float xpart = (float)(t - x);
	         float ypart = (float)(u - y) + w;
	         float zpart = (float)(v - z);
	         VertexConsumer consumer = vertexConsumerProvider.getBuffer(
	         RenderLayer.of(
	        		 "line",
	        		 VertexFormats.POSITION_COLOR,
	        		 1, 256,
	        		 RenderLayer.MultiPhaseParameters.builder()
	        		 .lineWidth(
	        				 new RenderPhase.LineWidth(OptionalDouble.of(10)))
	        		 .layering(
	        				 new RenderPhase.Layering("projection_layering", () -> {
	        					 RenderSystem.matrixMode(5889);
	        					 RenderSystem.pushMatrix();
	        					 RenderSystem.scalef(1.0F, 1.0F, 0.999F);
	        					 RenderSystem.matrixMode(5888);
	        				 }, () -> {
	        					 RenderSystem.matrixMode(5889);
	        					 RenderSystem.popMatrix();
	        					 RenderSystem.matrixMode(5888);
	        				 }))
	        		 .transparency(
	        				 new RenderPhase.Transparency("translucent_transparency", () -> {
	        					 RenderSystem.enableBlend();
	        					 RenderSystem.defaultBlendFunc();
	        				 }, () -> {
	        					 RenderSystem.disableBlend();
	        				 }))
	        		 .writeMaskState(new RenderPhase.WriteMaskState(true, false)).build(false))
	         );
	         Matrix4f matrix4f2 = matrixStack.peek().getModel();
	         
	         for(int counter = 0; counter < 16; ++counter) {
	        	int part = counter/16;
	        	consumer.vertex(matrix4f2, xpart*part, ypart* part/*(part*part+part) * 0.5F + 0.25F*/, zpart*part).color(0,0,0,255).next(); 
	        	part = (counter+1)/16;
	        	consumer.vertex(matrix4f2, xpart*part, ypart*part, zpart*part).color(0,0,0,255).next(); 
	         }

	         matrixStack.pop();
		}
		
		/*
		Vec3d start = entityplayer.getPos();//.add(new Vec3d(handAdjustment,entityplayer.getEyeHeight(entityplayer.getPose()),0));
		Vec3d offset = entity.getHitPos().subtract(start);

		buffer.begin(1, VertexFormats.POSITION_COLOR);
		for (int count = 0; count <= 16; ++count) {
			float proportion = (float) count / 16.0F;
			buffer.vertex(entity.getX() + deltaX*proportion,entity.getY()+ deltaY * (double) (proportion * proportion + proportion) * 0.5D, entity.getZ() + deltaZ * (double) proportion)
			.color(254, 254, 254, 254).light(254).texture(1, 1).overlay(OverlayTexture.DEFAULT_UV).normal(matrices.peek().getNormal(), 0, 1, 0).next();
		}
		*/
		//buffer.vertex(start.x,  start.y,  start.z).color(254, 254, 254, 254).light(254).texture(1, 1).overlay(OverlayTexture.DEFAULT_UV).normal(matrices.peek().getNormal(), 0, 1, 0).next();
		//buffer.vertex(entity.getHitPos().x,  entity.getHitPos().y,  entity.getHitPos().z).color(254, 254, 254, 254).light(254).texture(1, 1).overlay(OverlayTexture.DEFAULT_UV).normal(matrices.peek().getNormal(), 0, 1, 0).next();

	}

	private void method_23172(float f, float g, float h, VertexConsumer vertexConsumer, Matrix4f matrix4f, float i) {
		vertexConsumer.vertex(matrix4f, f * i, g * (i * i + i) * 0.5F + 0.25F, h * i).color(0, 0, 0, 255).next();
	}
	
}
