package icu.azim.wagrapple.item;

import icu.azim.wagrapple.entity.GrappleLine;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.world.World;

public class GrappleItem extends Item{

	public GrappleItem(Settings settings) {
		super(settings);
	}

	@Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity playerEntity, Hand hand)
    {
		HitResult result = playerEntity.rayTrace(32, 0, false);
		
		if(result.getType()==Type.BLOCK) {
			world.playSound(playerEntity, result.getPos().x,result.getPos().y,result.getPos().z, SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.PLAYERS, 1.0F, 1.0F);
			if(!world.isClient) {
				world.spawnEntity(new GrappleLine(world, playerEntity, 0, result.getPos()));
				System.out.println("server - spawned");
			}
		}else {
			playerEntity.playSound(SoundEvents.BLOCK_WOOL_BREAK, 1.0F, 1.0F);
		}
		
		
		playerEntity.swingHand(hand);
        return new TypedActionResult<>(ActionResult.SUCCESS, playerEntity.getStackInHand(hand));
    }
}
