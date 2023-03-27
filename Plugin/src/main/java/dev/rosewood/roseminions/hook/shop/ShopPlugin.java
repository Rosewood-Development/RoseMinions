package dev.rosewood.roseminions.hook.shop;

import dev.rosewood.roseminions.util.Lazy;
import org.bukkit.inventory.ItemStack;
import java.util.function.Supplier;

public enum ShopPlugin {

    SHOPGUIPLUS(ShopGUIPlusProvider::new),
    ECONOMYSHOPGUI(EconomyShopGUIProvider::new),
    // TODO: Add support for BossShopPro
    ESSENTIALS(EssentialsShopProvider::new),
    CMI(CMIShopProvider::new);

    private final Lazy<ShopProvider> shopProvider;

    ShopPlugin(Supplier<ShopProvider> shopProvider) {
        this.shopProvider = new Lazy<>(shopProvider);
    }

    /**
     * @return true if the provider is enabled, false otherwise
     */
    public boolean isEnabled() {
        return this.shopProvider.get().isEnabled();
    }

    /**
     * Gets the sell price of an itemstack in the shop
     *
     * @param itemStack the itemstack
     * @return the sell price of the itemstack
     */
    public double getSellPrice(ItemStack itemStack) {
        return this.shopProvider.get().getSellPrice(itemStack);
    }

    /**
     * Gets the buy price of an itemstack in the shop
     *
     * @param itemStack the itemstack
     * @return the buy price of the itemstack
     */
    public double getBuyPrice(ItemStack itemStack) {
        return this.shopProvider.get().getBuyPrice(itemStack);
    }

}
