package dev.rosewood.roseminions.minion.module;

import dev.rosewood.guiframework.GuiFactory;
import dev.rosewood.guiframework.gui.GuiSize;
import dev.rosewood.guiframework.gui.screen.GuiScreen;
import dev.rosewood.rosegarden.config.PDCRoseSetting;
import dev.rosewood.roseminions.hook.StackerHelper;
import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.minion.setting.PDCSettingHolder;
import dev.rosewood.roseminions.object.ModuleGuiProperties;
import dev.rosewood.roseminions.util.MinionUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import static dev.rosewood.roseminions.minion.module.ItemPickupModule.Settings.*;

public class ItemPickupModule extends EntityAttractorModule<Item> {

    public static class Settings implements PDCSettingHolder {

        public static final Settings INSTANCE = new Settings();
        private static final List<PDCRoseSetting<?>> SETTINGS = new ArrayList<>();

        public static final PDCRoseSetting<Integer> RADIUS = define(PDCRoseSetting.ofInteger("radius", 5, "The radius in which to pick up items"));
        public static final PDCRoseSetting<Long> PICKUP_FREQUENCY = define(PDCRoseSetting.ofLong("pickup-frequency", 1000L, "How often items will be picked up (in milliseconds)"));

        static {
            define(MinionModule.GUI_PROPERTIES.copy(() ->
                    new ModuleGuiProperties("Item Pickup Module", Material.HOPPER, MinionUtils.PRIMARY_COLOR + "Item Pickup Module",
                            List.of("", MinionUtils.SECONDARY_COLOR + "Allows the minion to pick up items."))));
        }

        private Settings() { }

        @Override
        public List<PDCRoseSetting<?>> get() {
            return Collections.unmodifiableList(SETTINGS);
        }

        private static <T> PDCRoseSetting<T> define(PDCRoseSetting<T> setting) {
            SETTINGS.add(setting);
            return setting;
        }

    }

    public ItemPickupModule(Minion minion) {
        super(minion, DefaultMinionModules.ITEM_PICKUP, Settings.INSTANCE, PICKUP_FREQUENCY, RADIUS);
    }

    @Override
    protected boolean collect(Item item) {
        Optional<InventoryModule> inventoryModule = this.getModule(InventoryModule.class);

        // Items can never be collected if there is no inventory
        if (inventoryModule.isEmpty())
            return false;

        // Add items to the inventory
        ItemStack itemStack = item.getItemStack();
        int originalAmount = StackerHelper.getItemStackAmount(item);
        int maxStackSize = Math.max(1, itemStack.getMaxStackSize());

        int amount = originalAmount;
        while (amount > 0) {
            ItemStack toAdd = itemStack.clone();
            int amountToAdd = Math.min(amount, maxStackSize);
            toAdd.setAmount(amountToAdd);
            amount -= amountToAdd;
            ItemStack overflow = inventoryModule.get().addItem(toAdd);
            if (overflow != null) {
                amount += overflow.getAmount();
                break;
            }
        }

        if (amount == 0) {
            return true; // Item is fully removed
        } else if (amount != originalAmount) {
            StackerHelper.setItemStackAmount(item, amount);
        }

        return false;
    }

    @Override
    protected boolean testEntity(Entity entity) {
        if (entity.getType() != EntityType.ITEM)
            return false;

        Optional<ItemFilterModule> filterModule = this.getModule(ItemFilterModule.class);
        return filterModule.isEmpty() || filterModule.get().isAllowed(((Item) entity).getItemStack());
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

}
