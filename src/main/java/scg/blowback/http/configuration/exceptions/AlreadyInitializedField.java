package scg.blowback.http.configuration.exceptions;

import org.springframework.beans.factory.BeanInitializationException;

import static java.lang.String.format;

public class AlreadyInitializedField extends BeanInitializationException {

    public AlreadyInitializedField(String fieldName) {
        super(format("field already initialized: %s", fieldName));
    }

}
