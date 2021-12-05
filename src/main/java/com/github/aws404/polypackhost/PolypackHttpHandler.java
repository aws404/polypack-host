package com.github.aws404.polypackhost;

import com.google.common.hash.Hashing;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import eu.pb4.polymer.api.resourcepack.PolymerRPUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.Executors;

public class PolypackHttpHandler implements HttpHandler {

    private static final Path POLYMER_PACK_FILE = Path.of(FabricLoader.getInstance().getGameDir().toFile() + "/polymer-resourcepack.zip");

    public static void start(MinecraftServer minecraftServer) {
        try {
            String serverIp = PolypackHostMod.CONFIG.hostIp == null || PolypackHostMod.CONFIG.hostIp.isEmpty() ? minecraftServer.getServerIp() : PolypackHostMod.CONFIG.hostIp;
            if (serverIp.isEmpty()) {
                serverIp = InetAddress.getLocalHost().getHostAddress();
            }
            PolypackHostMod.LOGGER.info("Building polymer resource pack...");
            PolymerRPUtils.build(POLYMER_PACK_FILE);

            String subUrl = PolypackHostMod.CONFIG.randomiseUrl ? Integer.toString(new Random().nextInt(Integer.MAX_VALUE)) : "pack";

            HttpServer server = HttpServer.create(new InetSocketAddress(serverIp, PolypackHostMod.CONFIG.hostPort), 0);
            server.createContext("/" + subUrl, new PolypackHttpHandler());
            server.setExecutor(Executors.newFixedThreadPool(PolypackHostMod.CONFIG.threadCount));
            server.start();

            String packIp = String.format("http://%s:%s/%s", serverIp, PolypackHostMod.CONFIG.hostPort, subUrl);

            PolypackHostMod.LOGGER.info("Polymer resource pack host started at {}", packIp);

            String hash = com.google.common.io.Files.asByteSource(PolypackHttpHandler.POLYMER_PACK_FILE.toFile()).hash(Hashing.sha1()).toString();
            minecraftServer.setResourcePack(packIp, hash);
        } catch (IOException e) {
            PolypackHostMod.LOGGER.error("Failed to start the resource pack server!", e);
            e.printStackTrace();
        }
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
            int b;
            while ((b = fis.read()) != -1) {
                outputStream.write(b);
            }
            fis.close();

            outputStream.flush();
            outputStream.close();
        } else {
            exchange.sendResponseHeaders(400, 0);
        }
    }
}
