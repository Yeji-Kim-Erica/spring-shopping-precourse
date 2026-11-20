package shopping.product.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import shopping.common.error.ErrorCode;
import shopping.common.error.ShoppingException;

class PurgoMalumClientTest {

    private HttpServer server;
    private ExecutorService workers;
    private PurgoMalumClient client;
    private volatile int status = 200;
    private volatile String response = "false";
    private volatile String query;
    private volatile boolean slow;
    private volatile boolean headersFirst;

    @BeforeEach
    void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        workers = Executors.newVirtualThreadPerTaskExecutor();
        server.setExecutor(workers);
        server.createContext("/check", this::respond);
        server.start();
        client = new PurgoMalumClient(endpoint(), Duration.ofSeconds(2));
    }

    @AfterEach
    void stop() {
        client.close();
        server.stop(0);
        workers.shutdownNow();
    }

    @ParameterizedTest
    @CsvSource({"true,true", "false,false"})
    void readsBooleanResponse(String body, boolean expected) {
        response = body;

        assertThat(client.containsProfanity("상품")).isEqualTo(expected);
    }

    @Test
    void encodesNameAsSingleParameter() {
        client.containsProfanity("상품 A+B&/");

        assertThat(URLDecoder.decode(query, StandardCharsets.UTF_8)).isEqualTo("text=상품 A+B&/");
        assertThat(query).doesNotContain("&");
    }

    @ParameterizedTest
    @CsvSource({"500,false", "200,unknown"})
    void rejectsHttpErrorsOrUnreadableBody(int code, String body) {
        status = code;
        response = body;

        assertUnavailable();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void limitsHeaderAndBodyReception(boolean sendHeadersFirst) {
        client.close();
        client = new PurgoMalumClient(endpoint(), Duration.ofMillis(200));
        client.containsProfanity("연결 준비");
        headersFirst = sendHeadersFirst;
        slow = true;

        assertUnavailable();
    }

    private void assertUnavailable() {
        assertThatThrownBy(() -> client.containsProfanity("상품"))
                .isInstanceOfSatisfying(ShoppingException.class,
                        error -> assertThat(error.code())
                                .isEqualTo(ErrorCode.PROFANITY_CHECK_UNAVAILABLE));
    }

    private String endpoint() {
        return "http://127.0.0.1:" + server.getAddress().getPort() + "/check";
    }

    private void respond(HttpExchange exchange) throws IOException {
        query = exchange.getRequestURI().getRawQuery();
        if (!headersFirst) {
            delay();
        }
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().flush();
        if (headersFirst) {
            delay();
        }
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    private void delay() {
        try {
            if (slow) {
                Thread.sleep(600);
            }
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
        }
    }
}
