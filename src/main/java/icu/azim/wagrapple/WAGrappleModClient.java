package icu.azim.wagrapple;

import icu.azim.wagrapple.render.GrappleLineRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;

public class WAGrappleModClient implements ClientModInitializer {


	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.INSTANCE.register(WAGrappleMod.GRAPPLE_LINE, (entityRenderDispatcher, context) -> new GrappleLineRenderer(entityRenderDispatcher));
		System.out.println("client init done");
	}

}
