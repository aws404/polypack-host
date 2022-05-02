package com.github.aws404.polypackhost;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PolypackHostMod implements DedicatedServerModInitializer {
	public static final Logger LOGGER = LogManager.getLogger("Polypack Host");
	public static final PolypackHostConfig CONFIG = PolypackHostConfig.loadConfigFile();

	@Override
	public void onInitializeServer() {
		ServerLifecycleEvents.SERVER_STARTED.register(PolypackHttpHandler::start);
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> PolypackHttpHandler.stop());
		LOGGER.info("Polypack Host Mod Started!");
	}
}
