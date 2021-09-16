package scg.blowback.utils.functions;

import lombok.SneakyThrows;

@FunctionalInterface
public interface Action extends Runnable {

    void perform() throws Throwable;

    @Override
    @SneakyThrows
    default void run() {
        this.perform();
    }

}
