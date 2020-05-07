package icu.azim.wagrapple.render;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import com.mojang.datafixers.util.Pair;

import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.ModelItemPropertyOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;

public class DungeonBlockModel implements UnbakedModel{
	
	public DungeonBlockModel() {
	}
	
	@Override
	public Collection<Identifier> getModelDependencies() {
		return Collections.emptyList();
	}

	@Override
	public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter,
			Set<Pair<String, String>> unresolvedTextureReferences) {
		return Collections.emptyList();
	}

	@Override
	public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter,
			ModelBakeSettings rotationContainer, Identifier modelId) {
		
		return null;//umodel.bake(loader, textureGetter, rotationContainer, modelId);
	}
	
	
	public static class Baked implements FabricBakedModel, BakedModel{
		private FabricBakedModel model;
		private BakedModel bmodel;
		public Baked(BakedModel model) {
			this.model = (FabricBakedModel)model;
			this.bmodel =  model;
		}
		
		@Override
		public List<BakedQuad> getQuads(BlockState state, Direction face, Random random) {
			return bmodel.getQuads(state, face, random);
		}

		@Override
		public boolean useAmbientOcclusion() {
			return bmodel.useAmbientOcclusion();
		}

		@Override
		public boolean hasDepth() {
			return bmodel.hasDepth();
		}

		@Override
		public boolean isSideLit() {
			return bmodel.isSideLit();
		}

		@Override
		public boolean isBuiltin() {
			return bmodel.isBuiltin();
		}

		@Override
		public Sprite getSprite() {
			return bmodel.getSprite();
		}

		@Override
		public ModelTransformation getTransformation() {
			return bmodel.getTransformation();
		}

		@Override
		public ModelItemPropertyOverrideList getItemPropertyOverrides() {
			return bmodel.getItemPropertyOverrides();
		}

		@Override
		public boolean isVanillaAdapter() {
			return false;
		}

		@Override
		public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos,
				Supplier<Random> randomSupplier, RenderContext context) {
			model.emitBlockQuads(blockView, state, pos, randomSupplier, context);
			
		}

		@Override
		public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
			model.emitItemQuads(stack, randomSupplier, context);
		}
		
	}
}
