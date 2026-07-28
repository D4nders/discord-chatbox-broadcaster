package com.discordchatboxbroadcaster.event;

import com.discordchatboxbroadcaster.DiscordChatboxBroadcasterConfig;
import com.discordchatboxbroadcaster.SharedEventState;
import com.discordchatboxbroadcaster.notifier.Notifier;
import com.discordchatboxbroadcaster.render.ChatSegment;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CollectionLogEventProcessor extends GameEventProcessor {

    private static final Pattern COLLECTION_LOG_DETECTION_PATTERN = Pattern.compile("New item added to your collection log: (.*)");

    private final DiscordChatboxBroadcasterConfig pluginConfiguration;
    private final SharedEventState sharedEventState;

    public CollectionLogEventProcessor(List<Notifier> registeredNotifiers, DiscordChatboxBroadcasterConfig pluginConfiguration, SharedEventState sharedEventState) {
        super(registeredNotifiers);
        this.pluginConfiguration = pluginConfiguration;
        this.sharedEventState = sharedEventState;
    }

    @Override
    protected boolean isFeatureEnabled() {
        return pluginConfiguration.notifyCollectionLog();
    }

    @Override
    protected void processSanitizedMessage(String sanitizedMessageContent, String activePlayerName, String activeClanName, BufferedImage playerIcon, int currentTick) {
        if (currentTick != -1 && sharedEventState.isWithinPetDropWindow(currentTick)) {
            return;
        }

        Matcher patternMatcher = COLLECTION_LOG_DETECTION_PATTERN.matcher(sanitizedMessageContent);

        if (patternMatcher.find()) {
            if (currentTick != -1) {
                sharedEventState.registerCollectionLogDrop(currentTick);
            }

            executeNotificationSequence(activePlayerName, activeClanName, playerIcon, patternMatcher.group(1));
        }
    }

    private void executeNotificationSequence(String activePlayerName, String activeClanName, BufferedImage playerIcon, String itemName) {
        List<ChatSegment> notificationSegments = buildPlayerClanPrefixSegments(activePlayerName, activeClanName, playerIcon);
        notificationSegments.add(new ChatSegment("received a new collection log item: ", Color.BLACK));
        notificationSegments.add(new ChatSegment(itemName, HIGHLIGHT_COLOR));

        dispatchNotificationSegments(notificationSegments);
    }
}