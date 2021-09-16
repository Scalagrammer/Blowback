package scg.blowback.http.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.val;
import org.springframework.http.HttpStatus;
import scg.blowback.http.AlertException;

import java.time.Clock;
import java.util.Comparator;
import java.util.function.Function;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static java.lang.String.format;
import static java.time.ZonedDateTime.now;
import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;
import static java.util.Objects.nonNull;

import static scg.blowback.http.response.AlertLevel.*;

public interface Alert extends AlertFold {

    HttpStatus getStatus();
    AlertLevel  getLevel();
    String    getMessage();
    String    getElement();
    String  getTimestamp();

    @Override
    default <R> R fold(Function<ErrorAlert, R> onError, Function<Alert, R> onAlert) {
        return onAlert.apply(this);
    }

    final class InfoAlert extends AlertImpl {
        InfoAlert() {
            super.level = INFO;
        }
    }

    final class DebugAlert extends AlertImpl {
        DebugAlert() {
            super.level = DEBUG;
        }
    }

    final class TraceAlert extends AlertImpl {
        TraceAlert() {
            super.level = TRACE;
        }
    }

    final class WarningAlert extends AlertImpl {
        WarningAlert() {
            super.level = WARNING;
        }
    }

    final class ErrorAlert extends AlertImpl {

        ErrorAlert() {
            super.level = ERROR;
        }

        @Override
        public <R> R fold(Function<ErrorAlert, R> onError, Function<Alert, R> onAlert) {
            return onAlert.apply(this);
        }

        public Throwable throwable() {
            return new AlertException(this);
        }

    }

    class Builder {

        private HttpStatus status;
        private String    message;
        private String    element;

        private final Clock clock;

        private Builder(Clock clock) {
            this.clock = clock;
        }

        public Builder withStatus(HttpStatus status) {
            this.status = status;
            return this;
        }

        public Builder withMessageContent(String content) {
            this.message = content;
            return this;
        }

        public Builder withMessage(String message, Object... args) {
            return this.withMessageContent(args.length != 0 ? format(message, args) : message);
        }

        public Builder withElement(String element) {
            this.element = element;
            return this;
        }

        public TraceAlert yieldTrace() {
            return enrich(new TraceAlert());
        }

        public DebugAlert yieldDebug() {
            return enrich(new DebugAlert());
        }

        public InfoAlert yieldInfo() {
            return enrich(new InfoAlert());
        }

        public ErrorAlert yieldError() {
            return enrich(new ErrorAlert());
        }

        public WarningAlert yieldWarning() {
            return enrich(new WarningAlert());
        }

        private <A extends AlertImpl> A enrich(A alert) {

            alert.message = message;
            alert.status  = status;

            if (nonNull(element)) {
                alert.element = element;
            }

            alert.timestamp = now(clock).format(ISO_OFFSET_DATE_TIME);

            return alert;

        }

    }

    static Builder builder(Clock clock) {
        return new Builder(clock);
    }

    static Function<Clock, Builder> partiallyAlert(HttpStatus status, String message, Object... args) {
        return clock -> builder(clock).withStatus(status).withMessage(message, args);
    }

    static Function<Clock, Builder> partiallyAlert(HttpStatus status, String element, String message, Object... args) {
        return clock -> builder(clock).withStatus(status).withElement(element).withMessage(message, args);
    }

}

@Getter
@EqualsAndHashCode
@JsonInclude(NON_NULL)
abstract class AlertImpl implements Alert {

    @JsonIgnore HttpStatus status;

    AlertLevel level;
    String   message;
    String   element;
    String timestamp;

}
