package com.discordchatboxbroadcaster.event;

import com.discordchatboxbroadcaster.DiscordChatboxBroadcasterConfig;
import com.discordchatboxbroadcaster.notifier.Notifier;
import com.discordchatboxbroadcaster.render.ChatSegment;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AchievementDiaryEventProcessor extends GameEventProcessor {

    private static final Pattern DIARY_PATTERN = Pattern.compile("Congratulations! You have completed all of the (\\w+) tasks in the (.+) area\\.");

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
    protected void processSanitizedMessage(String sanitizedMessageContent, String activePlayerName, String activeClanName, BufferedImage playerIcon, int currentTick) {
        Matcher matcher = DIARY_PATTERN.matcher(sanitizedMessageContent);
        if (matcher.find()) {
            String tier = matcher.group(1);
            String area = matcher.group(2);

            // Capitalize tier
            if (tier.length() > 0) {
                tier = tier.substring(0, 1).toUpperCase() + tier.substring(1).toLowerCase();
            }

            executeNotificationSequence(activePlayerName, activeClanName, playerIcon, tier, area);
        }
    }

    private void executeNotificationSequence(String activePlayerName, String activeClanName, BufferedImage playerIcon, String tier, String area) {
        List<ChatSegment> notificationSegments = buildPlayerClanPrefixSegments(activePlayerName, activeClanName, playerIcon);
        notificationSegments.add(new ChatSegment("completed the ", Color.BLACK));
        notificationSegments.add(new ChatSegment(tier + " " + area, HIGHLIGHT_COLOR));
        notificationSegments.add(new ChatSegment(" diary.", Color.BLACK));

        dispatchNotificationSegments(notificationSegments);
    }
}