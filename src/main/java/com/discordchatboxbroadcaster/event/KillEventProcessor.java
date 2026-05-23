package com.discordchatboxbroadcaster.event;

import com.discordchatboxbroadcaster.DiscordChatboxBroadcasterConfig;
import com.discordchatboxbroadcaster.notifier.Notifier;
import com.discordchatboxbroadcaster.render.ChatSegment;
import net.runelite.api.Client;
import net.runelite.api.NPC;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KillEventProcessor extends GameEventProcessor {

    private static final Pattern KILL_DETECTION_PATTERN = Pattern.compile("You have defeated (.*?)(?: and received (.*) coins)?\\.");

    private final DiscordChatboxBroadcasterConfig pluginConfiguration;
    private final Client gameClient;

    public KillEventProcessor(List<Notifier> registeredNotifiers, DiscordChatboxBroadcasterConfig pluginConfiguration, Client gameClient) {
        super(registeredNotifiers);
        this.pluginConfiguration = pluginConfiguration;
        this.gameClient = gameClient;
    }

    @Override
    protected boolean isFeatureEnabled() {
        return pluginConfiguration.notifyKill();
    }

    @Override
    protected void processSanitizedMessage(String sanitizedMessageContent, String activePlayerName, String activeClanName, BufferedImage playerIcon, int currentTick) {
        Matcher patternMatcher = KILL_DETECTION_PATTERN.matcher(sanitizedMessageContent);

        if (patternMatcher.find()) {
            String defeatedTarget = patternMatcher.group(1);
            String coinsReceived = patternMatcher.group(2);

            if (isNpcTarget(defeatedTarget)) {
                return;
            }

            executeNotificationSequence(activePlayerName, activeClanName, playerIcon, defeatedTarget, coinsReceived);
        }
    }

    private boolean isNpcTarget(String targetName) {
        for (NPC activeNpc : gameClient.getNpcs()) {
            String npcName = activeNpc.getName();
            if (npcName != null && npcName.equals(targetName)) {
                return true;
            }
        }
        return false;
    }

    private void executeNotificationSequence(String activePlayerName, String activeClanName, BufferedImage playerIcon, String defeatedTarget, String coinsReceived) {
        List<ChatSegment> notificationSegments = buildPlayerClanPrefixSegments(activePlayerName, activeClanName, playerIcon);
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