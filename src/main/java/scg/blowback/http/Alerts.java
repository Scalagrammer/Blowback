package scg.blowback.http;

import io.atlassian.fugue.Unit;
import lombok.val;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;

import reactor.util.context.ContextView;
import scg.blowback.http.response.Alert;
import scg.blowback.http.response.Alert.*;
import scg.blowback.http.response.AlertLevel;

import java.time.Clock;
import java.util.function.Function;
import java.lang.StackWalker.StackFrame;

import static java.lang.StackWalker.getInstance;
import static java.lang.StackWalker.Option.RETAIN_CLASS_REFERENCE;

import static java.util.Objects.nonNull;

import static org.springframework.http.HttpStatus.OK;
import static reactor.core.publisher.Mono.*;

import static scg.blowback.utils.Reactor.tap;
import static scg.blowback.utils.Reactor.unit;
import static scg.blowback.http.response.AlertLevel.*;
import static scg.blowback.http.response.Alert.builder;
import static scg.blowback.http.request.RequestContext.*;
import static scg.blowback.http.response.Alert.partiallyAlert;

public final class Alerts {


    private static final StackWalker stack = getInstance(RETAIN_CLASS_REFERENCE);


    private Alerts() {
        throw new UnsupportedOperationException();
    }


    public static <A> Function<A, Mono<A>> breakout(String message) {
        // take previous frame of current thread (invocation point)
        val frame = snapshotFrame(1);
        //
        return tap(arg -> publishAlert(DEBUG, delayBreakpointAlert(message, frame, arg)));
    }

    public static Mono<Unit> breakpoint(String message, Object...args) {
        // take previous frame of current thread (invocation point)
        val frame = snapshotFrame(1);
        //
        return publishAlert(DEBUG, delayBreakpointAlert(message, frame, args));
    }


    public static Mono<Unit> trace(HttpStatus status, String message, Object...args) {
        return trace(partiallyAlert(status, message, args).andThen(Builder::yieldTrace));
    }

    public static Mono<Unit> trace(HttpStatus status, String element, String message, Object...args) {
        return trace(partiallyAlert(status, element, message, args).andThen(Builder::yieldTrace));
    }

    public static Mono<Unit> trace(Function<Clock, TraceAlert> delayed) {
        return publishAlert(TRACE, delayed);
    }


    public static Mono<Unit> debug(Function<Clock, DebugAlert> delayed) {
        return publishAlert(DEBUG, delayed);
    }

    public static Mono<Unit> debug(HttpStatus status, String message, Object...args) {
        return debug(partiallyAlert(status, message, args).andThen(Builder::yieldDebug));
    }

    public static Mono<Unit> debug(HttpStatus status, String element, String message, Object...args) {
        return debug(partiallyAlert(status, element, message, args).andThen(Builder::yieldDebug));
    }


    public static Mono<Unit> info(Function<Clock, InfoAlert> delayed) {
        return publishAlert(INFO, delayed);
    }

    public static Mono<Unit> info(HttpStatus status, String message, Object...args) {
        return info(partiallyAlert(status, message, args).andThen(Builder::yieldInfo));
    }

    public static Mono<Unit> info(HttpStatus status, String element, String message, Object...args) {
        return info(partiallyAlert(status, element, message, args).andThen(Builder::yieldInfo));
    }


    public static Mono<Unit> warn(Function<Clock, WarningAlert> delayed) {
        return publishAlert(WARNING, delayed);
    }

    public static Mono<Unit> warn(HttpStatus status, String message, Object...args) {
        return warn(partiallyAlert(status, message, args).andThen(Builder::yieldWarning));
    }

    public static Mono<Unit> warn(HttpStatus status, String element, String message, Object...args) {
        return warn(partiallyAlert(status, element, message, args).andThen(Builder::yieldWarning));
    }


    public static <R> Mono<R> raise(Function<Clock, ErrorAlert> delayed) {
        return deferContextual(context -> error(delayed.andThen(AlertException::new).apply(getClock(context))));
    }

    public static <R> Mono<R> raise(HttpStatus status, String message, Object...args) {
        return raise(partiallyAlert(status, message, args).andThen(Builder::yieldError));
    }

    public static <R> Mono<R> raise(HttpStatus status, String element, String message, Object...args) {
        return raise(partiallyAlert(status, element, message, args).andThen(Builder::yieldError));
    }


    private static Mono<Unit> publishAlert(AlertLevel level, Function<Clock, ? extends Alert> delayed) {
        //
        Function<ContextView, Mono<Unit>> publishHook = context -> {
            //
            val alert = delayed.apply(getClock(context));
            //
            if (nonNull(alert)) {
                return alert.raiseOr(getAlerting(context)::push);
            }
            //
            return unit();
        };
        //
        return deferOnAlertLevel(level, publishHook);
    }

    private static StackFrame snapshotFrame(int depth) {
        // skipping current frame by +1
        return stack.walk(f -> f.skip(1 + depth).findFirst()).orElse(null);
    }

    private static Mono<Unit> deferOnAlertLevel(AlertLevel level, Function<ContextView, Mono<Unit>> hook) {
        return deferContextual(context -> getDebugMode(context).test(level) ? hook.apply(context) : unit());
    }

    private static Function<Clock, DebugAlert> delayBreakpointAlert(String message, StackFrame frame, Object... messageArgs) {
        return clock -> builder(clock).withStatus(OK).withMessage(message, messageArgs).withElement(String.valueOf(frame)).yieldDebug();
    }

}
