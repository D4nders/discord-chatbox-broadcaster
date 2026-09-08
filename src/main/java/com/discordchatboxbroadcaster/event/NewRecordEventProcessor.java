package com.discordchatboxbroadcaster.event;

import com.discordchatboxbroadcaster.DiscordChatboxBroadcasterConfig;
import com.discordchatboxbroadcaster.notifier.Notifier;
import com.discordchatboxbroadcaster.render.ChatSegment;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NewRecordEventProcessor extends GameEventProcessor {

    private static final Pattern RECORD_DETECTION_PATTERN = Pattern.compile("(?i)((?:(?:Fight |Lap |Challenge |Corrupted challenge )?duration:|Subdued in|(?<!total )completion time:) [0-9:]+(?:\\.\\d+)?) \\(new personal best\\)");
    private static final Pattern KILLCOUNT_PATTERN = Pattern.compile("(?i)Your (?:completion count for |subdued |completed )?(.+?) (?:(?:kill|harvest|lap|completion|success|Total Ticket) )?(?:count )?is: ?[0-9,]+");

    private final DiscordChatboxBroadcasterConfig pluginConfiguration;

    private String cachedBossName = null;
    private int bossNameTick = -1;

    public NewRecordEventProcessor(List<Notifier> registeredNotifiers, DiscordChatboxBroadcasterConfig pluginConfiguration) {
        super(registeredNotifiers);
        this.pluginConfiguration = pluginConfiguration;
    }

    @Override
    protected boolean isFeatureEnabled() {
        return pluginConfiguration.notifyNewRecord();
    }

    @Override
    protected void processSanitizedMessage(String sanitizedMessageContent, String activePlayerName, String activeClanName, BufferedImage playerIcon, int currentTick) {
        Matcher kcMatcher = KILLCOUNT_PATTERN.matcher(sanitizedMessageContent);
        if (kcMatcher.find()) {
            cachedBossName = kcMatcher.group(1);
            bossNameTick = currentTick;
            return;
        }

        Matcher patternMatcher = RECORD_DETECTION_PATTERN.matcher(sanitizedMessageContent);

        if (patternMatcher.find()) {
            String recordDetails = patternMatcher.group(1);
            String bossName = null;
            if (cachedBossName != null && currentTick == bossNameTick) {
                bossName = cachedBossName;
            }
            executeNotificationSequence(activePlayerName, activeClanName, playerIcon, recordDetails, bossName);
        }
    }

    private void executeNotificationSequence(String activePlayerName, String activeClanName, BufferedImage playerIcon, String recordDetails, String bossName) {
        List<ChatSegment> notificationSegments = buildPlayerClanPrefixSegments(activePlayerName, activeClanName, playerIcon);
        
        if (bossName != null) {
            notificationSegments.add(new ChatSegment("achieved a new " + bossName + " record: ", Color.BLACK));
        } else {
            notificationSegments.add(new ChatSegment("achieved a new record: ", Color.BLACK));
        }
        
        notificationSegments.add(new ChatSegment(recordDetails, HIGHLIGHT_COLOR));

        dispatchNotificationSegments(notificationSegments);
    }

    @Override
    public void evaluateGameTick(int currentTick) {
        if (cachedBossName != null && currentTick > bossNameTick) {
            cachedBossName = null;
            bossNameTick = -1;
        }
    }
}