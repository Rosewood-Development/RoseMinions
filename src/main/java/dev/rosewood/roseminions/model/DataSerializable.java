package dev.rosewood.roseminions.model;

import dev.rosewood.roseminions.util.catching.CatchingConsumer;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public interface DataSerializable {

    /**
     * Serializes this object to a byte array
     *
     * @return the byge array
     */
    byte[] serialize();

    /**
     * Deserializes this object from a byte array
     *
     * @param input the byte array
     */
    void deserialize(byte[] input);

    static byte[] write(CatchingConsumer<ObjectOutputStream> consumer) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream outputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            consumer.accept(outputStream);
            outputStream.flush();
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            throw new DataSerializationException("An error occurred while writing to ObjectOutputStream", e);
        }
    }

    static void read(byte[] input, CatchingConsumer<ObjectInputStream> consumer) {
        try (ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(input))) {
            consumer.accept(inputStream);
        } catch (Exception e) {
            throw new DataSerializationException("An error occurred while reading from ObjectOutputStream", e);
        }
    }

    class DataSerializationException extends RuntimeException {

        public DataSerializationException(String message, Throwable cause) {
            super(message, cause);
        }

    }

}
