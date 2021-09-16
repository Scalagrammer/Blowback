package scg.blowback.http.request.events;

import org.springframework.context.ApplicationEvent;

import scg.blowback.http.HttpRouting;

public abstract class HttpEvent extends ApplicationEvent {

    public HttpEvent(HttpRouting source) {
        super(source);
    }

    @Override
    public HttpRouting getSource() {
        return (HttpRouting) super.getSource();
    }

}
