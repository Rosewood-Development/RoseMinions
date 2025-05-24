package dev.rosewood.roseminions.minion.module;

import dev.rosewood.guiframework.GuiFactory;
import dev.rosewood.guiframework.framework.util.GuiUtil;
import dev.rosewood.guiframework.gui.ClickAction;
import dev.rosewood.guiframework.gui.GuiSize;
import dev.rosewood.guiframework.gui.screen.GuiScreen;
import dev.rosewood.rosegarden.config.RoseSetting;
import dev.rosewood.rosegarden.config.SettingHolder;
import dev.rosewood.rosegarden.config.SettingSerializers;
import dev.rosewood.rosegarden.utils.EntitySpawnUtil;
import dev.rosewood.rosegarden.utils.HexUtils;
import dev.rosewood.rosegarden.utils.NMSUtil;
import dev.rosewood.roseminions.RoseMinions;
import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.model.ModuleGuiProperties;
import dev.rosewood.roseminions.model.PlayableParticle;
import dev.rosewood.roseminions.model.PlayableSound;
import dev.rosewood.roseminions.util.MinionUtils;
import dev.rosewood.roseminions.util.SkullUtils;
import dev.rosewood.roseminions.util.VersionUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import static dev.rosewood.roseminions.minion.module.ExperiencePickupModule.Settings.*;

public class ExperiencePickupModule extends EntityAttractorModule<ExperienceOrb> {

    public static class Settings implements SettingHolder {

        public static final Settings INSTANCE = new Settings();
        private static final List<RoseSetting<?>> SETTINGS = new ArrayList<>();

        public static final RoseSetting<Integer> STORED_XP = define(RoseSetting.ofHidden("stored-xp", SettingSerializers.INTEGER, () -> 0));
        public static final RoseSetting<Integer> MAX_EXP = define(RoseSetting.ofInteger("max-exp", 30970, "The maximum amount of XP the minion can store", ""));
        public static final RoseSetting<Long> UPDATE_FREQUENCY = define(RoseSetting.ofLong("update-frequency", 1000L, "How often the minion will update (in milliseconds)"));
        public static final RoseSetting<Integer> RADIUS = define(RoseSetting.ofInteger("radius", 8, "The radius for the minion to search for items"));
        public static final RoseSetting<PlayableSound> PICKUP_SOUND = define(RoseSetting.of("pickup-sound", PlayableSound.SERIALIZER, () -> new PlayableSound(true, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.5F, 1.0F), "The sound to play when collecting experience"));
        public static final RoseSetting<PlayableParticle> PICKUP_PARTICLE = define(RoseSetting.of("pickup-particle", PlayableParticle.SERIALIZER, () -> new PlayableParticle(false, VersionUtils.DUST, new PlayableParticle.DustOptionsData(Color.fromRGB(0, 255, 0), 1.0F), 5, new Vector(0.1, 0.1, 0.1), 0.0F, false), "The particle to display when collecting experience"));

        static {
            define(MinionModule.GUI_PROPERTIES.copy(() ->
                    new ModuleGuiProperties("Experience Pickup Module", Material.EXPERIENCE_BOTTLE, MinionUtils.PRIMARY_COLOR + "Experience Pickup Module",
                            List.of("", MinionUtils.SECONDARY_COLOR + "Allows the minion to collect XP", MinionUtils.SECONDARY_COLOR + "and store it for later use."))));
        }

        private Settings() { }

        @Override
        public List<RoseSetting<?>> get() {
            return Collections.unmodifiableList(SETTINGS);
        }

        private static <T> RoseSetting<T> define(RoseSetting<T> setting) {
            SETTINGS.add(setting);
            return setting;
        }

    }

    private final NamespacedKey xpKey;

    public ExperiencePickupModule(Minion minion) {
        super(minion, DefaultMinionModules.EXPERIENCE_PICKUP, Settings.INSTANCE, UPDATE_FREQUENCY, RADIUS);

        this.xpKey = new NamespacedKey(RoseMinions.getInstance(), "experience-orb");
    }

    @Override
    protected boolean collect(ExperienceOrb orb) {
        if (this.settings.get(STORED_XP) >= this.settings.get(MAX_EXP))
            return false;

        int xp = orb.getExperience();
        this.settings.set(STORED_XP, this.settings.get(STORED_XP) + xp);
        this.settings.get(PICKUP_SOUND).play(this.minion.getDisplayEntity());
        this.settings.get(PICKUP_PARTICLE).play(this.minion.getDisplayEntity());
        return true;
    }

