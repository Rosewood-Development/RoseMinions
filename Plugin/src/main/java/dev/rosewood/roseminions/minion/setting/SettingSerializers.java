package dev.rosewood.roseminions.minion.setting;

import dev.rosewood.rosegarden.config.CommentedConfigurationSection;
import dev.rosewood.roseminions.model.DataSerializable;
import dev.rosewood.roseminions.nms.NMSAdapter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

@SuppressWarnings("unchecked")
public final class SettingSerializers {

    private static final Map<String, SettingSerializer<?>> DYNAMIC_SERIALIZERS = new HashMap<>();

    private SettingSerializers() { }

    //region Primitive Serializers
    public static final SettingSerializer<Boolean> BOOLEAN = new SettingSerializer<>(Boolean.class) {
        public void write(CommentedConfigurationSection config, String key, Boolean value, String... comments) { config.set(key, value, comments); }
        public byte[] write(Boolean value) { return this.writeValue(value, ObjectOutputStream::writeBoolean); }
        public Boolean read(ConfigurationSection config, String key) { return config.getBoolean(key); }
        public Boolean read(byte[] input) { return this.readValue(input, ObjectInputStream::readBoolean); }
        public String stringify(Boolean value) { return value.toString(); }
        public Boolean parseString(String value) { return Boolean.parseBoolean(value); }
    };

    public static final SettingSerializer<Integer> INTEGER = new SettingSerializer<>(Integer.class) {
        public void write(CommentedConfigurationSection config, String key, Integer value, String... comments) { config.set(key, value, comments); }
        public byte[] write(Integer value) { return this.writeValue(value, ObjectOutputStream::writeInt); }
        public Integer read(ConfigurationSection config, String key) { return config.getInt(key); }
        public Integer read(byte[] input) { return this.readValue(input, ObjectInputStream::readInt); }
        public String stringify(Integer value) { return value.toString(); }
        public Integer parseString(String value) { return Integer.parseInt(value); }
    };

    public static final SettingSerializer<Long> LONG = new SettingSerializer<>(Long.class) {
        public void write(CommentedConfigurationSection config, String key, Long value, String... comments) { config.set(key, value, comments); }
        public byte[] write(Long value) { return this.writeValue(value, ObjectOutputStream::writeLong); }
        public Long read(ConfigurationSection config, String key) { return config.getLong(key); }
        public Long read(byte[] input) { return this.readValue(input, ObjectInputStream::readLong); }
        public String stringify(Long value) { return value.toString(); }
        public Long parseString(String value) { return Long.parseLong(value); }
    };

    public static final SettingSerializer<Short> SHORT = new SettingSerializer<>(Short.class) {
        public void write(CommentedConfigurationSection config, String key, Short value, String... comments) { config.set(key, value, comments); }
        public byte[] write(Short value) { return this.writeValue(value, ObjectOutputStream::writeShort); }
        public Short read(ConfigurationSection config, String key) { return (short) config.getInt(key); }
        public Short read(byte[] input) { return this.readValue(input, ObjectInputStream::readShort); }
        public String stringify(Short value) { return value.toString(); }
        public Short parseString(String value) { return Short.parseShort(value); }
    };

    public static final SettingSerializer<Byte> BYTE = new SettingSerializer<>(Byte.class) {
        public void write(CommentedConfigurationSection config, String key, Byte value, String... comments) { config.set(key, value, comments); }
        public byte[] write(Byte value) { return this.writeValue(value, ObjectOutputStream::writeByte); }
        public Byte read(ConfigurationSection config, String key) { return (byte) config.getInt(key); }
        public Byte read(byte[] input) { return this.readValue(input, ObjectInputStream::readByte); }
        public String stringify(Byte value) { return value.toString(); }
        public Byte parseString(String value) { return Byte.parseByte(value); }
    };

    public static final SettingSerializer<Double> DOUBLE = new SettingSerializer<>(Double.class) {
        public void write(CommentedConfigurationSection config, String key, Double value, String... comments) { config.set(key, value, comments); }
        public byte[] write(Double value) { return this.writeValue(value, ObjectOutputStream::writeDouble); }
        public Double read(ConfigurationSection config, String key) { return config.getDouble(key); }
        public Double read(byte[] input) { return this.readValue(input, ObjectInputStream::readDouble); }
        public String stringify(Double value) { return value.toString(); }
        public Double parseString(String value) { return Double.parseDouble(value); }
    };

