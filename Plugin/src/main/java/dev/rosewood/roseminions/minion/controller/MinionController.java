package dev.rosewood.roseminions.minion.controller;

import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.minion.setting.SettingsContainer;
import dev.rosewood.roseminions.model.DataSerializable;

public abstract class MinionController implements DataSerializable {

    protected final Minion minion;
    protected final SettingsContainer settings;

    public MinionController(Minion minion) {
        this.minion = minion;
        this.settings = new SettingsContainer(this.getClass());
    }

    public void mergeSettings(SettingsContainer settings) {
        this.settings.merge(settings);
    }

    @Override
    public final byte[] serialize() {
        return this.settings.serialize();
    }

    @Override
    public final void deserialize(byte[] input) {
        this.settings.deserialize(input);
    }

    public abstract void update();

    public void updateAsync() {
        // Does nothing by default
    }

}
