package com.github.aws404.polypackhost;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import eu.pb4.polymer.api.resourcepack.PolymerRPUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.text.Text;

import java.io.*;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PolypackHttpHandler implements HttpHandler {

    private static final Path POLYMER_PACK_FILE = Path.of(FabricLoader.getInstance().getGameDir().toFile() + "/polymer-resourcepack.zip");

    private static HttpServer server = null;
    private static ExecutorService threadPool = null;

    public static void stop() {
        if (PolypackHttpHandler.server != null) {
            PolypackHttpHandler.server.stop(0);
        }
        if (PolypackHttpHandler.threadPool != null) {
            PolypackHttpHandler.threadPool.shutdownNow();
        }
    }

    public static void start(MinecraftServer minecraftServer) {
        PolypackHttpHandler.threadPool = Executors.newFixedThreadPool(PolypackHostMod.CONFIG.threadCount(), new ThreadFactoryBuilder().setNameFormat("Polypack-Host-%d").build());

        CompletableFuture.runAsync(() -> {
            try {
                PolypackHostMod.LOGGER.info("Starting Polymer resource pack server...");
                PolymerRPUtils.build(POLYMER_PACK_FILE);

                String serverIp = PolypackHostMod.CONFIG.ip().isEmpty() ? minecraftServer.getServerIp() : PolypackHostMod.CONFIG.ip();
                if (serverIp.isEmpty()) {
                    PolypackHostMod.LOGGER.warn("No external IP address is defined in the configuration, this may cause issues outside of the local network.");
                    serverIp = InetAddress.getLocalHost().getHostAddress();
                }

                String subUrl = PolypackHostMod.CONFIG.randomiseUrl() ? Integer.toString(new Random().nextInt(Integer.MAX_VALUE)) : "pack";

                PolypackHttpHandler.server = HttpServer.create(new InetSocketAddress("0.0.0.0", PolypackHostMod.CONFIG.hostPort()), 0);
                PolypackHttpHandler.server.createContext("/" + subUrl, new PolypackHttpHandler());
                PolypackHttpHandler.server.setExecutor(PolypackHttpHandler.threadPool);
                PolypackHttpHandler.server.start();

                String packIp = String.format("http://%s:%s/%s", serverIp, PolypackHostMod.CONFIG.hostPort(), subUrl);

                String hash = String.format("%040x", new BigInteger(1, MessageDigest
                        .getInstance("SHA-1")
                        .digest(new FileInputStream(POLYMER_PACK_FILE.toString()).readAllBytes()))
                );

                if (minecraftServer instanceof MinecraftDedicatedServer dedicatedServer) {
                    boolean required = false;
                    Text prompt = Text.empty();
                    if (dedicatedServer.getProperties().serverResourcePackProperties.isPresent()) {
                        required = dedicatedServer.getProperties().serverResourcePackProperties.get().isRequired();
                        prompt = dedicatedServer.getProperties().serverResourcePackProperties.get().prompt();
                    }
                    dedicatedServer.getProperties().serverResourcePackProperties = Optional.of(new MinecraftServer.ServerResourcePackProperties(packIp, hash, required, prompt));
                }

                PolypackHostMod.LOGGER.info("Polymer resource pack host started at {} (Hash: {})", packIp, hash);
            } catch (Exception e) {
                PolypackHostMod.LOGGER.error("Failed to start the resource pack server!", e);
            }
        }, PolypackHttpHandler.threadPool);

    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (Objects.equals(exchange.getRequestMethod(), "GET")) {
            if (exchange.getRequestHeaders().getFirst("X-Minecraft-Username") != null) {
                PolypackHostMod.LOGGER.info("Supplying resource pack for Minecraft player: {}", exchange.getRequestHeaders().getFirst("X-Minecraft-Username"));
            } else {
                PolypackHostMod.LOGGER.info("Supplying resource pack to a non-Minecraft client");
            }

            OutputStream outputStream = exchange.getResponseBody();
            File pack = POLYMER_PACK_FILE.toFile();

            exchange.getResponseHeaders().add("User-Agent", "Java/polypack-host");
            exchange.sendResponseHeaders(200, pack.length());

            FileInputStream fis = new FileInputStream(pack);
            BufferedInputStream bis = new BufferedInputStream(fis);
            bis.transferTo(outputStream);
            bis.close();
            fis.close();

            outputStream.flush();
            outputStream.close();
        } else {
            exchange.sendResponseHeaders(400, 0);
        }
    }
}
