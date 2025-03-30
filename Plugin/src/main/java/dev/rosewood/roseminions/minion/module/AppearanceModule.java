package dev.rosewood.roseminions.minion.module;

import com.google.common.collect.Iterables;
import dev.rosewood.guiframework.GuiFactory;
import dev.rosewood.guiframework.framework.util.GuiUtil;
import dev.rosewood.guiframework.gui.ClickAction;
import dev.rosewood.guiframework.gui.GuiIcon;
import dev.rosewood.guiframework.gui.GuiSize;
import dev.rosewood.guiframework.gui.screen.GuiScreen;
import dev.rosewood.rosegarden.utils.EntitySpawnUtil;
import dev.rosewood.rosegarden.utils.HexUtils;
import dev.rosewood.roseminions.RoseMinions;
import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.minion.config.ModuleSettings;
import dev.rosewood.roseminions.minion.setting.SettingAccessor;
import dev.rosewood.roseminions.model.ModuleGuiProperties;
import dev.rosewood.roseminions.model.NotificationTicket;
import dev.rosewood.roseminions.nms.NMSAdapter;
import dev.rosewood.roseminions.nms.NMSHandler;
import dev.rosewood.roseminions.util.MinionUtils;
import dev.rosewood.roseminions.util.SkullUtils;
import dev.rosewood.roseminions.util.VersionUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;
import static dev.rosewood.roseminions.minion.module.AppearanceModule.Settings.*;

public class AppearanceModule extends MinionModule {

    public static class Settings implements ModuleSettings {

        public static final Settings INSTANCE = new Settings();
        private static final List<SettingAccessor<?>> ACCESSORS = new ArrayList<>();

