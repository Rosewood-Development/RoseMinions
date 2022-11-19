package dev.rosewood.roseminions.minion.module;

import dev.rosewood.guiframework.GuiFactory;
import dev.rosewood.guiframework.gui.GuiSize;
import dev.rosewood.guiframework.gui.screen.GuiScreen;
import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.minion.setting.SettingAccessor;
import dev.rosewood.roseminions.minion.setting.SettingSerializers;
import dev.rosewood.roseminions.minion.setting.SettingsContainer;
import dev.rosewood.roseminions.util.MinionUtils;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

@MinionModuleInfo(name = "item_pickup")
public class ItemPickupModule extends MinionModule {

    private static final SettingAccessor<Integer> RADIUS;
    private static final SettingAccessor<Long> PICKUP_FREQUENCY;

    static {
        RADIUS = SettingsContainer.defineSetting(ItemPickupModule.class, SettingSerializers.INTEGER, "radius", 5, "The radius in which to pick up items");
        PICKUP_FREQUENCY = SettingsContainer.defineSetting(ItemPickupModule.class, SettingSerializers.LONG, "pickup-frequency", 1000L, "How often items will be picked up (in milliseconds)");
        SettingsContainer.redefineSetting(ItemPickupModule.class, MinionModule.GUI_TITLE, "Item Pickup Module");
        SettingsContainer.redefineSetting(ItemPickupModule.class, MinionModule.GUI_ICON, Material.HOPPER);
        SettingsContainer.redefineSetting(ItemPickupModule.class, MinionModule.GUI_ICON_NAME, MinionUtils.PRIMARY_COLOR + "Item Pickup Module");
        SettingsContainer.redefineSetting(ItemPickupModule.class, MinionModule.GUI_ICON_LORE, List.of("", MinionUtils.SECONDARY_COLOR + "Allows the minion to pick up items.", MinionUtils.SECONDARY_COLOR + "Left-click to open.", MinionUtils.SECONDARY_COLOR + "Right-click to edit settings."));
    }

    private long lastPickupTime;

    public ItemPickupModule(Minion minion) {
        super(minion);
    }

    @Override
    public void update() {
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
        Optional<InventoryModule> inventoryModule = this.minion.getModule(InventoryModule.class);
        if (inventoryModule.isEmpty()) {
            items.forEach(x -> x.teleport(this.minion.getCenterLocation()));
        } else {
            for (Item item : items) {
                // TODO: Support for plugins that stack items
                ItemStack itemStack = item.getItemStack();
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
