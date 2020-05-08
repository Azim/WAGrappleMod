package icu.azim.wagrapple;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import javax.imageio.ImageIO;

import icu.azim.wagrapple.blocks.DungeonBlock;
import icu.azim.wagrapple.components.GrappledPlayerComponent;
import icu.azim.wagrapple.entity.GrappleLineEntity;
import icu.azim.wagrapple.item.GrappleItem;
import icu.azim.wagrapple.item.enchantments.BoostPowerEnchantment;
import icu.azim.wagrapple.item.enchantments.RopeLengthEnchantment;
import nerdhub.cardinal.components.api.ComponentRegistry;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.event.EntityComponentCallback;
import nerdhub.cardinal.components.api.util.EntityComponents;
import nerdhub.cardinal.components.api.util.RespawnCopyStrategy;
import net.devtech.arrp.api.RRPCallback;
import net.devtech.arrp.api.RuntimeResourcePack;
import net.devtech.arrp.json.blockstate.JState;
import net.devtech.arrp.json.blockstate.JVariant;
import net.devtech.arrp.json.models.JModel;
import net.devtech.arrp.json.models.JTextures;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.block.FabricBlockSettings;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.entity.FabricEntityTypeBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EntityCategory;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;


public class WAGrappleMod implements ModInitializer{
	public static final String modid = "wagrapple";
	public static double maxLength = 24;
	
	//TODO sort all that stuff so it looks more presentable
	public static EntityType<GrappleLineEntity> GRAPPLE_LINE;
	
	
	public static ItemGroup ITEM_GROUP;
	
	public static GrappleItem GRAPPLE_ITEM;
	
	public static Enchantment LINE_LENGTH_ENCHANTMENT;
	public static Enchantment BOOST_POWER_ENCHANTMENT;
	
	public static ComponentType<GrappledPlayerComponent> GRAPPLE_COMPONENT;
	
	public static Block DUNGEON_BLOCK;
	
	public static Identifier DETACH_LINE_PACKET_ID = new Identifier(modid, "detach_line");
	public static Identifier UPDATE_LINE_PACKET_ID = new Identifier(modid, "update_line");
	public static Identifier CREATE_LINE_PACKET_ID = new Identifier(modid, "create_line");
	public static Identifier UPDATE_LINE_LENGTH_PACKET_ID = new Identifier(modid, "update_line_length");
	
	public static Identifier LINE_LENGTH_ENCHANTMENT_ID = new Identifier(modid, "rope_length");
	public static Identifier BOOST_POWER_ENCHANTMENT_ID = new Identifier(modid, "boost_power");
	
	public static final RuntimeResourcePack RESOURCE_PACK = RuntimeResourcePack.create(modid+":rpack");
	
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
		GRAPPLE_ITEM = new GrappleItem(new Item.Settings().group(ITEM_GROUP).maxCount(1).rarity(Rarity.EPIC).maxDamage(690));
		DUNGEON_BLOCK = new DungeonBlock(FabricBlockSettings.of(Material.METAL).build());
		
		
		LINE_LENGTH_ENCHANTMENT = Registry.register(
		        Registry.ENCHANTMENT,
			LINE_LENGTH_ENCHANTMENT_ID,
			new RopeLengthEnchantment(
			    Enchantment.Weight.RARE,
			    EnchantmentTarget.ALL,
			    new EquipmentSlot[] {
				EquipmentSlot.MAINHAND,
				EquipmentSlot.OFFHAND
			    }
			));
		
		BOOST_POWER_ENCHANTMENT = Registry.register(
		        Registry.ENCHANTMENT,
			BOOST_POWER_ENCHANTMENT_ID,
			new BoostPowerEnchantment(
			    Enchantment.Weight.RARE,
			    EnchantmentTarget.ALL,
			    new EquipmentSlot[] {
				EquipmentSlot.MAINHAND,
				EquipmentSlot.OFFHAND
			    }
			));
		
		GRAPPLE_COMPONENT = ComponentRegistry.INSTANCE.registerIfAbsent(
				new Identifier(modid,"grapple_component"),
				GrappledPlayerComponent.class)
				.attach(
						EntityComponentCallback.event(PlayerEntity.class),
						player->new GrappledPlayerComponent(player));
		
		
		Registry.register(Registry.ITEM, new Identifier(modid, "grapple"), GRAPPLE_ITEM);
		
		Registry.register(Registry.BLOCK, new Identifier(modid, "dungeon_block"), DUNGEON_BLOCK);
		Registry.register(Registry.ITEM, new Identifier(modid, "dungeon_block"), new BlockItem(DUNGEON_BLOCK, new Item.Settings().group(ITEM_GROUP)));
		
