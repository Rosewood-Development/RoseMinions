package dev.rosewood.roseminions.minion.setting;

import dev.rosewood.rosegarden.config.CommentedConfigurationSection;
import dev.rosewood.rosegarden.utils.NMSUtil;
import dev.rosewood.roseminions.datatype.CustomPersistentDataType;
import dev.rosewood.roseminions.nms.NMSAdapter;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
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

public final class SettingSerializers {

    private SettingSerializers() { }

    //region Primitive Serializers
    public static final SettingSerializer<Boolean> BOOLEAN = new SettingSerializer<>(Boolean.class, PersistentDataType.BOOLEAN, Object::toString, Boolean::parseBoolean) {
        public void write(ConfigurationSection config, String key, Boolean value, String... comments) { setWithComments(config, key, value, comments); }
        public Boolean read(ConfigurationSection config, String key) { return config.getBoolean(key); }
    };

    public static final SettingSerializer<Integer> INTEGER = new SettingSerializer<>(Integer.class, PersistentDataType.INTEGER, Object::toString, Integer::parseInt) {
        public void write(ConfigurationSection config, String key, Integer value, String... comments) { setWithComments(config, key, value, comments); }
        public Integer read(ConfigurationSection config, String key) { return config.getInt(key); }
    };

    public static final SettingSerializer<Long> LONG = new SettingSerializer<>(Long.class, PersistentDataType.LONG, Object::toString, Long::parseLong) {
        public void write(ConfigurationSection config, String key, Long value, String... comments) { setWithComments(config, key, value, comments); }
        public Long read(ConfigurationSection config, String key) { return config.getLong(key); }
    };

    public static final SettingSerializer<Short> SHORT = new SettingSerializer<>(Short.class, PersistentDataType.SHORT, Object::toString, Short::parseShort) {
        public void write(ConfigurationSection config, String key, Short value, String... comments) { setWithComments(config, key, value, comments); }
        public Short read(ConfigurationSection config, String key) { return (short) config.getInt(key); }
    };

    public static final SettingSerializer<Byte> BYTE = new SettingSerializer<>(Byte.class, PersistentDataType.BYTE, Object::toString, Byte::parseByte) {
        public void write(ConfigurationSection config, String key, Byte value, String... comments) { setWithComments(config, key, value, comments); }
        public Byte read(ConfigurationSection config, String key) { return (byte) config.getInt(key); }
    };

    public static final SettingSerializer<Double> DOUBLE = new SettingSerializer<>(Double.class, PersistentDataType.DOUBLE, Object::toString, Double::parseDouble) {
        public void write(ConfigurationSection config, String key, Double value, String... comments) { setWithComments(config, key, value, comments); }
        public Double read(ConfigurationSection config, String key) { return config.getDouble(key); }
    };

    public static final SettingSerializer<Float> FLOAT = new SettingSerializer<>(Float.class, PersistentDataType.FLOAT, Object::toString, Float::parseFloat) {
        public void write(ConfigurationSection config, String key, Float value, String... comments) { setWithComments(config, key, value, comments); }
        public Float read(ConfigurationSection config, String key) { return (float) config.getDouble(key); }
    };

    public static final SettingSerializer<Character> CHAR = new SettingSerializer<>(Character.class, CustomPersistentDataType.CHARACTER, Object::toString, x -> x.charAt(0)) {
        public void write(ConfigurationSection config, String key, Character value, String... comments) { setWithComments(config, key, value, comments); }
        public Character read(ConfigurationSection config, String key) {
            String value = config.getString(key);
            if (value == null || value.isEmpty())
                return ' ';
            return value.charAt(0);
        }
    };
    //endregion

    //region Other Serializers
    public static final SettingSerializer<String> STRING = new SettingSerializer<>(String.class, PersistentDataType.STRING, Function.identity(), Function.identity()) {
        public void write(ConfigurationSection config, String key, String value, String... comments) { setWithComments(config, key, value, comments); }
        public String read(ConfigurationSection config, String key) { return config.getString(key); }
    };

