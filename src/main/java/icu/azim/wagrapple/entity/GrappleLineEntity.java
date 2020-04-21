package icu.azim.wagrapple.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.RayTraceContext;
import net.minecraft.world.World;
import net.minecraft.world.RayTraceContext.FluidHandling;
import net.minecraft.world.RayTraceContext.ShapeType;

public class GrappleLineEntity extends Entity {
	

	private PlayerEntity player;
	private Vec3d hitPos;
	private float length = 0;
	
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
		this.setLength(length);
		this.ignoreCameraFrustum = true;
		lineHandler= new GrappleLineHandler();
		lineHandler.add(pos, pos);
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
		}
		if(world.isClient) {
			BlockHitResult res = this.world.rayTrace(new RayTraceContext(new Vec3d(player.getX(),player.getEyeY(),player.getZ()),lineHandler.getPiece(lineHandler.size()-1), ShapeType.OUTLINE, FluidHandling.NONE, player));
			
			if(res.getType()==Type.BLOCK) {
				Vec3d pos = res.getPos();
				Box shape = world.getBlockState(res.getBlockPos()).getOutlineShape(world, res.getBlockPos()).getBoundingBox();
				
				
				if(lineHandler.getPiece(lineHandler.size()-1) != pos) {
					Vec3d draw = getToDraw(pos,shape);
					lineHandler.add(pos,draw);
					System.out.println(lineHandler.size()+":"+pos.toString()+":"+draw.toString());
				}
			}else {
				
			}
		}
		
		
		super.tick();
	}
	
	private Vec3d getToDraw(Vec3d pos, Box shape) {
		/*
		int ix = (int)pos.x;
		double px = pos.x-ix;
		double cx = getClosest(shape.x1,shape.x2,px);
		double dx = Math.abs(cx-px);
		
		int iy = (int)pos.y;
		double py = pos.y-iy;
		double cy = getClosest(shape.y1,shape.y2,py);
		double dy = Math.abs(cy-py);
		
		int iz = (int)pos.z;
		double pz = pos.z-iz;
		double cz = getClosest(shape.z1,shape.z2,pz);
		double dz = Math.abs(cz-pz);
		
		if(dx<=dz && dy<=dz) {
			return new Vec3d(
					ix+(ix>0?cx:-cx),
					iy+(iy>0?cy:-cy),
					pos.z);
					
		}else if(dx<=dy && dz<=dy) {
			return new Vec3d(
					ix+(ix>0?cx:-cx),
					pos.y,
					iz+(iz>0?cz:-cz));
		}else {
			return new Vec3d(
					pos.x,
					iy+(iy>0?cy:-cy),
					iz+(iz>0?cz:-cz));
		}*/
		return new Vec3d(
				pos.x+((pos.x>0?1:-1)*
						Math.abs(pos.x-(int)pos.x)>0.5?+0.05:-0.05),
				pos.y+((pos.y>0?1:-1)*
						Math.abs(pos.y-(int)pos.y)>0.5?+0.05:-0.05),
				pos.z+((pos.z>0?1:-1)*
						Math.abs(pos.z-(int)pos.z)>0.5?+0.05:-0.05)
				);
	}
	
	
	private double getClosest(double a, double b, double x) {
		if(Math.abs(b-x)>Math.abs(a-x)) {
			return a;
		}else {
			return b;
		}
	}
	
	private double round(double x, int i) {
		BigDecimal bd = BigDecimal.valueOf(x);
	    bd = bd.setScale(i, RoundingMode.HALF_UP);
	    return bd.doubleValue();
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
