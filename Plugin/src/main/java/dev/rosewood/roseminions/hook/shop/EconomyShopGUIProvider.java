package dev.rosewood.roseminions.hook.shop;

import me.gypopo.economyshopgui.api.EconomyShopGUIHook;
import me.gypopo.economyshopgui.objects.ShopItem;
import org.bukkit.inventory.ItemStack;

public class EconomyShopGUIProvider extends ShopProvider {

    public EconomyShopGUIProvider() {
        super("EconomyShopGUI");
    }

    /**
     * Gets the sell price of an itemstack in the shop
     *
     * @param itemStack the itemstack
     * @return the sell price of the itemstack
     */
    @Override
    public double getSellPrice(ItemStack itemStack) {
        if (itemStack == null) return -1;

        ShopItem shopItem = EconomyShopGUIHook.getShopItem(itemStack);
        if (shopItem == null) return -1; // No shop item found

        return EconomyShopGUIHook.getItemSellPrice(shopItem, itemStack);
    }

    /**
     * Gets the buy price of an itemstack in the shop
     *
     * @param itemStack the itemstack
     * @return the buy price of the itemstack
     */
    @Override
    public double getBuyPrice(ItemStack itemStack) {
        if (itemStack == null) return -1;

        ShopItem shopItem = EconomyShopGUIHook.getShopItem(itemStack);
        if (shopItem == null) return -1; // No shop item found

        return EconomyShopGUIHook.getItemBuyPrice(shopItem, itemStack);
    }

}
