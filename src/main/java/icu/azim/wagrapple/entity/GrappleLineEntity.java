package icu.azim.wagrapple.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;

import icu.azim.wagrapple.WAGrappleMod;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RayTraceContext;
import net.minecraft.world.World;
import net.minecraft.world.RayTraceContext.FluidHandling;
import net.minecraft.world.RayTraceContext.ShapeType;

public class GrappleLineEntity extends Entity {
	

	private PlayerEntity player;
	private float length = 0;
	private Vec3d motion;
	
	private GrappleLineHandler lineHandler;
	private KeyBinding ascend;
	private KeyBinding descend;

	public GrappleLineEntity(EntityType<?> type, World world) {
		super(type, world);
	}
	
	public GrappleLineEntity(World world, PlayerEntity player, float length, Vec3d pos) {
		this(WAGrappleMod.GRAPPLE_LINE, world);
		this.updatePosition(pos.x, pos.y, pos.z);
		this.player = player;
		this.setLength(16);
		this.ignoreCameraFrustum = true;
		lineHandler= new GrappleLineHandler(this, 16);
		
		BlockHitResult result = (BlockHitResult) player.rayTrace(16, 0, false);
		if(result==null) {
			this.detachLine();
		}else {
			lineHandler.add(0, pos, result.getBlockPos());
		}
		
		motion = new Vec3d(0,0,0);
		ascend = MinecraftClient.getInstance().options.keySneak;
		descend = MinecraftClient.getInstance().options.keySprint;
	}

	@Override
	protected void initDataTracker() {
		//FishingBobberEntityRenderer
	}

	@Override
	protected void readCustomDataFromTag(CompoundTag tag) {
	}

	@Override
	protected void writeCustomDataToTag(CompoundTag tag) {
	}

	@Override
	public Packet<?> createSpawnPacket() {
	    return new EntitySpawnS2CPacket(this, player==null?this.getEntityId():player.getEntityId());
	}
	
	public GrappleLineHandler getHandler(){
		return lineHandler;
	}
	
	@Override
	public void tick() {
		if(player==null) {
			this.remove();
			return;
		}

		if(!player.onGround) {
			player.setSprinting(false);
			player.setSneaking(false);
		}
		
		if(world.isClient) {
			lineHandler.tick();
			handlePlayerInput();
			grapplePhysicsTick();
			movementPhysicsTick();
			
		}else {
			if(!WAGrappleMod.GRAPPLE_COMPONENT.get(player).isGrappled()) {
				this.remove();
			}
		}
		
		
		super.tick();
	}
	
	public void handlePlayerInput() {
		if(ascend.isPressed()&&descend.isPressed()) {
			return; //not moving anywhere
		}
		if(ascend.isPressed()) {
			if(lineHandler.getMaxLen()-lineHandler.getPiecesLen()>1) {
				lineHandler.setMaxLen(lineHandler.getMaxLen()-0.1);
			}
		}
		
		if(descend.isPressed()) {
			lineHandler.setMaxLen(lineHandler.getMaxLen()+0.1);
		}
	}
	
	
	public void grapplePhysicsTick() {
		BlockHitResult res = this.world.rayTrace(new RayTraceContext(new Vec3d(player.getX(),player.getEyeY(),player.getZ()),lineHandler.getPiece(lineHandler.size()-1), ShapeType.OUTLINE, FluidHandling.NONE, player));
		
		if(res.getType()==Type.BLOCK) {
			Vec3d pos = res.getPos();
			BlockPos blockPos = res.getBlockPos();
			//Box shape = world.getBlockState(blockPos).getCollisionShape(world, blockPos).getBoundingBox();
			//System.out.println(shape.toString());
			
			if(!isSamePos( lineHandler.getPiece(lineHandler.size()-1), pos)) {
				if(lineHandler.size()>1) {
					if(!isSamePos( lineHandler.getPiece(lineHandler.size()-2), pos)) {
						lineHandler.add(pos,blockPos);
						//System.out.println(lineHandler.size()+":"+pos.toString()+":"+draw.toString());
					}
				}else {
					lineHandler.add(pos,blockPos);
				}
			}
		}else {
			
		}
	}
	
	public void movementPhysicsTick() {
		/*
		if(true) {
			return;
		}//*/
		
		
		Vec3d origin = lineHandler.getLastPiece();
		double distanceToOrigin = player.getPos().distanceTo(origin);
		double totalLen = distanceToOrigin+lineHandler.getPiecesLen();
		if(totalLen>lineHandler.getMaxLen()) {
			
			Vec3d originToPlayer = origin.subtract(player.getPos());
			Vec3d direction = originToPlayer.normalize().multiply(totalLen-lineHandler.getMaxLen());
			Vec3d projection = project(player.getVelocity(),originToPlayer);
			

			Vec3d newSpeed = player.getVelocity().subtract(projection);
			
			//double angle = getAngle(new Vec3d(0, 1,0), direction.normalize())*180/Math.PI;
			newSpeed = newSpeed.multiply((player.getVelocity().length()-0.001)/newSpeed.length());
			
			
			
			if(newSpeed.lengthSquared()<direction.lengthSquared()) { //outside of the radius, but not swinging
				newSpeed = newSpeed.add(direction);
			}
			motion = newSpeed;//.add(direction);
			
			if(MinecraftClient.getInstance().options.keyForward.isPressed() && player.getPos().y<origin.y) {
				motion = motion.add(player.getRotationVector().normalize().multiply(0.05));
			}
			if(motion.lengthSquared()>6.25) {
				motion = motion.normalize().multiply(2.5);
			}
			System.out.println(round(motion.length(),2)+" - "+round(totalLen-lineHandler.getMaxLen(),2));
			player.setVelocity(motion.x, motion.y, motion.z);
		}
	}
	
	
	public void detachLine() {
		if(world.isClient) {
			//WAGrappleMod.GRAPPLE_COMPONENT.get(player).setLineId(-1);
			//WAGrappleMod.GRAPPLE_COMPONENT.get(player).setGrappled(false);
			//WAGrappleMod.GRAPPLE_COMPONENT.get(player).sync();
			player.playSound(SoundEvents.ENTITY_ITEM_BREAK, 1, 1);
			
			PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
			passedData.writeBoolean(true);
			ClientSidePacketRegistry.INSTANCE.sendToServer(WAGrappleMod.DETACH_LINE_PACKET_ID, passedData);
		}
		this.remove();
	}
	

	private double round(double x, int i) {
		BigDecimal bd = BigDecimal.valueOf(x);
		bd = bd.setScale(i, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}
	
	private Vec3d project(Vec3d a, Vec3d b) {
		return b.multiply(a.dotProduct(b)/b.dotProduct(b));
	}
	
	@SuppressWarnings("unused")
	private static double getAngle(Vec3d a, Vec3d b) {
		double part = (a.x*b.x+a.y*b.y+a.z*b.z)/(a.length()*b.length());
		return Math.acos(part);
	}
	

	private boolean isSamePos(Vec3d a, Vec3d b) {
		if(Math.round(a.getX())==Math.round(b.getX())&&Math.round(a.getY())==Math.round(b.getY())&&Math.round(a.getZ())==Math.round(b.getZ())) {
			return true;
		}
		return false;
	}
	
	
	public PlayerEntity getPlayer() {
		return player;
	}

	public float getLength() {
		return length;
	}

	public void setLength(float length) {
		this.length = length;
	}
	

	@Override
	public PistonBehavior getPistonBehavior() {
		return PistonBehavior.IGNORE;
	}
	
	
}
