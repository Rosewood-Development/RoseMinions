package dev.rosewood.roseminions.minion.setting;

import dev.rosewood.rosegarden.config.CommentedConfigurationSection;
import dev.rosewood.roseminions.model.DataSerializable;
import dev.rosewood.roseminions.util.MinionUtils;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;

@SuppressWarnings("unchecked")
public final class SettingSerializerFactories {

    private static final Map<String, SettingSerializer<?>> DYNAMIC_SERIALIZERS = new HashMap<>();

    private SettingSerializerFactories() { }

    //region Serializer Factories
    public static <T extends Enum<T>> SettingSerializer<T> ofEnum(Class<T> enumClass) {
        String key = enumClass.getName();
        if (DYNAMIC_SERIALIZERS.containsKey(key))
            return (SettingSerializer<T>) DYNAMIC_SERIALIZERS.get(key);

        SettingSerializer<T> serializer = new SettingSerializer<>(enumClass, Enum::name, x -> Enum.valueOf(enumClass, x)) {
            public void write(CommentedConfigurationSection config, String key, T value, String... comments) { config.set(key, value.name(), comments); }
            public byte[] write(T value) { return DataSerializable.write(x -> x.writeUTF(value.name())); }
            public T read(ConfigurationSection config, String key) { return Enum.valueOf(enumClass, config.getString(key, "")); }
            public T read(byte[] input) {
                AtomicReference<T> value = new AtomicReference<>();
                DataSerializable.read(input, x -> value.set(Enum.valueOf(enumClass, x.readUTF())));
                return value.get();
            }
        };

        DYNAMIC_SERIALIZERS.put(key, serializer);
        return serializer;
    }

    public static <T extends Keyed> SettingSerializer<T> ofKeyed(Class<T> keyedClass, Function<NamespacedKey, T> valueOfFunction) {
        String key = keyedClass.getName();
        if (DYNAMIC_SERIALIZERS.containsKey(key))
            return (SettingSerializer<T>) DYNAMIC_SERIALIZERS.get(key);

        SettingSerializer<T> serializer = new SettingSerializer<>(keyedClass, x -> translateName(x.getKey()), x -> valueOfFunction.apply(translateKey(x))) {
            public void write(CommentedConfigurationSection config, String key, T value, String... comments) { config.set(key, translateName(value.getKey()), comments); }
            public byte[] write(T value) { return DataSerializable.write(x -> x.writeUTF(translateName(value.getKey()))); }
            public T read(ConfigurationSection config, String key) { return valueOfFunction.apply(translateKey(config.getString(key))); }
            public T read(byte[] input) {
                AtomicReference<T> value = new AtomicReference<>();
                DataSerializable.read(input, x -> value.set(valueOfFunction.apply(translateKey(x.readUTF()))));
                return value.get();
            }
        };

        DYNAMIC_SERIALIZERS.put(key, serializer);
        return serializer;
    }

