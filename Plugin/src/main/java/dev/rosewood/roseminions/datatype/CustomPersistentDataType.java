package dev.rosewood.roseminions.datatype;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.roseminions.RoseMinions;
import dev.rosewood.roseminions.minion.module.controller.WorkerAreaController;
import dev.rosewood.roseminions.model.ChunkLocation;
import dev.rosewood.roseminions.nms.NMSAdapter;
import java.awt.Color;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;

public final class CustomPersistentDataType {

    public static final PersistentDataType<PersistentDataContainer, ChunkLocation> CHUNK_LOCATION = new PersistentDataType<>() {

        private static final NamespacedKey KEY_WORLD = KeyHelper.get("world");
        private static final NamespacedKey KEY_X = KeyHelper.get("x");
        private static final NamespacedKey KEY_Z = KeyHelper.get("z");

        public Class<PersistentDataContainer> getPrimitiveType() { return PersistentDataContainer.class; }
        public Class<ChunkLocation> getComplexType() { return ChunkLocation.class; }

        @Override
        public PersistentDataContainer toPrimitive(ChunkLocation chunkLocation, PersistentDataAdapterContext context) {
            PersistentDataContainer container = context.newPersistentDataContainer();
            container.set(KEY_WORLD, PersistentDataType.STRING, chunkLocation.world());
            container.set(KEY_X, PersistentDataType.INTEGER, chunkLocation.x());
            container.set(KEY_Z, PersistentDataType.INTEGER, chunkLocation.z());
            return container;
        }

        @Override
        public ChunkLocation fromPrimitive(PersistentDataContainer container, PersistentDataAdapterContext context) {
            String world = container.get(KEY_WORLD, PersistentDataType.STRING);
            Integer x = container.get(KEY_X, PersistentDataType.INTEGER);
            Integer z = container.get(KEY_Z, PersistentDataType.INTEGER);
            if (world == null || x == null || z == null)
                throw new IllegalArgumentException("Invalid ChunkLocation");
            return new ChunkLocation(world, x, z);
        }

    };

    public static final PersistentDataType<PersistentDataContainer, Location> LOCATION = new PersistentDataType<>() {

        private static final NamespacedKey KEY_WORLD = KeyHelper.get("world");
        private static final NamespacedKey KEY_X = KeyHelper.get("x");
        private static final NamespacedKey KEY_Y = KeyHelper.get("y");
        private static final NamespacedKey KEY_Z = KeyHelper.get("z");
        private static final NamespacedKey KEY_YAW = KeyHelper.get("yaw");
        private static final NamespacedKey KEY_PITCH = KeyHelper.get("pitch");

        public Class<PersistentDataContainer> getPrimitiveType() { return PersistentDataContainer.class; }
        public Class<Location> getComplexType() { return Location.class; }

        @Override
        public PersistentDataContainer toPrimitive(Location location, PersistentDataAdapterContext context) {
            PersistentDataContainer container = context.newPersistentDataContainer();
            container.set(KEY_WORLD, PersistentDataType.STRING, location.getWorld().getName());
            container.set(KEY_X, PersistentDataType.DOUBLE, location.getX());
            container.set(KEY_Y, PersistentDataType.DOUBLE, location.getY());
            container.set(KEY_Z, PersistentDataType.DOUBLE, location.getZ());
            container.set(KEY_YAW, PersistentDataType.FLOAT, location.getYaw());
            container.set(KEY_PITCH, PersistentDataType.FLOAT, location.getPitch());
            return container;
        }

        @Override
        public Location fromPrimitive(PersistentDataContainer container, PersistentDataAdapterContext context) {
            String worldName = container.get(KEY_WORLD, PersistentDataType.STRING);
            Double x = container.get(KEY_X, PersistentDataType.DOUBLE);
            Double y = container.get(KEY_Y, PersistentDataType.DOUBLE);
            Double z = container.get(KEY_Z, PersistentDataType.DOUBLE);
            Float yaw = container.get(KEY_YAW, PersistentDataType.FLOAT);
            Float pitch = container.get(KEY_PITCH, PersistentDataType.FLOAT);
            if (worldName == null || x == null || y == null || z == null || yaw == null || pitch == null)
                throw new IllegalArgumentException("Invalid Location");
            World world = Bukkit.getWorld(worldName);
            if (world == null)
                throw new IllegalArgumentException("Invalid Location, world is not loaded");
            return new Location(world, x, y, z, yaw, pitch);
        }

    };

