package com.discordchatboxbroadcaster.event;

import com.discordchatboxbroadcaster.DiscordChatboxBroadcasterConfig;
import com.discordchatboxbroadcaster.notifier.Notifier;
import com.discordchatboxbroadcaster.render.ChatSegment;
import com.discordchatboxbroadcaster.render.ChatSegment.Role;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LevelUpEventProcessor extends GameEventProcessor {

    private static final Pattern MAX_LEVEL_PATTERN = Pattern.compile("Congratulations, you(?:'ve)? reached the highest possible (.*?) level of 99\\.");
    private static final Pattern STANDARD_LEVEL_NAME_PATTERN = Pattern.compile("Congratulations, you(?:'ve)? just advanced (?:a|an|your) (.*?) level\\.");
    private static final Pattern STANDARD_LEVEL_NUMBER_PATTERN = Pattern.compile("You are now level (\\d+)\\.");

    private final DiscordChatboxBroadcasterConfig pluginConfiguration;

    public LevelUpEventProcessor(List<Notifier> registeredNotifiers, DiscordChatboxBroadcasterConfig pluginConfiguration) {
        super(registeredNotifiers);
        this.pluginConfiguration = pluginConfiguration;
    }

    @Override
    protected boolean isFeatureEnabled() {
        return pluginConfiguration.notifyLevelUp();
    }

    @Override
    protected void processSanitizedMessage(String sanitizedMessageContent, String activePlayerName, String activeClanName, BufferedImage playerIcon, int currentTick) {
        Matcher maxLevelMatcher = MAX_LEVEL_PATTERN.matcher(sanitizedMessageContent);

        if (maxLevelMatcher.find()) {
            if (!shouldBroadcastLevel(99)) {
                return;
            }
            executeNotificationSequence(activePlayerName, activeClanName, playerIcon, maxLevelMatcher.group(1), "99");
            return;
        }

        Matcher skillNameMatcher = STANDARD_LEVEL_NAME_PATTERN.matcher(sanitizedMessageContent);

        if (skillNameMatcher.find()) {
            String skillName = skillNameMatcher.group(1);
            String skillLevel = null;

            Matcher skillLevelMatcher = STANDARD_LEVEL_NUMBER_PATTERN.matcher(sanitizedMessageContent);

            if (skillLevelMatcher.find()) {
                skillLevel = skillLevelMatcher.group(1);
            }

            Integer parsedLevel = null;
            if (skillLevel != null) {
                try {
                    parsedLevel = Integer.parseInt(skillLevel);
                } catch (NumberFormatException ignored) {
                }
            }

            if (!shouldBroadcastLevel(parsedLevel)) {
                return;
            }

            executeNotificationSequence(activePlayerName, activeClanName, playerIcon, skillName, skillLevel);
        }
    }

    private boolean shouldBroadcastLevel(Integer level) {
        if (level == null) {
            return true;
        }
        int minLevel = Math.max(1, pluginConfiguration.levelUpMinLevel());
        int interval = Math.max(1, pluginConfiguration.levelUpInterval());
        return level >= minLevel && (level % interval == 0 || level == 99);
    }

    private void executeNotificationSequence(String activePlayerName, String activeClanName, BufferedImage playerIcon, String skillName, String skillLevel) {
        List<ChatSegment> notificationSegments = buildPlayerClanPrefixSegments(activePlayerName, activeClanName, playerIcon);

        if (skillLevel != null) {
            notificationSegments.add(new ChatSegment("has reached ", Role.TEXT));
            notificationSegments.add(new ChatSegment(skillName + " level " + skillLevel, Role.HIGHLIGHT));
            notificationSegments.add(new ChatSegment(".", Role.TEXT));
        } else {
            notificationSegments.add(new ChatSegment("advanced a level: ", Role.TEXT));
            notificationSegments.add(new ChatSegment(skillName, Role.HIGHLIGHT));
        }

        dispatchNotificationSegments(notificationSegments);
    }
}