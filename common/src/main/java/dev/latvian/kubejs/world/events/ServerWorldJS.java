package dev.latvian.kubejs.world.events;

import com.google.common.collect.Lists;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.latvian.kubejs.player.AttachPlayerDataEvent;
import dev.latvian.kubejs.player.EntityArrayList;
import dev.latvian.kubejs.player.FakeServerPlayerDataJS;
import dev.latvian.kubejs.player.ServerPlayerDataJS;
import dev.latvian.kubejs.script.ScriptType;
import dev.latvian.kubejs.server.ServerJS;
import dev.latvian.kubejs.world.WorldCommandSender;
import dev.latvian.kubejs.world.WorldJS;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ServerLevelData;

/**
 * @author LatvianModder
 */
public class ServerWorldJS extends WorldJS {
	private final ServerJS server;
	public final CompoundTag persistentData;

	public ServerWorldJS(ServerJS s, ServerLevel w) {
		super(w);
		server = s;

		String t = w.dimension().location().toString();
		persistentData = s.persistentData.getCompound(t);
		s.persistentData.put(t, persistentData);
	}

	@Override
	public ScriptType getSide() {
		return ScriptType.SERVER;
	}

	@Override
	public ServerJS getServer() {
		return server;
	}

	public long getSeed() {
		return ((ServerLevel) minecraftLevel).getSeed();
	}

	public void setTime(long time) {
		((ServerLevelData) minecraftLevel.getLevelData()).setGameTime(time);
	}

	public void setLocalTime(long time) {
		((ServerLevelData) minecraftLevel.getLevelData()).setDayTime(time);
	}

	@Override
	public ServerPlayerDataJS getPlayerData(Player player) {
		ServerPlayerDataJS data = server.playerMap.get(player.getUUID());

		if (data != null) {
			return data;
		}

		FakeServerPlayerDataJS fakeData = server.fakePlayerMap.get(player.getUUID());

		if (fakeData == null) {
			fakeData = new FakeServerPlayerDataJS(server, (ServerPlayer) player);
			new AttachPlayerDataEvent(fakeData).invoke();
		}

		fakeData.player = (ServerPlayer) player;
		return fakeData;
	}

	@Override
	public String toString() {
		return "ServerWorld:" + getDimension();
	}

	@Override
	public EntityArrayList getEntities() {
		return new EntityArrayList(this, Lists.newArrayList(((ServerLevel) minecraftLevel).getAllEntities()));
	}

	public EntityArrayList getEntities(String filter) {
		if (filter.equals("@e")) {
			return getEntities();
		} else if (filter.equals("@a")) {
			return getPlayers();
		}

		try {
			return createEntityList(new EntitySelectorParser(new StringReader(filter), true).parse().findEntities(new WorldCommandSender(this)));
		} catch (CommandSyntaxException e) {
			return new EntityArrayList(this, 0);
		}
	}
}