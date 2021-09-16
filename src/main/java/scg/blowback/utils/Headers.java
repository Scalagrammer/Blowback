package scg.blowback.utils;

import io.atlassian.fugue.Option;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.server.ServerRequest;
import scg.blowback.http.DebugMode;

import java.util.UUID;

import static io.atlassian.fugue.Option.option;
import static java.util.Objects.requireNonNullElse;
import static lombok.AccessLevel.PRIVATE;
import static scg.blowback.http.DebugMode.getByName;

@RequiredArgsConstructor(access = PRIVATE)
public final class Headers {

    public static final String X_DEBUG_MODE_HEADER     =     "X-Debug-Mode";
    public static final String X_REQUEST_ID_HEADER     =     "X-Request-ID";
    public static final String X_CORRELATION_ID_HEADER = "X-Correlation-ID";

    private final HttpHeaders value;

    public Option<String> findCorrIdValue() {
        return option(value.getFirst(X_CORRELATION_ID_HEADER));
    }

    public Option<UUID> findCorrId() {
        return (findCorrIdValue()).map(UUID::fromString);
    }

    public DebugMode getDebugMode(DebugMode defaultMode) {
        return requireNonNullElse(getByName(value.getFirst(X_DEBUG_MODE_HEADER)), defaultMode);
    }

    public static Headers headers(ServerRequest.Headers value) {
        return new Headers(value.asHttpHeaders());
    }

}
