package icu.azim.wagrapple.item;

import java.util.List;
import java.util.function.Consumer;

import icu.azim.wagrapple.WAGrappleMod;
import icu.azim.wagrapple.entity.GrappleLineEntity;
import icu.azim.wagrapple.util.Util;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
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
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand)
    {
		if(!WAGrappleMod.GRAPPLE_COMPONENT.get(player).isGrappled()) {

			ItemStack stack = player.getStackInHand(hand);
			int lengthModifier = getLengthEnchantmentMultiplier(stack.getEnchantments());
			double boostModifier = getBoostEnchantmentMultiplier(stack.getEnchantments());
			
			int ihand = player.getMainArm() == Arm.RIGHT ? 1 : -1;
			ihand *= (hand==Hand.MAIN_HAND)?1:-1;
		    Vec3d from = Util.getPlayerShoulder(player, ihand, 1);
		    Vec3d to = player.getCameraPosVec(0).add(player.getRotationVec(0).multiply(WAGrappleMod.maxLength*lengthModifier));
		    
		    BlockHitResult result = player.world.rayTrace(new RayTraceContext(from, to, RayTraceContext.ShapeType.COLLIDER, RayTraceContext.FluidHandling.NONE, player));
		    
			if(result.getType()==Type.BLOCK) {
				player.getItemCooldownManager().set(this, 16);
				if(!world.isClient) {
					EquipmentSlot slot = hand==Hand.MAIN_HAND?EquipmentSlot.MAINHAND:EquipmentSlot.OFFHAND;
					
					stack.damage(1, (LivingEntity)player, (Consumer<LivingEntity>)((e) -> {
			            ((LivingEntity) e).sendEquipmentBreakStatus(slot);
			         }));
					
					GrappleLineEntity entity = new GrappleLineEntity(world, player, player.getPos().distanceTo(result.getPos())+1.5, boostModifier, result);
					world.spawnEntity(entity);
					WAGrappleMod.GRAPPLE_COMPONENT.get(player).setLineId(entity.getEntityId());
					WAGrappleMod.GRAPPLE_COMPONENT.get(player).setGrappled(true);
					WAGrappleMod.GRAPPLE_COMPONENT.get(player).sync();
				}
			}else {
				player.playSound(SoundEvents.ITEM_ARMOR_EQUIP_CHAIN, 1.0F, 0.6F);
			}
			
		}else {
			if(world.isClient) {
				if(player!=MinecraftClient.getInstance().player) {
					return new TypedActionResult<>(ActionResult.PASS, player.getStackInHand(hand));
				}
			}
			
			int id = WAGrappleMod.GRAPPLE_COMPONENT.get(player).getLineId();
			if(id>0) {
				Entity e = world.getEntityById(id);
				if(e!=null) {
					e.remove();
				}
			}
			
			WAGrappleMod.GRAPPLE_COMPONENT.get(player).setLineId(-1);
			WAGrappleMod.GRAPPLE_COMPONENT.get(player).setGrappled(false);
			WAGrappleMod.GRAPPLE_COMPONENT.get(player).sync();

			player.playSound(SoundEvents.ITEM_ARMOR_EQUIP_CHAIN, 1.0F, 0.6F);
		}
        return new TypedActionResult<>(ActionResult.PASS, player.getStackInHand(hand));
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
	
	public int getLengthEnchantmentMultiplier(ListTag listTag) {
		int result = 1;
		CompoundTag ctag = (CompoundTag)listTag.stream().filter(tag -> {
			return ((CompoundTag)tag).getString("id").equalsIgnoreCase(WAGrappleMod.LINE_LENGTH_ENCHANTMENT_ID.toString());
		}).findFirst().orElse(null);
		if(ctag!=null) {
			result = result * ctag.getShort("lvl")*5;
		}
		return result;
	}
	
	public double getBoostEnchantmentMultiplier(ListTag listTag) {
		double result = 1;
		CompoundTag ctag = (CompoundTag)listTag.stream().filter(tag -> {
			return ((CompoundTag)tag).getString("id").equalsIgnoreCase(WAGrappleMod.BOOST_POWER_ENCHANTMENT_ID.toString());
		}).findFirst().orElse(null);
		if(ctag!=null) {
			result = result + ctag.getShort("lvl")*0.5;
		}
		return result;
	}
	
	@Override
	public boolean isEnchantable(ItemStack stack) {
		return true;
	}
	
	@Override
	public int getEnchantability() {
		return 1;
	}
	@Override
	public boolean isDamageable() {
		return true;
	}
}
