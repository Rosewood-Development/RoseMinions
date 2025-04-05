package dev.rosewood.roseminions.datatype;

import dev.rosewood.roseminions.nms.NMSAdapter;
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

    private MinionPersistentDataType() {

    }

}
