package dev.rosewood.roseminions.minion.animation;

import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.minion.setting.SettingsContainer;

public abstract class MinionAnimation {

    protected final Minion minion;
    protected final SettingsContainer settings;

    public MinionAnimation(Minion minion) {
        this.minion = minion;
        this.settings = new SettingsContainer();
        this.settings.loadDefaults(this.getClass());
    }

    public abstract void update();

    public abstract void updateEntity();

}
