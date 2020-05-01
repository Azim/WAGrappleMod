package icu.azim.wagrapple.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import icu.azim.wagrapple.WAGrappleMod;
import icu.azim.wagrapple.components.GrappleComponent;
import icu.azim.wagrapple.entity.GrappleLineEntity;

import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;

@Mixin(PlayerEntityModel.class)
public class PlayerHandMixin<T extends LivingEntity> {
	@Shadow
	public ModelPart leftSleeve;
	@Shadow
	public ModelPart rightSleeve;
	
	private GrappleComponent gcomponent;

	@SuppressWarnings("unchecked")
	@Inject(method="setAngles",at = @At("TAIL"))
	public void changeHandPosition(T livingEntity, float f, float g, float h, float i, float j, CallbackInfo ci) {
		if(!(livingEntity instanceof PlayerEntity)) {
			return;
		}
		PlayerEntity player = (PlayerEntity) livingEntity;
		if(gcomponent==null) {
			gcomponent = WAGrappleMod.GRAPPLE_COMPONENT.get(player);
		}
		//System.out.println(gcomponent);
		if(!gcomponent.isGrappled()) {
			return;
		}
		Entity e =player.world.getEntityById(gcomponent.getLineId());
		if(e==null||!(e instanceof GrappleLineEntity)) {
			gcomponent = WAGrappleMod.GRAPPLE_COMPONENT.get(player);
			return;
		}
		GrappleLineEntity line = (GrappleLineEntity)e;
		
		int hand = player.getMainArm() == Arm.RIGHT ? 1 : -1; //get hand
		ItemStack itemStack = player.getMainHandStack();
		if (itemStack.getItem() != WAGrappleMod.GRAPPLE_ITEM) {
			hand = -hand;
			itemStack = player.getOffHandStack();
			if(itemStack.getItem()!=WAGrappleMod.GRAPPLE_ITEM) { //neither of hands have an item
				System.out.println("none of the hands have the line");
				return;
			}
		}
		ModelPart arm = hand==1?((BipedEntityModel<T>)(Object) this).rightArm:((BipedEntityModel<T>)(Object) this).leftArm;
		ModelPart sleeve = hand==1?rightSleeve:leftSleeve;
		sleeve.pitch = arm.pitch = -(line.getLinePitch()+(float)Math.PI/2);
		sleeve.yaw = arm.yaw = -line.getLineYaw() + (float) Math.PI -((player.bodyYaw%360)*(float)Math.PI/180);
	}
}