    public static final SettingSerializer<ItemStack> ITEMSTACK = new SettingSerializer<>(ItemStack.class, CustomPersistentDataType.ITEMSTACK) {
        public void write(ConfigurationSection config, String key, ItemStack value, String... comments) { setWithComments(config, key, Base64.getEncoder().encodeToString(NMSAdapter.getHandler().serializeItemStack(value)), comments); }
        public ItemStack read(ConfigurationSection config, String key) { return NMSAdapter.getHandler().deserializeItemStack(Base64.getDecoder().decode(config.getString(key, ""))); }
    };
    //endregion

    //region Keyed Serializers
    public static final SettingSerializer<Material> MATERIAL = ofKeyed(Material.class, Registry.MATERIAL::get);
    public static final SettingSerializer<List<Material>> MATERIAL_LIST = ofList(MATERIAL);
    public static final SettingSerializer<Enchantment> ENCHANTMENT = ofKeyed(Enchantment.class, Registry.ENCHANTMENT::get);
    public static final SettingSerializer<PotionEffectType> POTION_EFFECT_TYPE = ofKeyed(PotionEffectType.class, Registry.EFFECT::get);
    public static final SettingSerializer<Sound> SOUND = ofKeyed(Sound.class, Registry.SOUNDS::get);
    //endregion

    //region Enum Serializers
    public static final SettingSerializer<SoundCategory> SOUND_CATEGORY = ofEnum(SoundCategory.class);
    //endregion

    //region Record Serializers
    public static final SettingSerializer<PotionEffect> POTION_EFFECT = RecordSettingSerializerBuilder.create(PotionEffect.class, instance -> instance.group(
            new Field<>("type", SettingSerializers.POTION_EFFECT_TYPE, PotionEffect::getType, "The potion effect type"),
            new Field<>("duration", SettingSerializers.INTEGER, PotionEffect::getDuration, "The duration in seconds"),
            new Field<>("amplifier", SettingSerializers.INTEGER, PotionEffect::getDuration, "The potion effect level amplifier, 0 = level 1"),
            new Field<>("ambient", SettingSerializers.BOOLEAN, PotionEffect::isAmbient, "true for the potion effect particles to be transparent, false for solid"),
            new Field<>("particles", SettingSerializers.BOOLEAN, PotionEffect::hasParticles, "true for particles to appear when the effect is applied, false for no particles"),
            new Field<>("icon", SettingSerializers.BOOLEAN, PotionEffect::hasIcon, "true for an icon to appear on the player's screen when the effect is applied")
    ).apply(instance, PotionEffect::new));
    public static final SettingSerializer<Vector> VECTOR = RecordSettingSerializerBuilder.create(Vector.class, instance -> instance.group(
            new Field<>("x", SettingSerializers.DOUBLE, Vector::getX),
            new Field<>("y", SettingSerializers.DOUBLE, Vector::getY),
            new Field<>("z", SettingSerializers.DOUBLE, Vector::getZ)
    ).apply(instance, Vector::new));
    //endregion

    //region Collection Serializer Factories
    public static <T extends Enum<T>> SettingSerializer<T> ofEnum(Class<T> enumClass) {
        return SettingSerializerFactories.ofEnum(enumClass);
    }

    public static <T extends Keyed> SettingSerializer<T> ofKeyed(Class<T> keyedClass, Function<NamespacedKey, T> valueOfFunction) {
        return SettingSerializerFactories.ofKeyed(keyedClass, valueOfFunction);
    }

    public static <T> SettingSerializer<T[]> ofArray(SettingSerializer<T> serializer) {
        return SettingSerializerFactories.ofArray(serializer);
    }

    public static <T> SettingSerializer<List<T>> ofList(SettingSerializer<T> serializer) {
        return SettingSerializerFactories.ofList(serializer);
    }

    public static <K, V> SettingSerializer<Map<K, V>> ofMap(SettingSerializer<K> keySerializer, SettingSerializer<V> valueSerializer) {
        return SettingSerializerFactories.ofMap(keySerializer, valueSerializer);
    }
    //endregion

    static void setWithComments(ConfigurationSection section, String key, Object value, String[] comments) {
        if (section instanceof CommentedConfigurationSection commentedSection) {
            commentedSection.set(key, value, comments);
        } else {
            section.set(key, value);
            if (NMSUtil.getVersionNumber() > 18 || (NMSUtil.getVersionNumber() == 18 && NMSUtil.getMinorVersionNumber() >= 1))
                section.setComments(key, Arrays.asList(comments));
        }
    }

}
