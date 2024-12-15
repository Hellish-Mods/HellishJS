package dev.latvian.kubejs.world.events;

import dev.latvian.kubejs.event.EventJS;
import dev.latvian.kubejs.server.ServerJS;
import dev.latvian.kubejs.util.UtilsJS;
import dev.latvian.kubejs.world.WorldJS;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * @author LatvianModder
 */
public abstract class WorldEventJS extends EventJS {
	public abstract WorldJS getWorld();

	public WorldJS getLevel() {
		return getWorld();
	}

	@Nullable
	public ServerJS getServer() {
		return getWorld().getServer();
	}

	protected WorldJS worldOf(Level world) {
		return UtilsJS.getWorld(world);
	}

	protected WorldJS worldOf(Entity entity) {
		return worldOf(entity.level);
	}

	public final boolean post(String id) {
		return post(getWorld().getSide(), id);
	}

	public final boolean post(String id, String sub) {
		return post(getWorld().getSide(), id, sub);
	}
}