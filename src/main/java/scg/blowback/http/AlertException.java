package scg.blowback.http;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import scg.blowback.http.response.Alert.ErrorAlert;

@Getter
@RequiredArgsConstructor
public class AlertException extends RuntimeException {

    private final ErrorAlert alert;

    @Override
    public Throwable fillInStackTrace() {
        return this;
    }

}
