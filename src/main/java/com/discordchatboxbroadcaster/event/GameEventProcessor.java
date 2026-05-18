package com.discordchatboxbroadcaster.event;

import com.discordchatboxbroadcaster.notifier.Notifier;
import com.discordchatboxbroadcaster.render.ChatSegment;
import net.runelite.api.ChatMessageType;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.ChatMessage;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public abstract class GameEventProcessor {

    protected static final Color HIGHLIGHT_COLOR = new Color(143, 0, 0);

    private final List<Notifier> registeredNotifiers;

    public GameEventProcessor(List<Notifier> registeredNotifiers) {
        this.registeredNotifiers = registeredNotifiers;
    }

    protected abstract boolean isFeatureEnabled();

    protected abstract void processSanitizedMessage(String sanitizedMessageContent, String activePlayerName, String activeClanName, int currentTick);

    protected boolean canProcessEvent(int currentTick) {
        return true;
    }

    public void evaluateIncomingEvent(ChatMessage incomingChatMessage, String activePlayerName, String activeClanName, int currentTick) {
        if (!isFeatureEnabled() || !canProcessEvent(currentTick)) {
            return;
        }

        ChatMessageType incomingMessageType = incomingChatMessage.getType();
        String typeName = incomingMessageType.name();

        if (incomingMessageType != ChatMessageType.GAMEMESSAGE
                && incomingMessageType != ChatMessageType.SPAM
                && !typeName.contains("LEVELUP")
                && !typeName.contains("LEVEL_UP")) {
            return;
        }

        String sanitizedMessageContent = incomingChatMessage.getMessage().replaceAll("<[^>]+>", "");
        processSanitizedMessage(sanitizedMessageContent, activePlayerName, activeClanName, currentTick);
    }

    public void evaluateActorDeath(ActorDeath incomingDeathEvent, String activePlayerName, String activeClanName, int currentTick) {
    }

    protected List<ChatSegment> buildPlayerClanPrefixSegments(String activePlayerName, String activeClanName) {
        List<ChatSegment> prefixSegments = new ArrayList<>();

        if (activeClanName != null && !activeClanName.isEmpty()) {
            prefixSegments.add(new ChatSegment("[" + activeClanName + "] ", Color.BLUE));
        }

        prefixSegments.add(new ChatSegment(activePlayerName + " ", Color.BLACK));

        return prefixSegments;
    }

    protected void dispatchNotificationSegments(List<ChatSegment> generatedSegments) {
        for (Notifier currentNotifier : registeredNotifiers) {
            currentNotifier.dispatchNotification(generatedSegments);
        }
    }
}