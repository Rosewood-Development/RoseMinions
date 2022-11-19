package dev.rosewood.roseminions.minion.setting;

import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.roseminions.model.DataSerializable;
import dev.rosewood.roseminions.util.catching.CatchingBiConsumer;
import dev.rosewood.roseminions.util.catching.CatchingFunction;
import java.util.concurrent.atomic.AtomicReference;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

public interface SettingSerializer<T> {

    void write(CommentedFileConfiguration config, String key, T value, String... comments);

    byte[] write(T value);

    T read(ConfigurationSection config, String key);

    T read(byte[] input);

    default T readValue(byte[] input, CatchingFunction<BukkitObjectInputStream, T> function) {
        AtomicReference<T> value = new AtomicReference<>();
        DataSerializable.read(input, x -> value.set(function.apply(x)));
        return value.get();
    }

    default byte[] writeValue(T value, CatchingBiConsumer<BukkitObjectOutputStream, T> consumer) {
        return DataSerializable.write(x -> consumer.accept(x, value));
    }

}
