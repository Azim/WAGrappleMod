package icu.azim.wagrapple.item;

import icu.azim.wagrapple.WAGrappleMod;
import icu.azim.wagrapple.entity.GrappleLineEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RayTraceContext;
import net.minecraft.world.World;

public class GrappleItem extends Item{

	public GrappleItem(Settings settings) {
		super(settings);
	}
	
	@Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity playerEntity, Hand hand)
    {
		
		if(!WAGrappleMod.GRAPPLE_COMPONENT.get(playerEntity).isGrappled()) {
		
			Vec3d vec3d = playerEntity.getCameraPosVec(0);
		    Vec3d vec3d2 = playerEntity.getRotationVec(0);
		    Vec3d vec3d3 = vec3d.add(vec3d2.x * 16, vec3d2.y * 16, vec3d2.z * 16);
		    BlockHitResult result = playerEntity.world.rayTrace(new RayTraceContext(vec3d, vec3d3, RayTraceContext.ShapeType.COLLIDER, RayTraceContext.FluidHandling.NONE, playerEntity));
		   
		
			if(result.getType()==Type.BLOCK) {
				world.playSound(playerEntity, result.getPos().x,result.getPos().y,result.getPos().z, SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.PLAYERS, 1.0F, 1.0F);
				if(!world.isClient) {
					GrappleLineEntity entity = new GrappleLineEntity(world, playerEntity, vec3d.distanceTo(result.getPos())+1, result.getPos(), result.getBlockPos());
					world.spawnEntity(entity);
					WAGrappleMod.GRAPPLE_COMPONENT.get(playerEntity).setLineId(entity.getEntityId());
					WAGrappleMod.GRAPPLE_COMPONENT.get(playerEntity).setGrappled(true);
					WAGrappleMod.GRAPPLE_COMPONENT.get(playerEntity).sync();
					System.out.println("server - spawned");
				}
			}else {
				playerEntity.playSound(SoundEvents.BLOCK_WOOL_BREAK, 1.0F, 1.0F);
			}
			playerEntity.swingHand(hand);
			
		}else {
			
			int id = WAGrappleMod.GRAPPLE_COMPONENT.get(playerEntity).getLineId();
			if(id>0) {
				Entity e = world.getEntityById(id);
				if(e!=null) {
					e.remove();
				}
			}
			
			WAGrappleMod.GRAPPLE_COMPONENT.get(playerEntity).setLineId(-1);
			WAGrappleMod.GRAPPLE_COMPONENT.get(playerEntity).setGrappled(false);
			WAGrappleMod.GRAPPLE_COMPONENT.get(playerEntity).sync();

			playerEntity.playSound(SoundEvents.BLOCK_WOOL_BREAK, 1.0F, 1.0F);
		}
		
		
        return new TypedActionResult<>(ActionResult.SUCCESS, playerEntity.getStackInHand(hand));
    }
}
