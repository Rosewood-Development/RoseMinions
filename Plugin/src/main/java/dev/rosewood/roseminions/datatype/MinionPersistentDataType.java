package dev.rosewood.roseminions.datatype;

import dev.rosewood.roseminions.nms.NMSAdapter;
import org.bukkit.Color;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;

public final class MinionPersistentDataType {

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

    public static final PersistentDataType<Integer, Color> COLOR_RGB = new PersistentDataType<>() {

        public Class<Integer> getPrimitiveType() { return Integer.class; }
        public Class<Color> getComplexType() { return Color.class; }

        @Override
        public Integer toPrimitive(Color color, PersistentDataAdapterContext context) {
            return color.asRGB();
        }

        @Override
        public Color fromPrimitive(Integer primitive, PersistentDataAdapterContext context) {
            return Color.fromRGB(primitive);
        }

    };

    public static final PersistentDataType<Integer, Color> COLOR_ARGB = new PersistentDataType<>() {

        public Class<Integer> getPrimitiveType() { return Integer.class; }
        public Class<Color> getComplexType() { return Color.class; }

        @Override
        public Integer toPrimitive(Color color, PersistentDataAdapterContext context) {
            return color.asARGB();
        }

        @Override
        public Color fromPrimitive(Integer primitive, PersistentDataAdapterContext context) {
            return Color.fromARGB(primitive);
        }

    };

    private MinionPersistentDataType() {

    }

}
