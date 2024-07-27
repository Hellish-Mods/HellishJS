package dev.latvian.kubejs.core;

import dev.latvian.kubejs.item.ItemBuilder;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import org.jetbrains.annotations.Nullable;

/**
 * @author LatvianModder
 */
public interface ItemKJS extends KjsSelf<Item> {
	@Nullable
	ItemBuilder getItemBuilderKJS();

	void setItemBuilderKJS(ItemBuilder b);

	void setMaxStackSizeKJS(int i);

	void setMaxDamageKJS(int i);

	void setCraftingRemainderKJS(Item i);

	void setFireResistantKJS(boolean b);

	void setRarityKJS(Rarity r);

	void setBurnTimeKJS(int i);

	void setFoodPropertiesKJS(FoodProperties properties);
}
