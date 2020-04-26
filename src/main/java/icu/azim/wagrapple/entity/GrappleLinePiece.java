package icu.azim.wagrapple.entity;

import net.minecraft.block.BlockState;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class GrappleLinePiece {
	private Vec3d location;
	private BlockPos blockPos;
	private BlockState blockState;
	private Vec3d direction;
	private Identifier blockId;
	private World world;
	
	public GrappleLinePiece(Vec3d location, BlockPos block, Vec3d direction, World world) {
		this.location = location;
		this.blockPos = block;
		this.world = world;
		this.blockState = world.getBlockState(block);
		this.blockId = Registry.BLOCK.getId(blockState.getBlock());
		this.direction = direction;
	}
	
	public Vec3d getDirection() {
		return direction;
	}
	
	public Vec3d getLocation() {
		return location;
	}
	
	
	public boolean blockTick() {
		if(world.getBlockState(blockPos)==blockState) {
			return true;
		}
		return false;
	}
	
	
	public double compare(Vec3d vector) {
		double angle = getAngle(direction,vector)*180/Math.PI;
		return angle;
	}
	
	private static double getAngle(Vec3d a, Vec3d b) {
		double part = (a.x*b.x+a.y*b.y+a.z*b.z)/(a.length()*b.length());
		return Math.acos(part);
	}
	
	public boolean isSameBlock(BlockPos nblock) {
		return Registry.BLOCK.getId(world.getBlockState(nblock).getBlock()).compareTo(blockId)==0?true:false;
	}
}
