package dev.rosewood.roseminions.minion.setting;

import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SettingSerializers {

    public static final SettingSerializer<Boolean> BOOLEAN = new SettingSerializer<>() {
        public void write(CommentedFileConfiguration config, String key, Boolean value, String... comments) { config.set(key, value, comments); }
        public void write(ObjectOutputStream outputStream, Boolean value) throws IOException { outputStream.writeBoolean(value); }
        public Boolean read(CommentedFileConfiguration config, String key) { return config.getBoolean(key); }
        public Boolean read(ObjectInputStream inputStream) throws IOException { return inputStream.readBoolean(); }
    };

    public static final SettingSerializer<Integer> INTEGER = new SettingSerializer<>() {
        public void write(CommentedFileConfiguration config, String key, Integer value, String... comments) { config.set(key, value, comments); }
        public void write(ObjectOutputStream outputStream, Integer value) throws IOException { outputStream.writeInt(value); }
        public Integer read(CommentedFileConfiguration config, String key) { return config.getInt(key); }
        public Integer read(ObjectInputStream inputStream) throws IOException { return inputStream.readInt(); }
    };

    public static final SettingSerializer<Long> LONG = new SettingSerializer<>() {
        public void write(CommentedFileConfiguration config, String key, Long value, String... comments) { config.set(key, value, comments); }
        public void write(ObjectOutputStream outputStream, Long value) throws IOException { outputStream.writeLong(value); }
        public Long read(CommentedFileConfiguration config, String key) { return config.getLong(key); }
        public Long read(ObjectInputStream inputStream) throws IOException { return inputStream.readLong(); }
    };

    public static final SettingSerializer<Double> DOUBLE = new SettingSerializer<>() {
        public void write(CommentedFileConfiguration config, String key, Double value, String... comments) { config.set(key, value, comments); }
        public void write(ObjectOutputStream outputStream, Double value) throws IOException { outputStream.writeDouble(value); }
        public Double read(CommentedFileConfiguration config, String key) { return config.getDouble(key); }
        public Double read(ObjectInputStream inputStream) throws IOException { return inputStream.readDouble(); }
    };

    public static final SettingSerializer<String> STRING = new SettingSerializer<>() {
        public void write(CommentedFileConfiguration config, String key, String value, String... comments) { config.set(key, value, comments); }
        public void write(ObjectOutputStream outputStream, String value) throws IOException { outputStream.writeUTF(value); }
        public String read(CommentedFileConfiguration config, String key) { return config.getString(key); }
        public String read(ObjectInputStream inputStream) throws IOException { return inputStream.readUTF(); }
    };

}
