package com.discordchatboxbroadcaster.event;

import com.discordchatboxbroadcaster.DiscordChatboxBroadcasterConfig;
import com.discordchatboxbroadcaster.notifier.Notifier;
import com.discordchatboxbroadcaster.render.ChatSegment;
import com.discordchatboxbroadcaster.render.ChatboxImageGenerator;

import java.awt.Color;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AchievementDiaryEventProcessor extends GameEventProcessor {

    private static final Pattern ACHIEVEMENT_DIARY_DETECTION_PATTERN = Pattern.compile("Congratulations! You have completed all of the (.*) tasks in the (.*) area\\.");

    private final DiscordChatboxBroadcasterConfig pluginConfiguration;
    private final ChatboxImageGenerator imageGenerator;

    public AchievementDiaryEventProcessor(List<Notifier> registeredNotifiers, DiscordChatboxBroadcasterConfig pluginConfiguration) {
        super(registeredNotifiers);
        this.pluginConfiguration = pluginConfiguration;
        this.imageGenerator = new ChatboxImageGenerator();
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
        List<ChatSegment> notificationSegments = buildPlayerClanPrefixSegments(activePlayerName, activeClanName);
        notificationSegments.add(new ChatSegment("completed a diary: ", Color.BLACK));
        notificationSegments.add(new ChatSegment(tier + " " + area, new Color(20, 100, 200)));

        byte[] renderedImagePayload = imageGenerator.generateChatboxImage(notificationSegments);
        dispatchGeneratedImagePayload(renderedImagePayload);
    }
}