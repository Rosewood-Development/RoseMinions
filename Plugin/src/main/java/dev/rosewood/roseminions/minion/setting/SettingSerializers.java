package dev.rosewood.roseminions.minion.setting;

import dev.rosewood.rosegarden.config.CommentedConfigurationSection;
import dev.rosewood.roseminions.model.DataSerializable;
import dev.rosewood.roseminions.model.MinionConversation;
import dev.rosewood.roseminions.model.PlayableSound;
import dev.rosewood.roseminions.model.PotionEffectHelpers;
import dev.rosewood.roseminions.nms.NMSAdapter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import static dev.rosewood.roseminions.minion.setting.SettingSerializerFactories.ofComplex;

public final class SettingSerializers {

    private SettingSerializers() { }

    //region Primitive Serializers
    public static final SettingSerializer<Boolean> BOOLEAN = new SettingSerializer<>(Boolean.class, Object::toString, Boolean::parseBoolean) {
        public void write(CommentedConfigurationSection config, String key, Boolean value, String... comments) { config.set(key, value, comments); }
        public byte[] write(Boolean value) { return this.writeValue(value, ObjectOutputStream::writeBoolean); }
        public Boolean read(ConfigurationSection config, String key) { return config.getBoolean(key); }
        public Boolean read(byte[] input) { return this.readValue(input, ObjectInputStream::readBoolean); }
    };

    public static final SettingSerializer<Integer> INTEGER = new SettingSerializer<>(Integer.class, Object::toString, Integer::parseInt) {
        public void write(CommentedConfigurationSection config, String key, Integer value, String... comments) { config.set(key, value, comments); }
        public byte[] write(Integer value) { return this.writeValue(value, ObjectOutputStream::writeInt); }
        public Integer read(ConfigurationSection config, String key) { return config.getInt(key); }
        public Integer read(byte[] input) { return this.readValue(input, ObjectInputStream::readInt); }
    };

    public static final SettingSerializer<Long> LONG = new SettingSerializer<>(Long.class, Object::toString, Long::parseLong) {
        public void write(CommentedConfigurationSection config, String key, Long value, String... comments) { config.set(key, value, comments); }
        public byte[] write(Long value) { return this.writeValue(value, ObjectOutputStream::writeLong); }
        public Long read(ConfigurationSection config, String key) { return config.getLong(key); }
        public Long read(byte[] input) { return this.readValue(input, ObjectInputStream::readLong); }
    };

    public static final SettingSerializer<Short> SHORT = new SettingSerializer<>(Short.class, Object::toString, Short::parseShort) {
        public void write(CommentedConfigurationSection config, String key, Short value, String... comments) { config.set(key, value, comments); }
        public byte[] write(Short value) { return this.writeValue(value, ObjectOutputStream::writeShort); }
        public Short read(ConfigurationSection config, String key) { return (short) config.getInt(key); }
        public Short read(byte[] input) { return this.readValue(input, ObjectInputStream::readShort); }
    };

    public static final SettingSerializer<Byte> BYTE = new SettingSerializer<>(Byte.class, Object::toString, Byte::parseByte) {
        public void write(CommentedConfigurationSection config, String key, Byte value, String... comments) { config.set(key, value, comments); }
        public byte[] write(Byte value) { return this.writeValue(value, ObjectOutputStream::writeByte); }
        public Byte read(ConfigurationSection config, String key) { return (byte) config.getInt(key); }
        public Byte read(byte[] input) { return this.readValue(input, ObjectInputStream::readByte); }
    };

    public static final SettingSerializer<Double> DOUBLE = new SettingSerializer<>(Double.class, Object::toString, Double::parseDouble) {
        public void write(CommentedConfigurationSection config, String key, Double value, String... comments) { config.set(key, value, comments); }
        public byte[] write(Double value) { return this.writeValue(value, ObjectOutputStream::writeDouble); }
        public Double read(ConfigurationSection config, String key) { return config.getDouble(key); }
        public Double read(byte[] input) { return this.readValue(input, ObjectInputStream::readDouble); }
    };

