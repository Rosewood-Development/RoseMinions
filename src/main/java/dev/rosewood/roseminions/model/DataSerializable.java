package dev.rosewood.roseminions.model;

import dev.rosewood.roseminions.util.catching.CatchingConsumer;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

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

    static byte[] write(CatchingConsumer<BukkitObjectOutputStream> consumer) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             BukkitObjectOutputStream outputStream = new BukkitObjectOutputStream(byteArrayOutputStream)) {
            consumer.accept(outputStream);
            outputStream.flush();
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            throw new DataSerializationException("An error occurred while writing to ObjectOutputStream", e);
        }
    }

    static void read(byte[] input, CatchingConsumer<BukkitObjectInputStream> consumer) {
        try (BukkitObjectInputStream inputStream = new BukkitObjectInputStream(new ByteArrayInputStream(input))) {
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
