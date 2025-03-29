package dev.rosewood.roseminions.minion.setting;

import dev.rosewood.rosegarden.config.CommentedConfigurationSection;
import dev.rosewood.rosegarden.utils.NMSUtil;
import dev.rosewood.roseminions.datatype.CustomPersistentDataType;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import static dev.rosewood.roseminions.minion.setting.SettingSerializers.setWithComments;

@SuppressWarnings("unchecked")
public final class SettingSerializerFactories {

    private SettingSerializerFactories() { }

    //region Serializer Factories
    public static <T extends Enum<T>> SettingSerializer<T> ofEnum(Class<T> enumClass) {
        return new SettingSerializer<>(enumClass, CustomPersistentDataType.forEnum(enumClass), Enum::name, x -> Enum.valueOf(enumClass, x)) {
            public void write(ConfigurationSection config, String key, T value, String... comments) { setWithComments(config, key, value.name(), comments); }
            public T read(ConfigurationSection config, String key) { return Enum.valueOf(enumClass, config.getString(key, "")); }
        };
    }

    public static <T extends Keyed> SettingSerializer<T> ofKeyed(Class<T> keyedClass, Function<NamespacedKey, T> valueOfFunction) {
        return new SettingSerializer<>(keyedClass, CustomPersistentDataType.forKeyed(keyedClass, valueOfFunction), x -> translateName(x.getKey()), x -> valueOfFunction.apply(translateKey(x))) {
            public void write(ConfigurationSection config, String key, T value, String... comments) { setWithComments(config, key, translateName(value.getKey()), comments); }
            public T read(ConfigurationSection config, String key) { return valueOfFunction.apply(translateKey(config.getString(key))); }
        };
    }

    public static <T> SettingSerializer<T[]> ofArray(SettingSerializer<T> serializer) {
        return new SettingSerializer<>(CustomPersistentDataType.forArray(serializer.persistentDataType)) {
            public void write(ConfigurationSection config, String key, T[] value, String... comments) {
                if (serializer.isStringKey()) {
                    setWithComments(config, key, Arrays.stream(value).map(x -> x == null ? "" : serializer.asStringKey(x)).toList(), comments);
                } else {
                    ConfigurationSection section = getOrCreateSection(config, key, comments);
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
        return new SettingSerializer<>(CustomPersistentDataType.forList(serializer.persistentDataType)) {
            public void write(ConfigurationSection config, String key, List<T> value, String... comments) {
                if (serializer.isStringKey()) {
                    setWithComments(config, key, value.stream().map(serializer::asStringKey).toList(), comments);
                } else {
                    ConfigurationSection section = getOrCreateSection(config, key, comments);
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
        return new SettingSerializer<>(CustomPersistentDataType.forMap(keySerializer.persistentDataType, valueSerializer.persistentDataType)) {
            public void write(ConfigurationSection config, String key, Map<K, V> value, String... comments) {
                ConfigurationSection section = getOrCreateSection(config, key, comments);
                if (keySerializer.isStringKey() && valueSerializer.isStringKey()) {
                    for (Map.Entry<K, V> entry : value.entrySet())
                        section.set(keySerializer.asStringKey(entry.getKey()), valueSerializer.asStringKey(entry.getValue()));
                } else {
                    int index = 0;
                    for (Map.Entry<K, V> entry : value.entrySet()) {
                        ConfigurationSection indexedSection = getOrCreateSection(section, String.valueOf(index++));
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
    //endregion

    static ConfigurationSection getOrCreateSection(ConfigurationSection config, String key, String... comments) {
        if (config instanceof CommentedConfigurationSection commentedConfig) {
            CommentedConfigurationSection section = commentedConfig.getConfigurationSection(key);
            if (section == null) {
                commentedConfig.addPathedComments(key, comments);
                section = commentedConfig.createSection(key);
            }
            return section;
        } else {
            ConfigurationSection section = config.getConfigurationSection(key);
            if (section == null) {
                section = config.createSection(key);
                if (NMSUtil.getVersionNumber() > 18 || (NMSUtil.getVersionNumber() == 18 && NMSUtil.getMinorVersionNumber() >= 1))
                    config.setComments(key, Arrays.asList(comments));
            }
            return section;
        }
    }

    private static NamespacedKey translateKey(String key) {
        return NamespacedKey.fromString(key);
    }

    private static String translateName(NamespacedKey key) {
        if (key.getNamespace().equals(NamespacedKey.MINECRAFT))
            return key.getKey();
        return key.toString();
    }

}
