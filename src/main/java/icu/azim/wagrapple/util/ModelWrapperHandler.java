package icu.azim.wagrapple.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.resource.ResourceManager;

public class ModelWrapperHandler {
	public static final ModelWrapperHandler INSTANCE = new ModelWrapperHandler();
	
	private List<Function<ResourceManager,BiFunction<BlockState, UnbakedModel,UnbakedModel>>> handlers = new ArrayList<>();
	
	public void register( Function<ResourceManager, BiFunction<BlockState, UnbakedModel,UnbakedModel>> f) {
		handlers.add(f);
	}
	
	
	public BiFunction<BlockState, UnbakedModel, UnbakedModel> prepare(ResourceManager manager){
		
		List<BiFunction<BlockState, UnbakedModel, UnbakedModel>> list = new ArrayList<>();
		for(Function<ResourceManager, BiFunction<BlockState, UnbakedModel, UnbakedModel>> handler:handlers) {
			 list.add(handler.apply(manager));
		}
		return (blockstate, model)->{
			for(BiFunction<BlockState, UnbakedModel, UnbakedModel> item:list) {
				model = item.apply(blockstate,model);
			}
			return model;
		};
	}
}
