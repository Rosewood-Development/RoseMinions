package dev.rosewood.roseminions.minion.module;

import com.google.common.collect.Iterables;
import dev.rosewood.guiframework.GuiFactory;
import dev.rosewood.guiframework.framework.util.GuiUtil;
import dev.rosewood.guiframework.gui.ClickAction;
import dev.rosewood.guiframework.gui.GuiIcon;
import dev.rosewood.guiframework.gui.GuiSize;
import dev.rosewood.guiframework.gui.screen.GuiScreen;
import dev.rosewood.rosegarden.utils.HexUtils;
import dev.rosewood.roseminions.RoseMinions;
import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.minion.setting.SettingAccessor;
import dev.rosewood.roseminions.minion.setting.SettingSerializers;
import dev.rosewood.roseminions.minion.setting.SettingsContainer;
import dev.rosewood.roseminions.model.NotificationTicket;
import dev.rosewood.roseminions.nms.NMSAdapter;
import dev.rosewood.roseminions.util.MinionUtils;
import dev.rosewood.roseminions.util.nms.SkullUtils;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
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
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;

public class AppearanceModule extends MinionModule {

    public static final SettingAccessor<Boolean> SMALL;
    public static final SettingAccessor<String> TEXTURE;
    public static final SettingAccessor<String> DISPLAY_NAME;
    public static final SettingAccessor<Double> ROTATION_SPEED;

