package dev.latvian.kubejs.item.events;

import dev.latvian.kubejs.entity.EntityJS;
import dev.latvian.kubejs.item.ItemStackJS;
import dev.latvian.kubejs.player.PlayerEventJS;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * @author LatvianModder
 */
public class ItemSmeltedEventJS extends PlayerEventJS {
	private final Player player;
	private final ItemStack smelted;

	public ItemSmeltedEventJS(Player player, ItemStack smelted) {
		this.player = player;
		this.smelted = smelted;
	}

	@Override
	public EntityJS getEntity() {
		return entityOf(player);
	}

	public ItemStackJS getItem() {
		return ItemStackJS.of(smelted);
	}
}