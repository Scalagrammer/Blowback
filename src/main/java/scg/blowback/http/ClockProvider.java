package scg.blowback.http;

import java.time.Clock;

@FunctionalInterface
public interface ClockProvider {
    Clock getClock();
}
