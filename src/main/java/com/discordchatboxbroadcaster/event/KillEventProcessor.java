package com.discordchatboxbroadcaster.event;

import com.discordchatboxbroadcaster.DiscordChatboxBroadcasterConfig;
import com.discordchatboxbroadcaster.notifier.Notifier;
import com.discordchatboxbroadcaster.render.ChatSegment;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KillEventProcessor extends GameEventProcessor {

    private static final Pattern KILL_DETECTION_PATTERN = Pattern.compile("You have defeated (.*?)(?: and received (.*) coins)?\\.");

    private final DiscordChatboxBroadcasterConfig pluginConfiguration;
    private final Map<String, Integer> recentPlayerDeaths = new HashMap<>();

    public KillEventProcessor(List<Notifier> registeredNotifiers, DiscordChatboxBroadcasterConfig pluginConfiguration) {
        super(registeredNotifiers);
        this.pluginConfiguration = pluginConfiguration;
    }

    @Override
    protected boolean isFeatureEnabled() {
        return pluginConfiguration.notifyKill();
    }

    @Override
    protected void processSanitizedMessage(String sanitizedMessageContent, String activePlayerName, String activeClanName, BufferedImage playerIcon, int currentTick) {
        Matcher patternMatcher = KILL_DETECTION_PATTERN.matcher(sanitizedMessageContent);

        if (patternMatcher.find()) {
            String defeatedTarget = patternMatcher.group(1).replace('\u00A0', ' ');
            String coinsReceived = patternMatcher.group(2);

            Integer deathTick = recentPlayerDeaths.get(defeatedTarget);
            
            // A 10-tick window is appropriate because the ActorDeath event triggers when
            // HP reaches 0, but the chat message may not appear until the death animation 
            // completes (which is around 5-8 ticks for players).
            //
            // Edge Case Note: If a player defeats an NPC (e.g., a boss) that triggers a 
            // "You have defeated X." message, AND a player with the exact same name as 
            // the NPC died in the vicinity within the last 10 ticks, it will falsely 
            // broadcast as a PvP kill. This is extremely unlikely and accepted.
            if (deathTick == null || Math.abs(currentTick - deathTick) > 10) {
                return; // Target was not a player that died recently
            }

            executeNotificationSequence(activePlayerName, activeClanName, playerIcon, defeatedTarget, coinsReceived);
        }
    }

    @Override
    public void evaluateActorDeath(net.runelite.api.events.ActorDeath incomingDeathEvent, String activePlayerName, String activeClanName, BufferedImage playerIcon, int currentTick) {
        if (incomingDeathEvent.getActor() instanceof net.runelite.api.Player) {
            net.runelite.api.Player deadPlayer = (net.runelite.api.Player) incomingDeathEvent.getActor();
            if (deadPlayer.getName() != null) {
                String cleanName = deadPlayer.getName().replace('\u00A0', ' ');
                recentPlayerDeaths.put(cleanName, currentTick);
            }
        }
    }

    @Override
    public void evaluateGameTick(int currentTick) {
        recentPlayerDeaths.entrySet().removeIf(entry -> Math.abs(currentTick - entry.getValue()) > 20);
    }

    private void executeNotificationSequence(String activePlayerName, String activeClanName, BufferedImage playerIcon, String defeatedTarget, String coinsReceived) {
        List<ChatSegment> notificationSegments = buildPlayerClanPrefixSegments(activePlayerName, activeClanName, playerIcon);
        notificationSegments.add(new ChatSegment("has defeated ", Color.BLACK));
        notificationSegments.add(new ChatSegment(defeatedTarget, HIGHLIGHT_COLOR));

        if (coinsReceived != null) {
            notificationSegments.add(new ChatSegment(" and received ", Color.BLACK));
            notificationSegments.add(new ChatSegment(coinsReceived + " coins", HIGHLIGHT_COLOR));
        }

        notificationSegments.add(new ChatSegment("!", Color.BLACK));

        dispatchNotificationSegments(notificationSegments);
    }
}