package dev.rosewood.roseminions.minion.setting;

import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.roseminions.model.DataSerializable;
import dev.rosewood.roseminions.nms.NMSAdapter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

public class SettingSerializers {

    public static final SettingSerializer<Boolean> BOOLEAN = new SettingSerializer<>() {
        public void write(CommentedFileConfiguration config, String key, Boolean value, String... comments) { config.set(key, value, comments); }
        public byte[] write(Boolean value) { return this.writeValue(value, ObjectOutputStream::writeBoolean); }
        public Boolean read(ConfigurationSection config, String key) { return config.getBoolean(key); }
        public Boolean read(byte[] input) { return this.readValue(input, ObjectInputStream::readBoolean); }
    };

    public static final SettingSerializer<Integer> INTEGER = new SettingSerializer<>() {
        public void write(CommentedFileConfiguration config, String key, Integer value, String... comments) { config.set(key, value, comments); }
        public byte[] write(Integer value) { return this.writeValue(value, ObjectOutputStream::writeInt); }
        public Integer read(ConfigurationSection config, String key) { return config.getInt(key); }
        public Integer read(byte[] input) { return this.readValue(input, ObjectInputStream::readInt); }
    };

    public static final SettingSerializer<Long> LONG = new SettingSerializer<>() {
        public void write(CommentedFileConfiguration config, String key, Long value, String... comments) { config.set(key, value, comments); }
        public byte[] write(Long value) { return this.writeValue(value, ObjectOutputStream::writeLong); }
        public Long read(ConfigurationSection config, String key) { return config.getLong(key); }
        public Long read(byte[] input) { return this.readValue(input, ObjectInputStream::readLong); }
    };

    public static final SettingSerializer<Double> DOUBLE = new SettingSerializer<>() {
        public void write(CommentedFileConfiguration config, String key, Double value, String... comments) { config.set(key, value, comments); }
        public byte[] write(Double value) { return this.writeValue(value, ObjectOutputStream::writeDouble); }
        public Double read(ConfigurationSection config, String key) { return config.getDouble(key); }
        public Double read(byte[] input) { return this.readValue(input, ObjectInputStream::readDouble); }
    };

    public static final SettingSerializer<String> STRING = new SettingSerializer<>() {
        public void write(CommentedFileConfiguration config, String key, String value, String... comments) { config.set(key, value, comments); }
        public byte[] write(String value) { return this.writeValue(value, ObjectOutputStream::writeUTF); }
        public String read(ConfigurationSection config, String key) { return config.getString(key); }
        public String read(byte[] input) { return this.readValue(input, ObjectInputStream::readUTF); }
    };

    public static final SettingSerializer<List<String>> STRING_LIST = new SettingSerializer<>() {
        public void write(CommentedFileConfiguration config, String key, List<String> value, String... comments) { config.set(key, value, comments); }
        public byte[] write(List<String> value) {
            return DataSerializable.write(x -> {
                x.writeInt(value.size());
                for (String s : value)
                    x.writeUTF(s);
            });
        }
        public List<String> read(ConfigurationSection config, String key) { return config.getStringList(key); }
        public List<String> read(byte[] input) {
            List<String> list = new ArrayList<>();
            DataSerializable.read(input, x -> {
                int size = x.readInt();
                for (int i = 0; i < size; i++)
                    list.add(x.readUTF());
            });
            return list;
        }
    };

    public static final SettingSerializer<Material> MATERIAL = new SettingSerializer<>() {
        public void write(CommentedFileConfiguration config, String key, Material value, String... comments) { config.set(key, value.name(), comments); }
        public byte[] write(Material value) { return DataSerializable.write(x -> x.writeUTF(value.name())); }
        public Material read(ConfigurationSection config, String key) { return Material.getMaterial(config.getString(key, "BARRIER")); }
        public Material read(byte[] input) {
            AtomicReference<Material> value = new AtomicReference<>();
            DataSerializable.read(input, x -> value.set(Material.getMaterial(x.readUTF())));
            return value.get();
        }
    };

    public static final SettingSerializer<ItemStack[]> ITEMSTACK_ARRAY = new SettingSerializer<>() {
        public void write(CommentedFileConfiguration config, String key, ItemStack[] value, String... comments) { throw new IllegalStateException("Cannot write ItemStack[] to a ConfigurationSection"); }
        public byte[] write(ItemStack[] value) {
            return DataSerializable.write(x -> {
                byte[] data = NMSAdapter.getHandler().serializeItemStacks(value);
                x.writeInt(data.length);
                x.write(data);
            });
        }
        public ItemStack[] read(ConfigurationSection config, String key) { throw new IllegalStateException("Cannot read ItemStack[] from a ConfigurationSection"); }
        public ItemStack[] read(byte[] input) {
            AtomicReference<ItemStack[]> value = new AtomicReference<>();
            DataSerializable.read(input, x -> {
                int length = x.readInt();
                byte[] data = new byte[length];
                x.readFully(data);
                value.set(NMSAdapter.getHandler().deserializeItemStacks(data));
            });
            return value.get();
        }

    };

    public static final SettingSerializer<BlockFace> BLOCK_FACE = new SettingSerializer<>() {
        public void write(CommentedFileConfiguration config, String key, BlockFace value, String... comments) { config.set(key, value.name(), comments); }
        public byte[] write(BlockFace value) { return DataSerializable.write(x -> x.writeUTF(value.name())); }

        public BlockFace read(ConfigurationSection config, String key) {
            String name = config.getString(key, "NORTH");
            try {
                return BlockFace.valueOf(name);
            } catch (IllegalArgumentException e) {
                return BlockFace.NORTH;
            }
        }

        public BlockFace read(byte[] input) {
            AtomicReference<BlockFace> value = new AtomicReference<>();
            DataSerializable.read(input, x -> value.set(BlockFace.valueOf(x.readUTF())));
            return value.get();
        }
    };

}
