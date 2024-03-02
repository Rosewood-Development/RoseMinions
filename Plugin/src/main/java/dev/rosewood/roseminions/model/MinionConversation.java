package dev.rosewood.roseminions.model;

import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.minion.setting.SettingSerializerFactories;
import dev.rosewood.roseminions.minion.setting.SettingSerializers;
import org.bukkit.entity.ArmorStand;

import java.util.List;
import java.util.Map;

/**
 * @param participants Number of Participants in the conversation
 * @param messages Ordered messages for each participant
 * @param chance Chance for the conversation to start
 */
public record MinionConversation(int participants,
                                 double chance,
                                 int radius,
                                 List<String> messages) {

    /**
     * Creates a new MinionConversation
     */
    public MinionConversation {
        if (participants < 1)
            throw new IllegalArgumentException("You need to have at least 1 participant");

        if (messages.isEmpty())
            throw new IllegalArgumentException("Messages must have at least 1 message");
    }

    /**
     * Check whether the conversation can be started with the right amount of participants
     *
     * @return true if the conversation could be started
     */
    public boolean testNearby(Minion toStart) {
        return toStart.getWorld().getNearbyEntitiesByType(ArmorStand.class, toStart.getLocation(), radius).size() >= participants;
    }

    public static void defineComplex(SettingSerializerFactories.ComplexSettingWriter writer) {
        writer.withProperty("participants", SettingSerializers.INTEGER);
        writer.withProperty("chance", SettingSerializers.DOUBLE);
        writer.withProperty("radius", SettingSerializers.INTEGER);
        writer.withProperty("messages", SettingSerializers.ofList(SettingSerializers.STRING));
    }

    public static Map<String, Object> toMap(MinionConversation object) {
        return Map.of(
                "participants", object.participants(),
                "chance", object.chance(),
                "radius", object.radius(),
                "messages", object.messages()
        );
    }

    @SuppressWarnings("unchecked")
    public static MinionConversation fromMap(Map<String, Object> map) {
        return new MinionConversation(
                (int) map.get("participants"),
                (double) map.get("chance"),
                (int) map.get("radius"),
                (List<String>) map.get("messages")
        );
    }

}

