package icu.azim.wagrapple;

import icu.azim.wagrapple.entity.GrappleLineEntity;
import icu.azim.wagrapple.render.GrappleLineRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;

public class WAGrappleModClient implements ClientModInitializer {


	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.INSTANCE.register(WAGrappleMod.GRAPPLE_LINE, (entityRenderDispatcher, context) -> new GrappleLineRenderer(entityRenderDispatcher));
		ClientSidePacketRegistry.INSTANCE.register(WAGrappleMod.UPDATE_LINE_PACKED_ID, (context, data)->GrappleLineEntity.handleSyncPacket(context, data));
		ClientSidePacketRegistry.INSTANCE.register(WAGrappleMod.UPDATE_LINE_LENGTH_PACKET_ID, (context, data) ->{
			WAGrappleMod.maxLength = data.readDouble();
		});
		
		
		System.out.println("client init done");
	}

}
