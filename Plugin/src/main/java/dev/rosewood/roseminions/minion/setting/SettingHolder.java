package dev.rosewood.roseminions.minion.setting;

import dev.rosewood.roseminions.model.PDCSerializable;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;

public interface SettingHolder extends PDCSerializable {

    /**
     * @return the SettingsContainer
     */
    SettingsContainer getSettings();

    @Override
    default void writePDC(PersistentDataContainer container, PersistentDataAdapterContext context) {
        this.getSettings().writePDC(container, context);
    }

    @Override
    default void readPDC(PersistentDataContainer container) {
        this.getSettings().readPDC(container);
    }

}
