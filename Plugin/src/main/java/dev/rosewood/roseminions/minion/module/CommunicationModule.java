package dev.rosewood.roseminions.minion.module;

import dev.rosewood.guiframework.GuiFactory;
import dev.rosewood.guiframework.gui.ClickAction;
import dev.rosewood.guiframework.gui.GuiSize;
import dev.rosewood.guiframework.gui.screen.GuiScreen;
import dev.rosewood.rosegarden.config.PDCRoseSetting;
import dev.rosewood.rosegarden.config.PDCSettingSerializers;
import dev.rosewood.rosegarden.utils.HexUtils;
import dev.rosewood.roseminions.RoseMinions;
import dev.rosewood.roseminions.manager.MinionManager;
import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.minion.setting.PDCSettingHolder;
import dev.rosewood.roseminions.nms.NMSAdapter;
import dev.rosewood.roseminions.nms.hologram.Hologram;
import dev.rosewood.roseminions.object.MinionConversation;
import dev.rosewood.roseminions.object.ModuleGuiProperties;
import dev.rosewood.roseminions.util.MinionUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import static dev.rosewood.roseminions.minion.module.CommunicationModule.Settings.*;

public class CommunicationModule extends MinionModule {

    public static class Settings implements PDCSettingHolder {

        public static final Settings INSTANCE = new Settings();
        private static final List<PDCRoseSetting<?>> SETTINGS = new ArrayList<>();

        public static final PDCRoseSetting<Long> CONVERSATION_FREQUENCY = define(PDCRoseSetting.ofLong("conversation-frequency", 300000L, "How often a conversation will start (in milliseconds)"));
        public static final PDCRoseSetting<Long> MESSAGE_FREQUENCY = define(PDCRoseSetting.ofLong("message-frequency", 3000L, "How often a message will be sent (in milliseconds)"));
        public static final PDCRoseSetting<List<MinionConversation>> CONVERSATIONS = define(PDCRoseSetting.of("conversations", PDCSettingSerializers.ofList(MinionConversation.SERIALIZER), () -> List.of(
                new MinionConversation(1, 100, 10, List.of("oof", "ouch", "my bones"))
        ), "The conversations that the minion can have"));

        static {
            define(MinionModule.GUI_PROPERTIES.copy(() ->
                    new ModuleGuiProperties("Communication Module", Material.COMMAND_BLOCK, MinionUtils.PRIMARY_COLOR + "Communication Module",
                            List.of("", MinionUtils.SECONDARY_COLOR + "Allows the minion to communicate", MinionUtils.SECONDARY_COLOR + "with the world around them."))));
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

    public long lastConversation; // The last time the minion started a conversation
    public long lastMessage; // The last time the minion sent a message, TODO: Maybe make holograms display for x2 the message frequency

    // Active minions in conversation
    public MinionConversation active; // The current conversation
    public List<Minion> participants; // All participants in the conversation TODO: Fix participants not working, Fix randomization of speakers
    public Minion lastSpeaker; // The last minion to speak
    public int lastMessageIndex; // The last message index
    public Hologram hologram; // The hologram for the conversation

    public CommunicationModule(Minion minion) {
        super(minion, DefaultMinionModules.COMMUNICATION, Settings.INSTANCE);

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
    public void tick() {
        if (System.currentTimeMillis() - this.lastMessage <= this.settings.get(MESSAGE_FREQUENCY))
            return;

        this.lastMessage = System.currentTimeMillis();

        if (this.active == null) {
            if (System.currentTimeMillis() - this.lastConversation <= this.settings.get(CONVERSATION_FREQUENCY))
                return;

            this.lastConversation = System.currentTimeMillis();

            // This could be a bit funky, but it should work
            do {
                this.active = this.getRandomConversation();
                if (this.active == null)
                    return;
            } while (this.active.participants() > this.getNearbyMinions(this.active.radius()).size() + 1);

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
        if (this.participants.isEmpty())
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
                .setTitle(this.settings.get(MinionModule.GUI_PROPERTIES).title());

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

            this.getModule(CommunicationModule.class).ifPresent(communicationModule -> {
                // TODO: Maybe move this to a separate method
                if (reset) {
                    this.resetConversation(); // Reset the conversation
                    return;
                }

                // Update the other minions
                communicationModule.active = this.active;
                communicationModule.lastConversation = this.lastConversation;
                communicationModule.lastMessage = this.lastMessage;
                communicationModule.lastSpeaker = this.lastSpeaker;

                if (communicationModule.hologram != null)
                    communicationModule.hologram.getWatchers().forEach(communicationModule.hologram::removeWatcher);

                communicationModule.hologram = null;
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
