package com.discordchatboxbroadcaster.event;

import com.discordchatboxbroadcaster.DiscordChatboxBroadcasterConfig;
import com.discordchatboxbroadcaster.notifier.Notifier;
import com.discordchatboxbroadcaster.render.ChatSegment;
import com.discordchatboxbroadcaster.render.ChatboxImageGenerator;
import net.runelite.api.events.ActorDeath;

import java.awt.Color;
import java.util.List;

public class DeathEventProcessor extends GameEventProcessor {

    private final DiscordChatboxBroadcasterConfig pluginConfiguration;
    private final ChatboxImageGenerator imageGenerator;

    public DeathEventProcessor(List<Notifier> registeredNotifiers, DiscordChatboxBroadcasterConfig pluginConfiguration) {
        super(registeredNotifiers);
        this.pluginConfiguration = pluginConfiguration;
        this.imageGenerator = new ChatboxImageGenerator();
    }

    @Override
    protected boolean isFeatureEnabled() {
        return pluginConfiguration.notifyDeath();
    }

    @Override
    protected void processSanitizedMessage(String sanitizedMessageContent, String activePlayerName, String activeClanName, int currentTick) {
    }

    @Override
    public void evaluateActorDeath(ActorDeath incomingDeathEvent, String activePlayerName, String activeClanName, int currentTick) {
        if (!isFeatureEnabled() || !canProcessEvent(currentTick)) {
            return;
        }

        executeNotificationSequence(activePlayerName, activeClanName);
    }

    private void executeNotificationSequence(String activePlayerName, String activeClanName) {
        List<ChatSegment> notificationSegments = buildPlayerClanPrefixSegments(activePlayerName, activeClanName);
        notificationSegments.add(new ChatSegment("has died.", new Color(150, 20, 20)));

        byte[] renderedImagePayload = imageGenerator.generateChatboxImage(notificationSegments);
        dispatchGeneratedImagePayload(renderedImagePayload);
    }
}