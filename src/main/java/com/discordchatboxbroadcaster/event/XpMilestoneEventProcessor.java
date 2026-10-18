package com.discordchatboxbroadcaster.event;

import com.discordchatboxbroadcaster.DiscordChatboxBroadcasterConfig;
import com.discordchatboxbroadcaster.notifier.Notifier;
import com.discordchatboxbroadcaster.render.ChatSegment;
import net.runelite.api.Experience;
import net.runelite.api.GameState;
import net.runelite.api.Skill;
import net.runelite.api.events.StatChanged;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class XpMilestoneEventProcessor extends GameEventProcessor {

    private static final int LEVEL_99_XP = Experience.getXpForLevel(99);
    private static final int ONE_MILLION_XP = 1_000_000;

    private final DiscordChatboxBroadcasterConfig pluginConfiguration;
    private final Map<Skill, Integer> previousSkillXp = new EnumMap<>(Skill.class);

    public XpMilestoneEventProcessor(List<Notifier> registeredNotifiers, DiscordChatboxBroadcasterConfig pluginConfiguration) {
        super(registeredNotifiers);
        this.pluginConfiguration = pluginConfiguration;
    }

    @Override
    protected boolean isFeatureEnabled() {
        return pluginConfiguration.notifyXpMilestone();
    }

    @Override
    protected void processSanitizedMessage(String sanitizedMessageContent, String activePlayerName, String activeClanName, BufferedImage playerIcon, int currentTick) {
    }

    @Override
    public void evaluateGameStateChanged(GameState newGameState) {
        if (newGameState == GameState.LOGIN_SCREEN || newGameState == GameState.HOPPING) {
            previousSkillXp.clear();
        }
    }

    @Override
    public void evaluateStatChanged(StatChanged incomingStatEvent, String activePlayerName, String activeClanName, BufferedImage playerIcon, int currentTick) {
        Skill skill = incomingStatEvent.getSkill();
        if (skill == null) {
            return;
        }

        int currentXp = incomingStatEvent.getXp();
        Integer previousXp = previousSkillXp.put(skill, currentXp);
        if (previousXp == null) {
            return;
        }

        if (!isFeatureEnabled() || !canProcessEvent(currentTick)) {
            return;
        }

        if (currentXp <= previousXp || currentXp < LEVEL_99_XP) {
            return;
        }

        int effectivePreviousXp = Math.max(previousXp, LEVEL_99_XP);
        int previousMillions = effectivePreviousXp / ONE_MILLION_XP;
        int currentMillions = currentXp / ONE_MILLION_XP;

        if (currentMillions > previousMillions) {
            long milestoneXp = (long) currentMillions * ONE_MILLION_XP;
            String formattedXp = String.format(Locale.US, "%,d", milestoneXp);
            String skillName = skill.getName();

            List<ChatSegment> notificationSegments = buildPlayerClanPrefixSegments(activePlayerName, activeClanName, playerIcon);
            notificationSegments.add(new ChatSegment("has reached ", Color.BLACK));
            notificationSegments.add(new ChatSegment(formattedXp, HIGHLIGHT_COLOR));
            notificationSegments.add(new ChatSegment(" XP in ", Color.BLACK));
            notificationSegments.add(new ChatSegment(skillName, HIGHLIGHT_COLOR));
            notificationSegments.add(new ChatSegment("!", Color.BLACK));

            dispatchNotificationSegments(notificationSegments);
        }
    }
}
