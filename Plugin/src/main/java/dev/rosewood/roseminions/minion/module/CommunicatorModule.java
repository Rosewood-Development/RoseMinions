package dev.rosewood.roseminions.minion.module;

import dev.rosewood.guiframework.GuiFactory;
import dev.rosewood.guiframework.gui.ClickAction;
import dev.rosewood.guiframework.gui.GuiSize;
import dev.rosewood.guiframework.gui.screen.GuiScreen;
import dev.rosewood.rosegarden.utils.HexUtils;
import dev.rosewood.roseminions.RoseMinions;
import dev.rosewood.roseminions.manager.MinionManager;
import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.minion.setting.SettingAccessor;
import dev.rosewood.roseminions.minion.setting.SettingSerializers;
import dev.rosewood.roseminions.minion.setting.SettingsRegistry;
import dev.rosewood.roseminions.model.MinionConversation;
import dev.rosewood.roseminions.nms.NMSAdapter;
import dev.rosewood.roseminions.nms.hologram.Hologram;
import dev.rosewood.roseminions.util.MinionUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CommunicatorModule extends MinionModule {

    public static final SettingAccessor<Long> CONVERSATION_FREQUENCY;
    public static final SettingAccessor<Long> MESSAGE_FREQUENCY;
    public static final SettingAccessor<List<MinionConversation>> CONVERSATIONS;

    static {
        CONVERSATION_FREQUENCY = SettingsRegistry.defineLong(CommunicatorModule.class, "conversation-frequency", 300000L, "How often a conversation will start (in milliseconds)");
        MESSAGE_FREQUENCY = SettingsRegistry.defineLong(CommunicatorModule.class, "message-frequency", 3000L, "How often a message will be sent (in milliseconds)");
        CONVERSATIONS = SettingsRegistry.defineSetting(CommunicatorModule.class, SettingSerializers.ofList(SettingSerializers.MINION_CONVERSATION), "conversations", () -> List.of(
                new MinionConversation(1, 100, 10, List.of("oof", "ouch", "my bones"))
        ), "The conversations that the minion can have");

        SettingsRegistry.redefineString(CommunicatorModule.class, MinionModule.GUI_TITLE, "Communicator Module");
        SettingsRegistry.redefineEnum(CommunicatorModule.class, MinionModule.GUI_ICON, Material.COMMAND_BLOCK);
        SettingsRegistry.redefineString(CommunicatorModule.class, MinionModule.GUI_ICON_NAME, MinionUtils.PRIMARY_COLOR + "Communicator Module");
        SettingsRegistry.redefineStringList(CommunicatorModule.class, MinionModule.GUI_ICON_LORE, List.of("", MinionUtils.SECONDARY_COLOR + "Allows the minion to have", MinionUtils.SECONDARY_COLOR + "conversations with other minions."));
    }

    private long lastConversation; // The last time the minion started a conversation
    private long lastMessage; // The last time the minion sent a message, TODO: Maybe make holograms display for x2 the message frequency
    private int messageIndex; // The last message index
    private boolean isParticipating; // Whether the minion is participating in a conversation

    private MinionConversation active; // The current conversation
    private List<Minion> participants; // All participants in the conversation TODO: Fix participants not working, Fix randomization of speakers
    private Minion lastSpeaker; // The last minion to speak
    private Hologram hologram; // The hologram for the conversation

    public CommunicatorModule(Minion minion) {
        super(minion, DefaultMinionModules.COMMUNICATOR);

        // TODO: Clear all messages when the the plugin is reloaded or disabled

        // Add a delay before the minion can start a conversation
        this.lastConversation = System.currentTimeMillis() + this.settings.get(CONVERSATION_FREQUENCY);
        this.participants = new ArrayList<>();
        this.isParticipating = false;
    }

    @Override
    public void unload() {
        super.unload();

        // Reset all minions in the conversation to their original display names
        this.endConversation();
        this.setParticipating(true);
    }

    @Override
    public void update() {
        if (this.isParticipating && this.active == null) return;

        if (System.currentTimeMillis() - this.lastMessage <= this.settings.get(MESSAGE_FREQUENCY))
            return;

        this.lastMessage = System.currentTimeMillis();

        // Check if there is an active conversation going on
        if (this.active != null) {
            int messageIndex = ++this.messageIndex;

            // End the conversation if there are no more messages
            if (messageIndex >= this.active.messages().size()) {
                this.endConversation();
                return;
            }

            Minion newSpeaker = this.getNewSpeaker();
            String toSend = this.active.messages().get(messageIndex);
            this.updateHologram(newSpeaker, toSend);
            this.lastSpeaker = newSpeaker;
            return;
        }

        // Select a new conversation to have.
        if (System.currentTimeMillis() - this.lastConversation <= this.settings.get(CONVERSATION_FREQUENCY))
            return;

        this.lastConversation = System.currentTimeMillis();

        // Check if there are any conversations that can be started
        MinionConversation conversation = this.getRandomConversation();
        if (conversation == null) return;

        this.active = conversation;
        this.participants = this.getNearbyMinions(conversation.radius());
        this.messageIndex = 0; // Reset the last message index
        this.lastMessage = System.currentTimeMillis();
        this.lastSpeaker = this.minion;

        // Get the first message and update the hologram
        this.updateHologram(this.minion, this.active.messages().get(messageIndex));
        this.involveParticipants(true);
    }

    @Override
    protected void buildGui() {
        this.guiContainer = GuiFactory.createContainer();

        GuiScreen mainScreen = GuiFactory.createScreen(this.guiContainer, GuiSize.ROWS_THREE)
                .setTitle(this.settings.get(GUI_TITLE));

        mainScreen.addButtonAt(0, GuiFactory.createButton(new ItemStack(Material.BIRCH_SIGN))
                .setName(HexUtils.colorify("<g:#F7971E:#FFD200>Start Conversation"))
                .setClickAction(inventoryClickEvent -> {
                    this.endConversation();
                    this.involveParticipants(false);

                    this.lastConversation = 0;
                    this.lastMessage = 0;
                    return ClickAction.CLOSE;
                }));

        this.addBackButton(mainScreen);

        this.guiContainer.addScreen(mainScreen);
        this.guiFramework.getGuiManager().registerGui(this.guiContainer);
    }

    /**
     * Update all the other minions that are in the conversation (Prevent conversations from overlapping)
     */
    private void involveParticipants(boolean isParticipating) {
        this.participants.forEach(x -> {
            if (x.getDisplayEntity().getEntityId() == this.minion.getDisplayEntity().getEntityId())
                return;

            x.getModule(CommunicatorModule.class).ifPresent(module -> module.setParticipating(isParticipating));
        });
    }


    /**
     * Update the hologram with new text (If the hologram is null, create a new one)
     *
     * @param newText The new text for the hologram
     */
    public void updateHologram(Minion where, String newText) {
        if (this.active == null) return;

        Location holoLocation = where.getCenterLocation().clone().add(0, 1.25, 0);

        // Only one participant in the conversation
        if (this.active.participants() == 1 && this.hologram != null) {
            this.hologram.setText(List.of(HexUtils.colorify(newText)));
            this.addWatchers(where);
            return;
        }

        // Multiple participants in the conversation
        // Delete the old hologram
        if (this.hologram != null) {
            this.hologram.delete();
            this.hologram = null;
        }

        // Create a new hologram at the new location
        this.hologram = NMSAdapter.getHandler().createHologram(holoLocation, List.of(HexUtils.colorify(newText)));
        this.addWatchers(where);
    }

    /**
     * End the conversation
     */
    public void endConversation() {
        this.active = null;
        this.participants.clear();
        this.messageIndex = 0;
        this.lastSpeaker = null;
        this.involveParticipants(false);

        // Update the hologram
        if (this.hologram != null) {
            this.hologram.delete();
            this.hologram = null;
        }
    }

    /**
     * Get a new speaker for the conversation
     *
     * @return A new speaker
     */
    private Minion getNewSpeaker() {
        if (this.participants.size() == 1)
            return this.participants.get(0);

        Minion newSpeaker = this.participants.get((int) (Math.random() * this.participants.size()));
        if (newSpeaker == this.lastSpeaker)
            return this.getNewSpeaker();

        return newSpeaker;
    }

    /**
     * Add all nearby players to the hologram's watchers
     *
     * @param minion The minion to add the watchers for
     */
    public void addWatchers(Minion minion) {
        if (this.hologram == null) return;

        minion.getWorld().getNearbyPlayers(minion.getLocation(), 25).forEach(this.hologram::addWatcher);
    }

    /**
     * Get a random conversation based on the chances of each conversation
     *
     * @return A random conversation
     */
    private MinionConversation getRandomConversation() {
        List<MinionConversation> conversations = new ArrayList<>(this.settings.get(CONVERSATIONS));
        conversations.removeIf(conversation -> !conversation.testNearby(this.minion));

        if (conversations.isEmpty())
            return null;

        // Get a random conversation (each conversation has an individual chance) and return it
        double total = conversations.stream().mapToDouble(MinionConversation::chance).sum();
        double random = Math.random() * total;
        return conversations.stream().reduce((a, b) -> random <= a.chance() ? a : b).orElse(null);
    }

    /**
     * Get all nearby minions within a certain radius
     *
     * @param radius The radius to search
     * @return A list of nearby minions
     */
    private List<Minion> getNearbyMinions(int radius) {
        MinionManager minionManager = RoseMinions.getInstance().getManager(MinionManager.class);

        // Get all nearby armorstands, filter for minions, and return the list
        return this.minion.getLocation().getNearbyEntitiesByType(ArmorStand.class, radius, minionManager::isMinion)
                .stream()
                .flatMap(entity -> minionManager.getMinionFromEntity(entity).stream())
                .collect(Collectors.toList());
    }

    /**
     * Check if the minion is already participating in a conversation
     *
     * @return true if the minion is participating
     */
    public boolean isParticipating() {
        return this.isParticipating;
    }

    /**
     * Set whether the minion is already participating in a conversation
     *
     * @param isParticipating Whether the minion is participating
     */
    public void setParticipating(boolean isParticipating) {
        this.isParticipating = isParticipating;
    }


}
