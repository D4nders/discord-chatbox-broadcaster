package com.discordchatboxbroadcaster;

import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.WorldType;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.StatChanged;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.EnumSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DiscordChatboxBroadcasterPluginTest {

    @Mock
    private Client gameClientMock;

    @Mock
    private DiscordChatboxBroadcasterConfig pluginConfigurationMock;

    @Mock
    private OkHttpClient networkClientMock;

    @Mock
    private Call networkCallMock;

    @Mock
    private ChatMessageManager chatMessageManagerMock;

    @Mock
    private ClientThread clientThreadMock;

    private DiscordChatboxBroadcasterPlugin testPlugin;
    private AutoCloseable mockitoSession;
    private Player localPlayerMock;
    private ScheduledExecutorService localExecutorService;

    @Before
    public void setUp() {
        mockitoSession = MockitoAnnotations.openMocks(this);
        localExecutorService = Executors.newSingleThreadScheduledExecutor();

        localPlayerMock = mock(Player.class);
        when(localPlayerMock.getName()).thenReturn("TestPlayer");
        when(gameClientMock.getLocalPlayer()).thenReturn(localPlayerMock);
        when(gameClientMock.getVarbitValue(net.runelite.api.Varbits.ACCOUNT_TYPE)).thenReturn(0);
        when(gameClientMock.getWorldType()).thenReturn(EnumSet.noneOf(WorldType.class));

        when(pluginConfigurationMock.notifyPet()).thenReturn(true);
        when(pluginConfigurationMock.notifyCollectionLog()).thenReturn(true);
        when(pluginConfigurationMock.notifyValuableDrop()).thenReturn(true);
        when(pluginConfigurationMock.notifyQuest()).thenReturn(true);
        when(pluginConfigurationMock.notifyNewRecord()).thenReturn(true);
        when(pluginConfigurationMock.notifyLevelUp()).thenReturn(true);
        when(pluginConfigurationMock.notifyKill()).thenReturn(true);
        when(pluginConfigurationMock.notifyDeath()).thenReturn(true);
        when(pluginConfigurationMock.notifyCombatAchievement()).thenReturn(true);
        when(pluginConfigurationMock.notifyAchievementDiary()).thenReturn(true);
        when(pluginConfigurationMock.notifyXpMilestone()).thenReturn(true);
        when(pluginConfigurationMock.disableInTemporaryGameModes()).thenReturn(false);

        when(pluginConfigurationMock.webhookUrl()).thenReturn("https://discord.com/api/webhooks/test");

        when(networkClientMock.newCall(any(Request.class))).thenReturn(networkCallMock);

        testPlugin = new DiscordChatboxBroadcasterPlugin();
        testPlugin.gameClient = gameClientMock;
        testPlugin.pluginConfiguration = pluginConfigurationMock;
        testPlugin.sharedNetworkClient = networkClientMock;
        testPlugin.scheduledExecutorService = localExecutorService;
        testPlugin.chatMessageManager = chatMessageManagerMock;
        testPlugin.clientThread = clientThreadMock;

        testPlugin.startUp();
    }

    @After
    public void tearDown() throws Exception {
        if (mockitoSession != null) {
            mockitoSession.close();
        }
        testPlugin.shutDown();
        localExecutorService.shutdownNow();
    }

    @Test
    public void testPetEventSimulation() {
        ChatMessage petMessage = new ChatMessage();
        petMessage.setType(ChatMessageType.GAMEMESSAGE);
        petMessage.setMessage("You feel something weird sneaking into your backpack.");
        testPlugin.onChatMessage(petMessage);

        ChatMessage untradeableMessage = new ChatMessage();
        untradeableMessage.setType(ChatMessageType.GAMEMESSAGE);
        untradeableMessage.setMessage("Untradeable drop: Abyssal orphan");
        testPlugin.onChatMessage(untradeableMessage);

        verify(networkClientMock, timeout(2000).times(1)).newCall(any(Request.class));
    }

    @Test
    public void testCollectionLogEventSimulation() {
        ChatMessage simulatedMessage = new ChatMessage();
        simulatedMessage.setType(ChatMessageType.GAMEMESSAGE);
        simulatedMessage.setMessage("New item added to your collection log: Abyssal whip");
        testPlugin.onChatMessage(simulatedMessage);
        verify(networkClientMock, timeout(2000).times(1)).newCall(any(Request.class));
    }

    @Test
    public void testValuableDropEventSimulation() {
        // Set initial tick
        when(gameClientMock.getTickCount()).thenReturn(100);

        ChatMessage simulatedMessage = new ChatMessage();
        simulatedMessage.setType(ChatMessageType.GAMEMESSAGE);
        simulatedMessage.setMessage("Valuable drop: 1 x Abyssal whip (1,500,000 coins)");
        testPlugin.onChatMessage(simulatedMessage);

        // Advance the tick count by 2 and simulate a game tick to trigger the deferral evaluation
        when(gameClientMock.getTickCount()).thenReturn(102);
        testPlugin.onGameTick(new GameTick());

        verify(networkClientMock, timeout(2000).times(1)).newCall(any(Request.class));
    }

    @Test
    public void testValuableDropIgnoredWhenCollectionLogTriggers() {
        // Set initial tick
        when(gameClientMock.getTickCount()).thenReturn(100);

        ChatMessage simulatedCollectionLogMessage = new ChatMessage();
        simulatedCollectionLogMessage.setType(ChatMessageType.GAMEMESSAGE);
        simulatedCollectionLogMessage.setMessage("New item added to your collection log: Abyssal whip");

        ChatMessage simulatedValuableDropMessage = new ChatMessage();
        simulatedValuableDropMessage.setType(ChatMessageType.GAMEMESSAGE);
        simulatedValuableDropMessage.setMessage("Valuable drop: 1 x Abyssal whip (1,500,000 coins)");

        testPlugin.onChatMessage(simulatedCollectionLogMessage);
        testPlugin.onChatMessage(simulatedValuableDropMessage);

        // Advance the tick count by 2 and simulate a game tick to trigger the deferral evaluation
        when(gameClientMock.getTickCount()).thenReturn(102);
        testPlugin.onGameTick(new GameTick());

        // We still only expect 1 call (from the collection log). The valuable drop should be successfully suppressed.
        verify(networkClientMock, timeout(2000).times(1)).newCall(any(Request.class));
    }

    @Test
    public void testQuestEventSimulation() {
        ChatMessage simulatedMessage = new ChatMessage();
        simulatedMessage.setType(ChatMessageType.GAMEMESSAGE);
        simulatedMessage.setMessage("Congratulations, you've completed a quest: Cook's Assistant");
        testPlugin.onChatMessage(simulatedMessage);
        verify(networkClientMock, timeout(2000).times(1)).newCall(any(Request.class));
    }

    @Test
    public void testNewRecordEventSimulation() {
        when(gameClientMock.getTickCount()).thenReturn(100);

        ChatMessage kcMessage = new ChatMessage();
        kcMessage.setType(ChatMessageType.GAMEMESSAGE);
        kcMessage.setMessage("Your Zulrah kill count is: 42.");
        testPlugin.onChatMessage(kcMessage);

        ChatMessage simulatedMessage = new ChatMessage();
        simulatedMessage.setType(ChatMessageType.GAMEMESSAGE);
        simulatedMessage.setMessage("Fight duration: 1:15.40 (new personal best)");
        testPlugin.onChatMessage(simulatedMessage);

        verify(networkClientMock, timeout(2000).times(1)).newCall(any(Request.class));
    }

    @Test
    public void testStandardLevelEightyEventSimulation() {
        ChatMessage simulatedMessage = new ChatMessage();
        simulatedMessage.setType(ChatMessageType.GAMEMESSAGE);
        simulatedMessage.setMessage("Congratulations, you just advanced a Strength level. You are now level 80.");
        testPlugin.onChatMessage(simulatedMessage);
        verify(networkClientMock, timeout(2000).times(1)).newCall(any(Request.class));
    }

    @Test
    public void testMaxLevelNinetyNineEventSimulation() {
        ChatMessage simulatedMessage = new ChatMessage();
        simulatedMessage.setType(ChatMessageType.GAMEMESSAGE);
        simulatedMessage.setMessage("Congratulations, you've reached the highest possible Strength level of 99.");
        testPlugin.onChatMessage(simulatedMessage);
        verify(networkClientMock, timeout(2000).times(1)).newCall(any(Request.class));
    }

    @Test
    public void testKillEventSimulation() {
        when(gameClientMock.getTickCount()).thenReturn(100);

        Player deadPlayerMock = mock(Player.class);
        when(deadPlayerMock.getName()).thenReturn("Zezima");
        ActorDeath simulatedDeath = new ActorDeath(deadPlayerMock);
        testPlugin.onActorDeath(simulatedDeath);

        ChatMessage simulatedMessage = new ChatMessage();
        simulatedMessage.setType(ChatMessageType.GAMEMESSAGE);
        simulatedMessage.setMessage("You have defeated Zezima.");
        testPlugin.onChatMessage(simulatedMessage);
        
        verify(networkClientMock, timeout(2000).times(1)).newCall(any(Request.class));
    }

    @Test
    public void testDeathEventSimulation() {
        ActorDeath simulatedDeath = new ActorDeath(localPlayerMock);
        testPlugin.onActorDeath(simulatedDeath);
        verify(networkClientMock, timeout(2000).times(1)).newCall(any(Request.class));
    }

    @Test
    public void testCombatAchievementEventSimulation() {
        ChatMessage simulatedMessage = new ChatMessage();
        simulatedMessage.setType(ChatMessageType.GAMEMESSAGE);
        simulatedMessage.setMessage("Congratulations, you've completed an easy combat task: Defence? What Defence?");
        testPlugin.onChatMessage(simulatedMessage);
        verify(networkClientMock, timeout(2000).times(1)).newCall(any(Request.class));
    }

    @Test
    public void testAchievementDiaryEventSimulation() {
        ChatMessage simulatedMessage = new ChatMessage();
        simulatedMessage.setType(ChatMessageType.GAMEMESSAGE);
        simulatedMessage.setMessage("Congratulations! You have completed all of the easy tasks in the Lumbridge & Draynor area.");
        testPlugin.onChatMessage(simulatedMessage);

        verify(networkClientMock, timeout(2000).times(1)).newCall(any(Request.class));
    }

    @Test
    public void testMultipleCollectionLogsProcessConcurrently() {
        when(gameClientMock.getTickCount()).thenReturn(100);

        ChatMessage simulatedLogMessageOne = new ChatMessage();
        simulatedLogMessageOne.setType(ChatMessageType.GAMEMESSAGE);
        simulatedLogMessageOne.setMessage("New item added to your collection log: Guthix cloak");

        ChatMessage simulatedLogMessageTwo = new ChatMessage();
        simulatedLogMessageTwo.setType(ChatMessageType.GAMEMESSAGE);
        simulatedLogMessageTwo.setMessage("New item added to your collection log: Adamant shield (h5)");

        testPlugin.onChatMessage(simulatedLogMessageOne);
        testPlugin.onChatMessage(simulatedLogMessageTwo);

        verify(networkClientMock, timeout(2000).times(2)).newCall(any(Request.class));
    }

    @Test
    public void testNpcKillIgnored() {
        when(gameClientMock.getTickCount()).thenReturn(100);

        // Note: No preceding player ActorDeath event for "Kruk" is fired

        ChatMessage simulatedMessage = new ChatMessage();
        simulatedMessage.setType(ChatMessageType.GAMEMESSAGE);
        simulatedMessage.setMessage("You have defeated Kruk.");
        testPlugin.onChatMessage(simulatedMessage);

        verify(networkClientMock, timeout(2000).times(0)).newCall(any(Request.class));
    }

    @Test
    public void testTemporaryGameModeIsIgnored() {
        when(pluginConfigurationMock.disableInTemporaryGameModes()).thenReturn(true);
        when(gameClientMock.getWorldType()).thenReturn(EnumSet.of(WorldType.SEASONAL));

        ChatMessage simulatedMessage = new ChatMessage();
        simulatedMessage.setType(ChatMessageType.GAMEMESSAGE);
        simulatedMessage.setMessage("You feel something weird sneaking into your backpack.");
        testPlugin.onChatMessage(simulatedMessage);

        verify(networkClientMock, timeout(2000).times(0)).newCall(any(Request.class));
    }

    @Test
    public void testCombatAchievementTierCompletionSimulation() {
        ChatMessage simulatedMessage = new ChatMessage();
        simulatedMessage.setType(ChatMessageType.GAMEMESSAGE);
        simulatedMessage.setMessage("You've completed enough Combat Achievement tasks to unlock the Hard Tier rewards! You can now claim your rewards from Ghommal.");
        testPlugin.onChatMessage(simulatedMessage);

        verify(networkClientMock, timeout(2000).times(1)).newCall(any(Request.class));
    }

    @Test
    public void testToaCompletionTimeInlineRecordSimulation() {
        when(gameClientMock.getTickCount()).thenReturn(100);

        ChatMessage simulatedMessage = new ChatMessage();
        simulatedMessage.setType(ChatMessageType.GAMEMESSAGE);
        simulatedMessage.setMessage("Tombs of Amascut: Expert Mode total completion time: 20:31.00 (new personal best)");
        testPlugin.onChatMessage(simulatedMessage);

        verify(networkClientMock, timeout(2000).times(1)).newCall(any(Request.class));
    }

    @Test
    public void testToaChallengeCompletionTimeDeferredRecordSimulation() {
        when(gameClientMock.getTickCount()).thenReturn(100);

        ChatMessage challengeMessage = new ChatMessage();
        challengeMessage.setType(ChatMessageType.GAMEMESSAGE);
        challengeMessage.setMessage("Challenge completion time: 18:10 (new personal best)");
        testPlugin.onChatMessage(challengeMessage);

        ChatMessage kcMessage = new ChatMessage();
        kcMessage.setType(ChatMessageType.GAMEMESSAGE);
        kcMessage.setMessage("Your completed Tombs of Amascut: Expert Mode count is: 42.");
        testPlugin.onChatMessage(kcMessage);

        verify(networkClientMock, timeout(2000).times(1)).newCall(any(Request.class));
    }

    @Test
    public void testAchievementDiaryMesboxSimulation() {
        ChatMessage simulatedMessage = new ChatMessage();
        simulatedMessage.setType(ChatMessageType.MESBOX);
        simulatedMessage.setMessage("Congratulations! You have completed all of the hard<br>achievements in the Varrock set.");
        testPlugin.onChatMessage(simulatedMessage);

        verify(networkClientMock, timeout(2000).times(1)).newCall(any(Request.class));
    }

    @Test
    public void testWarningOnLoginWhenSettingsDisabled() {
        when(gameClientMock.getGameState()).thenReturn(GameState.LOGGED_IN);
        when(gameClientMock.getVarbitValue(VarbitID.OPTION_COLLECTION_NEW_ITEM)).thenReturn(0);
        when(gameClientMock.getVarbitValue(VarbitID.OPTION_LOOTNOTIFICATION_ON)).thenReturn(0);

        GameStateChanged loginScreenEvent = new GameStateChanged();
        loginScreenEvent.setGameState(GameState.LOGIN_SCREEN);
        testPlugin.onGameStateChanged(loginScreenEvent);

        GameStateChanged loggedInEvent = new GameStateChanged();
        loggedInEvent.setGameState(GameState.LOGGED_IN);
        testPlugin.onGameStateChanged(loggedInEvent);

        testPlugin.onGameTick(new GameTick());

        verify(chatMessageManagerMock, timeout(2000).times(2)).queue(any(QueuedMessage.class));
    }

    @Test
    public void testXpMilestoneEventSimulation() {
        when(gameClientMock.getTickCount()).thenReturn(100);

        StatChanged baselineStatChanged = new StatChanged(Skill.STRENGTH, 13_999_900, 99, 99);
        testPlugin.onStatChanged(baselineStatChanged);

        verify(networkClientMock, timeout(2000).times(0)).newCall(any(Request.class));

        StatChanged milestoneStatChanged = new StatChanged(Skill.STRENGTH, 14_000_050, 99, 99);
        testPlugin.onStatChanged(milestoneStatChanged);

        verify(networkClientMock, timeout(2000).times(1)).newCall(any(Request.class));
    }
}