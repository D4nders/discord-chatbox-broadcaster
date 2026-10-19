package com.discordchatboxbroadcaster.event;

import com.discordchatboxbroadcaster.DiscordChatboxBroadcasterConfig;
import com.discordchatboxbroadcaster.notifier.Notifier;
import com.discordchatboxbroadcaster.render.ChatSegment;
import com.discordchatboxbroadcaster.render.ChatSegment.Role;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CombatAchievementEventProcessor extends GameEventProcessor {

    private static final Pattern COMBAT_ACHIEVEMENT_DETECTION_PATTERN = Pattern.compile("(?:CA_ID:\\d+\\|)?Congratulations, you've completed an? (\\w+) combat task: (.*)");
    private static final Pattern TIER_COMPLETION_PATTERN = Pattern.compile("You've completed enough Combat Achievement tasks to unlock the (\\w+) Tier rewards!.*", Pattern.CASE_INSENSITIVE);

    private final DiscordChatboxBroadcasterConfig pluginConfiguration;

    public CombatAchievementEventProcessor(List<Notifier> registeredNotifiers, DiscordChatboxBroadcasterConfig pluginConfiguration) {
        super(registeredNotifiers);
        this.pluginConfiguration = pluginConfiguration;
    }

    @Override
    protected boolean isFeatureEnabled() {
        return pluginConfiguration.notifyCombatAchievement();
    }

    @Override
    protected void processSanitizedMessage(String sanitizedMessageContent, String activePlayerName, String activeClanName, BufferedImage playerIcon, int currentTick) {
        Matcher patternMatcher = COMBAT_ACHIEVEMENT_DETECTION_PATTERN.matcher(sanitizedMessageContent);

        if (patternMatcher.find()) {
            String taskName = patternMatcher.group(2);
            if (taskName.endsWith(".")) {
                taskName = taskName.substring(0, taskName.length() - 1);
            }
            executeNotificationSequence(activePlayerName, activeClanName, playerIcon, patternMatcher.group(1).toLowerCase(), taskName);
            return;
        }

        Matcher tierMatcher = TIER_COMPLETION_PATTERN.matcher(sanitizedMessageContent);
        if (tierMatcher.find()) {
            String tier = tierMatcher.group(1);
            executeTierNotificationSequence(activePlayerName, activeClanName, playerIcon, tier);
        }
    }

    private void executeNotificationSequence(String activePlayerName, String activeClanName, BufferedImage playerIcon, String tier, String taskName) {
        List<ChatSegment> notificationSegments = buildPlayerClanPrefixSegments(activePlayerName, activeClanName, playerIcon);
        notificationSegments.add(new ChatSegment("has completed a ", Role.TEXT));
        notificationSegments.add(new ChatSegment(tier, Role.HIGHLIGHT));
        notificationSegments.add(new ChatSegment(" combat task: ", Role.TEXT));
        notificationSegments.add(new ChatSegment(taskName + ".", Role.HIGHLIGHT));

        dispatchNotificationSegments(notificationSegments);
    }

    private void executeTierNotificationSequence(String activePlayerName, String activeClanName, BufferedImage playerIcon, String tier) {
        List<ChatSegment> segments = new ArrayList<>(buildPlayerClanPrefixSegments(activePlayerName, activeClanName, playerIcon));
        segments.add(new ChatSegment("has completed the ", Role.TEXT));
        segments.add(new ChatSegment(tier + " Tier", Role.HIGHLIGHT));
        segments.add(new ChatSegment(" of Combat Achievements!", Role.TEXT));

        dispatchNotificationSegments(segments);
    }
}