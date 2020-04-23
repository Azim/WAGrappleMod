package icu.azim.wagrapple.entity;

import icu.azim.wagrapple.WAGrappleMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RayTraceContext;
import net.minecraft.world.World;
import net.minecraft.world.RayTraceContext.FluidHandling;
import net.minecraft.world.RayTraceContext.ShapeType;

public class GrappleLineEntity extends Entity {
	

	private PlayerEntity player;
	private Vec3d hitPos;
	private float length = 0;
	private Vec3d motion;
	private static Vec3d gravity = new Vec3d(0, -0.05, 0);
	
	private GrappleLineHandler lineHandler;
	//private List<Vec3d> pieces;

	public GrappleLineEntity(EntityType<?> type, World world) {
		super(type, world);
	}
	
	public GrappleLineEntity(World world, PlayerEntity player, float length, Vec3d pos) {
		this(WAGrappleMod.GRAPPLE_LINE, world);
		this.updatePosition(pos.x, pos.y, pos.z);
		this.hitPos = pos;
		this.player = player;
		this.setLength(16);
		this.ignoreCameraFrustum = true;
		lineHandler= new GrappleLineHandler(16);
		lineHandler.add(0, pos, pos);
		motion = new Vec3d(0,0,0);
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
			grapplePhysicsTick();
			movementPhysicsTick();
		}
		
		
		super.tick();
	}
	
	public void grapplePhysicsTick() {
		BlockHitResult res = this.world.rayTrace(new RayTraceContext(new Vec3d(player.getX(),player.getEyeY(),player.getZ()),lineHandler.getPiece(lineHandler.size()-1), ShapeType.OUTLINE, FluidHandling.NONE, player));
		
		if(res.getType()==Type.BLOCK) {
			Vec3d pos = res.getPos();
			Box shape = world.getBlockState(res.getBlockPos()).getCollisionShape(world, res.getBlockPos()).getBoundingBox();
			System.out.println(shape.toString());
			
			if(!isSamePos( lineHandler.getPiece(lineHandler.size()-1), pos)) {
				if(lineHandler.size()>1) {
					if(!isSamePos( lineHandler.getPiece(lineHandler.size()-2), pos)) {
						Vec3d draw = getToDraw(pos,shape);
						lineHandler.add(pos,draw);
						//System.out.println(lineHandler.size()+":"+pos.toString()+":"+draw.toString());
					}
				}else {
					Vec3d draw = getToDraw(pos,shape);
					lineHandler.add(pos,draw);
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
		
		double totalLen = player.getPos().distanceTo(lineHandler.getLastPiece())+lineHandler.getPiecesLen();
		if(totalLen>lineHandler.getMaxLen() && player.getPos().squaredDistanceTo(lineHandler.getLastPiece())>9) {
			Vec3d originToPlayer = lineHandler.getLastPiece().subtract(player.getPos());
			
			Vec3d projection = project(player.getVelocity(),originToPlayer);
			
			Vec3d newSpeed = player.getVelocity().subtract(projection);
			newSpeed = newSpeed.multiply((player.getVelocity().length()+0.01)/newSpeed.length());
			
			Vec3d direction = originToPlayer.normalize().multiply(totalLen-lineHandler.getMaxLen());
			
			if(newSpeed.lengthSquared()<direction.lengthSquared()) { //outside of the radius, but not swinging
				newSpeed = newSpeed.add(direction);
			}
			motion = newSpeed;//.add(direction);
			
			if(MinecraftClient.getInstance().options.keyForward.isPressed() && player.getPos().y<lineHandler.getLastPiece().y) {
				motion = motion.add(player.getRotationVector().normalize().multiply(0.1));
			}
			
			
			player.setVelocity(motion.x, motion.y, motion.z);
		}
	}
	private Vec3d project(Vec3d a, Vec3d b) {
		return b.multiply(a.dotProduct(b)/b.dotProduct(b));
	}
	
	@SuppressWarnings("unused")
	private static double getAngle(Vec3d a, Vec3d b) {
		double part = (a.x*b.x+a.y*b.y+a.z*b.z)/(a.length()*b.length());
		return Math.acos(part);
	}
	
	private Vec3d getToDraw(Vec3d pos, Box shape) {
		return new Vec3d(
				pos.x+((pos.x>0?1:-1)*
						(Math.abs(pos.x-(int)pos.x)>0.5?0.05:-0.05)),
				pos.y+((pos.y>0?1:-1)*
						(Math.abs(pos.y-(int)pos.y)>0.5?0.05:-0.05)),
				pos.z+((pos.z>0?1:-1)*
						(Math.abs(pos.z-(int)pos.z)>0.5?0.05:-0.05))
				);
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

	public Vec3d getHitPos() {
		return hitPos;
	}

	public void setHitPos(Vec3d hitPos) {
		this.hitPos = hitPos;
	}

	public float getLength() {
		return length;
	}

	public void setLength(float length) {
		this.length = length;
	}

}
