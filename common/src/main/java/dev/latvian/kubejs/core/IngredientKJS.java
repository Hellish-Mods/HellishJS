package dev.latvian.kubejs.core;

import dev.latvian.kubejs.item.ingredient.IngredientJS;
import net.minecraft.world.item.ItemStack;

public interface IngredientKJS extends AsKJS<IngredientJS> {
	@Override
	default IngredientJS asKJS() {
		return IngredientJS.of(this);
	}

	ItemStack[] getItemsKJS();
}
