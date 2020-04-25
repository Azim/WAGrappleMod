package icu.azim.wagrapple.entity;

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
	private Vec3d motion;
	
	private GrappleLineHandler lineHandler;
	private KeyBinding ascend;
	private KeyBinding descend;
	private KeyBinding boost;
	private double boostSpeed;
	private int boostCooldown;

	public GrappleLineEntity(EntityType<?> type, World world) {
		super(type, world);
	}
	
	public GrappleLineEntity(World world, PlayerEntity player, double length, Vec3d pos, BlockPos bpos) {
		this(WAGrappleMod.GRAPPLE_LINE, world);
		this.updatePosition(pos.x, pos.y, pos.z);
		this.player = player;
		this.ignoreCameraFrustum = true;
		lineHandler= new GrappleLineHandler(this, length);
		lineHandler.add(0, pos, bpos);
		motion = new Vec3d(0,0,0);
		boostSpeed = 1;
		
		ascend = MinecraftClient.getInstance().options.keySneak;
		descend = MinecraftClient.getInstance().options.keySprint;
		boost = MinecraftClient.getInstance().options.keyJump;
		
		boostCooldown = 15;
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
		
		if(world.isClient) {
			if(boostCooldown>0) boostCooldown--;

			lineHandler.tick();
			if(this.removed) {
				return;
			}
			handlePlayerInput();
			if(this.removed) {
				return;
			}
			grapplePhysicsTick();
			if(this.removed) {
				return;
			}
			movementPhysicsTick();
			if(this.removed) {
				return;
			}
			
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
		System.out.println(player.abilities.flying+" "+player.onGround);
		if(player.abilities.flying||player.onGround) {
			boostCooldown = 15;
		}
		
		if(boost.isPressed() && !player.abilities.flying && (boostCooldown==0)) {
			Vec3d origin = lineHandler.getLastPiece();
			Vec3d direction = player.getCameraPosVec(0).subtract(origin).normalize().multiply(-boostSpeed);
			player.addVelocity(direction.x,direction.y,direction.z);
			
			detachLine();
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
		BlockHitResult res = this.world.rayTrace(new RayTraceContext(player.getCameraPosVec(0),lineHandler.getPiece(lineHandler.size()-1), ShapeType.COLLIDER, FluidHandling.NONE, player));
		
		if(res.getType()==Type.BLOCK) {
			Vec3d pos = res.getPos();
			BlockPos blockPos = res.getBlockPos();
			
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
		double distanceToOrigin = player.getCameraPosVec(0).distanceTo(origin);
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
			
			if(MinecraftClient.getInstance().options.keyForward.isPressed() && player.getCameraPosVec(0).y<origin.y) {
				motion = motion.add(player.getRotationVector().normalize().multiply(0.05));
			}
			if(motion.lengthSquared()>6.25) {
				motion = motion.normalize().multiply(2.5);
			}
			player.setVelocity(motion.x, motion.y, motion.z);
		}else {
			if(player.onGround && totalLen<lineHandler.getMaxLen()) { //player moves towards the pivot point on land
				//do nothing actually, it's not this way in WA
			}
		}
	}
	
	
	public void destroyLine() {
		if(world.isClient) {
			player.playSound(SoundEvents.ENTITY_ITEM_BREAK, 1, 1);
			
			PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
			passedData.writeBoolean(true);
			ClientSidePacketRegistry.INSTANCE.sendToServer(WAGrappleMod.DETACH_LINE_PACKET_ID, passedData);
		}
		this.remove();
	}
	
	public void detachLine() {
		if(world.isClient) {
			player.playSound(SoundEvents.ENTITY_SPLASH_POTION_BREAK, 1, 1);
			
			PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
			passedData.writeBoolean(true);
			ClientSidePacketRegistry.INSTANCE.sendToServer(WAGrappleMod.DETACH_LINE_PACKET_ID, passedData);
		}
		this.remove();
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
	

	@Override
	public PistonBehavior getPistonBehavior() {
		return PistonBehavior.IGNORE;
	}
	
	@Override
	public boolean shouldRender(double distance) {
		return true;
	}
	
	@Override
	public boolean shouldRender(double cameraX, double cameraY, double cameraZ) {
		return true;
	}
	
}
