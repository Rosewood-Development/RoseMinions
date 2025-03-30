package dev.rosewood.roseminions.minion.module;

import dev.rosewood.guiframework.GuiFactory;
import dev.rosewood.guiframework.gui.GuiSize;
import dev.rosewood.guiframework.gui.screen.GuiScreen;
import dev.rosewood.roseminions.hook.StackerHelper;
import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.minion.config.ModuleSettings;
import dev.rosewood.roseminions.minion.setting.SettingAccessor;
import dev.rosewood.roseminions.model.ModuleGuiProperties;
import dev.rosewood.roseminions.util.MinionUtils;
import dev.rosewood.roseminions.util.VersionUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import static dev.rosewood.roseminions.minion.module.ItemPickupModule.Settings.*;

public class ItemPickupModule extends MinionModule {

    public static class Settings implements ModuleSettings {

        public static final Settings INSTANCE = new Settings();
        private static final List<SettingAccessor<?>> ACCESSORS = new ArrayList<>();

        public static final SettingAccessor<Integer> RADIUS = define(SettingAccessor.defineInteger("radius", 5, "The radius in which to pick up items"));
        public static final SettingAccessor<Long> PICKUP_FREQUENCY = define(SettingAccessor.defineLong("pickup-frequency", 1000L, "How often items will be picked up (in milliseconds)"));

        static {
            define(MinionModule.GUI_PROPERTIES.copy(() ->
                    new ModuleGuiProperties("Item Pickup Module", Material.HOPPER, MinionUtils.PRIMARY_COLOR + "Item Pickup Module",
                            List.of("", MinionUtils.SECONDARY_COLOR + "Allows the minion to pick up items."))));
        }

        private Settings() { }

        @Override
        public List<SettingAccessor<?>> get() {
            return Collections.unmodifiableList(ACCESSORS);
        }

        private static <T> SettingAccessor<T> define(SettingAccessor<T> accessor) {
            ACCESSORS.add(accessor);
            return accessor;
        }

    }

    private long lastPickupTime;

    public ItemPickupModule(Minion minion) {
        super(minion, DefaultMinionModules.ITEM_PICKUP, Settings.INSTANCE);
    }

    @Override
    public void update() {
        super.update();

        if (System.currentTimeMillis() - this.lastPickupTime <= this.settings.get(PICKUP_FREQUENCY))
            return;

        this.lastPickupTime = System.currentTimeMillis();

        int radius = this.settings.get(RADIUS);
        this.pickup(this.minion.getWorld().getNearbyEntities(this.minion.getCenterLocation(), radius, radius, radius, entity -> entity.getType() == VersionUtils.ITEM)
                .stream()
                .map(x -> (Item) x)
                .collect(Collectors.toList()));
    }

    @Override
    protected void buildGui() {
        this.guiContainer = GuiFactory.createContainer();

        GuiScreen mainScreen = GuiFactory.createScreen(this.guiContainer, GuiSize.ROWS_THREE)
                .setTitle(this.settings.get(MinionModule.GUI_PROPERTIES).title());

        this.addBackButton(mainScreen);

        this.guiContainer.addScreen(mainScreen);
        this.guiFramework.getGuiManager().registerGui(this.guiContainer);
    }

    private void pickup(List<Item> items) {
        Optional<InventoryModule> inventoryModule = this.getModule(InventoryModule.class);
        Optional<ItemFilterModule> filterModule = this.getModule(ItemFilterModule.class);

        if (inventoryModule.isEmpty()) {
            // Teleport items directly under the minion
            for (Item item : items)
                if (filterModule.isEmpty() || filterModule.get().isAllowed(item.getItemStack()))
                    item.teleport(this.minion.getCenterLocation());
            return;
        }

        // Add items to the inventory
        for (Item item : items) {
            ItemStack itemStack = item.getItemStack();
            int originalAmount = StackerHelper.getItemStackAmount(item);
            int maxStackSize = Math.max(1, itemStack.getMaxStackSize());

            // Don't pick up items that are filtered
            if (filterModule.isPresent() && !filterModule.get().isAllowed(itemStack))
                continue;

            int amount = originalAmount;
            while (amount > 0) {
                ItemStack toAdd = itemStack.clone();
                int amountToAdd = Math.min(amount, maxStackSize);
                toAdd.setAmount(amountToAdd);
                amount -= amountToAdd;
                ItemStack overflow = inventoryModule.get().addItem(toAdd);
                if (overflow != null) {
                    amount += overflow.getAmount();
                    item.teleport(this.minion.getCenterLocation());
                    break;
                }
            }

            if (amount == 0) {
                item.remove();
            } else if (amount != originalAmount) {
                StackerHelper.setItemStackAmount(item, amount);
            }
        }
    }

}
