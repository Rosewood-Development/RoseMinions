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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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

    public long lastConversation; // The last time the minion started a conversation
    public long lastMessage; // The last time the minion sent a message, TODO: Maybe make holograms display for x2 the message frequency

    // Active minions in conversation
    public MinionConversation active; // The current conversation
    public List<Minion> participants; // All participants in the conversation TODO: Fix participants not working, Fix randomization of speakers
    public Minion lastSpeaker; // The last minion to speak
    public int lastMessageIndex; // The last message index
    public Hologram hologram; // The hologram for the conversation

    public CommunicatorModule(Minion minion) {
        super(minion, DefaultMinionModules.COMMUNICATOR);

        // TODO: Clear all messages when the the plugin is reloaded or disabled

        // Add a delay before the minion can start a conversation
        this.lastConversation = System.currentTimeMillis() + this.settings.get(CONVERSATION_FREQUENCY);
        this.participants = new ArrayList<>();
        this.lastMessageIndex = -1;
    }

    @Override
    public void unload() {
        super.unload();

        // Reset all minions in the conversation to their original display names
        this.resetConversation();
        this.updateCommunicators(true);
    }

    @Override
    public void update() {
        if (System.currentTimeMillis() - this.lastMessage <= this.settings.get(MESSAGE_FREQUENCY))
            return;

        this.lastMessage = System.currentTimeMillis();

        if (this.active == null) {
            if (System.currentTimeMillis() - this.lastConversation <= this.settings.get(CONVERSATION_FREQUENCY))
                return;

            this.lastConversation = System.currentTimeMillis();

            this.active = this.getRandomConversation();
            if (this.active == null)
                return;

            // This could be a bit funky, but it should work
            while (this.active.participants() > this.getNearbyMinions(this.active.radius()).size() + 1) {
                this.active = this.getRandomConversation();
                if (this.active == null)
                    return;
            }

            this.participants = this.getNearbyMinions(this.active.radius());
            this.lastConversation = System.currentTimeMillis();
            this.lastMessageIndex = -1; // Reset the last message index
        }

        // Get the next message in the conversation, if there is none then end the conversation
        int nextMessageIndex = ++this.lastMessageIndex;
        if (this.active.messages().size() <= nextMessageIndex) {
            this.active = null; // Set the active conversation to null
            this.participants.clear(); // Clear the participants
            this.lastMessageIndex = 0; // Reset the last message index
            this.lastSpeaker = null; // Set the last speaker to null

            // Remove the hologram
            if (this.hologram != null)
                this.hologram.getWatchers().forEach(hologram::removeWatcher);

            this.hologram = null;

            // TODO: Reset all minions in the conversation to their original display names
            return;
        }

        String nextMessage = this.active.messages().get(nextMessageIndex);

        // Get the next speaker in the conversation, try and not repeat the last speaker if possible
        if (this.participants.size() < 1)
            return; // There are no participants in the conversation

        this.lastSpeaker = this.participants.get(this.lastSpeaker == null ? 0 : this.lastSpeaker == this.participants.get(this.participants.size() - 1)
                ? 0 : this.participants.indexOf(this.lastSpeaker) + 1);

        // Create the hologram
        Location location = this.lastSpeaker == this.minion ? this.minion.getCenterLocation() : this.lastSpeaker.getCenterLocation();
        location.add(0, 0.75, 0); // Add 0.75 to the Y axis

        if (this.hologram == null || location != this.hologram.getLocation()) {
            if (this.hologram != null) // Remove the hologram from all players
                this.hologram.getWatchers().forEach(hologram::removeWatcher);

            this.hologram = NMSAdapter.getHandler().createHologram(location, List.of(HexUtils.colorify(nextMessage)));
        } else {
            this.hologram.setText(List.of(HexUtils.colorify(nextMessage)));
        }

        // Get all players within 15 blocks of the minion
        List<Player> nearbyPlayers = this.minion.getWorld().getNearbyPlayers(this.minion.getCenterLocation(), 15)
                .stream()
                .filter(player -> hologram == null || !hologram.getWatchers().contains(player))
                .toList();

        if (this.hologram != null)
            nearbyPlayers.forEach(this.hologram::addWatcher);

        // Update the other minions nearby
        this.updateCommunicators(false);
    }

    @Override
    protected void buildGui() {
        this.guiContainer = GuiFactory.createContainer();

        GuiScreen mainScreen = GuiFactory.createScreen(this.guiContainer, GuiSize.ROWS_FOUR)
                .setTitle(this.settings.get(GUI_TITLE));

        mainScreen.addButtonAt(0, GuiFactory.createButton(new ItemStack(Material.BIRCH_SIGN))
                .setName(HexUtils.colorify("<g:#F7971E:#FFD200>Start Conversation"))
                .setClickAction(inventoryClickEvent -> {
                    this.resetConversation();
                    this.lastConversation = 0;

                    this.updateCommunicators(false);

                    return ClickAction.CLOSE;
                }));

        this.addBackButton(mainScreen);

        this.guiContainer.addScreen(mainScreen);
        this.guiFramework.getGuiManager().registerGui(this.guiContainer);
    }

    /**
     * Update all the other minions that are in the conversation (Prevent conversations from overlapping)
     */
    private void updateCommunicators(boolean reset) {
        for (Minion minion : this.participants) {
            if (minion == this.minion)
                continue;

            minion.getModule(CommunicatorModule.class).ifPresent(communicatorModule -> {
                // TODO: Maybe move this to a separate method
                if (reset) {
                    this.resetConversation(); // Reset the conversation
                    return;
                }

                // Update the other minions
                communicatorModule.active = this.active;
                communicatorModule.lastConversation = this.lastConversation;
                communicatorModule.lastMessage = this.lastMessage;
                communicatorModule.lastSpeaker = this.lastSpeaker;

                if (communicatorModule.hologram != null)
                    communicatorModule.hologram.getWatchers().forEach(communicatorModule.hologram::removeWatcher);

                communicatorModule.hologram = null;
            });
        }

    }

    /**
     * Reset the conversation
     */
    public void resetConversation() {
        this.active = null;
        this.participants.clear();
        this.lastMessageIndex = 0;
        this.lastSpeaker = null;

        if (this.hologram != null)
            this.hologram.getWatchers().forEach(hologram::removeWatcher);

        this.hologram = null;
    }

    /**
     * Get a random conversation based on the chances of each conversation
     *
     * @return A random conversation
     */
    private MinionConversation getRandomConversation() {
        List<MinionConversation> conversations = this.settings.get(CONVERSATIONS);

        // Get a random conversation (each conversation has an individual chance) and return it
        double total = conversations.stream().mapToDouble(MinionConversation::chance).sum();
        double random = Math.random() * total;

        for (MinionConversation conversation : conversations) {
            random -= conversation.chance();
            if (random <= 0)
                return conversation;
        }

        return null;
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

}
