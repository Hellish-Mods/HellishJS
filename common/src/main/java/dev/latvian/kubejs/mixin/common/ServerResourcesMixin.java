package dev.latvian.kubejs.mixin.common;

import dev.latvian.kubejs.KubeJS;
import dev.latvian.kubejs.server.ServerScriptManager;
import net.minecraft.commands.Commands;
import net.minecraft.server.ServerResources;
import net.minecraft.server.packs.PackResources;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * @author LatvianModder
 */
@Mixin(ServerResources.class)
public abstract class ServerResourcesMixin implements AutoCloseable {
	@Inject(
			method = "<init>",
			at = @At(
					value = "NEW",
					target = "net/minecraft/server/packs/resources/SimpleReloadableResourceManager"))
	private void init(Commands.CommandSelection commandSelection, int i, CallbackInfo ci) {
		//TODO: exactly the problem, command registry event is posted when `new Commands(...)` is called
		//so we need to make injection point targeting somewhere before `this.commands = new Commands(arg);` in `ServerResources.<init>`
		KubeJS.LOGGER.info("time stamping: ServerResources init");
		ServerScriptManager.instance = new ServerScriptManager();
		ServerScriptManager.instance.init((ServerResources) (Object) this);
	}

	@ModifyArg(method = "loadResources", at = @At(value = "INVOKE", ordinal = 0,
			target = "Lnet/minecraft/server/packs/resources/ReloadableResourceManager;reload(Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;Ljava/util/List;Ljava/util/concurrent/CompletableFuture;)Ljava/util/concurrent/CompletableFuture;"),
			index = 2)
	private static List<PackResources> resourcePackList(List<PackResources> list) {
		return ServerScriptManager.instance.resourcePackList(list);
	}
}