    public static <T> SettingSerializer<T[]> ofArray(SettingSerializer<T> serializer) {
        String key = serializer.getTypeName() + "[]";
        if (DYNAMIC_SERIALIZERS.containsKey(key))
            return (SettingSerializer<T[]>) DYNAMIC_SERIALIZERS.get(key);

        SettingSerializer<T[]> arraySerializer = new SettingSerializer<>() {
            public void write(CommentedConfigurationSection config, String key, T[] value, String... comments) {
                if (serializer.isStringificationAllowed()) {
                    config.set(key, Arrays.stream(value).map(x -> x == null ? "" : serializer.stringify(x)).toList(), comments);
                } else {
                    CommentedConfigurationSection section = getOrCreateSection(config, key, comments);
                    int index = 0;
                    for (T t : value) {
                        if (t == null) {
                            section.set(String.valueOf(index++), "");
                        } else {
                            serializer.write(section, String.valueOf(index++), t);
                        }
                    }
                }
            }
            public byte[] write(T[] value) {
                return DataSerializable.write(x -> {
                    x.writeInt(value.length);
                    for (T t : value) {
                        x.writeBoolean(t != null);
                        if (t != null) {
                            byte[] data = serializer.write(t);
                            x.writeInt(data.length);
                            x.write(data);
                        }
                    }
                });
            }
            public T[] read(ConfigurationSection config, String key) {
                if (serializer.isStringificationAllowed()) {
                    List<String> contents = config.getStringList(key);
                    T[] array = (T[]) Array.newInstance(serializer.type, contents.size());
                    for (int i = 0; i < contents.size(); i++) {
                        String content = contents.get(i);
                        if (!content.isEmpty())
                            array[i] = serializer.parseString(content);
                    }
                    return array;
                } else {
                    ConfigurationSection section = config.getConfigurationSection(key);
                    if (section != null) {
                        List<String> contents = config.getStringList(key);
                        T[] array = (T[]) Array.newInstance(serializer.type, contents.size());
                        for (String configKey : section.getKeys(false)) {
                            int index = Integer.parseInt(configKey);
                            String content = section.getString(configKey, "");
                            if (!content.isEmpty())
                                array[index] = serializer.read(section, configKey);
                        }
                    }
                    return (T[]) Array.newInstance(serializer.type, 0);
                }
            }
            public T[] read(byte[] input) {
                AtomicReference<T[]> value = new AtomicReference<>();
                DataSerializable.read(input, x -> {
                    T[] array = (T[]) Array.newInstance(serializer.type, x.readInt());
                    for (int i = 0; i < array.length; i++) {
                        if (x.readBoolean()) {
                            byte[] data = new byte[x.readInt()];
                            x.readFully(data);
                            array[i] = serializer.read(data);
                        }
                    }
                    value.set(array);
                });
                return value.get();
            }
            public String getTypeName() {
                return key;
            }
        };

        DYNAMIC_SERIALIZERS.put(key, arraySerializer);
        return arraySerializer;
    }

    public static <T> SettingSerializer<List<T>> ofList(SettingSerializer<T> serializer) {
        String key = "List<" + serializer.getTypeName() + ">";
        if (DYNAMIC_SERIALIZERS.containsKey(key))
            return (SettingSerializer<List<T>>) DYNAMIC_SERIALIZERS.get(key);

        SettingSerializer<List<T>> listSerializer = new SettingSerializer<>() {
            public void write(CommentedConfigurationSection config, String key, List<T> value, String... comments) {
                if (serializer.isStringificationAllowed()) {
                    config.set(key, value.stream().map(serializer::stringify).toList(), comments);
                } else {
                    CommentedConfigurationSection section = getOrCreateSection(config, key, comments);
                    int index = 0;
                    for (T t : value)
                        serializer.write(section, String.valueOf(index++), t);
                }
            }
            public byte[] write(List<T> value) {
                return DataSerializable.write(x -> {
                    x.writeInt(value.size());
                    for (T t : value) {
                        byte[] data = serializer.write(t);
                        x.writeInt(data.length);
                        x.write(data);
                    }
                });
            }
            public List<T> read(ConfigurationSection config, String key) {
                if (serializer.isStringificationAllowed()) {
                    return config.getStringList(key).stream().map(serializer::parseString).toList();
                } else {
                    List<T> list = new ArrayList<>();
                    ConfigurationSection section = config.getConfigurationSection(key);
                    if (section != null) {
                        for (String configKey : section.getKeys(false)) {
                            T t = serializer.read(section, configKey);
                            if (t != null)
                                list.add(t);
                        }
                    }
                    return list;
                }
            }
            public List<T> read(byte[] input) {
                List<T> list = new ArrayList<>();
                DataSerializable.read(input, x -> {
                    int size = x.readInt();
                    for (int i = 0; i < size; i++) {
                        int dataLength = x.readInt();
                        byte[] data = new byte[dataLength];
                        x.readFully(data);
                        list.add(serializer.read(data));
                    }
                });
                return list;
            }
            public String getTypeName() {
                return key;
            }
        };

        DYNAMIC_SERIALIZERS.put(key, listSerializer);
        return listSerializer;
    }

