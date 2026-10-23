package com.discordchatboxbroadcaster;

import com.discordchatboxbroadcaster.config.CombatAchievementTier;
import com.discordchatboxbroadcaster.config.XpMilestoneInterval;
import com.discordchatboxbroadcaster.event.CombatAchievementEventProcessor;
import com.discordchatboxbroadcaster.event.LevelUpEventProcessor;
import com.discordchatboxbroadcaster.event.ValuableDropEventProcessor;
import com.discordchatboxbroadcaster.event.XpMilestoneEventProcessor;
import com.discordchatboxbroadcaster.notifier.Notifier;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Skill;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.StatChanged;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EventFilterTest {

    @Mock
    private Notifier notifierMock;

    @Mock
    private DiscordChatboxBroadcasterConfig configMock;

    private AutoCloseable mockitoSession;

    @Before
    public void setUp() {
        mockitoSession = MockitoAnnotations.openMocks(this);
    }

    @After
    public void tearDown() throws Exception {
        if (mockitoSession != null) {
            mockitoSession.close();
        }
    }

    private ChatMessage createChatMessage(String message) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setType(ChatMessageType.GAMEMESSAGE);
        chatMessage.setMessage(message);
        return chatMessage;
    }

    // --- Valuable Drop Filters ---

    @Test
    public void valuableDrop_belowThreshold_isSkipped() {
        when(configMock.notifyValuableDrop()).thenReturn(true);
        when(configMock.valuableDropMinValue()).thenReturn(50_000);

        ValuableDropEventProcessor processor = new ValuableDropEventProcessor(
                Collections.singletonList(notifierMock), configMock, new SharedEventState());

        ChatMessage msg = createChatMessage("Valuable drop: Rune platebody (38,413 coins)");
        processor.evaluateIncomingEvent(msg, "TestPlayer", "TestClan", null, 100);

        verify(notifierMock, never()).dispatchNotification(any());
    }

    @Test
    public void valuableDrop_aboveThreshold_isDispatched() {
        when(configMock.notifyValuableDrop()).thenReturn(true);
        when(configMock.valuableDropMinValue()).thenReturn(50_000);

        ValuableDropEventProcessor processor = new ValuableDropEventProcessor(
                Collections.singletonList(notifierMock), configMock, new SharedEventState());

        ChatMessage msg = createChatMessage("Valuable drop: 1 x Abyssal whip (1,500,000 coins)");
        processor.evaluateIncomingEvent(msg, "TestPlayer", "TestClan", null, 100);

        verify(notifierMock, times(1)).dispatchNotification(any());
    }

    @Test
    public void valuableDrop_equalToThreshold_isDispatched() {
        when(configMock.notifyValuableDrop()).thenReturn(true);
        when(configMock.valuableDropMinValue()).thenReturn(50_000);

        ValuableDropEventProcessor processor = new ValuableDropEventProcessor(
                Collections.singletonList(notifierMock), configMock, new SharedEventState());

        ChatMessage msg = createChatMessage("Valuable drop: Granite maul (50,000 coins)");
        processor.evaluateIncomingEvent(msg, "TestPlayer", "TestClan", null, 100);

        verify(notifierMock, times(1)).dispatchNotification(any());
    }

    @Test
    public void valuableDrop_unparseableValue_isDispatched() {
        when(configMock.notifyValuableDrop()).thenReturn(true);
        when(configMock.valuableDropMinValue()).thenReturn(50_000);

        ValuableDropEventProcessor processor = new ValuableDropEventProcessor(
                Collections.singletonList(notifierMock), configMock, new SharedEventState());

        ChatMessage msg = createChatMessage("Valuable drop: Mysterious mystery box");
        processor.evaluateIncomingEvent(msg, "TestPlayer", "TestClan", null, 100);

        verify(notifierMock, times(1)).dispatchNotification(any());
    }

    @Test
    public void valuableDrop_zeroThreshold_broadcastsAll() {
        when(configMock.notifyValuableDrop()).thenReturn(true);
        when(configMock.valuableDropMinValue()).thenReturn(0);

        ValuableDropEventProcessor processor = new ValuableDropEventProcessor(
                Collections.singletonList(notifierMock), configMock, new SharedEventState());

        ChatMessage msg = createChatMessage("Valuable drop: Bronze dagger (10 coins)");
        processor.evaluateIncomingEvent(msg, "TestPlayer", "TestClan", null, 100);

        verify(notifierMock, times(1)).dispatchNotification(any());
    }

    @Test
    public void valuableDrop_featureDisabled_isSkipped() {
        when(configMock.notifyValuableDrop()).thenReturn(false);
        when(configMock.valuableDropMinValue()).thenReturn(0);

        ValuableDropEventProcessor processor = new ValuableDropEventProcessor(
                Collections.singletonList(notifierMock), configMock, new SharedEventState());

        ChatMessage msg = createChatMessage("Valuable drop: 1 x Abyssal whip (1,500,000 coins)");
        processor.evaluateIncomingEvent(msg, "TestPlayer", "TestClan", null, 100);

        verify(notifierMock, never()).dispatchNotification(any());
    }

    // --- Level Up Filters ---

    @Test
    public void levelUp_belowMinLevel_isSkipped() {
        when(configMock.notifyLevelUp()).thenReturn(true);
        when(configMock.levelUpMinLevel()).thenReturn(50);
        when(configMock.levelUpInterval()).thenReturn(1);

        LevelUpEventProcessor processor = new LevelUpEventProcessor(
                Collections.singletonList(notifierMock), configMock);

        ChatMessage msg = createChatMessage("Congratulations, you just advanced a Strength level. You are now level 40.");
        processor.evaluateIncomingEvent(msg, "TestPlayer", "TestClan", null, 100);

        verify(notifierMock, never()).dispatchNotification(any());
    }

    @Test
    public void levelUp_aboveMinLevel_notMatchingInterval_isSkipped() {
        when(configMock.notifyLevelUp()).thenReturn(true);
        when(configMock.levelUpMinLevel()).thenReturn(50);
        when(configMock.levelUpInterval()).thenReturn(5);

        LevelUpEventProcessor processor = new LevelUpEventProcessor(
                Collections.singletonList(notifierMock), configMock);

        ChatMessage msg = createChatMessage("Congratulations, you just advanced a Strength level. You are now level 52.");
        processor.evaluateIncomingEvent(msg, "TestPlayer", "TestClan", null, 100);

        verify(notifierMock, never()).dispatchNotification(any());
    }

    @Test
    public void levelUp_aboveMinLevel_matchingInterval_isDispatched() {
        when(configMock.notifyLevelUp()).thenReturn(true);
        when(configMock.levelUpMinLevel()).thenReturn(50);
        when(configMock.levelUpInterval()).thenReturn(5);

        LevelUpEventProcessor processor = new LevelUpEventProcessor(
                Collections.singletonList(notifierMock), configMock);

        ChatMessage msg = createChatMessage("Congratulations, you just advanced a Strength level. You are now level 55.");
        processor.evaluateIncomingEvent(msg, "TestPlayer", "TestClan", null, 100);

        verify(notifierMock, times(1)).dispatchNotification(any());
    }

    @Test
    public void levelUp_level99_alwaysDispatchedDespiteInterval() {
        when(configMock.notifyLevelUp()).thenReturn(true);
        when(configMock.levelUpMinLevel()).thenReturn(50);
        when(configMock.levelUpInterval()).thenReturn(5);

        LevelUpEventProcessor processor = new LevelUpEventProcessor(
                Collections.singletonList(notifierMock), configMock);

        ChatMessage msg = createChatMessage("Congratulations, you just advanced a Strength level. You are now level 99.");
        processor.evaluateIncomingEvent(msg, "TestPlayer", "TestClan", null, 100);

        verify(notifierMock, times(1)).dispatchNotification(any());
    }

    @Test
    public void levelUp_maxLevel99Message_alwaysDispatched() {
        when(configMock.notifyLevelUp()).thenReturn(true);
        when(configMock.levelUpMinLevel()).thenReturn(50);
        when(configMock.levelUpInterval()).thenReturn(5);

        LevelUpEventProcessor processor = new LevelUpEventProcessor(
                Collections.singletonList(notifierMock), configMock);

        ChatMessage msg = createChatMessage("Congratulations, you've reached the highest possible Strength level of 99.");
        processor.evaluateIncomingEvent(msg, "TestPlayer", "TestClan", null, 100);

        verify(notifierMock, times(1)).dispatchNotification(any());
    }

    @Test
    public void levelUp_unknownLevel_isDispatched() {
        when(configMock.notifyLevelUp()).thenReturn(true);
        when(configMock.levelUpMinLevel()).thenReturn(50);
        when(configMock.levelUpInterval()).thenReturn(5);

        LevelUpEventProcessor processor = new LevelUpEventProcessor(
                Collections.singletonList(notifierMock), configMock);

        ChatMessage msg = createChatMessage("Congratulations, you just advanced your Strength level.");
        processor.evaluateIncomingEvent(msg, "TestPlayer", "TestClan", null, 100);

        verify(notifierMock, times(1)).dispatchNotification(any());
    }

    @Test
    public void levelUp_featureDisabled_isSkipped() {
        when(configMock.notifyLevelUp()).thenReturn(false);

        LevelUpEventProcessor processor = new LevelUpEventProcessor(
                Collections.singletonList(notifierMock), configMock);

        ChatMessage msg = createChatMessage("Congratulations, you've reached the highest possible Strength level of 99.");
        processor.evaluateIncomingEvent(msg, "TestPlayer", "TestClan", null, 100);

        verify(notifierMock, never()).dispatchNotification(any());
    }

    // --- XP Milestone Filters ---

    @Test
    public void xpMilestone_fiveMillionInterval_crossing_isDispatched() {
        when(configMock.notifyXpMilestone()).thenReturn(true);
        when(configMock.xpMilestoneInterval()).thenReturn(XpMilestoneInterval.FIVE_MILLION);

        XpMilestoneEventProcessor processor = new XpMilestoneEventProcessor(
                Collections.singletonList(notifierMock), configMock);

        // Baseline XP past 99 (99 is 13,034,431)
        StatChanged baseline = new StatChanged(Skill.STRENGTH, 14_000_000, 99, 99);
        processor.evaluateStatChanged(baseline, "TestPlayer", "TestClan", null, 100);
        verify(notifierMock, never()).dispatchNotification(any());

        // XP increase within the same 5M block (< 15M)
        StatChanged subMilestone = new StatChanged(Skill.STRENGTH, 14_800_000, 99, 99);
        processor.evaluateStatChanged(subMilestone, "TestPlayer", "TestClan", null, 101);
        verify(notifierMock, never()).dispatchNotification(any());

        // Crossing 15M milestone
        StatChanged milestoneCrossed = new StatChanged(Skill.STRENGTH, 15_000_050, 99, 99);
        processor.evaluateStatChanged(milestoneCrossed, "TestPlayer", "TestClan", null, 102);
        verify(notifierMock, times(1)).dispatchNotification(any());

        // XP increase within 15M-20M block
        StatChanged nextSubMilestone = new StatChanged(Skill.STRENGTH, 19_999_999, 99, 99);
        processor.evaluateStatChanged(nextSubMilestone, "TestPlayer", "TestClan", null, 103);
        verify(notifierMock, times(1)).dispatchNotification(any());

        // Crossing 20M milestone
        StatChanged nextMilestoneCrossed = new StatChanged(Skill.STRENGTH, 20_000_000, 99, 99);
        processor.evaluateStatChanged(nextMilestoneCrossed, "TestPlayer", "TestClan", null, 104);
        verify(notifierMock, times(2)).dispatchNotification(any());
    }

    @Test
    public void xpMilestone_belowLevel99_isSkipped() {
        when(configMock.notifyXpMilestone()).thenReturn(true);
        when(configMock.xpMilestoneInterval()).thenReturn(XpMilestoneInterval.ONE_MILLION);

        XpMilestoneEventProcessor processor = new XpMilestoneEventProcessor(
                Collections.singletonList(notifierMock), configMock);

        StatChanged initial = new StatChanged(Skill.STRENGTH, 10_000_000, 75, 75);
        processor.evaluateStatChanged(initial, "TestPlayer", "TestClan", null, 100);

        StatChanged increase = new StatChanged(Skill.STRENGTH, 11_000_000, 78, 78);
        processor.evaluateStatChanged(increase, "TestPlayer", "TestClan", null, 101);

        verify(notifierMock, never()).dispatchNotification(any());
    }

    @Test
    public void xpMilestone_nullConfigInterval_defaultsToOneMillion() {
        when(configMock.notifyXpMilestone()).thenReturn(true);
        when(configMock.xpMilestoneInterval()).thenReturn(null);

        XpMilestoneEventProcessor processor = new XpMilestoneEventProcessor(
                Collections.singletonList(notifierMock), configMock);

        StatChanged baseline = new StatChanged(Skill.STRENGTH, 13_900_000, 99, 99);
        processor.evaluateStatChanged(baseline, "TestPlayer", "TestClan", null, 100);

        StatChanged milestone = new StatChanged(Skill.STRENGTH, 14_050_000, 99, 99);
        processor.evaluateStatChanged(milestone, "TestPlayer", "TestClan", null, 101);

        verify(notifierMock, times(1)).dispatchNotification(any());
    }

    // --- Combat Achievement Filters ---

    @Test
    public void combatAchievement_taskBelowMinTier_isSkipped() {
        when(configMock.notifyCombatAchievement()).thenReturn(true);
        when(configMock.combatAchievementMinTier()).thenReturn(CombatAchievementTier.HARD);

        CombatAchievementEventProcessor processor = new CombatAchievementEventProcessor(
                Collections.singletonList(notifierMock), configMock);

        ChatMessage msg = createChatMessage("Congratulations, you've completed an easy combat task: Defence? What Defence?");
        processor.evaluateIncomingEvent(msg, "TestPlayer", "TestClan", null, 100);

        verify(notifierMock, never()).dispatchNotification(any());
    }

    @Test
    public void combatAchievement_taskAtMinTier_isDispatched() {
        when(configMock.notifyCombatAchievement()).thenReturn(true);
        when(configMock.combatAchievementMinTier()).thenReturn(CombatAchievementTier.HARD);

        CombatAchievementEventProcessor processor = new CombatAchievementEventProcessor(
                Collections.singletonList(notifierMock), configMock);

        ChatMessage msg = createChatMessage("Congratulations, you've completed a hard combat task: Perfect Olm.");
        processor.evaluateIncomingEvent(msg, "TestPlayer", "TestClan", null, 100);

        verify(notifierMock, times(1)).dispatchNotification(any());
    }

    @Test
    public void combatAchievement_taskAboveMinTier_isDispatched() {
        when(configMock.notifyCombatAchievement()).thenReturn(true);
        when(configMock.combatAchievementMinTier()).thenReturn(CombatAchievementTier.HARD);

        CombatAchievementEventProcessor processor = new CombatAchievementEventProcessor(
                Collections.singletonList(notifierMock), configMock);

        ChatMessage msg = createChatMessage("Congratulations, you've completed a grandmaster combat task: Peach Picnic.");
        processor.evaluateIncomingEvent(msg, "TestPlayer", "TestClan", null, 100);

        verify(notifierMock, times(1)).dispatchNotification(any());
    }

    @Test
    public void combatAchievement_unknownTier_isDispatched() {
        when(configMock.notifyCombatAchievement()).thenReturn(true);
        when(configMock.combatAchievementMinTier()).thenReturn(CombatAchievementTier.HARD);

        CombatAchievementEventProcessor processor = new CombatAchievementEventProcessor(
                Collections.singletonList(notifierMock), configMock);

        ChatMessage msg = createChatMessage("Congratulations, you've completed a mythical combat task: Something Crazy.");
        processor.evaluateIncomingEvent(msg, "TestPlayer", "TestClan", null, 100);

        verify(notifierMock, times(1)).dispatchNotification(any());
    }

    @Test
    public void combatAchievement_nullMinTierConfig_defaultsToEasy() {
        when(configMock.notifyCombatAchievement()).thenReturn(true);
        when(configMock.combatAchievementMinTier()).thenReturn(null);

        CombatAchievementEventProcessor processor = new CombatAchievementEventProcessor(
                Collections.singletonList(notifierMock), configMock);

        ChatMessage msg = createChatMessage("Congratulations, you've completed an easy combat task: Defence? What Defence?");
        processor.evaluateIncomingEvent(msg, "TestPlayer", "TestClan", null, 100);

        verify(notifierMock, times(1)).dispatchNotification(any());
    }

    @Test
    public void combatAchievement_tierCompletion_whenEnabled_isDispatched() {
        when(configMock.notifyCombatAchievement()).thenReturn(true);
        when(configMock.notifyCombatAchievementTierCompletion()).thenReturn(true);

        CombatAchievementEventProcessor processor = new CombatAchievementEventProcessor(
                Collections.singletonList(notifierMock), configMock);

        ChatMessage msg = createChatMessage("You've completed enough Combat Achievement tasks to unlock the Hard Tier rewards! You can now claim your rewards from Ghommal.");
        processor.evaluateIncomingEvent(msg, "TestPlayer", "TestClan", null, 100);

        verify(notifierMock, times(1)).dispatchNotification(any());
    }

    @Test
    public void combatAchievement_tierCompletion_whenDisabled_isSkipped() {
        when(configMock.notifyCombatAchievement()).thenReturn(true);
        when(configMock.notifyCombatAchievementTierCompletion()).thenReturn(false);

        CombatAchievementEventProcessor processor = new CombatAchievementEventProcessor(
                Collections.singletonList(notifierMock), configMock);

        ChatMessage msg = createChatMessage("You've completed enough Combat Achievement tasks to unlock the Hard Tier rewards! You can now claim your rewards from Ghommal.");
        processor.evaluateIncomingEvent(msg, "TestPlayer", "TestClan", null, 100);

        verify(notifierMock, never()).dispatchNotification(any());
    }

    @Test
    public void combatAchievement_masterToggleDisabled_suppressesAll() {
        when(configMock.notifyCombatAchievement()).thenReturn(false);
        when(configMock.notifyCombatAchievementTierCompletion()).thenReturn(true);

        CombatAchievementEventProcessor processor = new CombatAchievementEventProcessor(
                Collections.singletonList(notifierMock), configMock);

        ChatMessage msg = createChatMessage("You've completed enough Combat Achievement tasks to unlock the Hard Tier rewards! You can now claim your rewards from Ghommal.");
        processor.evaluateIncomingEvent(msg, "TestPlayer", "TestClan", null, 100);

        verify(notifierMock, never()).dispatchNotification(any());
    }
}
