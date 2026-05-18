package com.discordchatboxbroadcaster.event;

import com.discordchatboxbroadcaster.DiscordChatboxBroadcasterConfig;
import com.discordchatboxbroadcaster.notifier.Notifier;
import com.discordchatboxbroadcaster.render.ChatSegment;

import java.awt.Color;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KillEventProcessor extends GameEventProcessor {

    private static final Pattern KILL_DETECTION_PATTERN = Pattern.compile("You have defeated (.*?)(?: and received (.*) coins)?\\.");

    private final DiscordChatboxBroadcasterConfig pluginConfiguration;

    public KillEventProcessor(List<Notifier> registeredNotifiers, DiscordChatboxBroadcasterConfig pluginConfiguration) {
        super(registeredNotifiers);
        this.pluginConfiguration = pluginConfiguration;
    }

    @Override
    protected boolean isFeatureEnabled() {
        return pluginConfiguration.notifyKill();
    }

    @Override
    protected void processSanitizedMessage(String sanitizedMessageContent, String activePlayerName, String activeClanName, int currentTick) {
        Matcher patternMatcher = KILL_DETECTION_PATTERN.matcher(sanitizedMessageContent);

        if (patternMatcher.find()) {
            executeNotificationSequence(activePlayerName, activeClanName, patternMatcher.group(1), patternMatcher.group(2));
        }
    }

    private void executeNotificationSequence(String activePlayerName, String activeClanName, String defeatedTarget, String coinsReceived) {
        List<ChatSegment> notificationSegments = buildPlayerClanPrefixSegments(activePlayerName, activeClanName);
        notificationSegments.add(new ChatSegment("has defeated ", Color.BLACK));
        notificationSegments.add(new ChatSegment(defeatedTarget, HIGHLIGHT_COLOR));

        if (coinsReceived != null) {
            notificationSegments.add(new ChatSegment(" and received ", Color.BLACK));
            notificationSegments.add(new ChatSegment(coinsReceived + " coins", HIGHLIGHT_COLOR));
        }

        notificationSegments.add(new ChatSegment("!", Color.BLACK));

        dispatchNotificationSegments(notificationSegments);
    }
}