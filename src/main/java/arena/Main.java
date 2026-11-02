package arena;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.function.Function;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.joining;

/** `java arena.Main` starts the web arena. `java arena.Main tournament 20` prints a league table. */
public final class Main {
    public static void main(String[] args) throws IOException {
        if (args.length > 0 && args[0].equals("tournament")) {
            int games = args.length > 1 ? Integer.parseInt(args[1]) : 20;
            System.out.printf("%-12s %5s %5s %5s %6s%n", "Bot", "W", "D", "L", "Pts");
            Tournament.run(Bots.names(), games).forEach(s ->
                    System.out.printf("%-12s %5d %5d %5d %6d%n", s.name(), s.wins(), s.draws(), s.losses(), s.points()));
            return;
        }
        HttpServer server = start(args.length > 0 ? Integer.parseInt(args[0]) : 8080);
        System.out.println("Connect Four Arena running at http://localhost:" + server.getAddress().getPort());
    }

    /** Starts the arena on the given port (0 picks a free one, which the tests use) and returns the running server. */
    static HttpServer start(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newCachedThreadPool());
        server.createContext("/api/bots", ex -> api(ex, q ->
                "[" + Bots.names().stream().map(n -> "\"" + n + "\"").collect(joining(",")) + "]"));
        // Stateless: the client sends the moves so far and the server answers with the bot's reply.
        server.createContext("/api/move", ex -> api(ex, q -> {
            Board b = Board.of(parseMoves(q.getOrDefault("moves", "")));
            if (b.isFull()) throw new IllegalArgumentException("The board is full");
            return "{\"col\":" + Bots.create(q.get("bot")).chooseMove(b) + "}";
        }));
        server.createContext("/api/tournament", ex -> api(ex, q -> {
            int games = Math.min(40, Integer.parseInt(q.getOrDefault("games", "10")));
            return "[" + Tournament.run(Bots.names(), games).stream().map(s ->
                    "{\"name\":\"%s\",\"wins\":%d,\"draws\":%d,\"losses\":%d,\"points\":%d}"
                            .formatted(s.name(), s.wins(), s.draws(), s.losses(), s.points())).collect(joining(",")) + "]";
        }));
        server.createContext("/", Main::page);
        server.start();
        return server;
    }

    private static void api(HttpExchange ex, Function<Map<String, String>, String> handler) throws IOException {
        try {
            send(ex, 200, "application/json; charset=utf-8", handler.apply(query(ex.getRequestURI().getRawQuery())));
        } catch (RuntimeException e) {
            send(ex, 400, "application/json; charset=utf-8",
                    "{\"error\":\"" + String.valueOf(e.getMessage()).replace("\"", "'") + "\"}");
        }
    }

    private static void page(HttpExchange ex) throws IOException {
        if (!ex.getRequestURI().getPath().equals("/")) { ex.sendResponseHeaders(404, -1); ex.close(); return; }
        try (InputStream in = Main.class.getResourceAsStream("/static/index.html")) {
            send(ex, 200, "text/html; charset=utf-8", new String(in.readAllBytes(), UTF_8));
        }
    }

    private static void send(HttpExchange ex, int status, String type, String body) throws IOException {
        byte[] bytes = body.getBytes(UTF_8);
        ex.getResponseHeaders().set("Content-Type", type);
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(bytes); }
    }

    private static Map<String, String> query(String raw) {
        Map<String, String> q = new HashMap<>();
        if (raw == null) return q;
        for (String kv : raw.split("&")) {
            int i = kv.indexOf('=');
            if (i > 0) q.put(URLDecoder.decode(kv.substring(0, i), UTF_8), URLDecoder.decode(kv.substring(i + 1), UTF_8));
        }
        return q;
    }

    private static int[] parseMoves(String s) {
        return s.isBlank() ? new int[0] : Arrays.stream(s.split(",")).mapToInt(Integer::parseInt).toArray();
    }
}
