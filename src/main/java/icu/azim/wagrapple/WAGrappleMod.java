package icu.azim.wagrapple;

import icu.azim.wagrapple.components.GrappleComponent;
import icu.azim.wagrapple.entity.GrappleLineEntity;
import icu.azim.wagrapple.item.GrappleItem;
import icu.azim.wagrapple.render.GrappleLineRenderer;

import nerdhub.cardinal.components.api.ComponentRegistry;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.event.EntityComponentCallback;
import nerdhub.cardinal.components.api.util.EntityComponents;
import nerdhub.cardinal.components.api.util.RespawnCopyStrategy;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.entity.FabricEntityTypeBuilder;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCategory;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class WAGrappleMod implements ModInitializer,ClientModInitializer {
	public static final String modid = "wagrapple";
	
	//TODO sort all that stuff so it looks more presentable
	public static final EntityType<GrappleLineEntity> GRAPPLE_LINE =
		    Registry.register(
		        Registry.ENTITY_TYPE,
		        new Identifier(modid, "grapple_line"),
		        FabricEntityTypeBuilder.create(
		        		EntityCategory.MISC,
		        		(EntityType.EntityFactory<GrappleLineEntity>) GrappleLineEntity::new)
		        .size(EntityDimensions.fixed(0.2F, 0.2F))
		        .build()
		    );
	
	
	public static final ItemGroup ITEM_GROUP = FabricItemGroupBuilder.build(
			new Identifier(modid, "general"),
			() -> new ItemStack(Items.LEAD));
	
	
	public static final GrappleItem GRAPPLE_ITEM = new GrappleItem(new Item.Settings().group(ITEM_GROUP));
	
	public static final ComponentType<GrappleComponent> GRAPPLE_COMPONENT = 
	        ComponentRegistry.INSTANCE.registerIfAbsent(new Identifier(modid,"grapple_component"), GrappleComponent.class)
	        .attach(EntityComponentCallback.event(PlayerEntity.class), player->new GrappleComponent(player));
	
	
	public static final Identifier DETACH_LINE_PACKET_ID = new Identifier(modid, "detach_line");
	
	@Override
	public void onInitialize() {
		Registry.register(Registry.ITEM, new Identifier(modid, "grapple"), GRAPPLE_ITEM);
		EntityComponents.setRespawnCopyStrategy(GRAPPLE_COMPONENT, RespawnCopyStrategy.NEVER_COPY);
		//TODO separate this logic into it's own class
		ServerSidePacketRegistry.INSTANCE.register(DETACH_LINE_PACKET_ID, (packetContext, attachedData) -> {
            // Get the BlockPos we put earlier in the IO thread
            boolean detach = attachedData.readBoolean();
            PlayerEntity player = packetContext.getPlayer();
            packetContext.getTaskQueue().execute(() -> {
                // Execute on the main thread
            	if(WAGrappleMod.GRAPPLE_COMPONENT.get(player).isGrappled() && detach) {
            		int id = WAGrappleMod.GRAPPLE_COMPONENT.get(player).getLineId();
            		if(id>0) {
        				Entity e = player.world.getEntityById(id);
        				if(e!=null) {
        					e.remove();
        				}
            		}
            		WAGrappleMod.GRAPPLE_COMPONENT.get(player).setLineId(-1);
        			WAGrappleMod.GRAPPLE_COMPONENT.get(player).setGrappled(!detach);
        			WAGrappleMod.GRAPPLE_COMPONENT.get(player).sync();
            	}
 
            });
        });
		
		
	}

	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.INSTANCE.register(GRAPPLE_LINE, (entityRenderDispatcher, context) -> new GrappleLineRenderer(entityRenderDispatcher));
		System.out.println("init client");
	}
	
}





