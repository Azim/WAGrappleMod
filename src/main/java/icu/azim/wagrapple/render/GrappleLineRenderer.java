package icu.azim.wagrapple.render;

import com.mojang.blaze3d.systems.RenderSystem;

import icu.azim.wagrapple.WAGrappleMod;
import icu.azim.wagrapple.entity.GrappleLineEntity;
import icu.azim.wagrapple.util.Util;
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
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class GrappleLineRenderer extends EntityRenderer<GrappleLineEntity> {
	
	public static final double width = 0.02;
	public static boolean debug = false;
	private static int prevHand = 0;
	
	
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
			matrixStack.push(); //cause when you rotate or translate, you mutate the matrix. but when you're done rendering, you need to restore the original state, so as to not mess up everything else that renders after you (c)UpcraftLP 
			matrixStack.push();
			matrixStack.multiply(this.renderManager.getRotation());
			matrixStack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(180.0F));
			matrixStack.pop();
			int hand = playerEntity.getMainArm() == Arm.RIGHT ? 1 : -1; //get hand
			boolean mainHandStack = true;
			ItemStack itemStack = playerEntity.getMainHandStack();
			if(prevHand == 0) prevHand = hand;
			if (itemStack.getItem() != WAGrappleMod.GRAPPLE_ITEM) {
				hand = -hand;
				mainHandStack = false;
				itemStack = playerEntity.getOffHandStack();
				if(itemStack.getItem()!=WAGrappleMod.GRAPPLE_ITEM) { //neither of hands have an item - but we should draw it where it last was
					hand = prevHand;
				}
			}
			prevHand = hand;
			
			float handSwingProgress = playerEntity.getHandSwingProgress(tickDelta); //some hand offset
			float handSwingSin = MathHelper.sin(MathHelper.sqrt(handSwingProgress) * 3.1415927F); 
			double nx; //somewhat player coordinates
			double ny;
			double nz;
			double fov;
			double lwidth;
			Vec3d vec3d = null;
			if ((this.renderManager.gameOptions == null || this.renderManager.gameOptions.perspective <= 0) && playerEntity == MinecraftClient.getInstance().player) {
				fov = this.renderManager.gameOptions.fov;
				fov /= 100.0D;
				vec3d = new Vec3d((double) hand * -0.37D * fov, -0.22D * fov, 0.35D); //offset to the hand
				vec3d = vec3d.rotateX(-MathHelper.lerp(tickDelta, playerEntity.prevPitch, playerEntity.pitch) * 0.017453292F); //apply pitch
				vec3d = vec3d.rotateY(-MathHelper.lerp(tickDelta, playerEntity.prevYaw, playerEntity.yaw) * 0.017453292F);     //apply yaw
				if(mainHandStack) { //player only swings main hand
					vec3d = vec3d.rotateY(handSwingSin * 0.5F); //apply hand swinging on Y and X
					vec3d = vec3d.rotateX(-handSwingSin * 0.7F);
				}
				nx = MathHelper.lerp((double) tickDelta, playerEntity.prevX, playerEntity.getX()) + vec3d.x; //player coordinates + offset 
				ny = MathHelper.lerp((double) tickDelta, playerEntity.prevY, playerEntity.getY()) + vec3d.y+ playerEntity.getStandingEyeHeight();
				nz = MathHelper.lerp((double) tickDelta, playerEntity.prevZ, playerEntity.getZ()) + vec3d.z;
				lwidth = width;
			} else { //third person mode
				Vec3d shoulderCoordinates = Util.getPlayerShoulder(playerEntity, hand, tickDelta);
				
				double opitch = -(entity.getLinePitch()+(float)Math.PI/2);
				double oyaw = entity.getLineYaw();
				Vec3d toolOffset = new Vec3d(0.025*hand,-0.55,-0.2).rotateX((float) opitch).rotateY((float) oyaw);
				
				nx = shoulderCoordinates.x+toolOffset.x;
				ny = shoulderCoordinates.y+toolOffset.y;
				nz = shoulderCoordinates.z+toolOffset.z;
				lwidth = width*2;
			}
			

			double x = entity.prevX==0?entity.getX():MathHelper.lerp((double) tickDelta, entity.prevX, entity.getX());//entity coordinates
			double y = entity.prevY==0?entity.getY():MathHelper.lerp((double) tickDelta, entity.prevY, entity.getY());
			double z = entity.prevZ==0?entity.getZ():MathHelper.lerp((double) tickDelta, entity.prevZ, entity.getZ());
			
			float xpart = (float) (nx - x); //get relative coordinates
			float ypart = (float) (ny - y);
			float zpart = (float) (nz - z);
			
			if(xpart>20||ypart>20||zpart>20) {
				System.out.println(xpart+" "+ypart+" "+zpart);
				System.out.println(entity.prevY+" "+entity.getY());
			}
			
			VertexConsumer consumer = vertexConsumerProvider
					.getBuffer(RenderLayer.of(
							"grapple_line", VertexFormats.POSITION_COLOR, 7, 256,
							RenderLayer.MultiPhaseParameters.builder()
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
							})).writeMaskState(new RenderPhase.WriteMaskState(true, true)).build(false)));  //creating custom render layer based off existing layers. adjusting values to fit the needs
			Matrix4f matrix4f2 = matrixStack.peek().getModel(); //it didnt work without the matrix


			Vec3d begin = new Vec3d(0,0,0);
			if(entity.getHandler().size()>1) { //multiple points - multiple lines
				drawPiece(begin, entity.getHandler().getPiecePos(1).subtract(entity.getPos()), lwidth, consumer, matrix4f2, entity.getHandler().getDirection(1)); //draw the line between the entity and the first point

				for (int i = 1; i < entity.getHandler().size()-1; i++) { // skip the very start of it, cuz we already added it above
					Vec3d start = entity.getHandler().getPiecePos(i).subtract(entity.getPos());
					Vec3d end = entity.getHandler().getPiecePos(i+1).subtract(entity.getPos());
					drawPiece(start, end, lwidth, consumer, matrix4f2, entity.getHandler().getDirection(i+1));

				}
				drawPiece(
						entity.getHandler().getPiecePos(entity.getHandler().size()-1).subtract(entity.getPos()), //from last piece to player's hand
						new Vec3d(xpart, ypart, zpart),
						lwidth,
						consumer,
						matrix4f2,
						entity.getHandler().getDirection(entity.getHandler().size()-1)
						);
			}else { //only have 1 attachment point - direct line from it to the hand
				drawPiece(
						begin,
						new Vec3d(xpart, ypart, zpart),
						lwidth,
						consumer, matrix4f2, entity.getHandler().getDirection(0));
			}
			matrixStack.pop();
		}

	}

	private void drawPiece(Vec3d start, Vec3d end, double width, VertexConsumer consumer, Matrix4f matrix, Vec3d direction) {
		drawPiece(start,end,width,consumer,matrix, false);
		if(debug) {
			drawPiece(end, end.add(direction), width, consumer, matrix, true);
		}
	}
	
	private void drawPiece(Vec3d start, Vec3d end, double width, VertexConsumer consumer, Matrix4f matrix, boolean debug) {
		
		if(start.squaredDistanceTo(end)>64) { //split each segment onto smaller segments
			Vec3d ba = end.subtract(start);
			ba = ba.normalize().multiply(8);
			drawPiece(start, end.subtract(ba), width, consumer, matrix, debug);
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
		if(!debug) {
			drawQuad(a1, a2, b1, b2, consumer, matrix, 20, 20, 20, 255);
			drawQuad(a2, a3, b2, b3, consumer, matrix, 10, 10, 10, 255);
			drawQuad(a3, a4, b3, b4, consumer, matrix, 20, 20, 20, 255);
			drawQuad(a4, a1, b4, b1, consumer, matrix, 10, 10, 10, 255);
			drawQuad(a2, a1, a3, a4, consumer, matrix, 0, 0, 0, 255);    //draw the squares at the start and the end of the line
			drawQuad(b1, b2, b4, b3, consumer, matrix, 0, 0, 0, 255);
		}else {
			drawQuad(a1, a2, b1, b2, consumer, matrix, 255, 0, 0, 255);
			drawQuad(a2, a3, b2, b3, consumer, matrix, 245, 10, 10, 255);
			drawQuad(a3, a4, b3, b4, consumer, matrix, 255, 0, 0, 255);
			drawQuad(a4, a1, b4, b1, consumer, matrix, 245, 10, 10, 255);
			drawQuad(a2, a1, a3, a4, consumer, matrix, 0, 0, 0, 255);    //draw the squares at the start and the end of the line
			drawQuad(b1, b2, b4, b3, consumer, matrix, 0, 0, 0, 255);
		}
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
