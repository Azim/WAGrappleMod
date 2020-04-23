package icu.azim.wagrapple;

import icu.azim.wagrapple.components.GrappleComponent;
import icu.azim.wagrapple.components.IGrappleComponent;
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
	
	public static final ComponentType<IGrappleComponent> GRAPPLE_COMPONENT = 
	        ComponentRegistry.INSTANCE.registerIfAbsent(new Identifier(modid,"grapple_component"), IGrappleComponent.class)
	        .attach(EntityComponentCallback.event(PlayerEntity.class), player->new GrappleComponent());
	
	
	@Override
	public void onInitialize() {
		Registry.register(Registry.ITEM, new Identifier(modid, "grapple"), GRAPPLE_ITEM);
		EntityComponents.setRespawnCopyStrategy(GRAPPLE_COMPONENT, RespawnCopyStrategy.NEVER_COPY);
	}

	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.INSTANCE.register(GRAPPLE_LINE, (entityRenderDispatcher, context) -> new GrappleLineRenderer(entityRenderDispatcher));
		System.out.println("init client");
	}
	
}





