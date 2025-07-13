package dev.rosewood.roseminions.object;

import dev.rosewood.rosegarden.config.PDCSettingField;
import dev.rosewood.rosegarden.config.PDCSettingSerializer;
import dev.rosewood.rosegarden.config.PDCSettingSerializers;
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

    public static final PDCSettingSerializer<MinionConversation> SERIALIZER = PDCSettingSerializers.ofRecord(MinionConversation.class, instance -> instance.group(
            PDCSettingField.of("participants", PDCSettingSerializers.INTEGER, MinionConversation::participants, "The number of minions to in the conversation"),
            PDCSettingField.of("chance", PDCSettingSerializers.DOUBLE, MinionConversation::chance, "The chance of this conversation happening"),
            PDCSettingField.of("radius", PDCSettingSerializers.INTEGER, MinionConversation::radius, "The radius in blocks to search for nearby minions"),
            PDCSettingField.of("messages", PDCSettingSerializers.STRING_LIST, MinionConversation::messages, "The messages for this conversation")
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