    static {
        SMALL = SettingsContainer.defineSetting(AppearanceModule.class, SettingSerializers.BOOLEAN, "small", true, "If the skull should be small");
        TEXTURE = SettingsContainer.defineSetting(AppearanceModule.class, SettingSerializers.STRING, "texture", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGUyY2UzMzcyYTNhYzk3ZmRkYTU2MzhiZWYyNGIzYmM0OWY0ZmFjZjc1MWZlOWNhZDY0NWYxNWE3ZmI4Mzk3YyJ9fX0=", "The texture of the skull");
        DISPLAY_NAME = SettingsContainer.defineSetting(AppearanceModule.class, SettingSerializers.STRING, "display-name", "<r#5:0.5>Default Minion", "The display name of the skull");
        ROTATION_SPEED = SettingsContainer.defineSetting(AppearanceModule.class, SettingSerializers.DOUBLE, "rotation-speed", 0.05, "The speed at which the skull should rotate");

        SettingsContainer.redefineSetting(AppearanceModule.class, MinionModule.GUI_TITLE, "Minion Appearance");
        SettingsContainer.redefineSetting(AppearanceModule.class, MinionModule.GUI_ICON, Material.PLAYER_HEAD);
        SettingsContainer.redefineSetting(AppearanceModule.class, MinionModule.GUI_ICON_NAME, MinionUtils.PRIMARY_COLOR + "Minion Appearance");
        SettingsContainer.redefineSetting(AppearanceModule.class, MinionModule.GUI_ICON_LORE, List.of("", MinionUtils.SECONDARY_COLOR + "Allows modifying the minion's appearance.", MinionUtils.SECONDARY_COLOR + "Click to open."));
    }

    private static BukkitTask thetaUpdateTask;
    private static long thetaTicks;
    private double heightOffset;
    private int nametagUpdateTicks;

    private final Deque<NotificationTicket> notificationTickets;
    private long nextTicketTime;

    public AppearanceModule(Minion minion) {
        super(minion, DefaultMinionModules.APPEARANCE);

        if (thetaUpdateTask == null)
            thetaUpdateTask = Bukkit.getScheduler().runTaskTimer(RoseMinions.getInstance(), () -> thetaTicks++, 0L, 1L);

        this.notificationTickets = new LinkedList<>();
    }

    @Override
    public void update() {
        ArmorStand armorStand = this.minion.getDisplayEntity();
        Location centerLocation = this.getCenterVisibleLocation();
        NMSAdapter.getHandler().setPositionRotation(armorStand, centerLocation.clone().subtract(0, this.heightOffset, 0));

        if (System.currentTimeMillis() >= this.nextTicketTime) {
            NotificationTicket notificationTicket = this.getNextNotificationTicket();
            if (notificationTicket == null) {
                this.resetNotificationTicketTimer(500);
                // If we have no notifications, remove the notifications entity if it exists
                for (Entity passenger : armorStand.getPassengers()) {
                    armorStand.removePassenger(passenger);
                    passenger.remove();
                }
            } else {
                this.resetNotificationTicketTimer(notificationTicket.getDuration());

                // If we don't have a notification entity, create one
                Entity notificationEntity = Iterables.getFirst(armorStand.getPassengers(), null);
                if (notificationEntity == null) {
                    notificationEntity = this.createNotificationEntity();
                    armorStand.addPassenger(notificationEntity);
                }

                // Update the notification entity
                notificationEntity.setCustomName(HexUtils.colorify(notificationTicket.getMessage()));
                notificationEntity.setCustomNameVisible(true);
            }
        }
    }

    @Override
    public void updateAsync() {
        ArmorStand armorStand = this.minion.getDisplayEntity();
        this.nametagUpdateTicks = (this.nametagUpdateTicks + 1) % 2;
        if (this.nametagUpdateTicks == 0) {
            String newName = HexUtils.colorify(this.settings.get(DISPLAY_NAME));
            if (!newName.equals(armorStand.getCustomName())) {
                armorStand.setCustomName(newName);
                armorStand.setCustomNameVisible(true);
            }
        }

        if (MinionUtils.RANDOM.nextInt(10) == 0)
            armorStand.getWorld().spawnParticle(Particle.END_ROD, this.getCenterVisibleLocation(), 1, 0.25, 0.25, 0.25, 0);
    }

    @Override
    public void deserialize(byte[] input) {
        super.deserialize(input);
        this.updateEntity();
    }

    @Override
    protected void buildGui() {
        this.guiContainer = GuiFactory.createContainer();
        int rows = Math.max(1, Math.min((int) Math.ceil(36 / 9.0), 3));
        GuiSize editableSize = GuiSize.fromRows(rows);
        GuiSize fullSize = GuiSize.fromRows(rows + 1);

        GuiScreen mainScreen = GuiFactory.createScreen(this.guiContainer, fullSize)
                .setTitle(this.settings.get(MinionModule.GUI_TITLE));

        // Fill inventory border with glass for now
        ItemStack borderItem = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
        ItemMeta itemMeta = borderItem.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setDisplayName(" ");
            itemMeta.addItemFlags(ItemFlag.values());
            borderItem.setItemMeta(itemMeta);
        }

        GuiUtil.fillRow(mainScreen, editableSize.getRows(), borderItem);

//         Name
        mainScreen.addButtonAt(10, GuiFactory.createButton()
                .setIcon(Material.NAME_TAG)
                .setName(HexUtils.colorify(MinionUtils.PRIMARY_COLOR + "Name"))
                .setLore(HexUtils.colorify(MinionUtils.SECONDARY_COLOR + "The name of the minion"))
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

//         Small
        mainScreen.addButtonAt(11, GuiFactory.createButton()
                .setIcon(Material.ARMOR_STAND)
                .setNameSupplier(() -> GuiFactory.createString(HexUtils.colorify(MinionUtils.PRIMARY_COLOR + "Small: " + MinionUtils.SECONDARY_COLOR + this.settings.get(SMALL))))
                .setLore(HexUtils.colorify(MinionUtils.SECONDARY_COLOR + "Toggle the size of the minion"))
                .setClickAction(event -> {
                    this.settings.set(SMALL, !this.settings.get(SMALL));
                    this.updateEntity();
                    this.update();
                    return ClickAction.CLOSE;
                })
        );

//         Texture
        GuiIcon textureIcon = GuiFactory.createIcon(Material.PLAYER_HEAD,
                meta -> SkullUtils.setSkullTexture((SkullMeta) meta, this.settings.get(TEXTURE)));

        mainScreen.addButtonAt(12, GuiFactory.createButton()
                .setIconSupplier(() -> textureIcon)
                .setIcon(Material.PLAYER_HEAD)
                .setName(HexUtils.colorify(MinionUtils.PRIMARY_COLOR + "Texture"))
                .setLore(HexUtils.colorify(MinionUtils.SECONDARY_COLOR + "The texture of the minion"))
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
            if (equipment != null)
                equipment.setItem(EquipmentSlot.HEAD, skullItem);
        }
    }

    public void registerNotificationTicket(NotificationTicket ticket) {
        Bukkit.broadcastMessage("registered notification");
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
        return this.minion.getWorld().spawn(this.minion.getLocation(), AreaEffectCloud.class, entity -> {
            //entity.setInvisible(true);
            //entity.setVisible(false);
            entity.setGravity(false);
            //entity.setSmall(true);
            entity.setSilent(true);
            entity.setInvulnerable(true);
            entity.setRadius(0.5F);
            entity.setParticle(Particle.BLOCK_CRACK, Material.AIR.createBlockData());
            entity.clearCustomEffects();
            //entity.setCanPickupItems(false);
            entity.setPersistent(true);
            entity.getPersistentDataContainer().set(MinionUtils.MINION_NOTIFICATION_KEY, PersistentDataType.BYTE, (byte) 1);

//            Arrays.stream(EquipmentSlot.values()).forEach(x -> {
//                entity.addEquipmentLock(x, ArmorStand.LockType.ADDING_OR_CHANGING);
//                entity.addEquipmentLock(x, ArmorStand.LockType.REMOVING_OR_CHANGING);
//            });
        });
    }

}
