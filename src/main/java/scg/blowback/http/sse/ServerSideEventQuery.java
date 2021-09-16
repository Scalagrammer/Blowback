package scg.blowback.http.sse;

import io.atlassian.fugue.Option;

public interface ServerSideEventQuery {
    Option<String> getEventId();
}
