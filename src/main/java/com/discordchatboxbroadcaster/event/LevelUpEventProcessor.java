package com.discordchatboxbroadcaster.event;

import com.discordchatboxbroadcaster.DiscordChatboxBroadcasterConfig;
import com.discordchatboxbroadcaster.notifier.Notifier;
import com.discordchatboxbroadcaster.render.ChatSegment;

import java.awt.Color;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LevelUpEventProcessor extends GameEventProcessor {

    private static final Pattern LEVEL_UP_DETECTION_PATTERN = Pattern.compile("Congratulations, you(?:'ve)? just advanced (?:a|an|your) (.*?) level\\.(?: You are now level (\\d+)\\.)?");

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
            String skillName = patternMatcher.group(1);
            String skillLevel = patternMatcher.group(2);
            executeNotificationSequence(activePlayerName, activeClanName, skillName, skillLevel);
        }
    }

    private void executeNotificationSequence(String activePlayerName, String activeClanName, String skillName, String skillLevel) {
        List<ChatSegment> notificationSegments = buildPlayerClanPrefixSegments(activePlayerName, activeClanName);

        if (skillLevel != null) {
            notificationSegments.add(new ChatSegment("has reached ", Color.BLACK));
            notificationSegments.add(new ChatSegment(skillName + " level " + skillLevel, HIGHLIGHT_COLOR));
            notificationSegments.add(new ChatSegment(".", Color.BLACK));
        } else {
            notificationSegments.add(new ChatSegment("advanced a level: ", Color.BLACK));
            notificationSegments.add(new ChatSegment(skillName, HIGHLIGHT_COLOR));
        }

        dispatchNotificationSegments(notificationSegments);
    }
}