package dev.rosewood.roseminions.minion.setting;

import dev.rosewood.rosegarden.config.CommentedConfigurationSection;
import dev.rosewood.roseminions.datatype.CustomPersistentDataType;
import dev.rosewood.roseminions.util.MinionUtils;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.persistence.PersistentDataType;

@SuppressWarnings("unchecked")
public final class SettingSerializerFactories {

    private SettingSerializerFactories() { }

    //region Serializer Factories
    public static <T extends Enum<T>> SettingSerializer<T> ofEnum(Class<T> enumClass) {
        return new SettingSerializer<>(enumClass, CustomPersistentDataType.forEnum(enumClass), Enum::name, x -> Enum.valueOf(enumClass, x)) {
            public void write(CommentedConfigurationSection config, String key, T value, String... comments) { config.set(key, value.name(), comments); }
            public T read(ConfigurationSection config, String key) { return Enum.valueOf(enumClass, config.getString(key, "")); }
        };
    }

    public static <T extends Keyed> SettingSerializer<T> ofKeyed(Class<T> keyedClass, Function<NamespacedKey, T> valueOfFunction) {
        return new SettingSerializer<>(keyedClass, CustomPersistentDataType.forKeyed(keyedClass, valueOfFunction), x -> translateName(x.getKey()), x -> valueOfFunction.apply(translateKey(x))) {
            public void write(CommentedConfigurationSection config, String key, T value, String... comments) { config.set(key, translateName(value.getKey()), comments); }
            public T read(ConfigurationSection config, String key) { return valueOfFunction.apply(translateKey(config.getString(key))); }
        };
    }

    public static <T> SettingSerializer<T[]> ofArray(SettingSerializer<T> serializer) {
        return new SettingSerializer<>(CustomPersistentDataType.forArray(serializer.getPersistentDataType())) {
            public void write(CommentedConfigurationSection config, String key, T[] value, String... comments) {
                if (serializer.isStringKey()) {
                    config.set(key, Arrays.stream(value).map(x -> x == null ? "" : serializer.asStringKey(x)).toList(), comments);
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
            public T[] read(ConfigurationSection config, String key) {
                if (serializer.isStringKey()) {
                    List<String> contents = config.getStringList(key);
                    T[] array = (T[]) Array.newInstance(serializer.type, contents.size());
                    for (int i = 0; i < contents.size(); i++) {
                        String content = contents.get(i);
                        if (!content.isEmpty())
                            array[i] = serializer.fromStringKey(content);
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
        };
    }

    public static <T> SettingSerializer<List<T>> ofList(SettingSerializer<T> serializer) {
        return new SettingSerializer<>(CustomPersistentDataType.forList(serializer.getPersistentDataType())) {
            public void write(CommentedConfigurationSection config, String key, List<T> value, String... comments) {
                if (serializer.isStringKey()) {
                    config.set(key, value.stream().map(serializer::asStringKey).toList(), comments);
                } else {
                    CommentedConfigurationSection section = getOrCreateSection(config, key, comments);
                    int index = 0;
                    for (T t : value)
                        serializer.write(section, String.valueOf(index++), t);
                }
            }
            public List<T> read(ConfigurationSection config, String key) {
                if (serializer.isStringKey()) {
                    return config.getStringList(key).stream().map(serializer::fromStringKey).toList();
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
        };
    }

    public static <K, V> SettingSerializer<Map<K, V>> ofMap(SettingSerializer<K> keySerializer, SettingSerializer<V> valueSerializer) {
        return new SettingSerializer<>(CustomPersistentDataType.forMap(keySerializer.getPersistentDataType(), valueSerializer.getPersistentDataType())) {
            public void write(CommentedConfigurationSection config, String key, Map<K, V> value, String... comments) {
                CommentedConfigurationSection section = getOrCreateSection(config, key, comments);
                if (keySerializer.isStringKey() && valueSerializer.isStringKey()) {
                    for (Map.Entry<K, V> entry : value.entrySet())
                        section.set(keySerializer.asStringKey(entry.getKey()), valueSerializer.asStringKey(entry.getValue()));
                } else {
                    int index = 0;
                    for (Map.Entry<K, V> entry : value.entrySet()) {
                        CommentedConfigurationSection indexedSection = getOrCreateSection(section, String.valueOf(index++));
                        keySerializer.write(indexedSection, "key", entry.getKey());
                        valueSerializer.write(indexedSection, "value", entry.getValue());
                    }
                }
            }
            public Map<K, V> read(ConfigurationSection config, String key) {
                Map<K, V> map = new HashMap<>();
                ConfigurationSection section = config.getConfigurationSection(key);
                if (section != null) {
                    if (keySerializer.isStringKey() && valueSerializer.isStringKey()) {
                        for (String configKey : section.getKeys(false)) {
                            K k = keySerializer.fromStringKey(configKey);
                            V v = valueSerializer.fromStringKey(section.getString(configKey, ""));
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
        };
    }

    public static <T> SettingSerializer<T> ofComplex(Class<T> clazz,
                                                     PersistentDataType<?, T> persistentDataType,
                                                     Consumer<ComplexSettingWriter> writerConsumer,
                                                     Function<T, Map<String, Object>> toMapFunction,
                                                     Function<Map<String, Object>, T> fromMapFunction) {
        ComplexSettingWriter builder = new ComplexSettingWriter();
        writerConsumer.accept(builder);
        List<ComplexSettingProperty<?>> properties = builder.getProperties();

        return new SettingSerializer<>(clazz, persistentDataType) {
            @Override
            public void write(CommentedConfigurationSection config, String key, T value, String... comments) {
                config.set(key, null, comments);
                Map<String, Object> data = toMapFunction.apply(value);
                for (ComplexSettingProperty<?> property : properties) {
                    SettingSerializer<?> propertySerializer = property.serializer();
                    if (propertySerializer.isStringKey()) {
                        config.set(key + "." + property.name(), propertySerializer.asStringKey(MinionUtils.forceCast(data.get(property.name()))), property.comments());
                    } else {
                        propertySerializer.write(config, key + "." + property.name(), MinionUtils.forceCast(data.get(property.name())), property.comments());
                    }
                }
            }
            @Override
            public T read(ConfigurationSection config, String key) {
                Map<String, Object> values = new HashMap<>();
                for (ComplexSettingProperty<?> property : properties) {
                    SettingSerializer<?> propertySerializer = property.serializer();
                    if (propertySerializer.isStringKey()) {
                        values.put(property.name(), propertySerializer.fromStringKey(config.getString(key + "." + property.name(), "")));
                    } else {
                        values.put(property.name(), propertySerializer.read(config, key + "." + property.name()));
                    }
                }
                return fromMapFunction.apply(values);
            }
        };
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
