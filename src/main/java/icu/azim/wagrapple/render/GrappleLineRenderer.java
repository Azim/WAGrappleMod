package icu.azim.wagrapple.render;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import icu.azim.wagrapple.entity.GrappleLine;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class GrappleLineRenderer extends EntityRenderer<GrappleLine>{
	int counter = 0;
	
	public GrappleLineRenderer(EntityRenderDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	public Identifier getTexture(GrappleLine entity) {
		return new Identifier("wagrapple:textures/entity/scanner_line.png");
	}
	
	
	@Override
	public void render(GrappleLine entity, float yaw, float tickDelta, MatrixStack matrices,
			VertexConsumerProvider vertexConsumers, int light) {
		RenderSystem.pushMatrix();
		RenderSystem.translated(entity.getX(), entity.getY(), entity.getZ());
		RenderSystem.enableRescaleNormal();
		RenderSystem.scalef(0.5F, 0.5F, 0.5F);
		RenderSystem.popMatrix();
		
		RenderSystem.disableTexture();
        RenderSystem.enableBlend();
		GL11.glEnable(GL11.GL_LINE_SMOOTH);
		RenderSystem.lineWidth(1);
		
		Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

		PlayerEntity entityplayer = entity.getPlayer();
		
		//VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayer.getLines());
		
		int k = (entity.getPlayer().getActiveHand()==Hand.MAIN_HAND?1:-1);
        
		float swingProgress = entityplayer.getHandSwingProgress(tickDelta);
		float f8 = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
		float f9 = (entityplayer.prevBodyYaw
				+ (entityplayer.bodyYaw - entityplayer.prevBodyYaw) * tickDelta) * 0.017453292F;
		double yawSin = (double) MathHelper.sin(f9);
		double yawCos = (double) MathHelper.cos(f9);
		double handAdjustment = (double) k * 0.35D;
		//double d3 = 0.8D;
		double playerX;
		double playerY;
		double playerZ;
		double eyeHeightAdjustment;

		if ((this.renderManager.gameOptions == null || this.renderManager.gameOptions.perspective <= 0)
				/*&& entityplayer == Minecraft.getMinecraft().player*/) {
			float f10 = (float) this.renderManager.gameOptions.fov;
			f10 = f10 / 100.0F;
			Vec3d vec3d = new Vec3d((double) k * -0.36D * (double) f10, -0.045D * (double) f10, 0.4D);
			vec3d = vec3d.rotateX(-(entityplayer.prevPitch
					+ (entityplayer.pitch - entityplayer.prevPitch) * tickDelta) * 0.017453292F);
			vec3d = vec3d.rotateY(-(entityplayer.prevYaw
					+ (entityplayer.yaw - entityplayer.prevYaw) * tickDelta) * 0.017453292F);
			vec3d = vec3d.rotateY(f8 * 0.5F);
			vec3d = vec3d.rotateX(-f8 * 0.7F);
			playerX = entityplayer.prevX + (entityplayer.getX() - entityplayer.prevX) * (double) tickDelta
					+ vec3d.x;
			playerY = entityplayer.prevY + (entityplayer.getY() - entityplayer.prevY) * (double) tickDelta
					+ vec3d.y;
			playerZ = entityplayer.prevZ + (entityplayer.getZ() - entityplayer.prevZ) * (double) tickDelta
					+ vec3d.z;
			eyeHeightAdjustment = (double) entityplayer.getEyeHeight(entityplayer.getPose());
		} else {
			playerX = entityplayer.prevX + (entityplayer.getX() - entityplayer.prevX) * (double) tickDelta
					- yawCos * handAdjustment - yawSin * 0.8D;
			playerY = entityplayer.prevY + (double) entityplayer.getEyeHeight(entityplayer.getPose())
					+ (entityplayer.getX() - entityplayer.prevY) * (double) tickDelta - 0.8D;
			playerZ = entityplayer.prevZ + (entityplayer.getZ() - entityplayer.prevZ) * (double) tickDelta
					- yawSin * handAdjustment + yawCos * 0.4D;
			eyeHeightAdjustment = entityplayer.isSneaking() ? -0.1875D : 0.0D;
		}
		double groundX = entity.prevX + (entity.getX() - entity.prevX) * (double) tickDelta;
		double groundY = entity.prevY + (entity.getY() - entity.prevY) * (double) tickDelta + 0.25D;
		double grounxZ = entity.prevZ + (entity.getZ() - entity.prevZ) * (double) tickDelta;
		double deltaX = (double) ((float) (playerX - groundX));
		double deltaY = (double) ((float) (playerY - groundY)) + eyeHeightAdjustment;
		double deltaZ = (double) ((float) (playerZ - grounxZ));
		
		
		
		Vec3d start = entityplayer.getPos();//.add(new Vec3d(handAdjustment,entityplayer.getEyeHeight(entityplayer.getPose()),0));
		Vec3d offset = entity.getHitPos().subtract(start);

		buffer.begin(1, VertexFormats.POSITION_COLOR);
		for (int count = 0; count <= 16; ++count) {
			float proportion = (float) count / 16.0F;
			buffer.vertex(entity.getX() + deltaX*proportion,entity.getY()+ deltaY * (double) (proportion * proportion + proportion) * 0.5D, entity.getZ() + deltaZ * (double) proportion)
			.color(254, 254, 254, 254).light(254).texture(1, 1).overlay(OverlayTexture.DEFAULT_UV).normal(matrices.peek().getNormal(), 0, 1, 0).next();
		}
		
		//buffer.vertex(start.x,  start.y,  start.z).color(254, 254, 254, 254).light(254).texture(1, 1).overlay(OverlayTexture.DEFAULT_UV).normal(matrices.peek().getNormal(), 0, 1, 0).next();
		//buffer.vertex(entity.getHitPos().x,  entity.getHitPos().y,  entity.getHitPos().z).color(254, 254, 254, 254).light(254).texture(1, 1).overlay(OverlayTexture.DEFAULT_UV).normal(matrices.peek().getNormal(), 0, 1, 0).next();
		
		
		
		//for(int i = 0;i<16;i++) {
			//buffer.vertex(  (start.x+offset.x*(i/16)),  (start.y+offset.y*(i/16)),  (start.z+offset.z*(i/16))).color(254, 254, 254, 254).light(254).texture(1, 1).overlay(OverlayTexture.DEFAULT_UV).normal(matrices.peek().getNormal(), 0, 1, 0).next();
			//buffer.vertex(  (start.x+offset.x*((i+1)/16)),  (start.y+offset.y*((i+1)/16)),  (start.z+offset.z*((i+1)/16))).color(254, 254, 254, 254).light(254).texture(1, 1).overlay(OverlayTexture.DEFAULT_UV).normal(matrices.peek().getNormal(), 0, 1, 0).next();
	//	}
		tessellator.draw();
		//matrices.pop();
		RenderSystem.disableBlend();
        RenderSystem.enableTexture();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
		//super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
	}
	
}
