package dev.rosewood.roseminions.util.catching;

public interface CatchingConsumer<T> {

    void accept(T element) throws Exception;

}
