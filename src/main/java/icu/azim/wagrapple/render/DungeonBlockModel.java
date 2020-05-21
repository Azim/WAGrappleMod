package icu.azim.wagrapple.render;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;

import icu.azim.wagrapple.WAGrappleMod;
import icu.azim.wagrapple.blocks.DungeonBlock;
import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelProviderException;
import net.fabricmc.fabric.api.client.model.ModelVariantProvider;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext.QuadTransform;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.ModelItemPropertyOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

public class DungeonBlockModel implements UnbakedModel{
	
	private static DungeonBlockModel INSTANCE;
	public static SpriteIdentifier id = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEX, new Identifier("wagrapple","dungeon_block"));
	public static SpriteIdentifier glass = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEX, new Identifier("minecraft","glass"));
	private UnbakedModel original;
	
	public static DungeonBlockModel INSTANCE(UnbakedModel m) {
		if (INSTANCE==null) {
			INSTANCE = new DungeonBlockModel(m);
		}
		return INSTANCE;
	}
	
	public DungeonBlockModel(UnbakedModel m) {
		original = m;
	}
	
	@Override
	public Collection<Identifier> getModelDependencies() {
		return Collections.emptyList();
	}

	@Override
	public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter,
			Set<Pair<String, String>> unresolvedTextureReferences) {
		return ImmutableSet.of(id);
	}

	@Override
	public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter,
			ModelBakeSettings rotationContainer, Identifier modelId) {
		
		return new Baked(original.bake(loader, textureGetter, rotationContainer, modelId), textureGetter.apply(glass));
	}
	

	public static class Baked extends ForwardingBakedModel{
		
		private Sprite glassSprite;
		
		public Baked(BakedModel original, Sprite overlay) {
			glassSprite = overlay;
			this.wrapped = original;//what do i choose here
		}
		@Override
		public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
			if(state.getBlock() instanceof DungeonBlock) {
				QuadTransform retextureTransform = new RetextureTransform(glassSprite);
				
				BakedModel model = MinecraftClient.getInstance().getBlockRenderManager().getModel(state);

				context.fallbackConsumer().accept(this.wrapped);
				//super.emitBlockQuads(blockView, state, pos, randomSupplier, context);
				//emitQuads(blockView, pos, randomSupplier, context, state, model);
				
				//context.pushTransform(retextureTransform);
				//emitQuads(blockView, pos, randomSupplier, context, state, model);
				//context.popTransform();
			}else {
				context.fallbackConsumer().accept(this.wrapped);
			}
		}
		
		public void emitQuads(BlockRenderView blockView, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context, BlockState camoState, BakedModel model) {
			
			if (model instanceof FabricBakedModel)
				super.emitBlockQuads(blockView, camoState, pos, randomSupplier, context);
			else
				context.fallbackConsumer().accept(model);
		}
		
		@Override
		public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {}
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

	public static enum VariantProvider implements ModelVariantProvider
	{
		INSTANCE;
		
		/*
		@Override
		public UnbakedModel loadModelVariant(ModelIdentifier modelId, ModelProviderContext context) throws ModelProviderException
		{
			return modelId.getNamespace().equals(Polar.MOD_ID) && modelId.getPath().equals("stabilised_block")
					? StabilisedBlockModel.INSTANCE
					: null;
		}*/

		@Override
		public UnbakedModel loadModelVariant(ModelIdentifier modelId, ModelProviderContext context)
				throws ModelProviderException {
			
			return modelId.getNamespace().equals(WAGrappleMod.modid) && modelId.getPath().equals("dungeon_block")? DungeonBlockModel.INSTANCE(context.loadModel(modelId)):null;
		}
	}
}
