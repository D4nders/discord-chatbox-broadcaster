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

    private static final Pattern PET_DETECTION_PATTERN = Pattern.compile("(You have a funny feeling[^:]+|You feel something weird sneaking into your backpack[^:]+)(?:: (.*?)(?: at (.*))?)?");

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

            String baseMessage = patternMatcher.group(1);
            String petName = patternMatcher.group(2);
            String kcOrXp = patternMatcher.group(3);

            if (baseMessage != null) {
                baseMessage = baseMessage.replaceAll("\\.+$", "").trim();
            }
            if (petName != null) {
                petName = petName.replaceAll("\\.+$", "").trim();
            }
            if (kcOrXp != null) {
                kcOrXp = kcOrXp.replaceAll("\\.+$", "").trim();
            }

            executeNotificationSequence(activePlayerName, activeClanName, baseMessage, petName, kcOrXp);
        }
    }

    private void executeNotificationSequence(String activePlayerName, String activeClanName, String baseMessage, String petName, String kcOrXp) {
        String formattedPetMessage = baseMessage
                .replace("You have a funny feeling like you're", "has a funny feeling like they're")
                .replace("You feel something weird sneaking into your", "feels something weird sneaking into their")
                .replace("You have a funny feeling like you would have been", "has a funny feeling like they would have been");

        List<ChatSegment> notificationSegments = buildPlayerClanPrefixSegments(activePlayerName, activeClanName);

        if (petName != null && !petName.isEmpty()) {
            notificationSegments.add(new ChatSegment(formattedPetMessage + ": ", Color.BLACK));
            notificationSegments.add(new ChatSegment(petName, HIGHLIGHT_COLOR));

            if (kcOrXp != null && !kcOrXp.isEmpty()) {
                notificationSegments.add(new ChatSegment(" at " + kcOrXp + ".", Color.BLACK));
            } else {
                notificationSegments.add(new ChatSegment(".", Color.BLACK));
            }
        } else {
            notificationSegments.add(new ChatSegment(formattedPetMessage + ".", Color.BLACK));
        }

        dispatchNotificationSegments(notificationSegments);
    }
}