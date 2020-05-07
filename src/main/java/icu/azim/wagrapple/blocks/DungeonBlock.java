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
	
	public static final IntProperty DUNGEON = IntProperty.of("dungeon", 0, 15);
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
		int x = pos.getX()%maxConnect;
		int y = pos.getY()%maxConnect;
		int z = pos.getZ()%maxConnect;
		
		return x + maxConnect*y+maxConnect*maxConnect*z;
	}
	
}
