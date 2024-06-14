package dev.latvian.kubejs.item.events;

import dev.latvian.kubejs.entity.EntityJS;
import dev.latvian.kubejs.item.InventoryJS;
import dev.latvian.kubejs.item.ItemStackJS;
import dev.latvian.kubejs.player.PlayerEventJS;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * @author LatvianModder
 */
public class ItemCraftedEventJS extends PlayerEventJS {
	private final Player player;
	private final ItemStack crafted;
	private final Container container;

	public ItemCraftedEventJS(Player player, ItemStack crafted, Container container) {
		this.player = player;
		this.crafted = crafted;
		this.container = container;
	}

	@Override
	public EntityJS getEntity() {
		return entityOf(player);
	}

	public ItemStackJS getItem() {
		return ItemStackJS.of(crafted);
	}

	public InventoryJS getInventory() {
		return new InventoryJS(container);
	}
}