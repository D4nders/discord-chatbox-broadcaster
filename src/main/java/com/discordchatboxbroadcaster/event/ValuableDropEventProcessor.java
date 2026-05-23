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

public class ValuableDropEventProcessor extends GameEventProcessor {

    private static final Pattern VALUABLE_DROP_DETECTION_PATTERN = Pattern.compile("(?:Valuable drop: |Untradeable drop: )(?:\\d+ x )?(.*)");

    private final DiscordChatboxBroadcasterConfig pluginConfiguration;
    private final SharedEventState sharedEventState;

    public ValuableDropEventProcessor(List<Notifier> registeredNotifiers, DiscordChatboxBroadcasterConfig pluginConfiguration, SharedEventState sharedEventState) {
        super(registeredNotifiers);
        this.pluginConfiguration = pluginConfiguration;
        this.sharedEventState = sharedEventState;
    }

    @Override
    protected boolean isFeatureEnabled() {
        return pluginConfiguration.notifyValuableDrop();
    }

    @Override
    protected void processSanitizedMessage(String sanitizedMessageContent, String activePlayerName, String activeClanName, BufferedImage playerIcon, int currentTick) {
        if (currentTick != -1 && sharedEventState.isWithinCollectionLogWindow(currentTick)) {
            return;
        }

        Matcher patternMatcher = VALUABLE_DROP_DETECTION_PATTERN.matcher(sanitizedMessageContent);

        if (patternMatcher.find()) {
            executeNotificationSequence(activePlayerName, activeClanName, playerIcon, patternMatcher.group(1));
        }
    }

    private void executeNotificationSequence(String activePlayerName, String activeClanName, BufferedImage playerIcon, String dropDetails) {
        List<ChatSegment> notificationSegments = buildPlayerClanPrefixSegments(activePlayerName, activeClanName, playerIcon);
        notificationSegments.add(new ChatSegment("received a drop: ", Color.BLACK));
        notificationSegments.add(new ChatSegment(dropDetails, HIGHLIGHT_COLOR));

        dispatchNotificationSegments(notificationSegments);
    }
}