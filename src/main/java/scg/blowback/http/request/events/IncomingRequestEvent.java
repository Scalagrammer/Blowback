package scg.blowback.http.request.events;

import lombok.EqualsAndHashCode;
import scg.blowback.http.HttpRouting;

@EqualsAndHashCode(callSuper = true)
public final class IncomingRequestEvent extends HttpEvent {
    public IncomingRequestEvent(HttpRouting source) {
        super(source);
    }
}
