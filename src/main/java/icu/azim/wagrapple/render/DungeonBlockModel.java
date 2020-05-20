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
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
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
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;

public class DungeonBlockModel implements UnbakedModel{
	
	public static final DungeonBlockModel INSTANCE = new DungeonBlockModel();
	public SpriteIdentifier id = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEX, new Identifier("wagrapple","dungeon_block"));
	
	public DungeonBlockModel() {
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
		
		return new Baked(textureGetter);
	}
	
	
	public static class Baked implements FabricBakedModel, BakedModel{
		
		private Function<SpriteIdentifier, Sprite> textureGetter;
		private Sprite glassSprite;
		public static SpriteIdentifier glass = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEX, new Identifier("minecraft","glass"));
		
		public Baked(Function<SpriteIdentifier, Sprite> textureGetter) {
			this.textureGetter = textureGetter;
			glassSprite = textureGetter.apply(glass);
			
		}
		
		@Override
		public List<BakedQuad> getQuads(BlockState state, Direction face, Random random) {
			return Collections.emptyList();
		}
		@Override
		public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
			if(state.getBlock() instanceof DungeonBlock) {
				QuadTransform retextureTransform = new RetextureTransform(textureGetter.apply(glass));
				
				BakedModel model = MinecraftClient.getInstance().getBlockRenderManager().getModel(state);
				
				emitQuads(blockView, pos, randomSupplier, context, state, model);
			
				//context.pushTransform(retextureTransform);
				//emitQuads(blockView, pos, randomSupplier, context, state, model);
				//context.popTransform();
			}
		}
		
		@Override
		public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) { }
		
		public void emitQuads(BlockRenderView blockView, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context, BlockState camoState, BakedModel model) {
			
			
			//if (model instanceof FabricBakedModel)
				//((FabricBakedModel) model).emitBlockQuads(blockView, camoState, pos, randomSupplier, context);
			//else
				context.fallbackConsumer().accept(model);
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
		

		@Override
		public boolean useAmbientOcclusion() {
			return true;
		}

		@Override
		public boolean hasDepth() {
			return true;
		}

		@Override
		public boolean isSideLit() {
			return false;
		}

		@Override
		public boolean isBuiltin() {
			return false;
		}

		//TODO edit
		@Override
		public Sprite getSprite() {
			return glassSprite;
		}

		@Override
		public ModelTransformation getTransformation() {
			return ModelTransformation.NONE;
		}

		@Override
		public ModelItemPropertyOverrideList getItemPropertyOverrides() {
			return ModelItemPropertyOverrideList.EMPTY;
		}

		@Override
		public boolean isVanillaAdapter() {
			return false;
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
			// TODO Auto-generated method stub
			return modelId.getNamespace().equals(WAGrappleMod.modid) && modelId.getPath().equals("dungeon_block")? DungeonBlockModel.INSTANCE:null;
		}
	}
}
