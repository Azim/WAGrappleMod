package icu.azim.wagrapple.render;

import icu.azim.wagrapple.WAGrappleMod;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.Identifier;

public class CustomSprites {

	    public static void init() {
	        ClientSpriteRegistryCallback
	                .event(SpriteAtlasTexture.BLOCK_ATLAS_TEX)
	                .register(CustomSprites::registerSprites);
	    }

	    public static Sprite getBlockSprite(Identifier id) {
	    	if(id.equals(WAGrappleMod.DUNGEON_BLOCK_ID)) {
	    		
	    	}
	    	Sprite sprite = MinecraftClient.getInstance().getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEX).apply(id);
	    	
	    	
	        return MinecraftClient.getInstance().getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEX).apply(id);
	    }

	    private static void registerSprites(SpriteAtlasTexture texture, ClientSpriteRegistryCallback.Registry registry) {
	        registry.register(WAGrappleMod.DUNGEON_BLOCK_ID);
	    }
	
}
