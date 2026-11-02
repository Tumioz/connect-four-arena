package arena;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ExecutorService;

import static org.junit.jupiter.api.Assertions.*;

/** Starts the real server on a free port and talks to it over HTTP. */
class ApiTest {
    private static HttpServer server;
    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    @BeforeAll static void start() throws Exception { server = Main.start(0); }

    @AfterAll static void stop() {
        server.stop(0);
        if (server.getExecutor() instanceof ExecutorService es) es.shutdownNow();
    }

    private static HttpResponse<String> get(String path) throws Exception {
        URI uri = URI.create("http://127.0.0.1:" + server.getAddress().getPort() + path);
        return CLIENT.send(HttpRequest.newBuilder(uri).build(), HttpResponse.BodyHandlers.ofString());
    }

    @Test void listsTheBots() throws Exception {
        HttpResponse<String> r = get("/api/bots");
        assertEquals(200, r.statusCode());
        assertEquals("[\"Random\",\"Heuristic\",\"Minimax-3\",\"Minimax-5\"]", r.body());
    }

    @Test void answersWithALegalColumn() throws Exception {
        HttpResponse<String> r = get("/api/move?bot=Random&moves=3,3");
        assertEquals(200, r.statusCode());
        assertTrue(r.body().matches("\\{\"col\":[0-6]\\}"), r.body());
    }

    @Test void blocksAThreatOverHttp() throws Exception {
        assertEquals("{\"col\":0}", get("/api/move?bot=Heuristic&moves=0,1,0,1,0").body());
    }

    @Test void rejectsAnUnknownBot() throws Exception {
        HttpResponse<String> r = get("/api/move?bot=Nope&moves=");
        assertEquals(400, r.statusCode());
        assertTrue(r.body().contains("Unknown bot"));
    }

    @Test void rejectsIllegalMoves() throws Exception {
        assertEquals(400, get("/api/move?bot=Random&moves=0,0,0,0,0,0,0").statusCode());
    }

    @Test void servesTheArenaPage() throws Exception {
        HttpResponse<String> r = get("/");
        assertEquals(200, r.statusCode());
        assertTrue(r.body().contains("Connect Four Arena"));
    }

    @Test void unknownPathIsNotFound() throws Exception { assertEquals(404, get("/nope").statusCode()); }
}
