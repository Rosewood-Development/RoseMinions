package dev.rosewood.roseminions.minion.module;

import dev.rosewood.guiframework.GuiFactory;
import dev.rosewood.guiframework.gui.GuiSize;
import dev.rosewood.guiframework.gui.screen.GuiScreen;
import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.minion.setting.SettingAccessor;
import dev.rosewood.roseminions.minion.setting.SettingsRegistry;
import dev.rosewood.roseminions.util.MinionUtils;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

public class ItemPickupModule extends MinionModule {

    public static final SettingAccessor<Integer> RADIUS;
    public static final SettingAccessor<Long> PICKUP_FREQUENCY;

    static {
        RADIUS = SettingsRegistry.defineInteger(ItemPickupModule.class, "radius", 5, "The radius in which to pick up items");
        PICKUP_FREQUENCY = SettingsRegistry.defineLong(ItemPickupModule.class, "pickup-frequency", 1000L, "How often items will be picked up (in milliseconds)");

        SettingsRegistry.redefineString(ItemPickupModule.class, MinionModule.GUI_TITLE, "Item Pickup Module");
        SettingsRegistry.redefineEnum(ItemPickupModule.class, MinionModule.GUI_ICON, Material.HOPPER);
        SettingsRegistry.redefineString(ItemPickupModule.class, MinionModule.GUI_ICON_NAME, MinionUtils.PRIMARY_COLOR + "Item Pickup Module");
        SettingsRegistry.redefineStringList(ItemPickupModule.class, MinionModule.GUI_ICON_LORE, List.of("", MinionUtils.SECONDARY_COLOR + "Allows the minion to pick up items.", MinionUtils.SECONDARY_COLOR + "Click to open."));
    }

    private long lastPickupTime;

    public ItemPickupModule(Minion minion) {
        super(minion, DefaultMinionModules.ITEM_PICKUP);
    }

    @Override
    public void update() {
        super.update();

        if (System.currentTimeMillis() - this.lastPickupTime <= this.settings.get(PICKUP_FREQUENCY))
            return;

        this.lastPickupTime = System.currentTimeMillis();

        int radius = this.settings.get(RADIUS);
        this.pickup(this.minion.getWorld().getNearbyEntities(this.minion.getCenterLocation(), radius, radius, radius, entity -> entity.getType() == EntityType.DROPPED_ITEM)
                .stream()
                .map(x -> (Item) x)
                .collect(Collectors.toList()));
    }

    @Override
    protected void buildGui() {
        this.guiContainer = GuiFactory.createContainer();

        GuiScreen mainScreen = GuiFactory.createScreen(this.guiContainer, GuiSize.ROWS_THREE)
                .setTitle(this.settings.get(MinionModule.GUI_TITLE));

        this.addBackButton(mainScreen);

        this.guiContainer.addScreen(mainScreen);
        this.guiFramework.getGuiManager().registerGui(this.guiContainer);
    }

    private void pickup(List<Item> items) {
        Optional<InventoryModule> inventoryModule = this.getModule(InventoryModule.class);
        Optional<FilterModule> filterModule = this.getModule(FilterModule.class);

        if (inventoryModule.isEmpty()) {
            // Teleport items directly under the minion
            for (Item item : items)
                if (filterModule.isEmpty() || filterModule.get().isAllowed(item.getItemStack()))
                    item.teleport(this.minion.getCenterLocation());
        } else {
            // Add items to the inventory
            for (Item item : items) {
                // TODO: Support for plugins that stack items
                ItemStack itemStack = item.getItemStack();

                // Don't pick up items that are filtered
                if (filterModule.isPresent() && !filterModule.get().isAllowed(itemStack))
                    continue;

                ItemStack overflow = inventoryModule.get().addItem(itemStack);
                if (overflow == null) {
                    item.remove();
                } else {
                    item.setItemStack(overflow);
                    item.teleport(this.minion.getCenterLocation());
                }
            }
        }
    }

}
