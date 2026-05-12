package dev.rosewood.roseminions.setting;

import dev.rosewood.rosegarden.config.CommentedConfigurationSection;
import dev.rosewood.rosegarden.utils.NMSUtil;
import dev.rosewood.roseminions.datatype.MinionPersistentDataType;
import dev.rosewood.roseminions.nms.NMSAdapter;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.bukkit.Color;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public final class DataSerializers {

    private DataSerializers() { }

    //region Primitive Serializers
    public static final DataSerializer<Boolean> BOOLEAN = new DataSerializer<>(Boolean.class, PersistentDataType.BOOLEAN, Object::toString, Boolean::parseBoolean) {
        public void write(ConfigurationSection config, String key, Boolean value, String... comments) { setWithComments(config, key, value, comments); }
        public Boolean read(ConfigurationSection config, String key) { return getOrNull(config, key, ConfigurationSection::getBoolean); }
    };

    public static final DataSerializer<Integer> INTEGER = new DataSerializer<>(Integer.class, PersistentDataType.INTEGER, Object::toString, Integer::parseInt) {
        public void write(ConfigurationSection config, String key, Integer value, String... comments) { setWithComments(config, key, value, comments); }
        public Integer read(ConfigurationSection config, String key) { return getOrNull(config, key, ConfigurationSection::getInt); }
    };

    public static final DataSerializer<Long> LONG = new DataSerializer<>(Long.class, PersistentDataType.LONG, Object::toString, Long::parseLong) {
        public void write(ConfigurationSection config, String key, Long value, String... comments) { setWithComments(config, key, value, comments); }
        public Long read(ConfigurationSection config, String key) { return getOrNull(config, key, ConfigurationSection::getLong); }
    };

    public static final DataSerializer<Short> SHORT = new DataSerializer<>(Short.class, PersistentDataType.SHORT, Object::toString, Short::parseShort) {
        public void write(ConfigurationSection config, String key, Short value, String... comments) { setWithComments(config, key, value, comments); }
        public Short read(ConfigurationSection config, String key) { return getOrNull(config, key, (x, y) -> (short) x.getInt(y)); }
    };

    public static final DataSerializer<Byte> BYTE = new DataSerializer<>(Byte.class, PersistentDataType.BYTE, Object::toString, Byte::parseByte) {
        public void write(ConfigurationSection config, String key, Byte value, String... comments) { setWithComments(config, key, value, comments); }
        public Byte read(ConfigurationSection config, String key) { return getOrNull(config, key, (x, y) -> (byte) x.getInt(y)); }
    };

    public static final DataSerializer<Double> DOUBLE = new DataSerializer<>(Double.class, PersistentDataType.DOUBLE, Object::toString, Double::parseDouble) {
        public void write(ConfigurationSection config, String key, Double value, String... comments) { setWithComments(config, key, value, comments); }
        public Double read(ConfigurationSection config, String key) { return getOrNull(config, key, ConfigurationSection::getDouble); }
    };

    public static final DataSerializer<Float> FLOAT = new DataSerializer<>(Float.class, PersistentDataType.FLOAT, Object::toString, Float::parseFloat) {
        public void write(ConfigurationSection config, String key, Float value, String... comments) { setWithComments(config, key, value, comments); }
        public Float read(ConfigurationSection config, String key) { return getOrNull(config, key, (x, y) -> (float) x.getDouble(y)); }
    };

    public static final DataSerializer<Character> CHAR = new DataSerializer<>(Character.class, MinionPersistentDataType.CHARACTER, Object::toString, x -> x.charAt(0)) {
        public void write(ConfigurationSection config, String key, Character value, String... comments) { setWithComments(config, key, value, comments); }
        public Character read(ConfigurationSection config, String key) {
            String value = config.getString(key);
            if (value == null || value.isEmpty())
                return ' ';
            return value.charAt(0);
        }
    };

    private static <T> T getOrNull(ConfigurationSection section, String key, BiFunction<ConfigurationSection, String, T> function) {
        if (!section.contains(key))
            return null;
        return function.apply(section, key);
    }
    //endregion

    //region Primitive List Serializers
    public static final DataSerializer<List<Boolean>> BOOLEAN_LIST = ofList(BOOLEAN);
    public static final DataSerializer<List<Long>> LONG_LIST = ofList(LONG);
    public static final DataSerializer<List<Short>> SHORT_LIST = ofList(SHORT);
    public static final DataSerializer<List<Byte>> BYTE_LIST = ofList(BYTE);
    public static final DataSerializer<List<Double>> DOUBLE_LIST = ofList(DOUBLE);
    public static final DataSerializer<List<Float>> FLOAT_LIST = ofList(FLOAT);
    public static final DataSerializer<List<Character>> CHAR_LIST = ofList(CHAR);
    //endregion

    //region Other Serializers
    public static final DataSerializer<String> STRING = new DataSerializer<>(String.class, PersistentDataType.STRING, Function.identity(), Function.identity()) {
        public void write(ConfigurationSection config, String key, String value, String... comments) { setWithComments(config, key, value, comments); }
        public String read(ConfigurationSection config, String key) { return config.getString(key); }
    };
    public static final DataSerializer<List<String>> STRING_LIST = ofList(STRING);

    public static final DataSerializer<ConfigurationSection> SECTION = new DataSerializer<>(ConfigurationSection.class, MinionPersistentDataType.SECTION) {
        public void write(ConfigurationSection config, String key, ConfigurationSection value, String... comments) {
            if (config instanceof dev.rosewood.rosegarden.config.CommentedConfigurationSection) {
                ((dev.rosewood.rosegarden.config.CommentedConfigurationSection) config).addPathedComments(key, comments);
            } else {
                if (NMSUtil.getVersionNumber() > 18 || (NMSUtil.getVersionNumber() == 18 && NMSUtil.getMinorVersionNumber() >= 1)) {
                    if (!config.isConfigurationSection(key))
                        config.createSection(key);
                    config.setComments(key, Arrays.asList(comments));
                }
            }
        }
        public ConfigurationSection read(ConfigurationSection config, String key) { return config.getConfigurationSection(key); }
    };

    public static final DataSerializer<ItemStack> ITEMSTACK = new DataSerializer<>(ItemStack.class, MinionPersistentDataType.ITEMSTACK) {
        public void write(ConfigurationSection config, String key, ItemStack value, String... comments) { setWithComments(config, key, Base64.getEncoder().encodeToString(NMSAdapter.getHandler().serializeItemStack(value)), comments); }
        public ItemStack read(ConfigurationSection config, String key) { return NMSAdapter.getHandler().deserializeItemStack(Base64.getDecoder().decode(config.getString(key, ""))); }
    };

    public static final DataSerializer<Color> COLOR_RGB = new DataSerializer<>(Color.class, MinionPersistentDataType.COLOR_RGB) {
        public void write(ConfigurationSection config, String key, Color value, String... comments) {
            if (value != null)
                setWithComments(config, key, String.format("#%02x%02x%02x", value.getRed(), value.getGreen(), value.getBlue()), comments);
        }
        public Color read(ConfigurationSection config, String key) {
            try {
                java.awt.Color color = java.awt.Color.decode(config.getString(key, ""));
                return Color.fromRGB(color.getRed(), color.getGreen(), color.getBlue());
            } catch (NumberFormatException e) {
                return null;
            }
        }
    };

    public static final DataSerializer<Color> COLOR_ARGB = new DataSerializer<>(Color.class, MinionPersistentDataType.COLOR_ARGB) {
        public void write(ConfigurationSection config, String key, Color value, String... comments) {
            if (value != null)
                setWithComments(config, key, String.format("#%02x%02x%02x%02x", value.getAlpha(), value.getRed(), value.getGreen(), value.getBlue()), comments);
        }
        public Color read(ConfigurationSection config, String key) {
            try {
                java.awt.Color color = java.awt.Color.decode(config.getString(key, ""));
                return Color.fromARGB(color.getAlpha(), color.getRed(), color.getGreen(), color.getBlue());
            } catch (NumberFormatException e) {
                return null;
            }
        }
    };
    //endregion

    //region Keyed Serializers
    public static final DataSerializer<Enchantment> ENCHANTMENT = ofKeyed(Enchantment.class, Registry.ENCHANTMENT::get);
    public static final DataSerializer<PotionEffectType> POTION_EFFECT_TYPE = ofKeyed(PotionEffectType.class, Registry.EFFECT::get);
    public static final DataSerializer<Sound> SOUND = ofKeyed(Sound.class, Registry.SOUNDS::get);
    //endregion

    //region Enum Serializers
    public static final DataSerializer<SoundCategory> SOUND_CATEGORY = ofEnum(SoundCategory.class);
    public static final DataSerializer<Particle> PARTICLE = ofEnum(Particle.class);
    public static final DataSerializer<Material> MATERIAL = ofEnum(Material.class);
    public static final DataSerializer<List<Material>> MATERIAL_LIST = ofList(MATERIAL);
    //endregion

    //region Record Serializers
    public static final DataSerializer<Vector> VECTOR = ofRecord(Vector.class, instance -> instance.group(
            SettingField.of("x", DataSerializers.DOUBLE, Vector::getX),
            SettingField.of("y", DataSerializers.DOUBLE, Vector::getY),
            SettingField.of("z", DataSerializers.DOUBLE, Vector::getZ)
    ).apply(instance, Vector::new));

    public static final DataSerializer<PotionEffect> POTION_EFFECT = ofRecord(PotionEffect.class, instance -> instance.group(
            SettingField.of("type", POTION_EFFECT_TYPE, PotionEffect::getType, "The potion effect type"),
            SettingField.of("duration", INTEGER, PotionEffect::getDuration, "The duration in seconds"),
            SettingField.of("amplifier", INTEGER, PotionEffect::getDuration, "The potion effect level amplifier, 0 = level 1"),
            SettingField.of("ambient", BOOLEAN, PotionEffect::isAmbient, "true for the potion effect particles to be transparent, false for solid"),
            SettingField.of("particles", BOOLEAN, PotionEffect::hasParticles, "true for particles to appear when the effect is applied, false for no particles"),
            SettingField.of("icon", BOOLEAN, PotionEffect::hasIcon, "true for an icon to appear on the player's screen when the effect is applied")
    ).apply(instance, PotionEffect::new));
    //endregion

    //region Collection Serializer Factories
    public static <T extends Enum<T>> DataSerializer<T> ofEnum(Class<T> enumClass) {
        return DataSerializerFactories.ofEnum(enumClass);
    }

    public static <T extends Keyed> DataSerializer<T> ofKeyed(Class<T> keyedClass, Function<NamespacedKey, T> valueOfFunction) {
        return DataSerializerFactories.ofKeyed(keyedClass, valueOfFunction);
    }

    public static <T> DataSerializer<T[]> ofArray(DataSerializer<T> serializer) {
        return DataSerializerFactories.ofArray(serializer);
    }

    public static <T> DataSerializer<List<T>> ofList(DataSerializer<T> serializer) {
        return DataSerializerFactories.ofList(serializer);
    }

    public static <K, V> DataSerializer<Map<K, V>> ofMap(DataSerializer<K> keySerializer, DataSerializer<V> valueSerializer) {
        return DataSerializerFactories.ofMap(keySerializer, valueSerializer);
    }
    //endregion

    //region Record Serializers
    public static <O> DataSerializer<O> ofRecord(Class<O> clazz, Function<RecordDataSerializerBuilder<O>, RecordDataSerializerBuilder.Built<O>> builder) {
        return RecordDataSerializerBuilder.create(clazz, builder);
    }

    public static <T, M> DataSerializer<T> ofFieldMapped(Class<T> type, String fieldKey, DataSerializer<M> fieldSerializer, Map<M, DataSerializer<? extends T>> mapper) {
        return DataSerializerFactories.ofFieldMapped(type, fieldKey, fieldSerializer, mapper);
    }
    //endregion

    public static void setWithComments(ConfigurationSection section, String key, Object value, String[] comments) {
        if (section instanceof dev.rosewood.rosegarden.config.CommentedConfigurationSection) {
            ((CommentedConfigurationSection) section).set(key, value, comments);
        } else {
            section.set(key, value);
            if (NMSUtil.getVersionNumber() > 18 || (NMSUtil.getVersionNumber() == 18 && NMSUtil.getMinorVersionNumber() >= 1))
                section.setComments(key, Arrays.asList(comments));
        }
    }

}
