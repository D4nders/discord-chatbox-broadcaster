package com.discordchatboxbroadcaster.event;

import com.discordchatboxbroadcaster.DiscordChatboxBroadcasterConfig;
import com.discordchatboxbroadcaster.SharedEventState;
import com.discordchatboxbroadcaster.notifier.Notifier;
import com.discordchatboxbroadcaster.render.ChatSegment;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PetEventProcessor extends GameEventProcessor {

    private static final Pattern PET_DETECTION_PATTERN = Pattern.compile("(You have a funny feeling[^:]+|You feel something weird sneaking into your backpack[^:]+)(?:: (.*?)(?: at (.*))?)?");
    private static final Pattern UNTRADEABLE_DROP_PATTERN = Pattern.compile("Untradeable drop: (.+)");
    private static final String UNTRADEABLE_WARNING_MESSAGE = "Pet Notifier cannot identify pet names unless you enable the game setting: Untradeable loot notifications";

    private final DiscordChatboxBroadcasterConfig pluginConfiguration;
    private final SharedEventState sharedEventState;

    private PendingPetEvent pendingPetEvent;

    public PetEventProcessor(List<Notifier> registeredNotifiers, DiscordChatboxBroadcasterConfig pluginConfiguration, SharedEventState sharedEventState) {
        super(registeredNotifiers);
        this.pluginConfiguration = pluginConfiguration;
        this.sharedEventState = sharedEventState;
    }

    @Override
    protected boolean isFeatureEnabled() {
        return pluginConfiguration.notifyPet();
    }

    @Override
    protected void processSanitizedMessage(String sanitizedMessageContent, String activePlayerName, String activeClanName, BufferedImage playerIcon, int currentTick) {
        evaluatePendingPetEventTimeout(currentTick);

        Matcher petMatcher = PET_DETECTION_PATTERN.matcher(sanitizedMessageContent);

        if (petMatcher.find()) {
            if (currentTick != -1) {
                sharedEventState.registerPetDrop(currentTick);
            }

            String baseMessage = sanitizeText(petMatcher.group(1));
            String petName = sanitizeText(petMatcher.group(2));
            String kcOrXp = sanitizeText(petMatcher.group(3));

            if (isTextMissing(petName)) {
                pendingPetEvent = new PendingPetEvent(activePlayerName, activeClanName, playerIcon, baseMessage, currentTick);
                return;
            }

            executeNotificationSequence(activePlayerName, activeClanName, playerIcon, baseMessage, petName, kcOrXp);
            return;
        }

        if (pendingPetEvent != null) {
            Matcher untradeableMatcher = UNTRADEABLE_DROP_PATTERN.matcher(sanitizedMessageContent);

            if (untradeableMatcher.find()) {
                String extractedPetName = sanitizeText(untradeableMatcher.group(1));
                executeNotificationSequence(
                        pendingPetEvent.getPlayerName(),
                        pendingPetEvent.getClanName(),
                        pendingPetEvent.getPlayerIcon(),
                        pendingPetEvent.getBaseMessage(),
                        extractedPetName,
                        null
                );
                pendingPetEvent = null;
            }
        }
    }

    public void evaluatePendingPetEventTimeout(int currentTick) {
        if (pendingPetEvent != null && currentTick != -1 && currentTick > pendingPetEvent.getCreationTick() + 2) {
            executeNotificationSequence(
                    pendingPetEvent.getPlayerName(),
                    pendingPetEvent.getClanName(),
                    pendingPetEvent.getPlayerIcon(),
                    pendingPetEvent.getBaseMessage(),
                    null,
                    null
            );
            pendingPetEvent = null;
        }
    }

    private String sanitizeText(String text) {
        if (text != null) {
            return text.replaceAll("\\.+$", "").trim();
        }
        return null;
    }

    private boolean isTextMissing(String text) {
        return text == null || text.isEmpty();
    }

    private void executeNotificationSequence(String activePlayerName, String activeClanName, BufferedImage playerIcon, String baseMessage, String petName, String kcOrXp) {
        String formattedPetMessage = formatBasePetMessage(baseMessage);
        List<ChatSegment> notificationSegments = buildPlayerClanPrefixSegments(activePlayerName, activeClanName, playerIcon);

        if (isTextMissing(petName)) {
            notificationSegments.add(new ChatSegment(formattedPetMessage + ".", Color.BLACK));
            notificationSegments.add(new ChatSegment(" (" + UNTRADEABLE_WARNING_MESSAGE + ")", Color.GRAY));
        } else {
            notificationSegments.add(new ChatSegment(formattedPetMessage + ": ", Color.BLACK));
            notificationSegments.add(new ChatSegment(petName, HIGHLIGHT_COLOR));

            if (!isTextMissing(kcOrXp)) {
                notificationSegments.add(new ChatSegment(" at " + kcOrXp + ".", Color.BLACK));
            } else {
                notificationSegments.add(new ChatSegment(".", Color.BLACK));
            }
        }

        dispatchNotificationSegments(notificationSegments);
    }

    private String formatBasePetMessage(String baseMessage) {
        return baseMessage
                .replace("You have a funny feeling like you're", "has a funny feeling like they're")
                .replace("You feel something weird sneaking into your", "feels something weird sneaking into their")
                .replace("You have a funny feeling like you would have been", "has a funny feeling like they would have been");
    }

    private static class PendingPetEvent {
        private final String playerName;
        private final String clanName;
        private final BufferedImage playerIcon;
        private final String baseMessage;
        private final int creationTick;

        public PendingPetEvent(String playerName, String clanName, BufferedImage playerIcon, String baseMessage, int creationTick) {
            this.playerName = playerName;
            this.clanName = clanName;
            this.playerIcon = playerIcon;
            this.baseMessage = baseMessage;
            this.creationTick = creationTick;
        }

        public String getPlayerName() { return playerName; }
        public String getClanName() { return clanName; }
        public BufferedImage getPlayerIcon() { return playerIcon; }
        public String getBaseMessage() { return baseMessage; }
        public int getCreationTick() { return creationTick; }
    }
}