    public static <K, V> SettingSerializer<Map<K, V>> ofMap(SettingSerializer<K> keySerializer, SettingSerializer<V> valueSerializer) {
        String key = "Map<" + keySerializer.getTypeName() + "," + valueSerializer.getTypeName() + ">";
        if (DYNAMIC_SERIALIZERS.containsKey(key))
            return (SettingSerializer<Map<K, V>>) DYNAMIC_SERIALIZERS.get(key);

        SettingSerializer<Map<K, V>> mapSerializer = new SettingSerializer<>() {
            public void write(CommentedConfigurationSection config, String key, Map<K, V> value, String... comments) {
                CommentedConfigurationSection section = getOrCreateSection(config, key, comments);
                if (keySerializer.isStringificationAllowed() && valueSerializer.isStringificationAllowed()) {
                    for (Map.Entry<K, V> entry : value.entrySet())
                        section.set(keySerializer.stringify(entry.getKey()), valueSerializer.stringify(entry.getValue()));
                } else {
                    int index = 0;
                    for (Map.Entry<K, V> entry : value.entrySet()) {
                        CommentedConfigurationSection indexedSection = getOrCreateSection(section, String.valueOf(index++));
                        keySerializer.write(indexedSection, "key", entry.getKey());
                        valueSerializer.write(indexedSection, "value", entry.getValue());
                    }
                }
            }
            public byte[] write(Map<K, V> value) {
                return DataSerializable.write(x -> {
                    x.writeInt(value.size());
                    for (Map.Entry<K, V> entry : value.entrySet()) {
                        byte[] keyData = keySerializer.write(entry.getKey());
                        x.writeInt(keyData.length);
                        x.write(keyData);
                        byte[] valueData = valueSerializer.write(entry.getValue());
                        x.writeInt(valueData.length);
                        x.write(valueData);
                    }
                });
            }
            public Map<K, V> read(ConfigurationSection config, String key) {
                Map<K, V> map = new HashMap<>();
                ConfigurationSection section = config.getConfigurationSection(key);
                if (section != null) {
                    if (keySerializer.isStringificationAllowed() && valueSerializer.isStringificationAllowed()) {
                        for (String configKey : section.getKeys(false)) {
                            K k = keySerializer.parseString(configKey);
                            V v = valueSerializer.parseString(section.getString(configKey, ""));
                            if (k != null && v != null)
                                map.put(k, v);
                        }
                    } else {
                        for (String configKey : section.getKeys(false)) {
                            ConfigurationSection indexedSection = section.getConfigurationSection(configKey);
                            if (indexedSection != null) {
                                K k = keySerializer.read(indexedSection, "key");
                                V v = valueSerializer.read(indexedSection, "value");
                                if (k != null && v != null)
                                    map.put(k, v);
                            }
                        }
                    }
                }
                return map;
            }
            public Map<K, V> read(byte[] input) {
                Map<K, V> map = new HashMap<>();
                DataSerializable.read(input, x -> {
                    int size = x.readInt();
                    for (int i = 0; i < size; i++) {
                        byte[] keyData = new byte[x.readInt()];
                        x.readFully(keyData);
                        byte[] valueData = new byte[x.readInt()];
                        x.readFully(valueData);
                        map.put(keySerializer.read(keyData), valueSerializer.read(valueData));
                    }
                });
                return map;
            }
            public String getTypeName() {
                return key;
            }
        };

        DYNAMIC_SERIALIZERS.put(key, mapSerializer);
        return mapSerializer;
    }

