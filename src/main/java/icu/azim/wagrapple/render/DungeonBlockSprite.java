package icu.azim.wagrapple.render;

import net.fabricmc.fabric.impl.client.texture.FabricSprite;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.SpriteAtlasTexture;

public class DungeonBlockSprite extends FabricSprite{

	public DungeonBlockSprite(SpriteAtlasTexture spriteAtlasTexture, Info info, int mipmap, int u, int v, int x, int y,
			NativeImage nativeImage) {
		super(spriteAtlasTexture, info, mipmap, u, v, x, y, nativeImage);
	}
	
	
}
