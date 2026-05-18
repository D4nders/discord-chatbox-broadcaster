package com.discordchatboxbroadcaster.event;

import com.discordchatboxbroadcaster.DiscordChatboxBroadcasterConfig;
import com.discordchatboxbroadcaster.notifier.Notifier;
import com.discordchatboxbroadcaster.render.ChatSegment;

import java.awt.Color;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NewRecordEventProcessor extends GameEventProcessor {

    private static final Pattern RECORD_DETECTION_PATTERN = Pattern.compile("(?i)((?:(?:Fight |Lap |Challenge |Corrupted challenge )?duration:|Subdued in) [0-9:]+(?:\\.\\d+)?) \\(new personal best\\)");

    private final DiscordChatboxBroadcasterConfig pluginConfiguration;

    public NewRecordEventProcessor(List<Notifier> registeredNotifiers, DiscordChatboxBroadcasterConfig pluginConfiguration) {
        super(registeredNotifiers);
        this.pluginConfiguration = pluginConfiguration;
    }

    @Override
    protected boolean isFeatureEnabled() {
        return pluginConfiguration.notifyNewRecord();
    }

    @Override
    protected void processSanitizedMessage(String sanitizedMessageContent, String activePlayerName, String activeClanName, int currentTick) {
        Matcher patternMatcher = RECORD_DETECTION_PATTERN.matcher(sanitizedMessageContent);

        if (patternMatcher.find()) {
            executeNotificationSequence(activePlayerName, activeClanName, patternMatcher.group(1));
        }
    }

    private void executeNotificationSequence(String activePlayerName, String activeClanName, String recordDetails) {
        List<ChatSegment> notificationSegments = buildPlayerClanPrefixSegments(activePlayerName, activeClanName);
        notificationSegments.add(new ChatSegment("achieved a new record: ", Color.BLACK));
        notificationSegments.add(new ChatSegment(recordDetails, new Color(20, 100, 200)));

        dispatchNotificationSegments(notificationSegments);
    }
}