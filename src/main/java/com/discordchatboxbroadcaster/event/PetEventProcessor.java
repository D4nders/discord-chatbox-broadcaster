package com.discordchatboxbroadcaster.event;

import com.discordchatboxbroadcaster.DiscordChatboxBroadcasterConfig;
import com.discordchatboxbroadcaster.SharedEventState;
import com.discordchatboxbroadcaster.notifier.Notifier;
import com.discordchatboxbroadcaster.render.ChatSegment;

import java.awt.Color;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PetEventProcessor extends GameEventProcessor {

    private static final Pattern PET_DETECTION_PATTERN = Pattern.compile("(You have a funny feeling.*|You feel something weird sneaking into your backpack.*)");

    private final DiscordChatboxBroadcasterConfig pluginConfiguration;
    private final SharedEventState sharedEventState;

    public PetEventProcessor(List<Notifier> registeredNotifiers, DiscordChatboxBroadcasterConfig pluginConfiguration, SharedEventState sharedEventState) {
        super(registeredNotifiers);
        this.pluginConfiguration = pluginConfiguration;
        this.sharedEventState = sharedEventState;
    }

    @Override
    protected boolean isFeatureEnabled() {
        return pluginConfiguration.notifyPet();
    }

    @Override
    protected void processSanitizedMessage(String sanitizedMessageContent, String activePlayerName, String activeClanName, int currentTick) {
        Matcher patternMatcher = PET_DETECTION_PATTERN.matcher(sanitizedMessageContent);

        if (patternMatcher.find()) {
            if (currentTick != -1) {
                sharedEventState.registerPetDrop(currentTick);
            }

            executeNotificationSequence(activePlayerName, activeClanName, patternMatcher.group(1));
        }
    }

    private void executeNotificationSequence(String activePlayerName, String activeClanName, String petMessageContent) {
        String formattedPetMessage = petMessageContent
                .replace("You have a funny feeling like you're", "has a funny feeling like they're")
                .replace("You feel something weird sneaking into your", "feels something weird sneaking into their");

        List<ChatSegment> notificationSegments = buildPlayerClanPrefixSegments(activePlayerName, activeClanName);
        notificationSegments.add(new ChatSegment(formattedPetMessage, new Color(127, 0, 0)));

        dispatchNotificationSegments(notificationSegments);
    }
}