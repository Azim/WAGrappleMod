package icu.azim.wagrapple;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import javax.imageio.ImageIO;

import icu.azim.wagrapple.entity.GrappleLineEntity;
import icu.azim.wagrapple.render.DungeonBlockModel;
import icu.azim.wagrapple.render.GrappleLineRenderer;
import icu.azim.wagrapple.util.ModelWrapperHandler;
import net.devtech.arrp.api.RRPCallback;
import net.devtech.arrp.api.RuntimeResourcePack;
import net.devtech.arrp.json.blockstate.JState;
import net.devtech.arrp.json.blockstate.JVariant;
import net.devtech.arrp.json.models.JModel;
import net.devtech.arrp.json.models.JTextures;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelProviderException;
import net.fabricmc.fabric.api.client.model.ModelVariantProvider;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.profiler.Profiler;

@SuppressWarnings("unused")
public class WAGrappleModClient implements ClientModInitializer {

	private static KeyBinding ascend;
	private static KeyBinding descend;
	private static KeyBinding boost;


	public static final RuntimeResourcePack RESOURCE_PACK = RuntimeResourcePack.create(WAGrappleMod.modid+":rpack");

	public static KeyBinding getAscend() {
		if(ascend==null) ascend =  MinecraftClient.getInstance().options.keySneak;
		return ascend;
	}
	public static KeyBinding getDescend() {
		if(descend==null) descend = MinecraftClient.getInstance().options.keySprint;
		return descend;
	}
	public static KeyBinding getBoost() {
		if(boost==null) boost = MinecraftClient.getInstance().options.keyJump;
		return boost;
	}
	public static KeyBinding getDebug() {
		if(debug==null) debug = MinecraftClient.getInstance().options.keySwapHands;
		return debug;
	}
	private static KeyBinding debug;

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
			double boost = packet.readDouble();
			UUID entityUUID = packet.readUuid();
			BlockHitResult res = packet.readBlockHitResult();
			context.getTaskQueue().execute(() -> {
				Entity e = MinecraftClient.getInstance().world.getEntityById(ownerId);
				if(!(e instanceof PlayerEntity)) {
					return;
				}
				PlayerEntity player = (PlayerEntity)e;
				GrappleLineEntity toSpawn = new GrappleLineEntity(MinecraftClient.getInstance().world, player, length, boost, res);
				toSpawn.setEntityId(entityId);
				toSpawn.setUuid(entityUUID);
				MinecraftClient.getInstance().world.addEntity(entityId, toSpawn);
			});
		});
		
		/*
		ModelLoadingRegistry.INSTANCE.registerVariantProvider(manager -> {
			return new ModelVariantProvider() {
				@Override
				public UnbakedModel loadModelVariant(ModelIdentifier modelId, ModelProviderContext context) throws ModelProviderException {
					
					return modelId.getNamespace().equals(WAGrappleMod.modid) && modelId.getPath().equals("dungeon_block")
							?
									DungeonBlockModel.INSTANCE(context.loadModel(modelId))
									:null;
				}
			};
		});
		*/
		ModelWrapperHandler.INSTANCE.register(manager->(state,model)->{
			return new DungeonBlockModel(model);
		});
		
		
		RRPCallback.EVENT.register(a -> {
			a.add(0, RESOURCE_PACK);
		});
		
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new IdentifiableResourceReloadListener(){
			private final Identifier identifier = new Identifier(WAGrappleMod.modid, "rrp");

			@Override
			public CompletableFuture<Void> reload(Synchronizer synchronizer, ResourceManager manager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor){

				return CompletableFuture.supplyAsync(()->{
					try {
						generateDungeonBlockPattern(manager);
						System.out.println("generated dungeon block");
					} catch (IOException e) {
						e.printStackTrace();
					}
					return null;
				}, applyExecutor).thenCompose((v)->synchronizer.whenPrepared(null));
			}
			@Override
			public Identifier getFabricId(){
				return identifier;
			}
		});
		
		try {
			generateDungeonBlockPattern(null);
		} catch (IOException e) {
			e.printStackTrace();
		}
		//CustomSprites.init();
		
		
		System.out.println("client init done");
	}
	
	
	public InputStream getImageResource(ResourceManager manager, String resource) throws IOException {
		if(manager==null) {
			resource = "assets/wagrapple/"+resource;
			return WAGrappleMod.class.getClassLoader().getResourceAsStream(resource);
		}else {
			return manager.getResource(new Identifier("wagrapple", resource)).getInputStream();
		}
	}
	
	public void generateDungeonBlockPattern(ResourceManager manager) throws IOException {
		for(int x = 0; x<6; x++) {
			BufferedImage sheet = ImageIO.read(getImageResource(manager, "textures/block/test_x"+x+".png"));
			for(int iy = 0; iy<6; iy++) {
				for(int ix = 0; ix<6; ix++) {
					//north.add();
					RESOURCE_PACK.addTexture(new Identifier("wagrapple","block/north_"+(5-x)+"_"+(5-iy)+"_"+(5-ix)), sheet.getSubimage(ix*16, iy*16, 16, 16));
				}
			}
		}
		for(int y = 0; y<6; y++) {
			BufferedImage sheet = ImageIO.read(getImageResource(manager, "textures/block/test_y"+y+".png"));
			for(int iy = 0; iy<6; iy++) {
				for(int ix = 0; ix<6; ix++) {
					RESOURCE_PACK.addTexture(new Identifier("wagrapple","block/up_"+ix+"_"+y+"_"+iy), sheet.getSubimage(ix*16, iy*16, 16, 16));
				}
			}
		}
		for(int z = 0; z<6; z++) {
			BufferedImage sheet = ImageIO.read(getImageResource(manager, "textures/block/test_z"+z+".png"));
			for(int iy = 0; iy<6; iy++) {
				for(int ix = 0; ix<6; ix++) {
					RESOURCE_PACK.addTexture(new Identifier("wagrapple","block/east_"+(5-iy)+"_"+(5-ix)+"_"+z), sheet.getSubimage(ix*16, iy*16, 16, 16));
				}
			}
		}
		for(int x = 0; x<6;x++) {
			for(int y = 0; y<6; y++) {
				for(int z = 0; z<6; z++) {
					JModel model = JModel.model("wagrapple:block/dungeon_block");
					JTextures textures = JModel.textures()
							.var("up", "wagrapple:block/up_"+x+"_"+y+"_"+z)
							.var("down", "wagrapple:block/up_"+x+"_"+(y+5)%6+"_"+z)
							.var("south", "wagrapple:block/north_"+z+"_"+y+"_"+x)
							.var("north", "wagrapple:block/north_"+(5-z)+"_"+y+"_"+x)
							.var("west", "wagrapple:block/east_"+y+"_"+z+"_"+(x+5)%6)
							.var("east", "wagrapple:block/east_"+y+"_"+z+"_"+x)
							.var("particle", "#up")
							;
					model.textures(textures);
					RESOURCE_PACK.addModel(model, new Identifier("wagrapple","block/dungeon_block_"+(x+6*y+6*6*z)));
				}
			}
		}
		JState state = JState.state();
		JVariant variant = JState.variant();
		for(int i = 0; i < 216; i++) {
			variant.put("dungeon", i, JState.model("wagrapple:block/dungeon_block_"+i));
		}
		state.add(variant);

		RESOURCE_PACK.addBlockState(state, new Identifier("wagrapple","dungeon_block"));
	}


}
