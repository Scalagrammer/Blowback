package scg.blowback.http.sse;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

import static java.util.Objects.nonNull;

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
public final class ServerSideEvent<P> {

    private final String        id;
    private final P        payload;
    private final String qualifier;

    public boolean hasQualifier() {
        return nonNull(qualifier);
    }

}