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
		counter++;
		if(counter%5==0) {
			System.out.println(counter);
		}
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

		
		//VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayer.getLines());
		
		PlayerEntity entityplayer = entity.getPlayer();
		int k = (entity.getPlayer().getActiveHand()==Hand.MAIN_HAND?1:-1);
		
		double handAdjustment = (double) k * 0.35D;
		Vec3d start = entityplayer.getPos().add(new Vec3d(handAdjustment,entityplayer.getEyeHeight(entityplayer.getPose()),0));
		Vec3d offset = entity.getHitPos().subtract(start);

		buffer.begin(1, VertexFormats.POSITION_COLOR);
		
		for(int i = 0;i<16;i++) {
			buffer.vertex(  (start.x+offset.x*(i/16)),  (start.y+offset.y*(i/16)),  (start.z+offset.z*(i/16))).color(254, 254, 254, 254).light(254).texture(1, 1).overlay(OverlayTexture.DEFAULT_UV).normal(matrices.peek().getNormal(), 0, 1, 0).next();
			buffer.vertex(  (start.x+offset.x*((i+1)/16)),  (start.y+offset.y*((i+1)/16)),  (start.z+offset.z*((i+1)/16))).color(254, 254, 254, 254).light(254).texture(1, 1).overlay(OverlayTexture.DEFAULT_UV).normal(matrices.peek().getNormal(), 0, 1, 0).next();
		}
		tessellator.draw();
		//matrices.pop();
		RenderSystem.disableBlend();
        RenderSystem.enableTexture();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
		//super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
	}
	
}