    @Override
    protected boolean testEntity(Entity entity) {
        return entity.getType() == EntityType.EXPERIENCE_ORB && !entity.getPersistentDataContainer().has(this.xpKey);
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
        ItemMeta borderMeta = borderItem.getItemMeta();
        if (borderMeta != null) {
            borderMeta.setDisplayName(" ");
            borderMeta.addItemFlags(ItemFlag.values());
            borderItem.setItemMeta(borderMeta);
        }

        GuiUtil.fillRow(mainScreen, editableSize.getRows(), borderItem);

        // Experience Total
        mainScreen.addButtonAt(10, GuiFactory.createButton()
                .setIcon(Material.EXPERIENCE_BOTTLE)
                .setNameSupplier(() -> GuiFactory.createString(HexUtils.colorify(MinionUtils.PRIMARY_COLOR + "Stored XP: " + MinionUtils.SECONDARY_COLOR + this.settings.get(STORED_XP))))
                .setLore("", HexUtils.colorify(MinionUtils.SECONDARY_COLOR + "This is all the experience"), HexUtils.colorify(MinionUtils.SECONDARY_COLOR + "the minion has collected."))
        );

        // Deposit XP
        String depositTexture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjIyMWRhNDQxOGJkM2JmYjQyZWI2NGQyYWI0MjljNjFkZWNiOGY0YmY3ZDRjZmI3N2ExNjJiZTNkY2IwYjkyNyJ9fX0=";
        mainScreen.addButtonAt(12, GuiFactory.createButton()
                .setIcon(GuiFactory.createIcon(Material.PLAYER_HEAD, itemMeta -> SkullUtils.setSkullTexture((SkullMeta) itemMeta, depositTexture)))
                .setName(HexUtils.colorify(MinionUtils.PRIMARY_COLOR + "Deposit XP"))
                .setLore("", HexUtils.colorify(MinionUtils.SECONDARY_COLOR + "Deposit your experience"), HexUtils.colorify(MinionUtils.SECONDARY_COLOR + "into the minion."))
                .setClickAction(event -> {
                    int amountDeposited = this.depositExp((Player) event.getWhoClicked());
                    event.getWhoClicked().sendMessage(HexUtils.colorify(MinionUtils.PRIMARY_COLOR + "Deposited " + MinionUtils.SECONDARY_COLOR + amountDeposited + MinionUtils.PRIMARY_COLOR + " XP into the minion"));
                    return ClickAction.CLOSE;
                })
        );

        // Withdraw XP
        String withdrawTexture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTM4NTJiZjYxNmYzMWVkNjdjMzdkZTRiMGJhYTJjNWY4ZDhmY2E4MmU3MmRiY2FmY2JhNjY5NTZhODFjNCJ9fX0=";
        mainScreen.addButtonAt(13, GuiFactory.createButton()
                .setIcon(GuiFactory.createIcon(Material.PLAYER_HEAD, itemMeta -> SkullUtils.setSkullTexture((SkullMeta) itemMeta, withdrawTexture)))
                .setName(HexUtils.colorify(MinionUtils.PRIMARY_COLOR + "Withdraw XP"))
                .setLore("", HexUtils.colorify(MinionUtils.SECONDARY_COLOR + "Withdraw your experience"), HexUtils.colorify(MinionUtils.SECONDARY_COLOR + "from the minion."))
                .setClickAction(event -> {
                    int amountWithdrawn = this.withdrawExp((Player) event.getWhoClicked());
                    event.getWhoClicked().sendMessage(HexUtils.colorify(MinionUtils.PRIMARY_COLOR + "Withdrew " + MinionUtils.SECONDARY_COLOR + amountWithdrawn + MinionUtils.PRIMARY_COLOR + " XP from the minion"));
                    return ClickAction.CLOSE;
                })
        );

        this.addBackButton(mainScreen);

        this.guiContainer.addScreen(mainScreen);
        this.guiFramework.getGuiManager().registerGui(this.guiContainer);
    }

    /**
     * Deposits the player's XP into the minion
     *
     * @param player The player
     * @return The amount of XP deposited
     */
    public int depositExp(Player player) {
        int maxExp = this.settings.get(MAX_EXP);
        int storedExp = this.settings.get(STORED_XP);

        int maxDeposit = maxExp - storedExp;
        int toRemove = Math.min(maxDeposit, player.getTotalExperience());

        if (maxExp == -1)
            toRemove = player.getTotalExperience();

        player.giveExp(-toRemove);
        this.settings.set(STORED_XP, this.settings.get(STORED_XP) + toRemove);

        return toRemove;
    }

    /**
     * Withdraws the player's XP from the minion and spawns an XP orb
     */
    public int withdrawExp(@Nullable Player player) {
        int storedExp = this.settings.get(STORED_XP);

        // If they have paper, we can just give them the exp
        if (player != null && NMSUtil.isPaper()) {
            player.giveExp(storedExp, true);
            return storedExp;
        }

        // This is a fallback for when we don't have paper
        EntitySpawnUtil.spawn(this.minion.getCenterLocation(), ExperienceOrb.class, experienceOrb -> {
            experienceOrb.setExperience(storedExp);
            experienceOrb.getPersistentDataContainer().set(this.xpKey, PersistentDataType.INTEGER, 1);
        });
        this.settings.set(STORED_XP, 0);
        return storedExp;
    }

}
