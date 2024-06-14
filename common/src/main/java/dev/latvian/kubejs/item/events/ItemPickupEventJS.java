package dev.latvian.kubejs.item.events;

import dev.latvian.kubejs.entity.EntityJS;
import dev.latvian.kubejs.item.ItemStackJS;
import dev.latvian.kubejs.player.PlayerEventJS;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * @author LatvianModder
 */
public class ItemPickupEventJS extends PlayerEventJS {
	private final Player player;
	private final ItemEntity entity;
	private final ItemStack stack;

	public ItemPickupEventJS(Player player, ItemEntity entity, ItemStack stack) {
		this.player = player;
		this.entity = entity;
		this.stack = stack;
	}

	@Override
	public boolean canCancel() {
		return true;
	}

	@Override
	public EntityJS getEntity() {
		return entityOf(player);
	}

	public EntityJS getItemEntity() {
		return getWorld().getEntity(entity);
	}

	public ItemStackJS getItem() {
		return ItemStackJS.of(stack);
	}
}