		EntityComponents.setRespawnCopyStrategy(GRAPPLE_COMPONENT, RespawnCopyStrategy.NEVER_COPY);
		
		//generateDungeonBlockPattern();
		try {
			generateDungeonTest();
		} catch (IOException e) {
			e.printStackTrace();
		}
		RRPCallback.EVENT.register(a -> a.add(0, RESOURCE_PACK));
		//RESOURCE_PACK.dump();
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
				config.setProperty("maxLength", "24.0");
				FileOutputStream out = new FileOutputStream(f);
				config.store(out, null);
				out.close();
			}else{
				WAGrappleMod.maxLength = Double.valueOf(config.getProperty("maxLength"));
			}
		}catch(Exception ignored) {
			WAGrappleMod.maxLength = 24;
			ignored.printStackTrace();
		}
	}
	
	public void generateDungeonBlockPattern() {
		List<String> stextures = new ArrayList<String>();
		for(int i = 1; i<=8;i++) {
			stextures.add("wagrapple:block/dungeon"+i);
		}
		Random r = new Random(System.currentTimeMillis());
		
		for(int i = 0; i < 16; i++) {
			JModel model = JModel.model("minecraft:block/cube");
			JTextures textures = JModel.textures()
					.var("up", stextures.get(r.nextInt(8)))
					.var("down", stextures.get(r.nextInt(8)))
					.var("north", stextures.get(r.nextInt(8)))
					.var("south", stextures.get(r.nextInt(8)))
					.var("west", stextures.get(r.nextInt(8)))
					.var("east", stextures.get(r.nextInt(8)));
			model.textures(textures);
			
			RESOURCE_PACK.addModel(model, new Identifier("wagrapple","block/dungeon_block_"+i));
		}
		JState state = JState.state();
		JVariant variant = JState.variant();
		for(int i = 0; i < 16; i++) {
			variant.put("dungeon", i, JState.model("wagrapple:block/dungeon_block_"+i));
		}
		state.add(variant);
		
		RESOURCE_PACK.addBlockState(state, new Identifier("wagrapple","dungeon_block"));
	}
	
	//TODO add resource pack support - load this after resource packs are created
	public void generateDungeonTest() throws IOException {
		for(int x = 0; x<6; x++) {
			//System.out.println(MinecraftClient.getInstance());
			//System.out.println(MinecraftClient.getInstance().getResourceManager());
			//System.out.println(MinecraftClient.getInstance().getResourceManager().getResource(new Identifier("wagrapple","textures/block/test_x"+x+".png")));
			
			BufferedImage sheet = ImageIO.read(WAGrappleMod.class.getClassLoader().getResourceAsStream("assets/wagrapple/textures/block/test_x"+x+".png"));
			for(int iy = 0; iy<6; iy++) {
				for(int ix = 0; ix<6; ix++) {
					//north.add();
					RESOURCE_PACK.addTexture(new Identifier("wagrapple","block/north_"+x+"_"+iy+"_"+ix), sheet.getSubimage(ix*16, iy*16, 16, 16));
				}
			}
		}
		for(int y = 0; y<6; y++) {
			BufferedImage sheet = ImageIO.read(WAGrappleMod.class.getClassLoader().getResourceAsStream("assets/wagrapple/textures/block/test_y"+y+".png"));
			for(int iy = 0; iy<6; iy++) {
				for(int ix = 0; ix<6; ix++) {
					RESOURCE_PACK.addTexture(new Identifier("wagrapple","block/up_"+ix+"_"+y+"_"+iy), sheet.getSubimage(ix*16, iy*16, 16, 16));
				}
			}
		}
		for(int z = 0; z<6; z++) {
			BufferedImage sheet = ImageIO.read(WAGrappleMod.class.getClassLoader().getResourceAsStream("assets/wagrapple/textures/block/test_z"+z+".png"));
			for(int iy = 0; iy<6; iy++) {
				for(int ix = 0; ix<6; ix++) {
					RESOURCE_PACK.addTexture(new Identifier("wagrapple","block/east_"+ix+"_"+iy+"_"+z), sheet.getSubimage(ix*16, iy*16, 16, 16));
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
							.var("south", "wagrapple:block/north_"+z+"_"+x+"_"+y)
							.var("north", "wagrapple:block/north_"+(z+5)%6+"_"+x+"_"+y)
							.var("west", "wagrapple:block/east_"+y+"_"+z+"_"+(x+5)%6)
							.var("east", "wagrapple:block/east_"+y+"_"+z+"_"+x);
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





