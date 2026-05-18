package com.discordchatboxbroadcaster.event;

import com.discordchatboxbroadcaster.DiscordChatboxBroadcasterConfig;
import com.discordchatboxbroadcaster.notifier.Notifier;
import com.discordchatboxbroadcaster.render.ChatSegment;

import java.awt.Color;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CombatAchievementEventProcessor extends GameEventProcessor {

    private static final Pattern COMBAT_ACHIEVEMENT_DETECTION_PATTERN = Pattern.compile("Congratulations, you've completed a[n]? (.*) combat task: (.*)");

    private final DiscordChatboxBroadcasterConfig pluginConfiguration;

    public CombatAchievementEventProcessor(List<Notifier> registeredNotifiers, DiscordChatboxBroadcasterConfig pluginConfiguration) {
        super(registeredNotifiers);
        this.pluginConfiguration = pluginConfiguration;
    }

    @Override
    protected boolean isFeatureEnabled() {
        return pluginConfiguration.notifyCombatAchievement();
    }

    @Override
    protected void processSanitizedMessage(String sanitizedMessageContent, String activePlayerName, String activeClanName, int currentTick) {
        Matcher patternMatcher = COMBAT_ACHIEVEMENT_DETECTION_PATTERN.matcher(sanitizedMessageContent);

        if (patternMatcher.find()) {
            executeNotificationSequence(activePlayerName, activeClanName, patternMatcher.group(1), patternMatcher.group(2));
        }
    }

    private void executeNotificationSequence(String activePlayerName, String activeClanName, String tier, String taskName) {
        List<ChatSegment> notificationSegments = buildPlayerClanPrefixSegments(activePlayerName, activeClanName);
        notificationSegments.add(new ChatSegment("completed a combat task: ", Color.BLACK));
        notificationSegments.add(new ChatSegment(tier + " - " + taskName, new Color(20, 100, 200)));

        dispatchNotificationSegments(notificationSegments);
    }
}