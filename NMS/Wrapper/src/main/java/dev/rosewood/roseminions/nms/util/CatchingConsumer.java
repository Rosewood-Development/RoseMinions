package dev.rosewood.roseminions.nms.util;

public interface CatchingConsumer<T> {

    void accept(T element) throws Exception;

}
