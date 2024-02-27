package dev.rosewood.roseminions.model;

import dev.rosewood.roseminions.util.catching.CatchingConsumer;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Represents something that can be serialized and deserialized
 */
public interface DataSerializable {

    /**
     * Serializes this object to a byte array
     *
     * @return the byge array
     */
    byte[] serialize();

    /**
     * Deserializes the byte array into this object
     *
     * @param input the byte array
     */
    void deserialize(byte[] input);

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
            throw new DataSerializationException("An error occurred while reading from ObjectOutputStream", e);
        }
    }

    class DataSerializationException extends RuntimeException {

        public DataSerializationException(String message, Throwable cause) {
            super(message, cause);
        }

    }

}