    public static final PersistentDataType<Integer, Color> JAVA_COLOR = new PersistentDataType<>() {

        public Class<Integer> getPrimitiveType() { return Integer.class; }
        public Class<Color> getComplexType() { return Color.class; }

        @Override
        public Integer toPrimitive(Color color, PersistentDataAdapterContext context) {
            return color.getRGB();
        }

        @Override
        public Color fromPrimitive(Integer primitive, PersistentDataAdapterContext context) {
            return new Color(primitive);
        }

    };

    public static final PersistentDataType<String, BlockData> BLOCK_DATA = new PersistentDataType<>() {

        public Class<String> getPrimitiveType() { return String.class; }
        public Class<BlockData> getComplexType() { return BlockData.class; }

        @Override
        public String toPrimitive(BlockData blockData, PersistentDataAdapterContext context) {
            return blockData.getAsString();
        }

        @Override
        public BlockData fromPrimitive(String primitive, PersistentDataAdapterContext context) {
            return Bukkit.getServer().createBlockData(primitive);
        }

    };

    public static final PersistentDataType<byte[], ItemStack> ITEMSTACK = new PersistentDataType<>() {

        public Class<byte[]> getPrimitiveType() { return byte[].class; }
        public Class<ItemStack> getComplexType() { return ItemStack.class; }

        @Override
        public byte[] toPrimitive(ItemStack itemStack, PersistentDataAdapterContext context) {
            return NMSAdapter.getHandler().serializeItemStack(itemStack);
        }

        @Override
        public ItemStack fromPrimitive(byte[] primitive, PersistentDataAdapterContext context) {
            return NMSAdapter.getHandler().deserializeItemStack(primitive);
        }

    };

    public static final PersistentDataType<byte[], UUID> UUID = new PersistentDataType<>() {

        public Class<byte[]> getPrimitiveType() { return byte[].class; }
        public Class<UUID> getComplexType() { return UUID.class; }

        @Override
        public byte[] toPrimitive(UUID uuid, PersistentDataAdapterContext context) {
            return ByteBuffer.wrap(new byte[16])
                    .putLong(uuid.getMostSignificantBits())
                    .putLong(uuid.getLeastSignificantBits())
                    .array();
        }

        @Override
        public UUID fromPrimitive(byte[] primitive, PersistentDataAdapterContext context) {
            ByteBuffer byteBuffer = ByteBuffer.wrap(primitive);
            return new UUID(byteBuffer.getLong(), byteBuffer.getLong());
        }

    };

    public static final PersistentDataType<String, Character> CHARACTER = new PersistentDataType<>() {

        public Class<String> getPrimitiveType() { return String.class; }
        public Class<Character> getComplexType() { return Character.class; }

        @Override
        public String toPrimitive(Character character, PersistentDataAdapterContext context) {
            return String.valueOf(character);
        }

        @Override
        public Character fromPrimitive(String primitive, PersistentDataAdapterContext context) {
            return primitive.charAt(0);
        }

    };

    public static <T extends Enum<T>> PersistentDataType<String, T> forEnum(Class<T> enumClass) {
        return new PersistentDataType<>() {

            public Class<String> getPrimitiveType() { return String.class; }
            public Class<T> getComplexType() { return enumClass; }

            @Override
            public String toPrimitive(T enumValue, PersistentDataAdapterContext context) {
                return enumValue.name();
            }

            @Override
            public T fromPrimitive(String primitive, PersistentDataAdapterContext context) {
                return Enum.valueOf(enumClass, primitive);
            }

        };
    }

