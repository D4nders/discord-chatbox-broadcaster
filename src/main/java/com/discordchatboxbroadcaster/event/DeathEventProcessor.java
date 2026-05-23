package com.discordchatboxbroadcaster.event;

import com.discordchatboxbroadcaster.DiscordChatboxBroadcasterConfig;
import com.discordchatboxbroadcaster.notifier.Notifier;
import com.discordchatboxbroadcaster.render.ChatSegment;
import net.runelite.api.events.ActorDeath;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.List;

public class DeathEventProcessor extends GameEventProcessor {

    private final DiscordChatboxBroadcasterConfig pluginConfiguration;

    public DeathEventProcessor(List<Notifier> registeredNotifiers, DiscordChatboxBroadcasterConfig pluginConfiguration) {
        super(registeredNotifiers);
        this.pluginConfiguration = pluginConfiguration;
    }

    @Override
    protected boolean isFeatureEnabled() {
        return pluginConfiguration.notifyDeath();
    }

    @Override
    protected void processSanitizedMessage(String sanitizedMessageContent, String activePlayerName, String activeClanName, BufferedImage playerIcon, int currentTick) {
    }

    @Override
    public void evaluateActorDeath(ActorDeath incomingDeathEvent, String activePlayerName, String activeClanName, BufferedImage playerIcon, int currentTick) {
        if (!isFeatureEnabled() || !canProcessEvent(currentTick)) {
            return;
        }

        executeNotificationSequence(activePlayerName, activeClanName, playerIcon);
    }

    private void executeNotificationSequence(String activePlayerName, String activeClanName, BufferedImage playerIcon) {
        List<ChatSegment> notificationSegments = buildPlayerClanPrefixSegments(activePlayerName, activeClanName, playerIcon);
        notificationSegments.add(new ChatSegment("has died.", Color.BLACK));

        dispatchNotificationSegments(notificationSegments);
    }
}