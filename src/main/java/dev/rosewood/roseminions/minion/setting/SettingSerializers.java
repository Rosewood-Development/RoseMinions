package dev.rosewood.roseminions.minion.setting;

import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SettingSerializers {

    public static final SettingSerializer<Boolean> BOOLEAN = new SettingSerializer<>() {
        public void write(CommentedFileConfiguration config, String key, Boolean value, String... comments) { config.set(key, value, comments); }
        public byte[] write(Boolean value) { return this.writeValue(value, ObjectOutputStream::writeBoolean); }
        public Boolean read(CommentedFileConfiguration config, String key) { return config.getBoolean(key); }
        public Boolean read(byte[] input) { return this.readValue(input, ObjectInputStream::readBoolean); }
    };

    public static final SettingSerializer<Integer> INTEGER = new SettingSerializer<>() {
        public void write(CommentedFileConfiguration config, String key, Integer value, String... comments) { config.set(key, value, comments); }
        public byte[] write(Integer value) { return this.writeValue(value, ObjectOutputStream::writeInt); }
        public Integer read(CommentedFileConfiguration config, String key) { return config.getInt(key); }
        public Integer read(byte[] input) { return this.readValue(input, ObjectInputStream::readInt);  }
    };

    public static final SettingSerializer<Long> LONG = new SettingSerializer<>() {
        public void write(CommentedFileConfiguration config, String key, Long value, String... comments) { config.set(key, value, comments); }
        public byte[] write(Long value) { return this.writeValue(value, ObjectOutputStream::writeLong); }
        public Long read(CommentedFileConfiguration config, String key) { return config.getLong(key); }
        public Long read(byte[] input) { return this.readValue(input, ObjectInputStream::readLong); }
    };

    public static final SettingSerializer<Double> DOUBLE = new SettingSerializer<>() {
        public void write(CommentedFileConfiguration config, String key, Double value, String... comments) { config.set(key, value, comments); }
        public byte[] write(Double value) { return this.writeValue(value, ObjectOutputStream::writeDouble); }
        public Double read(CommentedFileConfiguration config, String key) { return config.getDouble(key); }
        public Double read(byte[] input) { return this.readValue(input, ObjectInputStream::readDouble); }
    };

    public static final SettingSerializer<String> STRING = new SettingSerializer<>() {
        public void write(CommentedFileConfiguration config, String key, String value, String... comments) { config.set(key, value, comments); }
        public byte[] write(String value) { return this.writeValue(value, ObjectOutputStream::writeUTF); }
        public String read(CommentedFileConfiguration config, String key) { return config.getString(key); }
        public String read(byte[] input) { return this.readValue(input, ObjectInputStream::readUTF); }
    };

}
