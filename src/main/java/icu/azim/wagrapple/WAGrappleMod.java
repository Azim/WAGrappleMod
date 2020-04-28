package icu.azim.wagrapple;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import icu.azim.wagrapple.components.GrappleComponent;
import icu.azim.wagrapple.entity.GrappleLineEntity;
import icu.azim.wagrapple.item.GrappleItem;
import nerdhub.cardinal.components.api.ComponentRegistry;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.event.EntityComponentCallback;
import nerdhub.cardinal.components.api.util.EntityComponents;
import nerdhub.cardinal.components.api.util.RespawnCopyStrategy;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.entity.FabricEntityTypeBuilder;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCategory;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;

public class WAGrappleMod implements ModInitializer{
	public static final String modid = "wagrapple";
	public static int maxLength = 24;
	
	//TODO sort all that stuff so it looks more presentable
	public static EntityType<GrappleLineEntity> GRAPPLE_LINE;
	
	
	public static ItemGroup ITEM_GROUP;
	
	public static GrappleItem GRAPPLE_ITEM;
	
	public static ComponentType<GrappleComponent> GRAPPLE_COMPONENT;
	
	
	public static Identifier DETACH_LINE_PACKET_ID = new Identifier(modid, "detach_line");
	
	
	
	
	@Override
	public void onInitialize() {
		generateDefaultConfig();
		
		GRAPPLE_LINE = Registry.register(
				Registry.ENTITY_TYPE,
				new Identifier(modid, "grapple_line"),
				FabricEntityTypeBuilder.create(
						EntityCategory.MISC,
						(EntityType.EntityFactory<GrappleLineEntity>) GrappleLineEntity::new)
				.size(EntityDimensions.fixed(0.2F, 0.2F))
				.build());
		ITEM_GROUP = FabricItemGroupBuilder.build(new Identifier(modid, "general"), () -> new ItemStack(WAGrappleMod.GRAPPLE_ITEM));
		GRAPPLE_ITEM =  new GrappleItem(new Item.Settings().group(ITEM_GROUP).maxCount(1).rarity(Rarity.EPIC));
		GRAPPLE_COMPONENT = ComponentRegistry.INSTANCE.registerIfAbsent(
				new Identifier(modid,"grapple_component"),
				GrappleComponent.class)
				.attach(
						EntityComponentCallback.event(PlayerEntity.class),
						player->new GrappleComponent(player));
		
		
		Registry.register(Registry.ITEM, new Identifier(modid, "grapple"), GRAPPLE_ITEM);
		EntityComponents.setRespawnCopyStrategy(GRAPPLE_COMPONENT, RespawnCopyStrategy.NEVER_COPY);
		ServerSidePacketRegistry.INSTANCE.register(DETACH_LINE_PACKET_ID, (packetContext, attachedData) -> {
            boolean detach = attachedData.readBoolean();
            PlayerEntity player = packetContext.getPlayer();
            packetContext.getTaskQueue().execute(() -> {
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
		System.out.println("init general");
	}

	public void generateDefaultConfig() {
		String path = FabricLoader.getInstance().getConfigDirectory().getPath()+File.separator+modid;
		Properties config = new Properties();
		try {
			File folder = new File(path);
			folder.mkdirs();
			File f = new File(path+File.separator+"wagrapple.properties");
			if(!f.exists()) {
				f.createNewFile();
			}
			FileInputStream in = new FileInputStream(f);
			config.load(in);
			in.close();
			if(config.getProperty("maxLength")==null) {
				WAGrappleMod.maxLength = 24;
				config.setProperty("maxLength", "24");
				FileOutputStream out = new FileOutputStream(f);
				config.store(out, null);
				out.close();
			}else{
				WAGrappleMod.maxLength = Integer.valueOf(config.getProperty("maxLength"));
			}
		}catch(Exception ignored) {
			WAGrappleMod.maxLength = 24;
			ignored.printStackTrace();
		}
	}
	
}





