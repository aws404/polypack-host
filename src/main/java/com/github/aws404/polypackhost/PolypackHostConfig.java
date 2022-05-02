package com.github.aws404.polypackhost;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;

public record PolypackHostConfig(String ip, int hostPort, int threadCount, boolean randomiseUrl) {

    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "polypack_host.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();
    private static final Codec<PolypackHostConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("external_ip").orElse("").forGetter(PolypackHostConfig::ip),
            Codec.INT.fieldOf("host_port").orElse(24464).forGetter(PolypackHostConfig::hostPort),
            Codec.INT.fieldOf("thread_count").orElse(1).forGetter(PolypackHostConfig::threadCount),
            Codec.BOOL.fieldOf("randomise_url").orElse(false).forGetter(PolypackHostConfig::randomiseUrl)
    ).apply(instance, PolypackHostConfig::new));

    public static PolypackHostConfig loadConfigFile() {
        JsonObject parsedFile = null;
        if (CONFIG_FILE.exists()) {
            try {
                FileReader reader = new FileReader(CONFIG_FILE);
                parsedFile = GSON.fromJson(reader, JsonObject.class);
                reader.close();
            } catch (IOException e) {
                PolypackHostMod.LOGGER.error("Failed to read configuration file! Resetting config to defaults.", e);
            }
        } else {
            PolypackHostMod.LOGGER.info("Configuration file not found! Creating new one...");
        }
        if (parsedFile == null) {
            parsedFile = new JsonObject();
        }

        PolypackHostConfig config = CODEC.decode(JsonOps.INSTANCE, parsedFile).getOrThrow(false, s -> PolypackHostMod.LOGGER.error("Error deserializing configuration file. Error: {}", s)).getFirst();
        config.saveConfigFile();
        return config;
    }

    public void saveConfigFile() {
        JsonElement jsonObject = CODEC.encodeStart(JsonOps.INSTANCE, this).getOrThrow(false, s -> PolypackHostMod.LOGGER.error("Error serializing configuration file. Error: {}", s));
        try {
            FileOutputStream stream = new FileOutputStream(CONFIG_FILE);
            Writer writer = new OutputStreamWriter(stream);
            GSON.toJson(jsonObject, writer);
            writer.close();
            stream.close();
        } catch (IOException e) {
            PolypackHostMod.LOGGER.error("Failed to save configuration file. Changes were not saved.", e);
        }
    }
}
