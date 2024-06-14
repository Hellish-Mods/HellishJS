package dev.latvian.kubejs.item;

import dev.latvian.kubejs.event.EventJS;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * @author LatvianModder
 */
@Deprecated
public class OldItemTooltipEventJS extends EventJS {
	private final ItemStack stack;
	private final List<Component> lines;
	private final boolean advanced;

	public OldItemTooltipEventJS(ItemStack stack, List<Component> lines, boolean a) {
		this.stack = stack;
		this.lines = lines;
		this.advanced = a;
	}

	public ItemStackJS getItem() {
		return ItemStackJS.of(stack);
	}

	public boolean isAdvanced() {
		return advanced;
	}

	public void add(Component text) {
		lines.add(text);
	}
}