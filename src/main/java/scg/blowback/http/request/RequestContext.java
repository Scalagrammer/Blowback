package scg.blowback.http.request;

import io.atlassian.fugue.Option;
import io.atlassian.fugue.Unit;
import lombok.Getter;
import lombok.val;

import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import reactor.util.context.ContextView;
import scg.blowback.http.*;
import scg.blowback.http.response.Alert;
import scg.blowback.http.request.events.IncomingRequestEvent;

import org.springframework.web.reactive.function.server.ServerRequest;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNullElse;
import static java.util.Collections.unmodifiableMap;

import static java.time.format.DateTimeFormatter.ofPattern;

import static reactor.util.context.Context.empty;
import static reactor.core.publisher.Flux.fromIterable;

import static scg.blowback.http.AlertCollector.stream;
import static scg.blowback.utils.Reactor.unit;
import static scg.blowback.http.Alerting.pushing;

public final class RequestContext {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSSSSSZ");

    private static final String MDC_KEY              = "MDC";
    private static final String CLOCK_KEY            = "CLOCK";
    private static final String REQUEST_ID_KEY       = "RQ_ID";
    private static final String CORR_ID_KEY          = "CORR_ID";
    private static final String HEADERS_KEY          = "HEADERS";
    private static final String ALERTING_KEY         = "ALERTING";
    private static final String EVENT_BUS            = "EVENT_BUS";
    private static final String DEBUG_MODE_KEY       = "DEBUG_MODE";
    private static final String ALERTS_COLLECTOR_KEY = "ALERTS_COLLECTOR";

    @Getter
    private Context context;

    public RequestContext(Context context) {
        this.context = injectAlerts(requireNonNullElse(context, empty()));
    }

    public RequestContext withCorrelationId(Option<UUID> value) {
        //
        for (UUID id : value) {
            this.context = context.put(CORR_ID_KEY, id);
        }
        //
        return this;
    }

    public RequestContext withDebugMode(DebugMode debugMode) {
        this.context = context.put(DEBUG_MODE_KEY, debugMode);
        return this;
    }

    public RequestContext withRequestId(UUID value) {
        this.context = context.put(REQUEST_ID_KEY, value);
        return this;
    }

    public RequestContext withEventbus(Eventbus value) {
        this.context = context.put(EVENT_BUS, value);
        return this;
    }

    public RequestContext withHeaders(ServerRequest.Headers value) {
        this.context = context.put(HEADERS_KEY, value);
        return this;
    }

    public RequestContext withClock(Clock value) {
        this.context = context.put(CLOCK_KEY, value);
        return this;
    }

    public RequestContext withMdc(Map<String, String> value) {
        this.context = context.put(MDC_KEY, unmodifiableMap(value));
        return this;
    }


    public static Mono<Unit> fire(EventObject event) {
        return Mono.deferContextual(view -> getEventbus(view).fire(event));
    }

    public static Mono<Unit> fire(Supplier<EventObject> delayedEvent) {
        return Mono.fromSupplier(delayedEvent).flatMap(RequestContext::fire);
    }

    public static Mono<Unit> bulkFire(Collection<? extends EventObject> events) {
        return fromIterable(events).concatMap(RequestContext::fire).then(unit);
    }

    public static Function<Context, Stream<String>> fetchHeader(String name) {
        return context -> getHeaders(context).header(name).stream();
    }

    public static Clock getClock(ContextView context) {
        return context.get(CLOCK_KEY);
    }

    public static DebugMode getDebugMode(ContextView context) {
        return context.get(DEBUG_MODE_KEY);
    }

    public static UUID getRequestId(ContextView context) {
        return context.get(REQUEST_ID_KEY);
    }

    public static Alerting getAlerting(ContextView context) {
        return context.get(ALERTING_KEY);
    }

    public static Map<String, String> getMdc(Context context) {
        return context.get(MDC_KEY);
    }

    public static UUID getCorrId(ContextView context) {
        return context.get(CORR_ID_KEY);
    }

    public static boolean hasCorrId(ContextView context) {
        return context.hasKey(CORR_ID_KEY);
    }

    public static Stream<Alert> collectAlerts(ContextView context) {
        return getAlertCollector(context).collectAll();
    }

    public static String timestamp(ContextView context) {
        return ZonedDateTime.now(getClock(context)).format(TIMESTAMP_FORMATTER);
    }

    public static IncomingRequestEvent incomingRequestEvent(HttpRouting source, ServerRequest request) {

        val method      = request.method();
        val requestPath = request.requestPath();

        return new IncomingRequestEvent(source);

    }

    private static AlertCollector getAlertCollector(ContextView context) {
        return context.get(ALERTS_COLLECTOR_KEY);
    }

    private static Eventbus getEventbus(ContextView view) {
        return view.get(EVENT_BUS);
    }

    private static ServerRequest.Headers getHeaders(Context context) {
        return context.get(HEADERS_KEY);
    }

    private static Context injectAlerts(Context context) {
        //
        return context.put(ALERTING_KEY, pushing()).put(ALERTS_COLLECTOR_KEY, stream());
    }


}