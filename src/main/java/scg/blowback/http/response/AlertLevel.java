package scg.blowback.http.response;

import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum AlertLevel {

    TRACE(0, "trace"), DEBUG(1, "debug"), INFO(2, "info"), WARNING(3, "warning"), ERROR(4, "error");

    @Getter
    private final int priority;
    private final String value;


    @Override
    @JsonValue
    public String toString() {
        return this.value;
    }

}
