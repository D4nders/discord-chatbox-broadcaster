package com.discordchatboxbroadcaster.event;

import com.discordchatboxbroadcaster.DiscordChatboxBroadcasterConfig;
import com.discordchatboxbroadcaster.config.XpMilestoneInterval;
import com.discordchatboxbroadcaster.notifier.Notifier;
import com.discordchatboxbroadcaster.render.ChatSegment;
import com.discordchatboxbroadcaster.render.ChatSegment.Role;
import net.runelite.api.Experience;
import net.runelite.api.GameState;
import net.runelite.api.Skill;
import net.runelite.api.events.StatChanged;

import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class XpMilestoneEventProcessor extends GameEventProcessor {

    private static final int LEVEL_99_XP = Experience.getXpForLevel(99);
    private static final long DEFAULT_XP_INTERVAL = 1_000_000L;

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

        long intervalXp = getMilestoneIntervalXp();
        long effectivePreviousXp = Math.max((long) previousXp, (long) LEVEL_99_XP);
        long previousIntervals = effectivePreviousXp / intervalXp;
        long currentIntervals = (long) currentXp / intervalXp;

        if (currentIntervals > previousIntervals) {
            long milestoneXp = currentIntervals * intervalXp;
            String formattedXp = String.format(Locale.US, "%,d", milestoneXp);
            String skillName = skill.getName();

            List<ChatSegment> notificationSegments = buildPlayerClanPrefixSegments(activePlayerName, activeClanName, playerIcon);
            notificationSegments.add(new ChatSegment("has reached ", Role.TEXT));
            notificationSegments.add(new ChatSegment(formattedXp, Role.HIGHLIGHT));
            notificationSegments.add(new ChatSegment(" XP in ", Role.TEXT));
            notificationSegments.add(new ChatSegment(skillName, Role.HIGHLIGHT));
            notificationSegments.add(new ChatSegment("!", Role.TEXT));

            dispatchNotificationSegments(notificationSegments);
        }
    }

    private long getMilestoneIntervalXp() {
        XpMilestoneInterval interval = pluginConfiguration.xpMilestoneInterval();
        if (interval == null) {
            return DEFAULT_XP_INTERVAL;
        }
        long xp = interval.getXp();
        return xp < 1 ? DEFAULT_XP_INTERVAL : xp;
    }
}
