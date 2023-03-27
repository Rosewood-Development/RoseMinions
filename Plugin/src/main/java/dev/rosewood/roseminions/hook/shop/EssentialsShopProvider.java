package dev.rosewood.roseminions.hook.shop;

import com.earth2me.essentials.Essentials;
import org.bukkit.inventory.ItemStack;

public class EssentialsShopProvider extends ShopProvider {

    public EssentialsShopProvider() {
        super("Essentials");
    }

    @Override
    public double getSellPrice(ItemStack itemStack) {
        if (itemStack == null) return -1;

        Essentials essentials = Essentials.getPlugin(Essentials.class);
        return essentials.getWorth().getPrice(essentials, itemStack).doubleValue();
    }

    @Override
    public double getBuyPrice(ItemStack itemStack) {
        if (itemStack == null)
            return -1;

        Essentials essentials = Essentials.getPlugin(Essentials.class);
        return essentials.getWorth().getPrice(essentials, itemStack).doubleValue();
    }

}

