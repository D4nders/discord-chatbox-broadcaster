package com.discordchatboxbroadcaster.event;

import com.discordchatboxbroadcaster.DiscordChatboxBroadcasterConfig;
import com.discordchatboxbroadcaster.notifier.Notifier;
import com.discordchatboxbroadcaster.render.ChatSegment;
import com.discordchatboxbroadcaster.render.ChatSegment.Role;
import net.runelite.api.GameState;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NewRecordEventProcessor extends GameEventProcessor {

    private static final Pattern RECORD_DETECTION_PATTERN = Pattern.compile(
        "(?i)^(?:(?<prefix>.+?) )??(?<detail>(?:(?:Fight |Lap |Challenge |Corrupted challenge )?duration:|Subdued in|(?:(?:Challenge |Total )?completion time:)) [0-9:]+(?:\\.\\d+)?) \\(new personal best\\)"
    );
    private static final Pattern COX_RECORD_PATTERN = Pattern.compile(
        "(?i)Congratulations - your raid is complete!.*?(?<detail>Duration: [0-9:]+(?:\\.\\d+)?) \\(new personal best\\)"
    );
    private static final Pattern RAID_COMPLETION_PREFIX_PATTERN = Pattern.compile(
        "(?i)^(?<prefix>.+?) (?:(?:Challenge |Total )?completion time:) [0-9:]+"
    );
    private static final Pattern KILLCOUNT_PATTERN = Pattern.compile(
        "(?i)Your (?:completion count for |subdued |completed )?(.+?) (?:(?:kill|chest|harvest|lap|completion|success|opened|Total Ticket) )?(?:count )?is: ?[0-9,]+"
    );

    private final DiscordChatboxBroadcasterConfig pluginConfiguration;

    private String cachedBossName = null;
    private int bossNameTick = -1;
    private String pendingRecordDetails = null;
    private List<ChatSegment> pendingPrefixSegments = null;
    private int pendingRecordTick = -1;

    public NewRecordEventProcessor(List<Notifier> registeredNotifiers, DiscordChatboxBroadcasterConfig pluginConfiguration) {
        super(registeredNotifiers);
        this.pluginConfiguration = pluginConfiguration;
    }

    @Override
    protected boolean isFeatureEnabled() {
        return pluginConfiguration.notifyNewRecord();
    }

    @Override
    public void evaluateGameStateChanged(GameState newGameState) {
        if (newGameState == GameState.LOGIN_SCREEN || newGameState == GameState.HOPPING) {
            cachedBossName = null;
            bossNameTick = -1;
            pendingRecordDetails = null;
            pendingPrefixSegments = null;
            pendingRecordTick = -1;
        }
    }

    @Override
    public void evaluateGameTick(int currentTick) {
        if (pendingRecordDetails != null) {
            if (currentTick > pendingRecordTick) {
                String resolvedBoss = (cachedBossName != null && Math.abs(currentTick - bossNameTick) <= 2) ? cachedBossName : null;
                if (pluginConfiguration.notifyNewRecord() && pendingPrefixSegments != null) {
                    sendRecordNotification(pendingPrefixSegments, resolvedBoss, pendingRecordDetails);
                }
                pendingRecordDetails = null;
                pendingPrefixSegments = null;
                pendingRecordTick = -1;
            }
        }

        if (cachedBossName != null && Math.abs(currentTick - bossNameTick) > 2) {
            cachedBossName = null;
            bossNameTick = -1;
        }
    }

    @Override
    protected void processSanitizedMessage(String sanitizedMessageContent, String activePlayerName, String activeClanName, BufferedImage playerIcon, int currentTick) {
        Matcher kcMatcher = KILLCOUNT_PATTERN.matcher(sanitizedMessageContent);
        if (kcMatcher.find()) {
            cachedBossName = kcMatcher.group(1).trim();
            bossNameTick = currentTick;
            if (pendingRecordDetails != null) {
                if (pluginConfiguration.notifyNewRecord() && pendingPrefixSegments != null) {
                    sendRecordNotification(pendingPrefixSegments, cachedBossName, pendingRecordDetails);
                }
                pendingRecordDetails = null;
                pendingPrefixSegments = null;
                pendingRecordTick = -1;
            }
            return;
        }

        Matcher coxMatcher = COX_RECORD_PATTERN.matcher(sanitizedMessageContent);
        if (coxMatcher.find()) {
            if (pluginConfiguration.notifyNewRecord()) {
                sendRecordNotification(buildPlayerClanPrefixSegments(activePlayerName, activeClanName, playerIcon), "Chambers of Xeric", coxMatcher.group("detail"));
            }
            return;
        }

        Matcher recordMatcher = RECORD_DETECTION_PATTERN.matcher(sanitizedMessageContent);
        if (recordMatcher.find()) {
            String inlinePrefix = recordMatcher.group("prefix");
            String recordDetails = recordMatcher.group("detail");

            if (inlinePrefix != null && !inlinePrefix.trim().isEmpty()) {
                cachedBossName = inlinePrefix.trim();
                bossNameTick = currentTick;
            }

            if (pendingRecordDetails != null) {
                if (pluginConfiguration.notifyNewRecord() && pendingPrefixSegments != null) {
                    sendRecordNotification(pendingPrefixSegments, cachedBossName, pendingRecordDetails);
                }
                pendingRecordDetails = null;
                pendingPrefixSegments = null;
                pendingRecordTick = -1;
            }

            if (!pluginConfiguration.notifyNewRecord()) {
                return;
            }

            String resolvedBoss = (cachedBossName != null && Math.abs(currentTick - bossNameTick) <= 2) ? cachedBossName : null;
            if (resolvedBoss != null) {
                sendRecordNotification(buildPlayerClanPrefixSegments(activePlayerName, activeClanName, playerIcon), resolvedBoss, recordDetails);
            } else {
                pendingRecordDetails = recordDetails;
                pendingPrefixSegments = buildPlayerClanPrefixSegments(activePlayerName, activeClanName, playerIcon);
                pendingRecordTick = currentTick;
            }
            return;
        }

        Matcher raidPrefixMatcher = RAID_COMPLETION_PREFIX_PATTERN.matcher(sanitizedMessageContent);
        if (raidPrefixMatcher.find()) {
            String prefix = raidPrefixMatcher.group("prefix").trim();
            if (!prefix.equalsIgnoreCase("Challenge") && !prefix.equalsIgnoreCase("Total")) {
                cachedBossName = prefix;
                bossNameTick = currentTick;
                if (pendingRecordDetails != null) {
                    if (pluginConfiguration.notifyNewRecord() && pendingPrefixSegments != null) {
                        sendRecordNotification(pendingPrefixSegments, cachedBossName, pendingRecordDetails);
                    }
                    pendingRecordDetails = null;
                    pendingPrefixSegments = null;
                    pendingRecordTick = -1;
                }
            }
        }
    }

    private void sendRecordNotification(List<ChatSegment> prefixSegments, String bossName, String recordDetails) {
        List<ChatSegment> segments = new ArrayList<>(prefixSegments);
        if (bossName != null && !bossName.isEmpty()) {
            segments.add(new ChatSegment("achieved a new ", Role.TEXT));
            segments.add(new ChatSegment(bossName, Role.HIGHLIGHT));
            segments.add(new ChatSegment(" record: ", Role.TEXT));
        } else {
            segments.add(new ChatSegment("achieved a new record: ", Role.TEXT));
        }
        segments.add(new ChatSegment(recordDetails, Role.HIGHLIGHT));
        dispatchNotificationSegments(segments);
    }
}