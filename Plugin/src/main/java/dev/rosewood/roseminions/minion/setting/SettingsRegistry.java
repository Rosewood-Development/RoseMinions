package dev.rosewood.roseminions.minion.setting;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import java.util.List;
import java.util.function.Supplier;

public final class SettingsRegistry {

    public static final Multimap<Class<?>, SettingAccessor<?>> REGISTERED_SETTINGS = MultimapBuilder.hashKeys().arrayListValues().build();

    private SettingsRegistry() {

    }

    public static SettingAccessor<Boolean> defineBoolean(Class<?> classForSettings, String name, boolean defaultValue, String... comments) {
        return defineSetting(classForSettings, SettingSerializers.BOOLEAN, name, () -> defaultValue, comments);
    }

    public static SettingAccessor<Integer> defineInteger(Class<?> classForSettings, String name, int defaultValue, String... comments) {
        return defineSetting(classForSettings, SettingSerializers.INTEGER, name, () -> defaultValue, comments);
    }

    public static SettingAccessor<Long> defineLong(Class<?> classForSettings, String name, long defaultValue, String... comments) {
        return defineSetting(classForSettings, SettingSerializers.LONG, name, () -> defaultValue, comments);
    }

    public static SettingAccessor<Short> defineShort(Class<?> classForSettings, String name, short defaultValue, String... comments) {
        return defineSetting(classForSettings, SettingSerializers.SHORT, name, () -> defaultValue, comments);
    }

    public static SettingAccessor<Byte> defineByte(Class<?> classForSettings, String name, byte defaultValue, String... comments) {
        return defineSetting(classForSettings, SettingSerializers.BYTE, name, () -> defaultValue, comments);
    }

    public static SettingAccessor<Double> defineDouble(Class<?> classForSettings, String name, double defaultValue, String... comments) {
        return defineSetting(classForSettings, SettingSerializers.DOUBLE, name, () -> defaultValue, comments);
    }

    public static SettingAccessor<Float> defineFloat(Class<?> classForSettings, String name, float defaultValue, String... comments) {
        return defineSetting(classForSettings, SettingSerializers.FLOAT, name, () -> defaultValue, comments);
    }

    public static SettingAccessor<Character> defineCharacter(Class<?> classForSettings, String name, char defaultValue, String... comments) {
        return defineSetting(classForSettings, SettingSerializers.CHAR, name, () -> defaultValue, comments);
    }

    public static SettingAccessor<String> defineString(Class<?> classForSettings, String name, String defaultValue, String... comments) {
        return defineSetting(classForSettings, SettingSerializers.STRING, name, () -> defaultValue, comments);
    }

    public static SettingAccessor<List<String>> defineStringList(Class<?> classForSettings, String name, List<String> defaultValue, String... comments) {
        return defineSetting(classForSettings, SettingSerializers.ofList(SettingSerializers.STRING), name, () -> List.copyOf(defaultValue), comments);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Enum<T>> SettingAccessor<T> defineEnum(Class<?> classForSettings, String name, T defaultValue, String... comments) {
        return defineSetting(classForSettings, SettingSerializers.ofEnum(defaultValue.getClass()), name, () -> defaultValue, comments);
    }

    public static <T> SettingAccessor<T> defineSetting(Class<?> classForSettings, SettingSerializer<T> serializer, String name, Supplier<T> defaultValueSupplier, String... comments) {
        String key = name.toLowerCase();

        SettingAccessor<T> accessor;
        if (comments == null) {
            accessor = new SettingAccessor<>(serializer, key, defaultValueSupplier);
        } else {
            accessor = new SettingAccessor<>(serializer, key, defaultValueSupplier, comments);
        }

        if (REGISTERED_SETTINGS.containsEntry(classForSettings, accessor))
            throw new IllegalArgumentException("Setting " + accessor.getKey() + " is already defined for class " + classForSettings.getSimpleName());

        REGISTERED_SETTINGS.put(classForSettings, accessor);
        return accessor;
    }

    public static <T> SettingAccessor<T> defineHiddenSetting(Class<?> classForSettings, SettingSerializer<T> serializer, String name, Supplier<T> defaultValueSupplier) {
        return defineSetting(classForSettings, serializer, name, defaultValueSupplier, (String[]) null);
    }

    public static void redefineBoolean(Class<?> classForSettings, SettingAccessor<Boolean> accessorToCopy, boolean defaultValue) {
        redefineSetting(classForSettings, accessorToCopy, () -> defaultValue);
    }

    public static void redefineInteger(Class<?> classForSettings, SettingAccessor<Integer> accessorToCopy, int defaultValue) {
        redefineSetting(classForSettings, accessorToCopy, () -> defaultValue);
    }

    public static void redefineLong(Class<?> classForSettings, SettingAccessor<Long> accessorToCopy, long defaultValue) {
        redefineSetting(classForSettings, accessorToCopy, () -> defaultValue);
    }

    public static void redefineShort(Class<?> classForSettings, SettingAccessor<Short> accessorToCopy, short defaultValue) {
        redefineSetting(classForSettings, accessorToCopy, () -> defaultValue);
    }

    public static void redefineByte(Class<?> classForSettings, SettingAccessor<Byte> accessorToCopy, byte defaultValue) {
        redefineSetting(classForSettings, accessorToCopy, () -> defaultValue);
    }

    public static void redefineDouble(Class<?> classForSettings, SettingAccessor<Double> accessorToCopy, double defaultValue) {
        redefineSetting(classForSettings, accessorToCopy, () -> defaultValue);
    }

    public static void redefineFloat(Class<?> classForSettings, SettingAccessor<Float> accessorToCopy, float defaultValue) {
        redefineSetting(classForSettings, accessorToCopy, () -> defaultValue);
    }

    public static void redefineCharacter(Class<?> classForSettings, SettingAccessor<Character> accessorToCopy, char defaultValue) {
        redefineSetting(classForSettings, accessorToCopy, () -> defaultValue);
    }

    public static void redefineString(Class<?> classForSettings, SettingAccessor<String> accessorToCopy, String defaultValue) {
        redefineSetting(classForSettings, accessorToCopy, () -> defaultValue);
    }

    public static void redefineStringList(Class<?> classForSettings, SettingAccessor<List<String>> accessorToCopy, List<String> defaultValue) {
        redefineSetting(classForSettings, accessorToCopy, () -> List.copyOf(defaultValue));
    }

    public static <T extends Enum<T>> void redefineEnum(Class<?> classForSettings, SettingAccessor<T> accessorToCopy, T defaultValue) {
        redefineSetting(classForSettings, accessorToCopy, () -> defaultValue);
    }

    public static <T> void redefineSetting(Class<?> classForSettings, SettingAccessor<T> accessorToCopy, Supplier<T> defaultValueSupplier) {
        REGISTERED_SETTINGS.put(classForSettings, accessorToCopy.copy(defaultValueSupplier));
    }

}
