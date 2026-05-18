package com.discordchatboxbroadcaster.event;

import com.discordchatboxbroadcaster.DiscordChatboxBroadcasterConfig;
import com.discordchatboxbroadcaster.notifier.Notifier;
import com.discordchatboxbroadcaster.render.ChatSegment;
import com.discordchatboxbroadcaster.render.ChatboxImageGenerator;

import java.awt.Color;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KillEventProcessor extends GameEventProcessor {

    private static final Pattern KILL_DETECTION_PATTERN = Pattern.compile("You have defeated (.*)\\.");

    private final DiscordChatboxBroadcasterConfig pluginConfiguration;
    private final ChatboxImageGenerator imageGenerator;

    public KillEventProcessor(List<Notifier> registeredNotifiers, DiscordChatboxBroadcasterConfig pluginConfiguration) {
        super(registeredNotifiers);
        this.pluginConfiguration = pluginConfiguration;
        this.imageGenerator = new ChatboxImageGenerator();
    }

    @Override
    protected boolean isFeatureEnabled() {
        return pluginConfiguration.notifyKill();
    }

    @Override
    protected void processSanitizedMessage(String sanitizedMessageContent, String activePlayerName, String activeClanName, int currentTick) {
        Matcher patternMatcher = KILL_DETECTION_PATTERN.matcher(sanitizedMessageContent);

        if (patternMatcher.find()) {
            executeNotificationSequence(activePlayerName, activeClanName, patternMatcher.group(1));
        }
    }

    private void executeNotificationSequence(String activePlayerName, String activeClanName, String defeatedTarget) {
        List<ChatSegment> notificationSegments = buildPlayerClanPrefixSegments(activePlayerName, activeClanName);
        notificationSegments.add(new ChatSegment("defeated a player: ", Color.BLACK));
        notificationSegments.add(new ChatSegment(defeatedTarget, new Color(150, 20, 20)));

        byte[] renderedImagePayload = imageGenerator.generateChatboxImage(notificationSegments);
        dispatchGeneratedImagePayload(renderedImagePayload);
    }
}