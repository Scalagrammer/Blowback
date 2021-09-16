package scg.blowback.http.response;

import org.springframework.http.HttpStatus;

public abstract class HttpStatusException extends RuntimeException {

    public abstract HttpStatus getHttpStatus();

    @Override
    public final Throwable fillInStackTrace() {
        return this;
    }

    public static HttpStatusException httpStatusException(HttpStatus status) {
        return new HttpStatusException() {
            @Override
            public HttpStatus getHttpStatus() {
                return status;
            }
        };
    }

}
