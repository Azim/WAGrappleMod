package icu.azim.wagrapple.mixins;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RayTraceContext;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import icu.azim.wagrapple.WAGrappleMod;
import icu.azim.wagrapple.entity.GrappleLineEntity;

@Mixin(ClientPlayNetworkHandler.class)
public class MixinClientPlayNetworkHandler {

    @Shadow private ClientWorld world;

    @SuppressWarnings("rawtypes")
	@Inject(method = "onEntitySpawn", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/network/packet/s2c/play/EntitySpawnS2CPacket;getEntityTypeId()Lnet/minecraft/entity/EntityType;"), cancellable = true, locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void spawnGrappleLine(EntitySpawnS2CPacket packet, CallbackInfo ci, double x, double y, double z, EntityType type) {
        if (type == WAGrappleMod.GRAPPLE_LINE) {
            Entity owner = world.getEntityById(packet.getEntityData());

            if (owner instanceof PlayerEntity) {
    			Vec3d vec3d = owner.getCameraPosVec(0);
    		    Vec3d vec3d2 = owner.getRotationVec(0);
    		    Vec3d vec3d3 = vec3d.add(vec3d2.x * 16, vec3d2.y * 16, vec3d2.z * 16);
    		    BlockHitResult result = owner.world.rayTrace(new RayTraceContext(vec3d, vec3d3, RayTraceContext.ShapeType.COLLIDER, RayTraceContext.FluidHandling.NONE, owner));
    		    
    		    Vec3d pos = new Vec3d(x,y,z);
            	GrappleLineEntity toSpawn = new GrappleLineEntity(world, (PlayerEntity) owner, owner.getPos().distanceTo(pos)+1, pos, result.getBlockPos());
                int id = packet.getId();
                toSpawn.updateTrackedPosition(x, y, z);
                toSpawn.pitch = (float)(packet.getPitch() * 360) / 256.0F;
                toSpawn.yaw = (float)(packet.getYaw() * 360) / 256.0F;
                toSpawn.setEntityId(id);
                toSpawn.setUuid(packet.getUuid());
                this.world.addEntity(id, toSpawn);
                ci.cancel();
            }
        }
    }
}