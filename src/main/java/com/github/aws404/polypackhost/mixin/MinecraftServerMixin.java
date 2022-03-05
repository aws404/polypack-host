package com.github.aws404.polypackhost.mixin;

import com.github.aws404.polypackhost.PolypackHttpServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

	@Inject(method = "prepareStartRegion", at = @At("HEAD"))
	private void initPolypackHost(WorldGenerationProgressListener worldGenerationProgressListener, CallbackInfo ci) {
		PolypackHttpServer.init((MinecraftServer) (Object) this);
	}

	@Inject(method = "shutdown", at = @At("TAIL"))
	private void stopPolypackHost(CallbackInfo ci) {
		PolypackHttpServer.stop();
	}
}
