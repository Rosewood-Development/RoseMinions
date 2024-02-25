package dev.rosewood.roseminions.model;

import java.util.List;

/**
 * @param participants Number of Participants in the conversation
 * @param messages Ordered messages for each participant
 * @param chance Chance for the conversation to start
 */
public record MinionConversation(int participants, double chance, int radius, List<String> messages) {

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

