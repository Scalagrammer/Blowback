package scg.blowback.http;

import lombok.val;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.ServerRequest;
import scg.blowback.utils.functions.RequestFlowHandler;
import scg.blowback.utils.functions.SseFlowHandler;

import static java.time.Clock.fixed;
import static java.time.Instant.EPOCH;
import static java.time.ZoneOffset.UTC;
import static java.time.ZoneId.ofOffset;

import static org.mockito.ArgumentMatchers.any;

import static java.util.UUID.fromString;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.BAD_GATEWAY;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static scg.blowback.http.Alerts.*;

@TestInstance(PER_CLASS)
@SpringBootTest( webEnvironment = RANDOM_PORT
               , classes        = SampleHttpRouting.class
               , properties     = "http-routing.debugger.enabled: true" )
public class HttpRoutingTest {

    private @MockBean RqIdGenerator           rqIdMock;
    private @MockBean SseFlowHandler         onSseMock;
    private @MockBean ClockProvider       providerMock;
    private @MockBean RequestFlowHandler onRequestMock;

    private @Autowired WebTestClient client;

    @Test void requestContractTest() {

        val rqId   = "af2b9db2-3b8e-46d4-8158-e9bd0f936b55";
        val corrId = "af2b9db2-3b8e-46d4-8158-e9bd0f936b56";

        val sampleResult            = "sampleResult";
        val sampleBreakpointMessage = "sampleBreakpoint";
        val sampleInfoAlertMessage  = "sampleInfoMessage";

        val sampleHttpStatus = BAD_GATEWAY;

        val sampleInfoAlert = info(sampleHttpStatus, sampleInfoAlertMessage);

        when(rqIdMock.next()).thenReturn(fromString(rqId));
        when(providerMock.getClock()).thenReturn(fixed(EPOCH, ofOffset("UTC", UTC)));
        when(onRequestMock.onRequest(any(ServerRequest.class))).thenReturn(breakpoint(sampleBreakpointMessage).then(sampleInfoAlert).thenReturn(sampleResult));

        (client.get()).uri("/sample")

                .header("X-Debug-Mode", "debug")
                .header("X-Correlation-ID", corrId)

                .exchange()

                .expectStatus().isEqualTo(sampleHttpStatus)
                .expectHeader().valueEquals("X-Correlation-ID", corrId)
                .expectBody()

                .jsonPath("$.rqid").isEqualTo(rqId)
                .jsonPath("$.result").isEqualTo(sampleResult)
                .jsonPath("$.timestamp").isEqualTo("1970-01-01T00:00:00.000000+0000")

                .jsonPath("$.alerts[0].level").isEqualTo("debug")
                .jsonPath("$.alerts[0].message").isEqualTo(sampleBreakpointMessage)
                .jsonPath("$.alerts[0].timestamp").isEqualTo("1970-01-01T00:00:00Z")
                .jsonPath("$.alerts[0].element").isEqualTo("scg.blowback.http.HttpRouterTest.contractTest(HttpRouterTest.java:64)")

                .jsonPath("$.alerts[1].level").isEqualTo("info")
                .jsonPath("$.alerts[1].message").isEqualTo(sampleInfoAlertMessage)
                .jsonPath("$.alerts[1].timestamp").isEqualTo("1970-01-01T00:00:00Z");

        verify(rqIdMock, times(1)).next();
        verify(providerMock, times(1)).getClock();
        verify(onRequestMock, times(1)).onRequest(any(ServerRequest.class));

    }

    @Test void sseContractTest() {



    }

}
