package scg.blowback.http;

import org.junit.jupiter.api.Test;

import static java.util.Arrays.stream;
import static java.util.function.Predicate.isEqual;
import static java.util.function.Predicate.not;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static scg.blowback.http.response.AlertLevel.TRACE;
import static scg.blowback.http.response.AlertLevel.values;
import static scg.blowback.http.DebugMode.*;

public class DebuggerModeTest {

    @Test void offModeTest() {
        assertTrue(stream(values()).noneMatch(OFF));
    }

    @Test void tracingModeTest() {
        assertTrue(stream(values()).allMatch(TRACING));
    }

    @Test void debuggingModeTest() {
        assertTrue(stream(values()).filter(not(isEqual(TRACE))).allMatch(DEBUGGING));
    }

}
