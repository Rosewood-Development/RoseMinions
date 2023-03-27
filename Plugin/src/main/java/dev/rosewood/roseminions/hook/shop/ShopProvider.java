package dev.rosewood.roseminions.hook.shop;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

public abstract class ShopProvider {

    private final boolean enabled;

    public ShopProvider(String pluginName) {
        this.enabled = Bukkit.getPluginManager().isPluginEnabled(pluginName);
    }

    /**
     * @return true if the provider is enabled, false otherwise
     */
    public final boolean isEnabled() {
        return this.enabled;
    }

    /**
     * Gets the sell price of an itemstack in the shop
     *
     * @param itemStack the itemstack
     * @return the sell price of the itemstack
     */
    public abstract double getSellPrice(ItemStack itemStack);

    /**
     * Gets the buy price of an itemstack in the shop
     *
     * @param itemStack the itemstack
     * @return the buy price of the itemstack
     */
    public abstract double getBuyPrice(ItemStack itemStack);

}
