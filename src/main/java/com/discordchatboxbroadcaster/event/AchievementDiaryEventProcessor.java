package com.discordchatboxbroadcaster.event;

import com.discordchatboxbroadcaster.DiscordChatboxBroadcasterConfig;
import com.discordchatboxbroadcaster.notifier.Notifier;
import com.discordchatboxbroadcaster.render.ChatSegment;

import java.awt.Color;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AchievementDiaryEventProcessor extends GameEventProcessor {

    private static final Pattern ACHIEVEMENT_DIARY_DETECTION_PATTERN = Pattern.compile("Congratulations! You have completed all of the (.*) tasks in the (.*) area\\.");

    private final DiscordChatboxBroadcasterConfig pluginConfiguration;

    public AchievementDiaryEventProcessor(List<Notifier> registeredNotifiers, DiscordChatboxBroadcasterConfig pluginConfiguration) {
        super(registeredNotifiers);
        this.pluginConfiguration = pluginConfiguration;
    }

    @Override
    protected boolean isFeatureEnabled() {
        return pluginConfiguration.notifyAchievementDiary();
    }

    @Override
    protected void processSanitizedMessage(String sanitizedMessageContent, String activePlayerName, String activeClanName, int currentTick) {
        Matcher patternMatcher = ACHIEVEMENT_DIARY_DETECTION_PATTERN.matcher(sanitizedMessageContent);

        if (patternMatcher.find()) {
            executeNotificationSequence(activePlayerName, activeClanName, patternMatcher.group(1), patternMatcher.group(2));
        }
    }

    private void executeNotificationSequence(String activePlayerName, String activeClanName, String tier, String area) {
        String formattedTier = tier.substring(0, 1).toUpperCase() + tier.substring(1).toLowerCase();

        List<ChatSegment> notificationSegments = buildPlayerClanPrefixSegments(activePlayerName, activeClanName);
        notificationSegments.add(new ChatSegment("completed the ", Color.BLACK));
        notificationSegments.add(new ChatSegment(formattedTier + " " + area, HIGHLIGHT_COLOR));
        notificationSegments.add(new ChatSegment(" diary.", Color.BLACK));

        dispatchNotificationSegments(notificationSegments);
    }
}