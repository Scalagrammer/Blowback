package scg.blowback.http;

import java.util.UUID;
import java.util.function.Supplier;

@FunctionalInterface
public interface RqIdGenerator extends Supplier<UUID> {

    UUID next();

    @Override
    default UUID get() {
        return this.next();
    }

    static RqIdGenerator randomizer() {
        return UUID::randomUUID;
    }

}
