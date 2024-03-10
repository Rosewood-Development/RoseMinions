package dev.rosewood.roseminions.hook;

import dev.rosewood.rosestacker.api.RoseStackerAPI;
import dev.rosewood.rosestacker.stack.StackedItem;
import org.bukkit.Bukkit;
import org.bukkit.entity.Item;

public final class StackerHelper {

    private static Boolean roseStackerEnabled;

    private StackerHelper() {

    }

    private static boolean isRoseStackerEnabled() {
        if (roseStackerEnabled != null)
            return roseStackerEnabled;
        return roseStackerEnabled = Bukkit.getPluginManager().isPluginEnabled("RoseStacker");
    }

    public static int getItemStackAmount(Item item) {
        if (item == null)
            return 0;

        if (isRoseStackerEnabled()) {
            StackedItem stackedItem = RoseStackerAPI.getInstance().getStackedItem(item);
            if (stackedItem != null)
                return stackedItem.getStackSize();
        }

        return item.getItemStack().getAmount();
    }

    public static void setItemStackAmount(Item item, int amount) {
        if (item == null)
            return;

        if (isRoseStackerEnabled()) {
            StackedItem stackedItem = RoseStackerAPI.getInstance().getStackedItem(item);
            if (stackedItem != null)
                stackedItem.setStackSize(amount);
        } else {
            item.getItemStack().setAmount(amount);
        }
    }

}
