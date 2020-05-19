package icu.azim.wagrapple.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DungeonBlock extends Block {
	
	public static final IntProperty DUNGEON = IntProperty.of("dungeon", 0, 215);
	private static final int maxConnect = 6;
	
	
	public DungeonBlock(Settings settings) {
		super(settings);
		setDefaultState(getStateManager().getDefaultState().with(DUNGEON, 0));
	}
	
	@Override
	protected void appendProperties(Builder<Block, BlockState> builder) {
		builder.add(DUNGEON);
	}
	
	@Override
	public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
		super.onPlaced(world, pos, state, placer, itemStack);
		world.setBlockState(pos, state.with(DUNGEON, getPositionIndex(pos)));
	}
	
	public static int getPositionIndex(BlockPos pos) {
		return getPositionIndex(pos.getX(), pos.getY(),pos.getZ());
	}
	
	public static int getPositionIndex(int x, int y, int z) {
		while(x<0) x+=6;
		while(y<0) y+=6;
		while(z<0) z+=6;
		x = x % maxConnect;
		y = y % maxConnect;
		z = z % maxConnect;
		return x + maxConnect*y + maxConnect*maxConnect*z;
	}
	
}
