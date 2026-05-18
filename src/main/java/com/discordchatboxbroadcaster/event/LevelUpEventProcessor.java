package com.discordchatboxbroadcaster.event;

import com.discordchatboxbroadcaster.DiscordChatboxBroadcasterConfig;
import com.discordchatboxbroadcaster.notifier.Notifier;
import com.discordchatboxbroadcaster.render.ChatSegment;

import java.awt.Color;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LevelUpEventProcessor extends GameEventProcessor {

    private static final Pattern LEVEL_UP_DETECTION_PATTERN = Pattern.compile("Congratulations, you just advanced a[n]? (.*) level\\.");

    private final DiscordChatboxBroadcasterConfig pluginConfiguration;

    public LevelUpEventProcessor(List<Notifier> registeredNotifiers, DiscordChatboxBroadcasterConfig pluginConfiguration) {
        super(registeredNotifiers);
        this.pluginConfiguration = pluginConfiguration;
    }

    @Override
    protected boolean isFeatureEnabled() {
        return pluginConfiguration.notifyLevelUp();
    }

    @Override
    protected void processSanitizedMessage(String sanitizedMessageContent, String activePlayerName, String activeClanName, int currentTick) {
        Matcher patternMatcher = LEVEL_UP_DETECTION_PATTERN.matcher(sanitizedMessageContent);

        if (patternMatcher.find()) {
            executeNotificationSequence(activePlayerName, activeClanName, patternMatcher.group(1));
        }
    }

    private void executeNotificationSequence(String activePlayerName, String activeClanName, String skillName) {
        List<ChatSegment> notificationSegments = buildPlayerClanPrefixSegments(activePlayerName, activeClanName);
        notificationSegments.add(new ChatSegment("advanced a level: ", Color.BLACK));
        notificationSegments.add(new ChatSegment(skillName, new Color(20, 100, 200)));

        dispatchNotificationSegments(notificationSegments);
    }
}