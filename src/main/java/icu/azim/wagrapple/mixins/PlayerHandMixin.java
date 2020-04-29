package icu.azim.wagrapple.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;

@Mixin(BipedEntityModel.class)
public class PlayerHandMixin<T extends LivingEntity> {
	@Shadow
	public ModelPart rightArm;

	@Inject(method="setAngles",at = @At("TAIL"))
	public void changeHandPosition(T livingEntity, float f, float g, float h, float i, float j) {
		if(livingEntity instanceof PlayerEntity) {
			float k = 1.0F;
			boolean bl = livingEntity.getRoll() > 4;
			if (bl) {
				k = (float)livingEntity.getVelocity().lengthSquared();
				k /= 0.2F;
				k *= k * k;
			}

			if (k < 1.0F) {
				k = 1.0F;
			}
			this.rightArm.pitch = MathHelper.cos(f * 0.6662F + 3.1415927F) * 2.0F * g * 0.5F / k;
			this.rightArm.pitch = this.rightArm.pitch * 0.5F - 3.1415927F;
			this.rightArm.yaw = 0.0F;
		}
	}
}
