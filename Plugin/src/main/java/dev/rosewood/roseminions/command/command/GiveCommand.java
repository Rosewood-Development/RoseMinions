package dev.rosewood.roseminions.command.command;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.RoseCommand;
import dev.rosewood.rosegarden.command.framework.RoseCommandWrapper;
import dev.rosewood.rosegarden.command.framework.annotation.Optional;
import dev.rosewood.rosegarden.command.framework.annotation.RoseExecutable;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import dev.rosewood.roseminions.manager.LocaleManager;
import dev.rosewood.roseminions.minion.config.MinionConfig;
import dev.rosewood.roseminions.minion.config.RankConfig;
import dev.rosewood.roseminions.util.MinionUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class GiveCommand extends RoseCommand {

    public GiveCommand(RosePlugin rosePlugin, RoseCommandWrapper parent) {
        super(rosePlugin, parent);
    }

    @RoseExecutable
    public void execute(CommandContext context, Player player, MinionConfig minionType, @Optional RankConfig rank, @Optional Integer amount) {
        LocaleManager locale = this.rosePlugin.getManager(LocaleManager.class);

        if (rank == null)
            rank = minionType.getDefaultRank();

        if (amount == null || amount < 1)
            amount = 1;

        ItemStack itemStack = minionType.getRank(rank.rank()).getDisplayItemStack();
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            PersistentDataContainer pdc = itemMeta.getPersistentDataContainer();
            pdc.set(MinionUtils.MINION_NEW_TYPE_KEY, PersistentDataType.STRING, minionType.getId());
            pdc.set(MinionUtils.MINION_NEW_RANK_KEY, PersistentDataType.STRING, rank.rank());
            itemStack.setItemMeta(itemMeta);
        }

        itemStack.setAmount(amount);

        player.getInventory().addItem(itemStack);
        locale.sendMessage(player, "command-give-success", StringPlaceholders.builder("minion", minionType.getId())
                .add("amount", amount)
                .add("rank", rank)
                .add("player", player.getName())
                .build()
        );
    }

    @Override
    protected String getDefaultName() {
        return "give";
    }

    @Override
    public String getDescriptionKey() {
        return "command-give-description";
    }

    @Override
    public String getRequiredPermission() {
        return "roseminions.give";
    }

    @Override
    public boolean isPlayerOnly() {
        return false;
    }

}
