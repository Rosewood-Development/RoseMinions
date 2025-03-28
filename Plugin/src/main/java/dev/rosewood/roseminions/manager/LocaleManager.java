package dev.rosewood.roseminions.manager;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.manager.AbstractLocaleManager;
import dev.rosewood.rosegarden.utils.HexUtils;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import net.md_5.bungee.api.ChatColor;
import org.jetbrains.annotations.NotNull;

public class LocaleManager extends AbstractLocaleManager {

    public LocaleManager(RosePlugin rosePlugin) {
        super(rosePlugin);
    }

    @SuppressWarnings("unchecked")
    @NotNull
    protected List<String> getLocaleStrings(String key) {
        Object value = this.loadedLocale.getLocaleValues().get(key);
        if (value instanceof String) {
            return new ArrayList<>(Collections.singletonList((String) value));
        } else if (value instanceof List) {
            return (List<String>) value;
        }

        value = this.defaultLocale.getLocaleValues().get(key);
        if (value instanceof String) {
            return new ArrayList<>(Collections.singletonList((String) value));
        } else if (value instanceof List) {
            return (List<String>) value;
        }

        return List.of(ChatColor.RED + "Missing locale string: " + key);
    }

    /**
     * Gets a list or single locale message with the given placeholders applied, will return an empty list for no messages
     *
     * @param messageKey The key of the message to get
     * @param stringPlaceholders The placeholders to apply
     * @return The locale messages with the given placeholders applied
     */
    public List<String> getLocaleMessages(String messageKey, StringPlaceholders stringPlaceholders) {
        return this.getLocaleStrings(messageKey).stream()
                .map(message -> HexUtils.colorify(stringPlaceholders.apply(message)))
                .collect(Collectors.toList());
    }

}