    public static <T extends Keyed> PersistentDataType<String, T> forKeyed(Class<T> keyedClass, Function<NamespacedKey, T> valueOfFunction) {
        return new PersistentDataType<>() {

            public Class<String> getPrimitiveType() { return String.class; }
            public Class<T> getComplexType() { return keyedClass; }

            @Override
            public String toPrimitive(T enumValue, PersistentDataAdapterContext context) {
                return enumValue.getKey().asString();
            }

            @Override
            public T fromPrimitive(String primitive, PersistentDataAdapterContext context) {
                return valueOfFunction.apply(NamespacedKey.fromString(primitive));
            }

        };
    }

    @SuppressWarnings("unchecked")
    public static <T> PersistentDataType<PersistentDataContainer, T[]> forArray(PersistentDataType<?, T> arrayElementDataType) {
        return new PersistentDataType<>() {

            private static final NamespacedKey KEY_SIZE = KeyHelper.get("size");

            public Class<PersistentDataContainer> getPrimitiveType() { return PersistentDataContainer.class; }
            public Class<T[]> getComplexType() { return (Class<T[]>) arrayElementDataType.getComplexType().arrayType(); }

            @Override
            public PersistentDataContainer toPrimitive(T[] array, PersistentDataAdapterContext context) {
                PersistentDataContainer container = context.newPersistentDataContainer();
                container.set(KEY_SIZE, PersistentDataType.INTEGER, array.length);
                for (int i = 0; i < array.length; i++) {
                    T element = array[i];
                    if (element == null)
                        continue;
                    NamespacedKey elementKey = KeyHelper.get(String.valueOf(i));
                    container.set(elementKey, arrayElementDataType, element);
                }
                return container;
            }

            @Override
            public T[] fromPrimitive(PersistentDataContainer container, PersistentDataAdapterContext context) {
                Integer size = container.get(KEY_SIZE, PersistentDataType.INTEGER);
                if (size == null)
                    throw new IllegalArgumentException("Invalid " + arrayElementDataType.getComplexType().getSimpleName() + "[]");
                T[] array = (T[]) Array.newInstance(arrayElementDataType.getComplexType(), size);
                for (int i = 0; i < size; i++) {
                    NamespacedKey elementKey = KeyHelper.get(String.valueOf(i));
                    array[i] = container.get(elementKey, arrayElementDataType);
                }
                return array;
            }

        };
    }

    @SuppressWarnings("unchecked")
    public static <T> PersistentDataType<PersistentDataContainer, List<T>> forList(PersistentDataType<?, T> listElementDataType) {
        return new PersistentDataType<>() {

            private static final NamespacedKey KEY_SIZE = KeyHelper.get("size");

            public Class<PersistentDataContainer> getPrimitiveType() { return PersistentDataContainer.class; }
            public Class<List<T>> getComplexType() { return (Class<List<T>>) (Class<?>) List.class; } // Lists types aren't real they can't hurt you

            @Override
            public PersistentDataContainer toPrimitive(List<T> list, PersistentDataAdapterContext context) {
                PersistentDataContainer container = context.newPersistentDataContainer();
                container.set(KEY_SIZE, PersistentDataType.INTEGER, list.size());
                for (int i = 0; i < list.size(); i++) {
                    T element = list.get(i);
                    if (element == null)
                        continue;
                    NamespacedKey elementKey = KeyHelper.get(String.valueOf(i));
                    container.set(elementKey, listElementDataType, element);
                }
                return container;
            }

            @Override
            public List<T> fromPrimitive(PersistentDataContainer container, PersistentDataAdapterContext context) {
                Integer size = container.get(KEY_SIZE, PersistentDataType.INTEGER);
                if (size == null)
                    throw new IllegalArgumentException("Invalid List<" + listElementDataType.getComplexType().getSimpleName() + ">");
                List<T> list = new ArrayList<>(size);
                for (int i = 0; i < size; i++) {
                    NamespacedKey elementKey = KeyHelper.get(String.valueOf(i));
                    list.add(container.get(elementKey, listElementDataType));
                }
                return list;
            }

        };
    }