    public static <T> SettingSerializer<T> ofComplex(Class<T> clazz,
                                                     Consumer<ComplexSettingWriter> writerConsumer,
                                                     Function<T, Map<String, Object>> toMapFunction,
                                                     Function<Map<String, Object>, T> fromMapFunction) {
        String key = "Complex:" + clazz.getName();
        if (DYNAMIC_SERIALIZERS.containsKey(key))
            return (SettingSerializer<T>) DYNAMIC_SERIALIZERS.get(key);

        ComplexSettingWriter builder = new ComplexSettingWriter();
        writerConsumer.accept(builder);
        List<ComplexSettingProperty<?>> properties = builder.getProperties();

        SettingSerializer<T> serializer = new SettingSerializer<>(clazz) {
            @Override
            public void write(CommentedConfigurationSection config, String key, T value, String... comments) {
                config.set(key, null, comments);
                Map<String, Object> data = toMapFunction.apply(value);
                for (ComplexSettingProperty<?> property : properties) {
                    SettingSerializer<?> serializer = property.serializer();
                    if (serializer.isStringificationAllowed()) {
                        config.set(key + "." + property.name(), serializer.stringify(MinionUtils.forceCast(data.get(property.name()))), property.comments());
                    } else {
                        serializer.write(config, key + "." + property.name(), MinionUtils.forceCast(data.get(property.name())), property.comments());
                    }
                }
            }
            @Override
            public byte[] write(T value) {
                return DataSerializable.write(x -> {
                    Map<String, Object> data = toMapFunction.apply(value);
                    for (ComplexSettingProperty<?> property : properties) {
                        byte[] keyData = property.serializer().write(MinionUtils.forceCast(data.get(property.name())));
                        x.writeInt(keyData.length);
                        x.write(keyData);
                    }
                });
            }
            @Override
            public T read(ConfigurationSection config, String key) {
                Map<String, Object> values = new HashMap<>();
                for (ComplexSettingProperty<?> property : properties) {
                    SettingSerializer<?> serializer = property.serializer();
                    if (serializer.isStringificationAllowed()) {
                        values.put(property.name(), serializer.parseString(config.getString(key + "." + property.name(), "")));
                    } else {
                        values.put(property.name(), serializer.read(config, key + "." + property.name()));
                    }
                }
                return fromMapFunction.apply(values);
            }
            @Override
            public T read(byte[] input) {
                AtomicReference<T> value = new AtomicReference<>();
                DataSerializable.read(input, x -> {
                    Map<String, Object> values = new HashMap<>();
                    for (ComplexSettingProperty<?> property : properties) {
                        byte[] keyData = new byte[x.readInt()];
                        x.readFully(keyData);
                        values.put(property.name(), property.serializer().read(keyData));
                    }
                    value.set(fromMapFunction.apply(values));
                });
                return value.get();
            }
            @Override
            public String getTypeName() {
                return key;
            }
        };

        DYNAMIC_SERIALIZERS.put(key, serializer);
        return serializer;
    }
    //endregion

    private static CommentedConfigurationSection getOrCreateSection(CommentedConfigurationSection config, String key, String... comments) {
        CommentedConfigurationSection section = config.getConfigurationSection(key);
        if (section == null) {
            config.addPathedComments(key, comments);
            section = config.createSection(key);
        }
        return section;
    }

    private static NamespacedKey translateKey(String key) {
        return NamespacedKey.fromString(key);
    }

    private static String translateName(NamespacedKey key) {
        if (key.getNamespace().equals(NamespacedKey.MINECRAFT))
            return key.getKey();
        return key.toString();
    }

    public static class ComplexSettingWriter {

        private final List<ComplexSettingProperty<?>> properties;

        private ComplexSettingWriter() {
            this.properties = new ArrayList<>();
        }

        public <T> void withProperty(String name, SettingSerializer<T> keySerializer, String... comments) {
            this.properties.add(new ComplexSettingProperty<>(name, keySerializer, comments));
        }

        private List<ComplexSettingProperty<?>> getProperties() {
            return this.properties;
        }

    }

    private record ComplexSettingProperty<T>(String name,
                                             SettingSerializer<T> serializer,
                                             String[] comments) { }

}
