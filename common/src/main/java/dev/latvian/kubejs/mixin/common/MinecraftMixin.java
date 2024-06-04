package dev.latvian.kubejs.mixin.common;

import dev.latvian.kubejs.client.ClientProperties;
import dev.latvian.kubejs.client.KubeJSClientResourcePack;
import dev.latvian.kubejs.core.MinecraftClientKJS;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.PackRepository;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * @author LatvianModder
 */
@Mixin(Minecraft.class)
public abstract class MinecraftMixin implements MinecraftClientKJS {

	@Inject(method = "createTitle", at = @At("HEAD"), cancellable = true)
	private void kjs$setWindowTitle(CallbackInfoReturnable<String> ci) {
		String s = ClientProperties.get().title;
		if (!s.isEmpty()) {
			ci.setReturnValue(s);
		}
	}

	@Redirect(
			method = {"reloadResourcePacks", "<init>"},
			at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/repository/PackRepository;openAllSelected()Ljava/util/List;")
	)
	private List<PackResources> kjs$loadPacks(PackRepository repository) {
		return KubeJSClientResourcePack.inject(repository.openAllSelected());
	}
}