    @SuppressWarnings("unchecked")
    public static <K, V> PersistentDataType<PersistentDataContainer, Map<K, V>> forMap(PersistentDataType<?, K> keyDataType, PersistentDataType<?, V> valueDataType) {
        return new PersistentDataType<>() {

            private static final NamespacedKey KEY_SIZE = KeyHelper.get("size");

            public Class<PersistentDataContainer> getPrimitiveType() { return PersistentDataContainer.class; }
            public Class<Map<K, V>> getComplexType() { return (Class<Map<K, V>>) (Class<?>) Map.class; } // Map types aren't real they can't hurt you

            @Override
            public PersistentDataContainer toPrimitive(Map<K, V> map, PersistentDataAdapterContext context) {
                PersistentDataContainer container = context.newPersistentDataContainer();
                container.set(KEY_SIZE, PersistentDataType.INTEGER, map.size());
                int index = 0;
                for (Map.Entry<K, V> entry : map.entrySet()) {
                    K keyValue = entry.getKey();
                    V valueValue = entry.getValue();
                    if (valueValue == null)
                        continue;
                    NamespacedKey keyKey = this.makeKey(true, index);
                    NamespacedKey valueKey = this.makeKey(false, index);
                    container.set(keyKey, keyDataType, keyValue);
                    container.set(valueKey, valueDataType, valueValue);
                    index++;
                }
                return container;
            }

            @Override
            public Map<K, V> fromPrimitive(PersistentDataContainer container, PersistentDataAdapterContext context) {
                Integer size = container.get(KEY_SIZE, PersistentDataType.INTEGER);
                if (size == null)
                    throw new IllegalArgumentException("Invalid Map<" + keyDataType.getComplexType().getSimpleName() + ", " + valueDataType.getComplexType().getSimpleName() + ">");
                Map<K, V> map = new HashMap<>();
                for (int i = 0; i < size; i++) {
                    NamespacedKey keyKey = this.makeKey(true, i);
                    NamespacedKey valueKey = this.makeKey(false, i);
                    K key = container.get(keyKey, keyDataType);
                    V value = container.get(valueKey, valueDataType);
                    map.put(key, value);
                }
                return map;
            }

            private NamespacedKey makeKey(boolean key, int index) {
                return KeyHelper.get(String.format("%s-%d", key ? "key" : "value", index));
            }

        };
    }

    public static final PersistentDataType<PersistentDataContainer, List<String>> STRING_LIST = forList(PersistentDataType.STRING);
    public static final PersistentDataType<String, Sound> SOUND = forKeyed(Sound.class, Registry.SOUNDS::get);
    public static final PersistentDataType<String, SoundCategory> SOUND_CATEGORY = forEnum(SoundCategory.class);
    public static final PersistentDataType<String, WorkerAreaController.RadiusType> RADIUS_TYPE = forEnum(WorkerAreaController.RadiusType.class);
    public static final PersistentDataType<String, WorkerAreaController.ScanDirection> SCAN_DIRECTION = forEnum(WorkerAreaController.ScanDirection.class);
    public static final PersistentDataType<String, PotionEffectType> POTION_EFFECT_TYPE = forKeyed(PotionEffectType.class, Registry.EFFECT::get);

    public static class KeyHelper {

        private static final RosePlugin PLUGIN = RoseMinions.getInstance();
        private static final Map<String, NamespacedKey> CACHE = new HashMap<>();

        public static NamespacedKey get(String key) {
            return CACHE.computeIfAbsent(key, x -> new NamespacedKey(PLUGIN, x));
        }

    }

}
