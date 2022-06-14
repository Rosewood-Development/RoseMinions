package dev.rosewood.roseminions.minion.setting;

import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public interface SettingSerializer<T> {

    void write(CommentedFileConfiguration config, String key, T value, String... comments);

    void write(ObjectOutputStream outputStream, T value) throws IOException;

    T read(CommentedFileConfiguration config, String key);

    T read(ObjectInputStream inputStream) throws IOException;

}
