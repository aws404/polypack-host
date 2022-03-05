package com.github.aws404.polypackhost;

import com.google.common.hash.Hashing;
import com.sun.net.httpserver.HttpServer;
import eu.pb4.polymer.api.resourcepack.PolymerRPUtils;
import net.minecraft.server.MinecraftServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.google.common.io.Files.asByteSource;

public class PolypackHttpServer {
    private static HttpServer server = null;
    private static ExecutorService threadPool = null;

    private PolypackHttpServer() {
    }

    public static void stop() {
        server.stop(1);
        threadPool.shutdownNow();
    }

    @SuppressWarnings({"UnstableApiUsage", "deprecation"})
    public static void init(MinecraftServer minecraftServer) {
        try {
            int port = PolypackHostMod.CONFIG.hostPort;

            String externalIp = PolypackHostMod.CONFIG.externalIp;
            assert externalIp != null;

            String listening = "0.0.0.0";
            PolypackHostMod.LOGGER.info("Building polymer resource pack...");
            PolymerRPUtils.build(PolypackHostMod.POLYMER_PACK_FILE);

            String subUrl = PolypackHostMod.CONFIG.randomiseUrl ? Integer.toString(new Random().nextInt(Integer.MAX_VALUE)) : "pack";

            server = HttpServer.create(new InetSocketAddress(listening, port), 0);
            server.createContext("/" + subUrl, PolypackHttpHandler.getHandler());
            threadPool = Executors.newFixedThreadPool(PolypackHostMod.CONFIG.threadCount);
            server.setExecutor(threadPool);
            server.start();

            PolypackHostMod.LOGGER.info("Server listening on {}:{}/{}", listening, port, subUrl);

            String packIp = String.format("http://%s:%s/%s", externalIp, port, subUrl);

            PolypackHostMod.LOGGER.info("Polymer resource pack host started at {}", packIp);

            String hash = asByteSource(PolypackHostMod.POLYMER_PACK_FILE.toFile()).hash(Hashing.sha1()).toString();
            minecraftServer.setResourcePack(packIp, hash);
        } catch (IOException e) {
            PolypackHostMod.LOGGER.error("Failed to start the resource pack server!", e);
            e.printStackTrace();
        }
    }
}
