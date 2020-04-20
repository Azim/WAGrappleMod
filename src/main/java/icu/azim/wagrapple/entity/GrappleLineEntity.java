package icu.azim.wagrapple.entity;

import java.util.ArrayList;
import java.util.List;
import icu.azim.wagrapple.WAGrappleMod;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RayTraceContext;
import net.minecraft.world.World;
import net.minecraft.world.RayTraceContext.FluidHandling;
import net.minecraft.world.RayTraceContext.ShapeType;

public class GrappleLineEntity extends Entity {
	

	private PlayerEntity player;
	private Vec3d hitPos;
	private float length = 0;
	
	private List<Vec3d> pieces;

	public GrappleLineEntity(EntityType<?> type, World world) {
		super(type, world);
	}
	
	public GrappleLineEntity(World world, PlayerEntity player, float length, Vec3d pos) {
		this(WAGrappleMod.GRAPPLE_LINE, world);
		this.updatePosition(pos.x, pos.y, pos.z);
		this.hitPos = pos;
		this.player = player;
		this.setLength(length);
		this.ignoreCameraFrustum = true;
		this.pieces = new ArrayList<Vec3d>();
		pieces.add(pos);
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
	
	public List<Vec3d> getPieces(){
		return pieces;
	}
	
	@Override
	public void tick() {
		if(player==null) {
			this.remove();
		}
		if(world.isClient) {
			BlockHitResult res = this.world.rayTrace(new RayTraceContext(new Vec3d(player.getX(),player.getEyeY(),player.getZ()),pieces.get(pieces.size()-1), ShapeType.OUTLINE, FluidHandling.NONE, player));
			
			if(res.getType()==Type.BLOCK) {
				if(pieces.get(pieces.size()-1)!=res.getPos()) {
					pieces.add(res.getPos());
					System.out.println(res.getPos().toString());
				}
			}else {
				
			}
		}
		
		
		
		
		
		
		
		
		super.tick();
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
