package dev.rosewood.roseminions.util.catching;

public interface CatchingFunction<T, R> {

    R apply(T element) throws Exception;

}
