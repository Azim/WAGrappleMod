package icu.azim.wagrapple.entity;

import net.minecraft.block.BlockState;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class GrappleLinePiece {
	private Vec3d location;
	private Vec3d drawLocation;
	private BlockPos blockPos;
	private BlockState blockState;
	private Identifier blockId;
	private World world;
	
	public GrappleLinePiece(Vec3d location, BlockPos block, World world) {
		this.location = location;
		this.blockPos = block;
		this.world = world;
		this.blockState = world.getBlockState(block);
		this.blockId = Registry.BLOCK.getId(blockState.getBlock());
		this.drawLocation = getToDraw(location, blockState.getCollisionShape(world, block).getBoundingBox());
	}
	
	public Vec3d getLocation() {
		return location;
	}
	
	public Vec3d getDrawLocation() {
		return drawLocation;
	}
	
	public boolean blockTick() {
		if(world.getBlockState(blockPos)==blockState) {
			return true;
		}	
		return false;
	}
	
	public boolean isSameBlock(BlockPos nblock) {
		return Registry.BLOCK.getId(world.getBlockState(nblock).getBlock()).compareTo(blockId)==0?true:false;
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
	
	
}
