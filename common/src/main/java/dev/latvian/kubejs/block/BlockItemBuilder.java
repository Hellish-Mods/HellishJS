package dev.latvian.kubejs.block;

import dev.latvian.kubejs.item.ItemBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;

/**
 * @author LatvianModder
 */
public class BlockItemBuilder extends ItemBuilder {
	public BlockBuilder blockBuilder;
	public BlockItem blockItem;

	public BlockItemBuilder(ResourceLocation id) {
		super(id);
	}

	@Override
	public String getBuilderType() {
		return "block";
	}
}
