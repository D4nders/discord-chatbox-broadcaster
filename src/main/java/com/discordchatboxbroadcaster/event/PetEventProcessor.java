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
    private static final Pattern UNTRADEABLE_DROP_PATTERN = Pattern.compile("Untradeable drop: (?:\\d+ x )?(.*)");

    private final DiscordChatboxBroadcasterConfig pluginConfiguration;
    private final SharedEventState sharedEventState;

    private int cachedFunnyFeelingTick = -1;
    private String cachedFunnyFeelingMessage = null;
    private String cachedPlayerName = null;
    private String cachedClanName = null;
    private BufferedImage cachedPlayerIcon = null;

    private int cachedUntradeableDropTick = -1;
    private String cachedUntradeableDropName = null;

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
        Matcher patternMatcher = PET_DETECTION_PATTERN.matcher(sanitizedMessageContent);

        if (patternMatcher.find()) {
            if (currentTick != -1) {
                sharedEventState.registerPetDrop(currentTick);
            }

            String baseMessage = patternMatcher.group(1);
            String petName = patternMatcher.group(2);
            String kcOrXp = patternMatcher.group(3);

            if (baseMessage != null) {
                baseMessage = baseMessage.replaceAll("\\.+$", "").trim();
            }
            if (petName != null) {
                petName = petName.replaceAll("\\.+$", "").trim();
            }
            if (kcOrXp != null) {
                kcOrXp = kcOrXp.replaceAll("\\.+$", "").trim();
            }

            if (petName != null && !petName.isEmpty()) {
                executeNotificationSequence(activePlayerName, activeClanName, playerIcon, baseMessage, petName, kcOrXp);
            } else {
                cachedFunnyFeelingTick = currentTick;
                cachedFunnyFeelingMessage = baseMessage;
                cachedPlayerName = activePlayerName;
                cachedClanName = activeClanName;
                cachedPlayerIcon = playerIcon;

                checkAndFireCachedEvent();
            }
            return;
        }

        Matcher untradeableMatcher = UNTRADEABLE_DROP_PATTERN.matcher(sanitizedMessageContent);
        if (untradeableMatcher.find()) {
            String petName = untradeableMatcher.group(1);
            if (petName != null) {
                petName = petName.replaceAll("\\.+$", "").trim();
            }
            cachedUntradeableDropTick = currentTick;
            cachedUntradeableDropName = petName;

            checkAndFireCachedEvent();
        }
    }

    private void checkAndFireCachedEvent() {
        if (cachedFunnyFeelingMessage != null && cachedUntradeableDropName != null) {
            if (Math.abs(cachedFunnyFeelingTick - cachedUntradeableDropTick) <= 1 || cachedFunnyFeelingTick == -1 || cachedUntradeableDropTick == -1) {
                executeNotificationSequence(cachedPlayerName, cachedClanName, cachedPlayerIcon, cachedFunnyFeelingMessage, cachedUntradeableDropName, null);
                clearFunnyFeelingCache();
                clearUntradeableDropCache();
            }
        }
    }

    private void clearFunnyFeelingCache() {
        cachedFunnyFeelingTick = -1;
        cachedFunnyFeelingMessage = null;
        cachedPlayerName = null;
        cachedClanName = null;
        cachedPlayerIcon = null;
    }

    private void clearUntradeableDropCache() {
        cachedUntradeableDropTick = -1;
        cachedUntradeableDropName = null;
    }

    @Override
    public void evaluateGameTick(int currentTick) {
        if (cachedFunnyFeelingMessage != null && currentTick > cachedFunnyFeelingTick + 1 && cachedFunnyFeelingTick != -1) {
            executeNotificationSequence(cachedPlayerName, cachedClanName, cachedPlayerIcon, cachedFunnyFeelingMessage, null, null);
            clearFunnyFeelingCache();
        }
        if (cachedUntradeableDropName != null && currentTick > cachedUntradeableDropTick + 1 && cachedUntradeableDropTick != -1) {
            clearUntradeableDropCache();
        }
    }

    private void executeNotificationSequence(String activePlayerName, String activeClanName, BufferedImage playerIcon, String baseMessage, String petName, String kcOrXp) {
        String formattedPetMessage = baseMessage
                .replace("You have a funny feeling like you're", "has a funny feeling like they're")
                .replace("You feel something weird sneaking into your", "feels something weird sneaking into their")
                .replace("You have a funny feeling like you would have been", "has a funny feeling like they would have been");

        List<ChatSegment> notificationSegments = buildPlayerClanPrefixSegments(activePlayerName, activeClanName, playerIcon);

        if (petName != null && !petName.isEmpty()) {
            notificationSegments.add(new ChatSegment(formattedPetMessage + ": ", Color.BLACK));
            notificationSegments.add(new ChatSegment(petName, HIGHLIGHT_COLOR));

            if (kcOrXp != null && !kcOrXp.isEmpty()) {
                notificationSegments.add(new ChatSegment(" at " + kcOrXp + ".", Color.BLACK));
            } else {
                notificationSegments.add(new ChatSegment(".", Color.BLACK));
            }
        } else {
            notificationSegments.add(new ChatSegment(formattedPetMessage + ".", Color.BLACK));
        }

        dispatchNotificationSegments(notificationSegments);
    }
}