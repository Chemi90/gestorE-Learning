package com.gestorelearning.apigateway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;
import org.junit.jupiter.api.Test;

class PingControllerTest {

    @Test
    void pingReturnsExpectedPayload() {
        PingController controller = new PingController();
        Map<String, String> payload = controller.ping();

        assertEquals("api-gateway", payload.get("service"));
        assertEquals("ok", payload.get("status"));
        assertNotNull(payload.get("time"));
    }
}