        public static final SettingAccessor<Boolean> SMALL = define(SettingAccessor.defineBoolean("small", true, "If the skull should be small"));
        public static final SettingAccessor<String> TEXTURE = define(SettingAccessor.defineString("texture", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGUyY2UzMzcyYTNhYzk3ZmRkYTU2MzhiZWYyNGIzYmM0OWY0ZmFjZjc1MWZlOWNhZDY0NWYxNWE3ZmI4Mzk3YyJ9fX0=", "The texture of the skull"));
        public static final SettingAccessor<String> DISPLAY_NAME = define(SettingAccessor.defineString("display-name", "<r#5:0.5>Default Minion", "The display name of the skull"));
        public static final SettingAccessor<Double> ROTATION_SPEED = define(SettingAccessor.defineDouble("rotation-speed", 0.05, "The speed at which the skull should rotate"));

        static {
            define(MinionModule.GUI_PROPERTIES.copy(() ->
                    new ModuleGuiProperties("Minion Appearance", Material.PLAYER_HEAD, MinionUtils.PRIMARY_COLOR + "Minion Appearance",
                            List.of("", MinionUtils.SECONDARY_COLOR + "Allows modifying the minion's appearance."))));
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

    private static BukkitTask thetaUpdateTask;
    private static long thetaTicks;
    private double heightOffset;
    private int nametagUpdateTicks;

    private final Deque<NotificationTicket> notificationTickets;
    private NotificationTicket currentTicket;
    private long nextTicketTime;

    public AppearanceModule(Minion minion) {
        super(minion, DefaultMinionModules.APPEARANCE, Settings.INSTANCE);

        if (thetaUpdateTask == null)
            thetaUpdateTask = Bukkit.getScheduler().runTaskTimer(RoseMinions.getInstance(), () -> thetaTicks++, 0L, 1L);

        this.notificationTickets = new LinkedList<>();
        this.currentTicket = null;
        this.nextTicketTime = System.currentTimeMillis() + 1000; // Wait a second before starting notifications
    }

    @Override
    public void update() {
        super.update();

        ArmorStand armorStand = this.minion.getDisplayEntity();
        Location centerLocation = this.getCenterVisibleLocation();
        NMSHandler nmsHandler = NMSAdapter.getHandler();
        nmsHandler.setPositionRotation(armorStand, centerLocation.clone().subtract(0, this.heightOffset, 0));

        if (System.currentTimeMillis() >= this.nextTicketTime) {
            NotificationTicket notificationTicket = this.getNextNotificationTicket();
            if (notificationTicket == null) {
                this.resetNotificationTicketTimer(500);
                // If we have no notifications, remove the notifications entity if it exists
                for (Entity passenger : armorStand.getPassengers()) {
                    armorStand.removePassenger(passenger);
                    passenger.remove();
                }
                this.currentTicket = null;
            } else if (!Objects.equals(this.currentTicket, notificationTicket)) {
                this.resetNotificationTicketTimer(notificationTicket.getDuration());

                // If we don't have a notification entity, create one
                Entity notificationEntity = Iterables.getFirst(armorStand.getPassengers(), null);
                if (notificationEntity == null) {
                    notificationEntity = this.createNotificationEntity();
                    armorStand.addPassenger(notificationEntity);
                }

                // Update the notification entity
                nmsHandler.setCustomNameUncapped(notificationEntity, HexUtils.colorify(notificationTicket.getMessage()));
                notificationEntity.setCustomNameVisible(true);
                this.currentTicket = notificationTicket;
            } else {
                this.resetNotificationTicketTimer(notificationTicket.getDuration());
            }
        }
    }

    @Override
    public void updateAsync() {
        super.updateAsync();

        ArmorStand armorStand = this.minion.getDisplayEntity();
        if (armorStand == null)
            return;

        this.nametagUpdateTicks = (this.nametagUpdateTicks + 1) % 2;
        if (this.nametagUpdateTicks == 0) {
            String newName = HexUtils.colorify(this.settings.get(DISPLAY_NAME));
            if (!newName.equals(armorStand.getCustomName())) {
                NMSAdapter.getHandler().setCustomNameUncapped(armorStand, newName);
                armorStand.setCustomNameVisible(true);
            }
        }

        if (MinionUtils.RANDOM.nextInt(10) == 0)
            armorStand.getWorld().spawnParticle(Particle.END_ROD, this.getCenterVisibleLocation(), 1, 0.25, 0.25, 0.25, 0);
    }

    @Override
    public void unload() {
        super.unload();

        // Remove notification entity
        ArmorStand armorStand = this.minion.getDisplayEntity();
        if (armorStand != null) {
            for (Entity passenger : armorStand.getPassengers()) {
                armorStand.removePassenger(passenger);
                passenger.remove();
            }
        }
    }

    @Override
    public void readPDC(PersistentDataContainer container) {
        super.readPDC(container);
        this.updateEntity();
    }

    @Override
    protected void buildGui() {
        this.guiContainer = GuiFactory.createContainer();
        int rows = Math.max(1, Math.min((int) Math.ceil(36 / 9.0), 3));
        GuiSize editableSize = GuiSize.fromRows(rows);
        GuiSize fullSize = GuiSize.fromRows(rows + 1);

        GuiScreen mainScreen = GuiFactory.createScreen(this.guiContainer, fullSize)
                .setTitle(this.settings.get(MinionModule.GUI_PROPERTIES).title());

        // Fill inventory border with glass for now
        ItemStack borderItem = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
        ItemMeta itemMeta = borderItem.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setDisplayName(" ");
            itemMeta.addItemFlags(ItemFlag.values());
            borderItem.setItemMeta(itemMeta);
        }

        GuiUtil.fillRow(mainScreen, editableSize.getRows(), borderItem);

        // Name
        mainScreen.addButtonAt(10, GuiFactory.createButton()
                .setIcon(Material.NAME_TAG)
                .setName(HexUtils.colorify(MinionUtils.PRIMARY_COLOR + "Name"))
                .setLore(HexUtils.colorify(MinionUtils.SECONDARY_COLOR + "The name of the minion"))
                .setItemFlags()
                .setClickAction(event -> {
                    event.getWhoClicked().sendMessage((HexUtils.colorify(MinionUtils.PRIMARY_COLOR + "Enter the new name of the minion:")));
                    this.createConversation((Conversable) event.getWhoClicked(), newName -> {
                        this.settings.set(DISPLAY_NAME, newName);
                        this.updateEntity();
                        this.update();

                        event.getWhoClicked().sendMessage((HexUtils.colorify(MinionUtils.PRIMARY_COLOR + "The name of the minion has been changed to " + MinionUtils.SECONDARY_COLOR + newName)));
                    });

                    return ClickAction.CLOSE;
                })
        );

        // Size
        mainScreen.addButtonAt(11, GuiFactory.createButton()
                .setIcon(Material.ARMOR_STAND)
                .setNameSupplier(() -> GuiFactory.createString(HexUtils.colorify(MinionUtils.PRIMARY_COLOR + "Small: " + MinionUtils.SECONDARY_COLOR + this.settings.get(SMALL))))
                .setLore(HexUtils.colorify(MinionUtils.SECONDARY_COLOR + "Toggle the size of the minion"))
                .setItemFlags()
                .setClickAction(event -> {
                    this.settings.set(SMALL, !this.settings.get(SMALL));
                    this.updateEntity();
                    this.update();
                    return ClickAction.CLOSE;
                })
        );

        // Texture
        GuiIcon textureIcon = GuiFactory.createIcon(Material.PLAYER_HEAD,
                meta -> SkullUtils.setSkullTexture((SkullMeta) meta, this.settings.get(TEXTURE)));

        mainScreen.addButtonAt(12, GuiFactory.createButton()
                .setIconSupplier(() -> textureIcon)
                .setIcon(Material.PLAYER_HEAD)
                .setName(HexUtils.colorify(MinionUtils.PRIMARY_COLOR + "Texture"))
                .setLore(HexUtils.colorify(MinionUtils.SECONDARY_COLOR + "The texture of the minion"))
                .setItemFlags()
                .setClickAction(event -> {
                    event.getWhoClicked().sendMessage(HexUtils.colorify(MinionUtils.PRIMARY_COLOR + "Enter the new texture of the minion:"));
                    this.createConversation((Conversable) event.getWhoClicked(), newTexture -> {
                        this.settings.set(TEXTURE, newTexture);
                        this.updateEntity();
                        this.update();

                        event.getWhoClicked().sendMessage(HexUtils.colorify(MinionUtils.PRIMARY_COLOR + "Texture updated!"));
                    });
                    // TODO: Add waiting for input here
                    return ClickAction.CLOSE;
                })
        );

        this.addBackButton(mainScreen);

        this.guiContainer.addScreen(mainScreen);
        this.guiFramework.getGuiManager().registerGui(this.guiContainer);
    }

    public void updateEntity() {
        ArmorStand armorStand = this.minion.getDisplayEntity();
        if (armorStand == null)
            return;

        if (this.settings.get(SMALL)) {
            armorStand.setSmall(true);
            this.heightOffset = 1.0;
        } else {
            armorStand.setSmall(false);
            this.heightOffset = 1.5;
        }

        ItemStack skullItem = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta itemMeta = skullItem.getItemMeta();
        if (itemMeta instanceof SkullMeta skullMeta) {
            SkullUtils.setSkullTexture(skullMeta, this.settings.get(TEXTURE));
            skullItem.setItemMeta(itemMeta);
            EntityEquipment equipment = armorStand.getEquipment();
            equipment.setItem(EquipmentSlot.HEAD, skullItem);
        }
    }

    public void registerNotificationTicket(NotificationTicket ticket) {
        this.notificationTickets.addLast(ticket);
    }

    public void unregisterNotificationTicket(MinionModule minionModule, String id) {
        this.notificationTickets.removeIf(ticket -> ticket.isFor(minionModule, id));
    }

    public void unregisterNotificationTickets(MinionModule minionModule) {
        this.notificationTickets.removeIf(ticket -> ticket.isFor(minionModule));
    }

    /**
     * Find the next ticket closest to the front of the list, move it to the front of the list, and return it.
     *
     * @return the next available notification ticket to display
     */
    private NotificationTicket getNextNotificationTicket() {
        for (NotificationTicket ticket : this.notificationTickets) {
            if (ticket.isVisible()) {
                this.notificationTickets.remove(ticket);
                this.notificationTickets.addFirst(ticket);
                return ticket;
            }
        }
        return null;
    }

    private void resetNotificationTicketTimer(long milliseconds) {
        this.nextTicketTime = System.currentTimeMillis() + milliseconds;
    }

    private Location getCenterVisibleLocation() {
        // Make the armor stand hover and spin in place around the location
        double theta = thetaTicks * this.settings.get(ROTATION_SPEED);
        Location centerLocation = this.minion.getCenterLocation().add(0, 0.5, 0);
        centerLocation.setY(centerLocation.getY() - this.heightOffset + Math.sin(theta) * 0.2 + this.heightOffset);
        centerLocation.setYaw((float) theta * 100);
        return centerLocation;
    }

    /**
     * Creates a conversation with the given conversable
     *
     * @param who    The conversable to create the conversation with
     * @param output The output of the conversation
     */
    private void createConversation(Conversable who, Consumer<String> output) {
        ConversationFactory factory = new ConversationFactory(this.guiFramework.getHookedPlugin());
        factory.withTimeout(120); // 2 minutes
        factory.withModality(true); // Suppresses chat messages
        factory.withLocalEcho(false); // Suppresses the user's input (we don't want to see it
//        factory.withInitialSessionData() // TODO: Add the default value as the initial session data
        factory.withFirstPrompt(new StringPrompt() { // Create a new prompt
            @Override
            public String getPromptText(ConversationContext context) {
                return context.getSessionData("input") == null ? "" : (String) context.getSessionData("input");
            }

            @Override
            public Prompt acceptInput(ConversationContext context, String input) {
                if (input != null) {
                    context.setSessionData("input", input);
                    return Prompt.END_OF_CONVERSATION;
                }
                return this;
            }
        });

        factory.addConversationAbandonedListener(abandonedEvent -> {
            if (abandonedEvent.gracefulExit()) {
                Object newName = abandonedEvent.getContext().getSessionData("input");
                if (newName == null)
                    return;

                // TODO: Add ability to cancel the conversation
                output.accept((String) newName);
            }
        });

        who.beginConversation(factory.buildConversation(who));
    }

    private Entity createNotificationEntity() {
        return EntitySpawnUtil.spawn(this.minion.getLocation().add(0.5, this.minion.getDisplayEntity().getHeight(), 0.5), AreaEffectCloud.class, entity -> {
            //entity.setInvisible(true);
            //entity.setVisible(false);
            entity.setGravity(false);
            //entity.setSmall(true);
            entity.setSilent(true);
            entity.setInvulnerable(true);
            entity.setRadius(0.5F);
            entity.setParticle(VersionUtils.BLOCK, Material.AIR.createBlockData());
            entity.clearCustomEffects();
            //entity.setCanPickupItems(false);
            entity.setPersistent(false);
            entity.getPersistentDataContainer().set(MinionUtils.MINION_NOTIFICATION_KEY, PersistentDataType.BYTE, (byte) 1);

//            Arrays.stream(EquipmentSlot.values()).forEach(x -> {
//                entity.addEquipmentLock(x, ArmorStand.LockType.ADDING_OR_CHANGING);
//                entity.addEquipmentLock(x, ArmorStand.LockType.REMOVING_OR_CHANGING);
//            });
        });
    }

}
