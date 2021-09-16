package scg.blowback.http;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import scg.blowback.http.response.AlertLevel;

import java.util.function.Predicate;

import static java.util.Objects.nonNull;
import static scg.blowback.http.response.AlertLevel.DEBUG;
import static scg.blowback.http.response.AlertLevel.TRACE;

@Slf4j
public enum DebugMode implements Predicate<AlertLevel> {

    OFF {
        @Override
        public boolean test(AlertLevel ignored) {
            return false;
        }
    }
    ,
    TRACING {
        @Override
        public boolean test(AlertLevel alertLevel) {
            return nonNull(alertLevel) && (TRACE.getPriority()) <= (alertLevel.getPriority());
        }
    }
    ,
    DEBUGGING {
        @Override
        public boolean test(AlertLevel alertLevel) {
            return nonNull(alertLevel) && (DEBUG.getPriority()) <= (alertLevel.getPriority());
        }
    }
    ;

    public static DebugMode getByName(String name) {

        if (("debug").equalsIgnoreCase(name)) return (DEBUGGING);

        if (("trace").equalsIgnoreCase(name)) return (TRACING);

        return (OFF);

    }

    @Override
    public abstract boolean test(AlertLevel alertLevel);

}
