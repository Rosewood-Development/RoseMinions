package dev.rosewood.roseminions.setting;

import java.util.function.Function;
import java.util.function.Supplier;

public record SettingField<O, T>(String key,
                                 DataSerializer<T> dataSerializer,
                                 Function<O, T> getter,
                                 Supplier<T> defaultValueSupplier,
                                 boolean optional,
                                 boolean flatten,
                                 String... comments) {

    public T defaultValue() {
        if (this.defaultValueSupplier == null)
            return null;
        return this.defaultValueSupplier.get();
    }

    public static <O, T> SettingField<O, T> of(String key,
                                               DataSerializer<T> dataSerializer,
                                               Function<O, T> getter,
                                               String... comments) {
        return new SettingField<>(key, dataSerializer, getter, null, false, false, comments);
    }

    public static <O, T> SettingField<O, T> ofOptional(String key,
                                                       DataSerializer<T> dataSerializer,
                                                       Function<O, T> getter,
                                                       Supplier<T> defaultValueSupplier,
                                                       String... comments) {
        return new SettingField<>(key, dataSerializer, getter, defaultValueSupplier, true, false, comments);
    }

    public static <O, T> SettingField<O, T> ofOptionalValue(String key,
                                                            DataSerializer<T> dataSerializer,
                                                            Function<O, T> getter,
                                                            T defaultValue,
                                                            String... comments) {
        return new SettingField<>(key, dataSerializer, getter, () -> defaultValue, true, false, comments);
    }

    public static <O, T> SettingField<O, T> ofFlattened(String key,
                                                        DataSerializer<T> dataSerializer,
                                                        Function<O, T> getter,
                                                        String... comments) {
        return new SettingField<>(key, dataSerializer, getter, null, false, true, comments);
    }

    public static <O, T> SettingField<O, T> ofFlattenedOptional(String key,
                                                                DataSerializer<T> dataSerializer,
                                                                Function<O, T> getter,
                                                                Supplier<T> defaultValueSupplier,
                                                                String... comments) {
        return new SettingField<>(key, dataSerializer, getter, defaultValueSupplier, true, true, comments);
    }

    public static <O, T> SettingField<O, T> ofFlattenedOptionalValue(String key,
                                                                     DataSerializer<T> dataSerializer,
                                                                     Function<O, T> getter,
                                                                     T defaultValue,
                                                                     String... comments) {
        return new SettingField<>(key, dataSerializer, getter, () -> defaultValue, true, true, comments);
    }

}
