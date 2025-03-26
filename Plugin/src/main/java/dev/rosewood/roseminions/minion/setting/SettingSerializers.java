package dev.rosewood.roseminions.minion.setting;

import dev.rosewood.rosegarden.config.CommentedConfigurationSection;
import dev.rosewood.roseminions.datatype.CustomPersistentDataType;
import dev.rosewood.roseminions.model.MinionConversation;
import dev.rosewood.roseminions.model.PlayableSound;
import dev.rosewood.roseminions.model.WorkerAreaProperties;
import dev.rosewood.roseminions.model.helpers.PotionEffectHelpers;
import dev.rosewood.roseminions.model.helpers.VectorHelpers;
import dev.rosewood.roseminions.nms.NMSAdapter;
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
import static dev.rosewood.roseminions.minion.setting.SettingSerializerFactories.ofComplex;

public final class SettingSerializers {

    private SettingSerializers() { }

    //region Primitive Serializers
    public static final SettingSerializer<Boolean> BOOLEAN = new SettingSerializer<>(Boolean.class, PersistentDataType.BOOLEAN, Object::toString, Boolean::parseBoolean) {
        public void write(CommentedConfigurationSection config, String key, Boolean value, String... comments) { config.set(key, value, comments); }
        public Boolean read(ConfigurationSection config, String key) { return config.getBoolean(key); }
    };

    public static final SettingSerializer<Integer> INTEGER = new SettingSerializer<>(Integer.class, PersistentDataType.INTEGER, Object::toString, Integer::parseInt) {
        public void write(CommentedConfigurationSection config, String key, Integer value, String... comments) { config.set(key, value, comments); }
        public Integer read(ConfigurationSection config, String key) { return config.getInt(key); }
    };

    public static final SettingSerializer<Long> LONG = new SettingSerializer<>(Long.class, PersistentDataType.LONG, Object::toString, Long::parseLong) {
        public void write(CommentedConfigurationSection config, String key, Long value, String... comments) { config.set(key, value, comments); }
        public Long read(ConfigurationSection config, String key) { return config.getLong(key); }
    };

    public static final SettingSerializer<Short> SHORT = new SettingSerializer<>(Short.class, PersistentDataType.SHORT, Object::toString, Short::parseShort) {
        public void write(CommentedConfigurationSection config, String key, Short value, String... comments) { config.set(key, value, comments); }
        public Short read(ConfigurationSection config, String key) { return (short) config.getInt(key); }
    };

    public static final SettingSerializer<Byte> BYTE = new SettingSerializer<>(Byte.class, PersistentDataType.BYTE, Object::toString, Byte::parseByte) {
        public void write(CommentedConfigurationSection config, String key, Byte value, String... comments) { config.set(key, value, comments); }
        public Byte read(ConfigurationSection config, String key) { return (byte) config.getInt(key); }
    };

    public static final SettingSerializer<Double> DOUBLE = new SettingSerializer<>(Double.class, PersistentDataType.DOUBLE, Object::toString, Double::parseDouble) {
        public void write(CommentedConfigurationSection config, String key, Double value, String... comments) { config.set(key, value, comments); }
        public Double read(ConfigurationSection config, String key) { return config.getDouble(key); }
    };

    public static final SettingSerializer<Float> FLOAT = new SettingSerializer<>(Float.class, PersistentDataType.FLOAT, Object::toString, Float::parseFloat) {
        public void write(CommentedConfigurationSection config, String key, Float value, String... comments) { config.set(key, value, comments); }
        public Float read(ConfigurationSection config, String key) { return (float) config.getDouble(key); }
    };

    public static final SettingSerializer<Character> CHAR = new SettingSerializer<>(Character.class, CustomPersistentDataType.CHARACTER, Object::toString, x -> x.charAt(0)) {
        public void write(CommentedConfigurationSection config, String key, Character value, String... comments) { config.set(key, value, comments); }
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
        public void write(CommentedConfigurationSection config, String key, String value, String... comments) { config.set(key, value, comments); }
        public String read(ConfigurationSection config, String key) { return config.getString(key); }
    };

    public static final SettingSerializer<ItemStack> ITEMSTACK = new SettingSerializer<>(ItemStack.class, CustomPersistentDataType.ITEMSTACK) {
        public void write(CommentedConfigurationSection config, String key, ItemStack value, String... comments) { config.set(key, Base64.getEncoder().encodeToString(NMSAdapter.getHandler().serializeItemStack(value)), comments); }
        public ItemStack read(ConfigurationSection config, String key) { return NMSAdapter.getHandler().deserializeItemStack(Base64.getDecoder().decode(config.getString(key, ""))); }
    };
    //endregion

    //region Keyed Serializers
    public static final SettingSerializer<Material> MATERIAL = ofKeyed(Material.class, Registry.MATERIAL::get);
    public static final SettingSerializer<Enchantment> ENCHANTMENT = ofKeyed(Enchantment.class, Registry.ENCHANTMENT::get);
    public static final SettingSerializer<PotionEffectType> POTION_EFFECT_TYPE = ofKeyed(PotionEffectType.class, Registry.MOB_EFFECT::get);
    public static final SettingSerializer<Sound> SOUND = ofKeyed(Sound.class, Registry.SOUND_EVENT::get);
    //endregion

    //region Enum Serializers
    public static final SettingSerializer<SoundCategory> SOUND_CATEGORY = ofEnum(SoundCategory.class);
    //endregion

    //region Complex Serializers
    public static final SettingSerializer<MinionConversation> MINION_CONVERSATION = ofComplex(MinionConversation.class, MinionConversation.PDC_TYPE, MinionConversation::defineComplex, MinionConversation::toMap, MinionConversation::fromMap);
    public static final SettingSerializer<PlayableSound> PLAYABLE_SOUND = ofComplex(PlayableSound.class, PlayableSound.PDC_TYPE, PlayableSound::defineComplex, PlayableSound::toMap, PlayableSound::fromMap);
    public static final SettingSerializer<PotionEffect> POTION_EFFECT = ofComplex(PotionEffect.class, PotionEffectHelpers.PDC_TYPE, PotionEffectHelpers::defineComplex, PotionEffectHelpers::toMap, PotionEffectHelpers::fromMap);
    public static final SettingSerializer<Vector> VECTOR = ofComplex(Vector.class, VectorHelpers.PDC_TYPE, VectorHelpers::defineComplex, VectorHelpers::toMap, VectorHelpers::fromMap);
    public static final SettingSerializer<WorkerAreaProperties> WORKER_AREA_PROPERTIES = ofComplex(WorkerAreaProperties.class, WorkerAreaProperties.PDC_TYPE, WorkerAreaProperties::defineComplex, WorkerAreaProperties::toMap, WorkerAreaProperties::fromMap);
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
