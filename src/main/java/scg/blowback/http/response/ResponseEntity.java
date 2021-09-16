package scg.blowback.http.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.atlassian.fugue.Either;
import io.atlassian.fugue.Option;
import io.atlassian.fugue.Unit;
import lombok.Data;
import lombok.val;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.util.context.ContextView;
import scg.blowback.http.AlertException;
import scg.blowback.http.response.Alert.Builder;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static io.atlassian.fugue.Eithers.merge;
import static java.util.Comparator.comparingInt;
import static java.util.Locale.getDefault;
import static java.util.function.BinaryOperator.maxBy;
import static java.util.stream.Collectors.reducing;
import static java.util.stream.Collectors.toUnmodifiableList;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.reactive.function.server.ServerResponse.status;
import static scg.blowback.http.request.RequestContext.*;
import static scg.blowback.http.response.Alert.partiallyAlert;
import static scg.blowback.utils.Headers.X_CORRELATION_ID_HEADER;

@Data
@JsonInclude(NON_NULL)
public class ResponseEntity {

    public static final ResponseExtractor toResponseEntity = (context, result) -> {
        //
        val entity = fromResult(context, result);
        //
        entity.setLocale(getDefault());
        entity.setRqid(getRequestId(context));
        entity.setTimestamp(timestamp(context));
        entity.setAlerts(collectAlerts(context));
        //
        Consumer<HttpHeaders> enrichHeaders = headers -> {
            if (hasCorrId(context)) headers.set(X_CORRELATION_ID_HEADER, getCorrId(context).toString());
        };
        //
        return status(computeHttpStatus(context)).headers(enrichHeaders).bodyValue(entity);
    };

    private Locale      locale;
    private UUID          rqid;
    private Object      result;
    private String   timestamp;
    private List<Alert> alerts;

    public void setAlerts(Stream<Alert> alerts) {
        //
        val collectedAlerts = alerts.collect(toUnmodifiableList());
        //
        if (!collectedAlerts.isEmpty()) {
            this.alerts = collectedAlerts;
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked", "StatementWithEmptyBody"})
    private static ResponseEntity fromResult(ContextView context, Object result) {
        //
        val entity = new ResponseEntity();
        //
        if (result instanceof Optional) {
            entity.result = ((Optional) result).orElse(null);
        } else if (result instanceof Option) {
            entity.result = ((Option) result).getOrNull();
        } else if (result instanceof Unit) {
            // nop
        } else if (result instanceof Either) {
            entity.result = merge((Either) result);
        } else if (result instanceof AlertException) {
            getAlerting(context).push(((AlertException) result).getAlert()).subscribe();
        } else if (result instanceof Throwable) {
            getAlerting(context).push(partiallyAlert(INTERNAL_SERVER_ERROR, "An error has occurred during request processing").andThen(Builder::yieldError).apply(getClock(context))).subscribe();
        } else {
            entity.result = result;
        }
        //
        return entity;
    }

    private static HttpStatus computeHttpStatus(ContextView context) {
        return collectAlerts(context).collect(toHttpStatus(OK));
    }

    @FunctionalInterface
    public interface ResponseExtractor extends Function<Object, Mono<ServerResponse>> {

        Mono<ServerResponse> extract(ContextView context, Object result);

        @Override
        default Mono<ServerResponse> apply(Object result) {
            return Mono.deferContextual(context -> extract(context, result));
        }
    }

    private static Collector<Alert, ?, HttpStatus> toHttpStatus(HttpStatus defaultStatus) {
        return reducing(defaultStatus, Alert::getStatus, maxBy(comparingInt(HttpStatus::value)));
    }

}
