package dev.rosewood.roseminions.hook.shop;

import net.brcdev.shopgui.ShopGuiPlusApi;
import org.bukkit.inventory.ItemStack;

public class ShopGUIPlusProvider extends ShopProvider {

    public ShopGUIPlusProvider() {
        super("ShopGUIPlus");
    }

    /**
     * Gets the sell price of an itemstack in the shop
     *
     * @param itemStack the itemstack
     * @return the sell price of the itemstack
     */
    @Override
    public double getSellPrice(ItemStack itemStack) {
        if (itemStack == null)
            return -1;

        return ShopGuiPlusApi.getItemStackPriceSell(itemStack);
    }

    /**
     * Gets the buy price of an itemstack in the shop
     *
     * @param itemStack the itemstack
     * @return the buy price of the itemstack
     */
    @Override
    public double getBuyPrice(ItemStack itemStack) {
        if (itemStack == null)
            return 0;

        return ShopGuiPlusApi.getItemStackPriceBuy(itemStack);
    }

}