    public static final SettingSerializer<Float> FLOAT = new SettingSerializer<>(Float.class, Object::toString, Float::parseFloat) {
        public void write(CommentedConfigurationSection config, String key, Float value, String... comments) { config.set(key, value, comments); }
        public byte[] write(Float value) { return this.writeValue(value, ObjectOutputStream::writeFloat); }
        public Float read(ConfigurationSection config, String key) { return (float) config.getDouble(key); }
        public Float read(byte[] input) { return this.readValue(input, ObjectInputStream::readFloat); }
    };

    public static final SettingSerializer<Character> CHAR = new SettingSerializer<>(Character.class, Object::toString, x -> x.charAt(0)) {
        public void write(CommentedConfigurationSection config, String key, Character value, String... comments) { config.set(key, value, comments); }
        public byte[] write(Character value) { return this.writeValue(value, ObjectOutputStream::writeChar); }
        public Character read(ConfigurationSection config, String key) {
            String value = config.getString(key);
            if (value == null || value.isEmpty())
                return ' ';
            return value.charAt(0);
        }
        public Character read(byte[] input) { return this.readValue(input, ObjectInputStream::readChar); }
    };
    //endregion

    //region Other Serializers
    public static final SettingSerializer<String> STRING = new SettingSerializer<>(String.class, Function.identity(), Function.identity()) {
        public void write(CommentedConfigurationSection config, String key, String value, String... comments) { config.set(key, value, comments); }
        public byte[] write(String value) { return this.writeValue(value, ObjectOutputStream::writeUTF); }
        public String read(ConfigurationSection config, String key) { return config.getString(key); }
        public String read(byte[] input) { return this.readValue(input, ObjectInputStream::readUTF); }
    };

    public static final SettingSerializer<ItemStack> ITEMSTACK = new SettingSerializer<>(ItemStack.class) {
        public void write(CommentedConfigurationSection config, String key, ItemStack value, String... comments) { config.set(key, Base64.getEncoder().encodeToString(this.write(value)), comments); }
        public byte[] write(ItemStack value) {
            return DataSerializable.write(x -> {
                byte[] data = NMSAdapter.getHandler().serializeItemStack(value);
                x.writeInt(data.length);
                x.write(data);
            });
        }
        public ItemStack read(ConfigurationSection config, String key) { return this.read(Base64.getDecoder().decode(config.getString(key, ""))); }
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

    //region Keyed Serializers
    public static final SettingSerializer<Material> MATERIAL = ofKeyed(Material.class, x -> Material.matchMaterial(x.getKey()));
    public static final SettingSerializer<Enchantment> ENCHANTMENT = ofKeyed(Enchantment.class, Enchantment::getByKey);
    public static final SettingSerializer<PotionEffectType> POTION_EFFECT_TYPE = ofKeyed(PotionEffectType.class, PotionEffectType::getByKey);
    public static final SettingSerializer<Sound> SOUND = ofKeyed(Sound.class, namespacedKey -> Arrays.stream(Sound.values()).filter(x -> x.getKey().equals(namespacedKey)).findFirst().orElse(null));
    //endregion

    //region Enum Serializers
    public static final SettingSerializer<SoundCategory> SOUND_CATEGORY = ofEnum(SoundCategory.class);
    //endregion

    //region Complex Serializers
    public static final SettingSerializer<MinionConversation> MINION_CONVERSATION = ofComplex(MinionConversation.class, MinionConversation::defineComplex, MinionConversation::toMap, MinionConversation::fromMap);
    public static final SettingSerializer<PlayableSound> PLAYABLE_SOUND = ofComplex(PlayableSound.class, PlayableSound::defineComplex, PlayableSound::toMap, PlayableSound::fromMap);
    public static final SettingSerializer<PotionEffect> POTION_EFFECT = ofComplex(PotionEffect.class, PotionEffectHelpers::defineComplex, PotionEffectHelpers::toMap, PotionEffectHelpers::fromMap);
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

}
