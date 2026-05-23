package com.discordchatboxbroadcaster.event;

import com.discordchatboxbroadcaster.DiscordChatboxBroadcasterConfig;
import com.discordchatboxbroadcaster.notifier.Notifier;
import com.discordchatboxbroadcaster.render.ChatSegment;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CombatAchievementEventProcessor extends GameEventProcessor {

    private static final Pattern COMBAT_ACHIEVEMENT_DETECTION_PATTERN = Pattern.compile("(?:CA_ID:\\d+\\|)?Congratulations, you've completed an? (\\w+) combat task: (.*)");

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
    protected void processSanitizedMessage(String sanitizedMessageContent, String activePlayerName, String activeClanName, BufferedImage playerIcon, int currentTick) {
        Matcher patternMatcher = COMBAT_ACHIEVEMENT_DETECTION_PATTERN.matcher(sanitizedMessageContent);

        if (patternMatcher.find()) {
            String taskName = patternMatcher.group(2);
            if (taskName.endsWith(".")) {
                taskName = taskName.substring(0, taskName.length() - 1);
            }
            executeNotificationSequence(activePlayerName, activeClanName, playerIcon, patternMatcher.group(1).toLowerCase(), taskName);
        }
    }

    private void executeNotificationSequence(String activePlayerName, String activeClanName, BufferedImage playerIcon, String tier, String taskName) {
        List<ChatSegment> notificationSegments = buildPlayerClanPrefixSegments(activePlayerName, activeClanName, playerIcon);
        notificationSegments.add(new ChatSegment("has completed a ", Color.BLACK));
        notificationSegments.add(new ChatSegment(tier, HIGHLIGHT_COLOR));
        notificationSegments.add(new ChatSegment(" combat task: ", Color.BLACK));
        notificationSegments.add(new ChatSegment(taskName + ".", HIGHLIGHT_COLOR));

        dispatchNotificationSegments(notificationSegments);
    }
}