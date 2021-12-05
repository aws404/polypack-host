package com.github.aws404.polypackhost;

import net.fabricmc.api.DedicatedServerModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PolypackHostMod implements DedicatedServerModInitializer {
	public static final Logger LOGGER = LogManager.getLogger("Polypack Host");
	public static final PolypackHostConfig CONFIG = PolypackHostConfig.loadConfigFile(PolypackHostConfig.CONFIG_FILE);

	@Override
	public void onInitializeServer() {
		LOGGER.info("Starting Polypack Host Mod");
	}
}
