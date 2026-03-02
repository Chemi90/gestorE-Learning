package com.gestorelearning.rag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;
import org.junit.jupiter.api.Test;

class PingControllerTest {

    @Test
    void pingReturnsExpectedPayload() {
        PingController controller = new PingController();
        Map<String, String> payload = controller.ping();

        assertEquals("rag-service", payload.get("service"));
        assertEquals("ok", payload.get("status"));
        assertNotNull(payload.get("time"));
    }
}
