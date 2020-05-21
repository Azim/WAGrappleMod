package icu.azim.wagrapple.render;

import java.util.Collection;
import java.util.Collections;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;

import icu.azim.wagrapple.blocks.DungeonBlock;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext.QuadTransform;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

public class DungeonBlockModel implements UnbakedModel{

	public static SpriteIdentifier id = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEX, new Identifier("wagrapple","block/east_0_0_0"));
	public static SpriteIdentifier glass = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEX, new Identifier("minecraft","block/glass"));
	private UnbakedModel original;
	
	public DungeonBlockModel(UnbakedModel m) {
		original = m;
	}
	
	@Override
	public Collection<Identifier> getModelDependencies() {
		return original.getModelDependencies();
	}

	@Override
	public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter,
			Set<Pair<String, String>> unresolvedTextureReferences) {
		return original.getTextureDependencies(unbakedModelGetter, unresolvedTextureReferences);
	}

	@Override
	public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter,
			ModelBakeSettings rotationContainer, Identifier modelId) {
		
		return new Baked(original.bake(loader, textureGetter, rotationContainer, modelId), textureGetter.apply(glass));
	}
	

	public static class Baked extends ForwardingBakedModel{
		
		private Sprite glassSprite;
		private QuadTransform retextureTransform;
		
		public Baked(BakedModel original, Sprite overlay) {
			glassSprite = overlay;
			this.wrapped = original;//what do i choose here
			retextureTransform = new RetextureTransform(glassSprite);
		}
		@Override
		public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
			
			if(state.getBlock() instanceof DungeonBlock) {
				//((FabricBakedModel) wrapped).emitBlockQuads(blockView, state, pos, randomSupplier, context);

				//emitQuads(blockView, pos, randomSupplier, context, state, this);
				//context.pushTransform(retextureTransform);
				emitQuads(blockView, pos, randomSupplier, context, state, this);
				//context.popTransform();
				
			}else {
				context.fallbackConsumer().accept(this.wrapped);
			}
		}
		
		public void emitQuads(BlockRenderView blockView, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context, BlockState state, BakedModel model) {
			if(model instanceof Baked) {
				((FabricBakedModel) wrapped).emitBlockQuads(blockView, state, pos, randomSupplier, context);
			}else if (model instanceof FabricBakedModel) {
				((FabricBakedModel) model).emitBlockQuads(blockView, state, pos, randomSupplier, context);
			}else {
				context.fallbackConsumer().accept(model);
			}
		}
		
		private static class RetextureTransform implements QuadTransform
		{
			private final Sprite newTexture;

			private RetextureTransform(Sprite newTexture)
			{
				this.newTexture = newTexture;
			}

			@Override
			public boolean transform(MutableQuadView quadView)
			{
				quadView.spriteBake(0, newTexture, MutableQuadView.BAKE_LOCK_UV)
					.colorIndex(-1);
				return true;
			}
		}
	}
}
