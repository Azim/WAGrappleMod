package icu.azim.wagrapple.render;

import java.util.OptionalDouble;
import com.mojang.blaze3d.systems.RenderSystem;
import icu.azim.wagrapple.entity.GrappleLineEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.Matrix4f;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class GrappleLineRenderer extends EntityRenderer<GrappleLineEntity> {
	
	public static final double width = 0.05;
	
	public GrappleLineRenderer(EntityRenderDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	public Identifier getTexture(GrappleLineEntity entity) {
		return new Identifier("wagrapple:textures/entity/scanner_line.png");
	}

	
	@Override
	public void render(GrappleLineEntity entity, float yaw, float tickDelta, MatrixStack matrixStack,
			VertexConsumerProvider vertexConsumerProvider, int light) {
		PlayerEntity playerEntity = entity.getPlayer();
		if (playerEntity != null) {
			matrixStack.push();
			matrixStack.push();
			matrixStack.multiply(this.renderManager.getRotation());
			matrixStack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(180.0F));
			matrixStack.pop();

			int j = playerEntity.getMainArm() == Arm.RIGHT ? 1 : -1;

			float h = playerEntity.getHandSwingProgress(tickDelta);
			float k = MathHelper.sin(MathHelper.sqrt(h) * 3.1415927F);
			float l = MathHelper.lerp(tickDelta, playerEntity.prevBodyYaw, playerEntity.bodyYaw) * 0.017453292F;
			double d = (double) MathHelper.sin(l);
			double e = (double) MathHelper.cos(l);
			double m = (double) j * 0.35D;
			double t;
			double u;
			double v;
			float w;
			double x;
			if ((this.renderManager.gameOptions == null || this.renderManager.gameOptions.perspective <= 0) && playerEntity == MinecraftClient.getInstance().player) {
				x = this.renderManager.gameOptions.fov;
				x /= 100.0D;
				Vec3d vec3d = new Vec3d((double) j * -0.36D * x, -0.045D * x, 0.4D);
				vec3d = vec3d.rotateX(-MathHelper.lerp(tickDelta, playerEntity.prevPitch, playerEntity.pitch) * 0.017453292F);
				vec3d = vec3d.rotateY(-MathHelper.lerp(tickDelta, playerEntity.prevYaw, playerEntity.yaw) * 0.017453292F);
				vec3d = vec3d.rotateY(k * 0.5F);
				vec3d = vec3d.rotateX(-k * 0.7F);
				t = MathHelper.lerp((double) tickDelta, playerEntity.prevX, playerEntity.getX()) + vec3d.x;
				u = MathHelper.lerp((double) tickDelta, playerEntity.prevY, playerEntity.getY()) + vec3d.y;
				v = MathHelper.lerp((double) tickDelta, playerEntity.prevZ, playerEntity.getZ()) + vec3d.z;
				w = playerEntity.getStandingEyeHeight();
			} else {
				t = MathHelper.lerp((double) tickDelta, playerEntity.prevX, playerEntity.getX()) - e * m - d * 0.8D;
				u = playerEntity.prevY + (double) playerEntity.getStandingEyeHeight() + (playerEntity.getY() - playerEntity.prevY) * (double) tickDelta - 0.45D;
				v = MathHelper.lerp((double) tickDelta, playerEntity.prevZ, playerEntity.getZ()) - d * m + e * 0.8D;
				w = playerEntity.isInSneakingPose() ? -0.1875F : 0.0F;
			}

			x = MathHelper.lerp((double) tickDelta, entity.prevX, entity.getX());
			double y = MathHelper.lerp((double) tickDelta, entity.prevY, entity.getY()) + 0.25D;
			double z = MathHelper.lerp((double) tickDelta, entity.prevZ, entity.getZ());
			float xpart = (float) (t - x);
			float ypart = (float) (u - y) + w;
			float zpart = (float) (v - z);
			VertexConsumer consumer = vertexConsumerProvider
					.getBuffer(RenderLayer.of(
							"grapple_line", VertexFormats.POSITION_COLOR, 7, 256,
							RenderLayer.MultiPhaseParameters.builder()
									.lineWidth(new RenderPhase.LineWidth(OptionalDouble.of(10)))
									.layering(new RenderPhase.Layering("projection_layering", () -> {
										RenderSystem.matrixMode(5889);
										RenderSystem.pushMatrix();
										RenderSystem.scalef(1.0F, 1.0F, 0.999F);
										RenderSystem.matrixMode(5888);
									}, () -> {
										RenderSystem.matrixMode(5889);
										RenderSystem.popMatrix();
										RenderSystem.matrixMode(5888);
									})).transparency(new RenderPhase.Transparency("translucent_transparency", () -> {
										RenderSystem.enableBlend();
										RenderSystem.defaultBlendFunc();
									}, () -> {
										RenderSystem.disableBlend();
									})).writeMaskState(new RenderPhase.WriteMaskState(true, true)).build(false)));
			Matrix4f matrix4f2 = matrixStack.peek().getModel();

			//consumer.vertex(matrix4f2, -0.001f, -0.001f, -0.001f).color(0, 0, 0, 255).next(); // the part at the very start of it
			//consumer.vertex(matrix4f2, 0.001f, 0.001f, 0.001f).color(0, 0, 0, 255).next();
			//FishingBobberEntityRenderer
			Vec3d begin = new Vec3d(0,0,0);
			if(entity.getHandler().size()>1) { //multiple points - multiple lines
				drawPiece(begin, entity.getHandler().getDrawPieces(1).subtract(entity.getPos()), consumer, matrix4f2); //draw the line between the entity and the first point
			
				for (int i = 1; i < entity.getHandler().size()-1; i++) { // skip the very start of it, cuz we already added it above
					Vec3d start = entity.getHandler().getDrawPieces(i).subtract(entity.getPos());
					Vec3d end = entity.getHandler().getDrawPieces(i+1).subtract(entity.getPos());
					drawPiece(start, end,  consumer, matrix4f2);
				
				}
				drawPiece(
					entity.getHandler().getDrawPieces(entity.getHandler().size()-1).subtract(entity.getPos()), //from last piece to player's hand
					new Vec3d(xpart, ypart, zpart),
					consumer, matrix4f2);
			}else { //only have 1 attachment point - direct line from it to the hand
				drawPiece(
						begin,
						new Vec3d(xpart, ypart, zpart),
						consumer, matrix4f2);
			}
			matrixStack.pop();
			super.render(entity, yaw, tickDelta, matrixStack, vertexConsumerProvider , light);
		}

	}
	
	private void drawPiece(Vec3d start, Vec3d end, VertexConsumer consumer, Matrix4f matrix) {
		
		if(start.squaredDistanceTo(end)>64) { //split each segment onto smaller segments
			Vec3d ba = end.subtract(start);
			ba = ba.normalize().multiply(8);
			drawPiece(start, end.subtract(ba), consumer, matrix);
			start = end.subtract(ba);
		}
		
		
		double offset = width/2;
		Vec3d diff = start.subtract(end);
		
		Vec3d any = new Vec3d(1,1,1);
		if(isSamePos(any, diff)) {
			any = new Vec3d(1,1,-1); //gotta check they are in fact different, to not break the rossProduct result
		}
		Vec3d perp = diff.crossProduct(any).normalize().multiply(offset); //get "any" perpendicular vector to create an offset
		Vec3d perpRotated = perp.crossProduct(diff).normalize().multiply(offset);      //get vector perpendicular to both of above vectors
		
		//do some offset magic, so the 2d line becomes a cuboid
		Vec3d a1 = start.add(perp);
		Vec3d a2 = start.add(perpRotated);
		Vec3d a3 = start.subtract(perp);
		Vec3d a4 = start.subtract(perpRotated);
		
		Vec3d b1 = end.add(perp);
		Vec3d b2 = end.add(perpRotated);
		Vec3d b3 = end.subtract(perp);
		Vec3d b4 = end.subtract(perpRotated);
		
		drawQuad(a1, a2, b1, b2, consumer, matrix, 20, 20, 20, 255);
		drawQuad(a2, a3, b2, b3, consumer, matrix, 10, 10, 10, 255);
		drawQuad(a3, a4, b3, b4, consumer, matrix, 20, 20, 20, 255);
		drawQuad(a4, a1, b4, b1, consumer, matrix, 10, 10, 10, 255);
		drawQuad(a1, a2, a4, a3, consumer, matrix, 0, 0, 0, 255);    //draw the squares at the start and the end of the line
		drawQuad(b1, b2, b4, b3, consumer, matrix, 0, 0, 0, 255);
	}
	
	private void drawQuad(Vec3d a1, Vec3d a2, Vec3d b1, Vec3d b2, VertexConsumer consumer, Matrix4f matrix, int r, int g, int b, int a) {
		consumer.vertex(matrix, (float)a1.x, (float)a1.y, (float)a1.z).color(r, g, b, a).next();
		consumer.vertex(matrix, (float)a2.x, (float)a2.y, (float)a2.z).color(r, g, b, a).next();
		consumer.vertex(matrix, (float)b2.x, (float)b2.y, (float)b2.z).color(r, g, b, a).next();
		consumer.vertex(matrix, (float)b1.x, (float)b1.y, (float)b1.z).color(r, g, b, a).next();
	}
	

	private boolean isSamePos(Vec3d a, Vec3d b) {
		if(Math.round(a.getX())==Math.round(b.getX())&&Math.round(a.getY())==Math.round(b.getY())&&Math.round(a.getZ())==Math.round(b.getZ())) {
			return true;
		}
		return false;
	}

}
