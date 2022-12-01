package dev.rosewood.roseminions.minion.setting;

import dev.rosewood.roseminions.model.DataSerializable;

public interface SettingHolder extends DataSerializable {

    /**
     * @return the SettingsContainer
     */
    SettingsContainer getSettings();

    @Override
    default byte[] serialize() {
        return this.getSettings().serialize();
    }

    @Override
    default void deserialize(byte[] input) {
        this.getSettings().deserialize(input);
    }

}
