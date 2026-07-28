package com.discordchatboxbroadcaster.event;

import com.discordchatboxbroadcaster.notifier.Notifier;
import com.discordchatboxbroadcaster.render.ChatSegment;
import net.runelite.api.ChatMessageType;
import net.runelite.api.GameState;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.ChatMessage;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public abstract class GameEventProcessor {

    protected static final Color HIGHLIGHT_COLOR = new Color(239, 16, 32);

    private final List<Notifier> registeredNotifiers;

    public GameEventProcessor(List<Notifier> registeredNotifiers) {
        this.registeredNotifiers = registeredNotifiers;
    }

    protected abstract boolean isFeatureEnabled();

    protected abstract void processSanitizedMessage(String sanitizedMessageContent, String activePlayerName, String activeClanName, BufferedImage playerIcon, int currentTick);

    protected boolean canProcessEvent(int currentTick) {
        return true;
    }

    public void evaluateIncomingEvent(ChatMessage incomingChatMessage, String activePlayerName, String activeClanName, BufferedImage playerIcon, int currentTick) {
        if (!isFeatureEnabled() || !canProcessEvent(currentTick)) {
            return;
        }

        ChatMessageType incomingMessageType = incomingChatMessage.getType();
        String typeName = incomingMessageType.name();

        boolean isGameMessage = incomingMessageType == ChatMessageType.GAMEMESSAGE || incomingMessageType == ChatMessageType.SPAM;
        boolean isLevelUp = typeName.contains("LEVELUP") || typeName.contains("LEVEL_UP");

        if (!isGameMessage && !isLevelUp) {
            return;
        }

        String senderName = incomingChatMessage.getName();
        if (senderName != null && !senderName.trim().isEmpty()) {
            return;
        }

        String sanitizedMessageContent = incomingChatMessage.getMessage().replaceAll("<[^>]+>", "");
        processSanitizedMessage(sanitizedMessageContent, activePlayerName, activeClanName, playerIcon, currentTick);
    }

    public void evaluateActorDeath(ActorDeath incomingDeathEvent, String activePlayerName, String activeClanName, BufferedImage playerIcon, int currentTick) {
    }

    public void evaluateVarbitChanged(int varbitId, int newValue, String activePlayerName, String activeClanName, BufferedImage playerIcon, int currentTick) {
    }

    public void evaluateGameStateChanged(GameState newGameState) {
    }

    protected List<ChatSegment> buildPlayerClanPrefixSegments(String activePlayerName, String activeClanName, BufferedImage playerIcon) {
        List<ChatSegment> prefixSegments = new ArrayList<>();

        if (activeClanName != null && !activeClanName.isEmpty()) {
            prefixSegments.add(new ChatSegment("[" + activeClanName + "] ", Color.BLUE));
        }

        if (playerIcon != null) {
            prefixSegments.add(new ChatSegment(playerIcon));
        }

        prefixSegments.add(new ChatSegment(activePlayerName + " ", Color.BLACK));

        return prefixSegments;
    }

    protected void dispatchNotificationSegments(List<ChatSegment> generatedSegments) {
        for (Notifier currentNotifier : registeredNotifiers) {
            currentNotifier.dispatchNotification(generatedSegments);
        }
    }

    public void evaluateGameTick(int currentTick) {
    }
}