package icu.azim.wagrapple.item;

import java.util.List;
import icu.azim.wagrapple.WAGrappleMod;
import icu.azim.wagrapple.entity.GrappleLineEntity;
import icu.azim.wagrapple.util.Util;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Arm;
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
			int ihand = playerEntity.getMainArm() == Arm.RIGHT ? 1 : -1;
			ihand *= (hand==Hand.MAIN_HAND)?1:-1;
		    Vec3d from = Util.getPlayerShoulder(playerEntity, ihand, 1);
		    Vec3d to = from.add(playerEntity.getRotationVec(0).multiply(WAGrappleMod.maxLength));
		    BlockHitResult result = playerEntity.world.rayTrace(new RayTraceContext(from, to, RayTraceContext.ShapeType.COLLIDER, RayTraceContext.FluidHandling.NONE, playerEntity));
			if(result.getType()==Type.BLOCK) {
				if(world.isClient) {
					
					world.playSound(playerEntity, result.getPos().x,result.getPos().y,result.getPos().z, SoundEvents.BLOCK_ANVIL_PLACE, SoundCategory.PLAYERS, 0.3F, 1.0F);
				}else {
					GrappleLineEntity entity = new GrappleLineEntity(world, playerEntity, playerEntity.getPos().distanceTo(result.getPos())+1, result);
					world.spawnEntity(entity);
					WAGrappleMod.GRAPPLE_COMPONENT.get(playerEntity).setLineId(entity.getEntityId());
					WAGrappleMod.GRAPPLE_COMPONENT.get(playerEntity).setGrappled(true);
					WAGrappleMod.GRAPPLE_COMPONENT.get(playerEntity).sync();
					System.out.println("server - spawned");
				}
			}else {
				playerEntity.playSound(SoundEvents.ITEM_ARMOR_EQUIP_CHAIN, 1.0F, 1.0F);
			}
			
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

			playerEntity.playSound(SoundEvents.ITEM_ARMOR_EQUIP_CHAIN, 1.0F, 1.0F);
		}
		
		
        return new TypedActionResult<>(ActionResult.PASS, playerEntity.getStackInHand(hand));
    }
	
	@Override
	public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
		//TranslatableText tooltipText = new TranslatableText("item.wagrapple.grapple.tooltip");
		String text = I18n.translate("item.wagrapple.grapple.tooltip",
				MinecraftClient.getInstance().options.keySneak.getLocalizedName(),
				MinecraftClient.getInstance().options.keySprint.getLocalizedName(),
				MinecraftClient.getInstance().options.keyJump.getLocalizedName());
		for(String line : text.split("\n")) {
			tooltip.add(new LiteralText(line));
		}
	}
}
