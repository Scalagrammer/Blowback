package scg.blowback.http;

import io.atlassian.fugue.Option;
import lombok.val;

import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.reactive.function.server.*;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.beans.factory.annotation.Autowired;

import reactor.util.context.Context;
import scg.blowback.http.request.RequestContext;
import scg.blowback.http.properties.HttpRoutingProperties;
import scg.blowback.http.configuration.exceptions.NotInitializedFields;
import scg.blowback.http.configuration.exceptions.AlreadyInitializedField;

import scg.blowback.http.sse.ServerSideEvent;
import scg.blowback.http.sse.ServerSideEventStreaming;
import scg.blowback.utils.Headers;
import scg.blowback.utils.functions.RequestFlowHandler;
import scg.blowback.utils.functions.SseFlowHandler;

import java.util.*;
import java.util.function.Function;

import static io.atlassian.fugue.Option.option;
import static java.util.Map.of;
import static java.util.Objects.*;

import static java.lang.String.valueOf;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.codec.ServerSentEvent.builder;
import static org.springframework.web.reactive.function.BodyInserters.fromServerSentEvents;
import static org.springframework.web.reactive.function.server.ServerResponse.status;
import static scg.blowback.http.DebugMode.OFF;

import static org.slf4j.MDC.getCopyOfContextMap;

import static scg.blowback.utils.Headers.*;
import static scg.blowback.utils.Maps.filterValues;
import static scg.blowback.http.response.ResponseEntity.*;
import static scg.blowback.http.Eventbus.fromEventPublisher;
import static scg.blowback.http.request.RequestContext.incomingRequestEvent;

public abstract class HttpRouting implements InitializingBean {

    private static final String LAST_EVENT_ID = "Last-Event-ID";

    private RqIdGenerator               rqid;
    private Eventbus                eventbus;
    private HttpRoutingProperties properties;
    private ClockProvider      clockProvider;

    @Override
    public final void afterPropertiesSet() {
        //
        val nulls = filterValues(of("rqid", rqid, "clockProvider", clockProvider, "eventbus", eventbus, "properties", properties), Objects::isNull);
        //
        if (!nulls.isEmpty()) {
            throw new NotInitializedFields((nulls.keySet()).toArray(String[]::new));
        }
    }

    public final RouterFunction<ServerResponse> route(RequestPredicate predicate, SseFlowHandler handler) {
        //
        Map<String, String> mdc = requireNonNullElseGet(getCopyOfContextMap(), HashMap::new);
        //
        HandlerFunction<ServerResponse> proxy = request -> {
            //
            this.onRequest(request);
            //
            return status(OK).body(fromServerSentEvents(handler.onRequest(request).contextWrite(enrich(mdc, request))));
        };
        //
        return RouterFunctions.route(predicate, proxy);
    }

    protected final RouterFunction<ServerResponse> route(RequestPredicate predicate, RequestFlowHandler handler) {
        //
        Map<String, String> mdc = requireNonNullElseGet(getCopyOfContextMap(), HashMap::new);
        //
        HandlerFunction<ServerResponse> proxy = request -> {
            //
            this.onRequest(request);
            //
            return handler.onRequest(request)
                    .flatMap(toResponseEntity)
                    .onErrorResume(toResponseEntity)
                    .contextWrite(enrich(mdc, request));
        };
        //
        return RouterFunctions.route(predicate, proxy);
    }

    @Autowired(required = false)
    protected final void injectClockProvider(ClockProvider value) {
        if (isNull(clockProvider)) {
            this.clockProvider = requireNonNull(value);
        } else {
            throw new AlreadyInitializedField("clockProvider");
        }
    }

    @Autowired(required = false)
    protected final void injectRqIdGenerator(RqIdGenerator value) {
        if (isNull(rqid)) {
            this.rqid = requireNonNull(value);
        } else {
            throw new AlreadyInitializedField("rqid");
        }
    }

    @Autowired(required = false)
    protected final void injectProperties(HttpRoutingProperties value) {
        if (isNull(properties)) {
            this.properties = requireNonNull(value);
        } else {
            throw new AlreadyInitializedField("properties");
        }
    }

    @Autowired(required = false)
    protected final void injectApplicationEventPublisher(ApplicationEventPublisher value) {
        if (isNull(eventbus)) {
            this.eventbus = fromEventPublisher(requireNonNull(value));
        } else {
            throw new AlreadyInitializedField("eventbus");
        }
    }

    private DebugMode acceptDebugMode(DebugMode debugMode) {
        //
        if (!(properties.getDebugger()).isEnabled()) debugMode = OFF;
        //
        return debugMode;
    }

    private void onRequest(ServerRequest request) {
        this.eventbus.fire(incomingRequestEvent(this, request));
    }

    private Function<Context, Context> enrich(Map<String, String> mdc, ServerRequest request) {
        //
        Function<RequestContext, RequestContext> f = requestContext -> {
            //
            val requestId = rqid.next();
            //
            val extractor = headers(request.headers());
            //
            mdc.putAll(extractMdc(extractor, requestId));
            //
            val debugMode = acceptDebugMode(extractor.getDebugMode(OFF));
            //
            return requestContext.withHeaders(request.headers())
                    .withCorrelationId(extractor.findCorrId())
                    .withClock(clockProvider.getClock())
                    .withRequestId(requestId)
                    .withDebugMode(debugMode)
                    .withEventbus(eventbus)
                    .withMdc(mdc);
        };
        //
        return f.compose(RequestContext::new)
                .andThen(RequestContext::getContext);
    }

    private static Map<String, String> extractMdc(Headers extractor, UUID requestId) {
        //
        val context = new HashMap<String, String>();
        //
        context.put(X_REQUEST_ID_HEADER, valueOf(requestId));
        //
        for (val corrId : extractor.findCorrIdValue()) {
            context.put(X_CORRELATION_ID_HEADER, corrId);
        }
        //
        return context;
    }

    private static Option<String> extractLastEventId(ServerRequest request) {
        return option((request.headers()).firstHeader(LAST_EVENT_ID));
    }

    private static ServerSentEvent<Object> toSse(ServerSideEvent<Object> event) {

        val sse = builder(event.getPayload());

        if (!event.hasQualifier()) {
            return sse.id(event.getId()).build();
        } else {
            return sse.id(event.getId()).event(event.getQualifier()).build();
        }

    }

}
