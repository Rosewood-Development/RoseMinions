package dev.rosewood.roseminions.setting;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;
import org.bukkit.configuration.ConfigurationSection;

class BasicMinionSetting<T> implements MinionSetting<T> {

    protected final DataSerializer<T> serializer;
    protected final String key;
    protected Supplier<T> defaultValueSupplier;
    protected final String[] comments;
    private final boolean hidden;

    protected BasicMinionSetting(DataSerializer<T> serializer, String key, Supplier<T> defaultValueSupplier, boolean hidden, String... comments) {
        this.serializer = serializer;
        this.key = key;
        this.defaultValueSupplier = defaultValueSupplier;
        this.hidden = hidden;
        this.comments = comments;
    }

    @Override
    public void write(ConfigurationSection config) {
        if (this.hidden)
            return;

        try {
            T defaultValue = this.defaultValueSupplier.get();
            this.serializer.write(config, this.key, defaultValue, this.comments);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void writeWithDefault(ConfigurationSection config) {
        if (this.hidden)
            return;

        try {
            T defaultValue = this.defaultValueSupplier.get();
            this.serializer.writeWithDefault(config, this.key, defaultValue, this.comments);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void readDefault(ConfigurationSection config) {
        if (this.hidden || !config.contains(this.key))
            return;

        this.defaultValueSupplier = () -> {
            try {
                return this.serializer.read(config, this.key);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        };
    }

    @Override
    public T read(ConfigurationSection config) {
        if (this.hidden || !config.contains(this.key))
            return null;

        try {
            return this.serializer.read(config, this.key);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean readIsValid(ConfigurationSection config) {
        if (this.hidden)
            return true;

        try {
            return this.serializer.readIsValid(config, this.key);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public DataSerializer<T> getSerializer() {
        return this.serializer;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public T getDefaultValue() {
        return this.defaultValueSupplier.get();
    }

    @Override
    public String[] getComments() {
        return this.comments;
    }

    @Override
    public boolean isHidden() {
        return this.hidden;
    }

    @Override
    public MinionSetting<T> copy(T defaultValue, String... comments) {
        return this.copy(() -> defaultValue, comments);
    }

    @Override
    public MinionSetting<T> copy(Supplier<T> defaultValueSupplier, String... comments) {
        String[] newComments;
        if (comments == null) {
            newComments = new String[0];
        } else {
            newComments = Arrays.copyOf(this.comments, this.comments.length);
        }
        return new BasicMinionSetting<>(this.serializer, this.key, defaultValueSupplier, false, newComments);
    }

    @Override
    public String toString() {
        return "BasicRoseSetting{" +
                "key='" + this.key + '\'' +
                ", defaultValue=" + this.defaultValueSupplier.get() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BasicMinionSetting<?> that)) return false;
        return Objects.equals(this.serializer, that.serializer) && Objects.equals(this.key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.serializer, this.key);
    }

}