    public static final SettingSerializer<Float> FLOAT = new SettingSerializer<>(Float.class) {
        public void write(CommentedConfigurationSection config, String key, Float value, String... comments) { config.set(key, value, comments); }
        public byte[] write(Float value) { return this.writeValue(value, ObjectOutputStream::writeFloat); }
        public Float read(ConfigurationSection config, String key) { return (float) config.getDouble(key); }
        public Float read(byte[] input) { return this.readValue(input, ObjectInputStream::readFloat); }
        public String stringify(Float value) { return value.toString(); }
        public Float parseString(String value) { return Float.parseFloat(value); }
    };

    public static final SettingSerializer<Character> CHAR = new SettingSerializer<>(Character.class) {
        public void write(CommentedConfigurationSection config, String key, Character value, String... comments) { config.set(key, value, comments); }
        public byte[] write(Character value) { return this.writeValue(value, ObjectOutputStream::writeChar); }
        public Character read(ConfigurationSection config, String key) {
            String value = config.getString(key);
            if (value == null || value.length() != 1)
                return ' ';
            return value.charAt(0);
        }
        public Character read(byte[] input) { return this.readValue(input, ObjectInputStream::readChar); }
        public String stringify(Character value) { return value.toString(); }
        public Character parseString(String value) { return value.charAt(0); }
    };
    //endregion

    //region Keyed Serializers
    public static final SettingSerializer<Material> MATERIAL = ofKeyed(Material.class, x -> Material.matchMaterial(x.getKey()));
    public static final SettingSerializer<Enchantment> ENCHANTMENT = ofKeyed(Enchantment.class, Enchantment::getByKey);
    //endregion

    //region Other Serializers
    public static final SettingSerializer<String> STRING = new SettingSerializer<>(String.class) {
        public void write(CommentedConfigurationSection config, String key, String value, String... comments) { config.set(key, value, comments); }
        public byte[] write(String value) { return this.writeValue(value, ObjectOutputStream::writeUTF); }
        public String read(ConfigurationSection config, String key) { return config.getString(key); }
        public String read(byte[] input) { return this.readValue(input, ObjectInputStream::readUTF); }
        public String stringify(String value) { return value; }
        public String parseString(String value) { return value; }
    };

    public static final SettingSerializer<ItemStack> ITEMSTACK = new SettingSerializer<>(ItemStack.class) {
        public void write(CommentedConfigurationSection config, String key, ItemStack value, String... comments) { throw new IllegalStateException("Cannot write ItemStack to a ConfigurationSection"); }
        public byte[] write(ItemStack value) {
            return DataSerializable.write(x -> {
                byte[] data = NMSAdapter.getHandler().serializeItemStack(value);
                x.writeInt(data.length);
                x.write(data);
            });
        }
        public ItemStack read(ConfigurationSection config, String key) { throw new IllegalStateException("Cannot read ItemStack from a ConfigurationSection"); }
        public ItemStack read(byte[] input) {
            AtomicReference<ItemStack> value = new AtomicReference<>();
            DataSerializable.read(input, x -> {
                int length = x.readInt();
                byte[] data = new byte[length];
                x.readFully(data);
                value.set(NMSAdapter.getHandler().deserializeItemStack(data));
            });
            return value.get();
        }
    };
    //endregion

