package dev.rosewood.roseminions.util.catching;

public interface CatchingBiConsumer<T, U> {

    void accept(T element, U element2) throws Exception;

}
