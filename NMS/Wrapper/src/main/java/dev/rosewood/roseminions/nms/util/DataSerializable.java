package dev.rosewood.roseminions.nms.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Utility class for reading/writing from Object data streams
 */
public interface DataSerializable {

    static byte[] write(CatchingConsumer<ObjectOutputStream> consumer) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             ObjectOutputStream dataOutput = new ObjectOutputStream(outputStream)) {
            consumer.accept(dataOutput);
            dataOutput.close();
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new DataSerializationException("An error occurred while writing to ObjectOutputStream", e);
        }
    }

    static void read(byte[] input, CatchingConsumer<ObjectInputStream> consumer) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(input);
             ObjectInputStream dataInput = new ObjectInputStream(inputStream)) {
            consumer.accept(dataInput);
        } catch (Exception e) {
            throw new DataSerializationException("An error occurred while reading from ObjectInputStream", e);
        }
    }

    class DataSerializationException extends RuntimeException {

        public DataSerializationException(String message, Throwable cause) {
            super(message, cause);
        }

    }

}
