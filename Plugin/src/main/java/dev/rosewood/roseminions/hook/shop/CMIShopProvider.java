package dev.rosewood.roseminions.hook.shop;

import com.Zrips.CMI.CMI;
import org.bukkit.inventory.ItemStack;

public class CMIShopProvider extends ShopProvider {

    public CMIShopProvider() {
        super("CMI");
    }

    @Override
    public double getSellPrice(ItemStack itemStack) {
        if (itemStack == null)
            return -1;

        return CMI.getInstance().getWorthManager().getWorth(itemStack).getSellPrice();
    }

    @Override
    public double getBuyPrice(ItemStack itemStack) {
        if (itemStack == null)
            return -1;

        return CMI.getInstance().getWorthManager().getWorth(itemStack).getSellPrice();
    }

}