    //region Serializer Factories
    public static <T extends Enum<T>> SettingSerializer<T> ofEnum(Class<T> enumClass) {
        String key = enumClass.getName();
        if (DYNAMIC_SERIALIZERS.containsKey(key))
            return (SettingSerializer<T>) DYNAMIC_SERIALIZERS.get(key);

        SettingSerializer<T> serializer = new SettingSerializer<>(enumClass) {
            public void write(CommentedConfigurationSection config, String key, T value, String... comments) { config.set(key, value.name(), comments); }
            public byte[] write(T value) { return DataSerializable.write(x -> x.writeUTF(value.name())); }
            public T read(ConfigurationSection config, String key) { return this.parse(config.getString(key)); }
            public T read(byte[] input) {
                AtomicReference<T> value = new AtomicReference<>();
                DataSerializable.read(input, x -> value.set(T.valueOf(enumClass, x.readUTF())));
                return value.get();
            }
            public String stringify(T value) { return value.name(); }
            public T parseString(String value) { return this.parse(value); }
            private T parse(String value) {
                try {
                    return T.valueOf(enumClass, value);
                } catch (IllegalArgumentException | NullPointerException e) {
                    return enumClass.getEnumConstants()[0];
                }
            }
        };

        DYNAMIC_SERIALIZERS.put(key, serializer);
        return serializer;
    }

    public static <T extends Keyed> SettingSerializer<T> ofKeyed(Class<T> keyedClass, Function<NamespacedKey, T> valueOfFunction) {
        String key = keyedClass.getName();
        if (DYNAMIC_SERIALIZERS.containsKey(key))
            return (SettingSerializer<T>) DYNAMIC_SERIALIZERS.get(key);

        SettingSerializer<T> serializer = new SettingSerializer<>(keyedClass) {
            public void write(CommentedConfigurationSection config, String key, T value, String... comments) { config.set(key, translateName(value.getKey()), comments); }
            public byte[] write(T value) { return DataSerializable.write(x -> x.writeUTF(translateName(value.getKey()))); }
            public T read(ConfigurationSection config, String key) { return valueOfFunction.apply(translateKey(config.getString(key))); }
            public T read(byte[] input) {
                AtomicReference<T> value = new AtomicReference<>();
                DataSerializable.read(input, x -> value.set(valueOfFunction.apply(translateKey(x.readUTF()))));
                return value.get();
            }
            public String stringify(T value) { return translateName(value.getKey()); }
            public T parseString(String value) { return valueOfFunction.apply(translateKey(value)); }
            private static NamespacedKey translateKey(String key) {
                return NamespacedKey.fromString(key);
            }
            private static String translateName(NamespacedKey key) {
                if (key.getNamespace().equals(NamespacedKey.MINECRAFT))
                    return key.getKey();
                return key.toString();
            }
        };

        DYNAMIC_SERIALIZERS.put(key, serializer);
        return serializer;
    }

    public static <T> SettingSerializer<T[]> ofArray(SettingSerializer<T> serializer) {
        String key = serializer.getTypeName() + "[]";
        if (DYNAMIC_SERIALIZERS.containsKey(key))
            return (SettingSerializer<T[]>) DYNAMIC_SERIALIZERS.get(key);

        SettingSerializer<T[]> arraySerializer = new SettingSerializer<>(null) {
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
                            int dataLength = x.readInt();
                            byte[] data = new byte[dataLength];
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

        SettingSerializer<List<T>> listSerializer = new SettingSerializer<>(null) {
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

        SettingSerializer<Map<K, V>> mapSerializer = new SettingSerializer<>(null) {
            public void write(CommentedConfigurationSection config, String key, Map<K, V> value, String... comments) {
                CommentedConfigurationSection section = getOrCreateSection(config, key, comments);
                if (keySerializer.isStringificationAllowed() && valueSerializer.isStringificationAllowed()) {
                    for (Map.Entry<K, V> entry : value.entrySet())
                        section.set(keySerializer.stringify(entry.getKey()), valueSerializer.stringify(entry.getValue()));
                } else {
                    int index = 0;
                    for (Map.Entry<K, V> entry : value.entrySet()) {
                        CommentedConfigurationSection indexedSection = getOrCreateSection(config, String.valueOf(index++), comments);
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
                        int keyDataLength = x.readInt();
                        byte[] keyData = new byte[keyDataLength];
                        x.readFully(keyData);
                        int valueDataLength = x.readInt();
                        byte[] valueData = new byte[valueDataLength];
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
    //endregion

    private static CommentedConfigurationSection getOrCreateSection(CommentedConfigurationSection config, String key, String... comments) {
        CommentedConfigurationSection section = config.getConfigurationSection(key);
        if (section == null) {
            config.addPathedComments(key, comments);
            section = config.createSection(key);
        }
        return section;
    }

}
