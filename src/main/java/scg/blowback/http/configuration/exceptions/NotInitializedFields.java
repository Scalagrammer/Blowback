package scg.blowback.http.configuration.exceptions;

import org.springframework.beans.factory.BeanInitializationException;

import java.util.Arrays;

import static java.lang.String.format;

public class NotInitializedFields extends BeanInitializationException {

    public NotInitializedFields(String[] fieldNames) {
        super(format("not initialized fields: %s", Arrays.toString(fieldNames)));
    }

}
