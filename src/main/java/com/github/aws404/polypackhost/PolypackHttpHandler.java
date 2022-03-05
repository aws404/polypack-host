package com.github.aws404.polypackhost;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.util.Objects;

public class PolypackHttpHandler implements HttpHandler {
    private static final PolypackHttpHandler handler = new PolypackHttpHandler();

    private PolypackHttpHandler() {
    }

    public static PolypackHttpHandler getHandler() {
        return handler;
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
            File pack = PolypackHostMod.POLYMER_PACK_FILE.toFile();

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
