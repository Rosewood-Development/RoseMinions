package dev.rosewood.roseminions.object;

import dev.rosewood.roseminions.setting.DataSerializer;
import dev.rosewood.roseminions.setting.DataSerializers;
import dev.rosewood.roseminions.setting.SettingField;
import java.util.List;

/**
 * @param participants Number of Participants in the conversation
 * @param chance Chance for the conversation to start
 * @param radius Radius to check around the minion for other minions to converse with
 * @param messages Ordered messages for each participant
 */
public record MinionConversation(int participants,
                                 double chance,
                                 int radius,
                                 List<String> messages) {

    public static final DataSerializer<MinionConversation> SERIALIZER = DataSerializers.ofRecord(MinionConversation.class, instance -> instance.group(
            SettingField.of("participants", DataSerializers.INTEGER, MinionConversation::participants, "The number of minions to in the conversation"),
            SettingField.of("chance", DataSerializers.DOUBLE, MinionConversation::chance, "The chance of this conversation happening"),
            SettingField.of("radius", DataSerializers.INTEGER, MinionConversation::radius, "The radius in blocks to search for nearby minions"),
            SettingField.of("messages", DataSerializers.STRING_LIST, MinionConversation::messages, "The messages for this conversation")
    ).apply(instance, MinionConversation::new));

    /**
     * Creates a new MinionConversation
     */
    public MinionConversation {
        if (participants < 1)
            throw new IllegalArgumentException("You need to have at least 1 participant");

        if (messages.isEmpty())
            throw new IllegalArgumentException("Messages must have at least 1 message");
    }

}

