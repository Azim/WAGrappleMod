package icu.azim.wagrapple;

import java.util.UUID;

import icu.azim.wagrapple.entity.GrappleLineEntity;
import icu.azim.wagrapple.render.GrappleLineRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;

public class WAGrappleModClient implements ClientModInitializer {


	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.INSTANCE.register(WAGrappleMod.GRAPPLE_LINE, (entityRenderDispatcher, context) -> new GrappleLineRenderer(entityRenderDispatcher));
		ClientSidePacketRegistry.INSTANCE.register(WAGrappleMod.UPDATE_LINE_PACKET_ID, (context, data)->GrappleLineEntity.handleSyncPacket(context, data));
		ClientSidePacketRegistry.INSTANCE.register(WAGrappleMod.UPDATE_LINE_LENGTH_PACKET_ID, (context, data) ->{
			WAGrappleMod.maxLength = data.readDouble();
		});
		ClientSidePacketRegistry.INSTANCE.register(WAGrappleMod.CREATE_LINE_PACKET_ID, (context, packet) -> {

            int entityId = packet.readInt();
            int ownerId = packet.readInt();
            double length = packet.readDouble();
            UUID entityUUID = packet.readUuid();
            BlockHitResult res = packet.readBlockHitResult();
            context.getTaskQueue().execute(() -> {
                Entity e = MinecraftClient.getInstance().world.getEntityById(ownerId);
                if(!(e instanceof PlayerEntity)) {
                	return;
                }
                PlayerEntity player = (PlayerEntity)e;
                GrappleLineEntity toSpawn = new GrappleLineEntity(MinecraftClient.getInstance().world, player, length, res);
                toSpawn.setEntityId(entityId);
                toSpawn.setUuid(entityUUID);
                MinecraftClient.getInstance().world.addEntity(entityId, toSpawn);
            });
        });
		
		System.out.println("client init done");
	}

}
