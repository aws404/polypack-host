package com.github.aws404.polypackhost;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class PolypackHostConfig {
    public static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "polypack_host.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();

    public String hostIp = null;
    public int hostPort = 8001;
    public int threadCount = 3;
    public boolean randomiseUrl = false;

    public static PolypackHostConfig loadConfigFile(File file) {
        PolypackHostConfig config = null;

        if (file.exists()) {
            try {
                BufferedReader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
                config = GSON.fromJson(fileReader, PolypackHostConfig.class);
            } catch (IOException e) {
                PolypackHostMod.LOGGER.error("Failed to load config file. Ignoring and loading defaults.", e);
            }
        }
        if (config == null) {
            config = new PolypackHostConfig();
        }

        config.saveConfigFile(file);
        return config;
    }

    public void saveConfigFile(File file) {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            PolypackHostMod.LOGGER.error("Failed to save config file.", e);
        }
    }
}
