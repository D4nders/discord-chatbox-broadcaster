package com.discordchatboxbroadcaster;

import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.WorldType;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.CommandExecuted;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.StatChanged;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.events.ConfigChanged;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import com.google.gson.Gson;
import com.discordchatboxbroadcaster.config.ConfigShareCodeManager;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.DrawManager;
import net.runelite.client.ui.NavigationButton;
import okio.Buffer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.EnumSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;
import com.discordchatboxbroadcaster.config.BackgroundStyle;
import com.discordchatboxbroadcaster.config.CombatAchievementTier;
import com.discordchatboxbroadcaster.config.ImageScale;
import com.discordchatboxbroadcaster.config.XpMilestoneInterval;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.discordchatboxbroadcaster.event.NewRecordEventProcessor;
import com.discordchatboxbroadcaster.notifier.Notifier;
import com.discordchatboxbroadcaster.render.ChatSegment;
import java.util.Collections;
import java.util.List;

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

    @Mock
    private DrawManager drawManagerMock;

    @Mock
    private ConfigManager configManagerMock;

    @Mock
    private ClientToolbar clientToolbarMock;

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
        when(pluginConfigurationMock.webhookUsername()).thenReturn("");
        when(pluginConfigurationMock.webhookAvatarUrl()).thenReturn("");
        when(pluginConfigurationMock.attachScreenshot()).thenReturn(false);

        when(pluginConfigurationMock.showClanName()).thenReturn(true);
        when(pluginConfigurationMock.showAccountIcon()).thenReturn(true);
        when(pluginConfigurationMock.backgroundStyle()).thenReturn(BackgroundStyle.CLASSIC);
        when(pluginConfigurationMock.highlightColor()).thenReturn(new Color(239, 16, 32));
        when(pluginConfigurationMock.clanNameColor()).thenReturn(Color.BLUE);
        when(pluginConfigurationMock.imageScale()).thenReturn(ImageScale.X1);

        when(pluginConfigurationMock.valuableDropMinValue()).thenReturn(0);

        when(pluginConfigurationMock.levelUpMinLevel()).thenReturn(1);
        when(pluginConfigurationMock.levelUpInterval()).thenReturn(1);

        when(pluginConfigurationMock.xpMilestoneInterval()).thenReturn(XpMilestoneInterval.ONE_MILLION);

        when(pluginConfigurationMock.combatAchievementMinTier()).thenReturn(CombatAchievementTier.EASY);
        when(pluginConfigurationMock.notifyCombatAchievementTierCompletion()).thenReturn(true);

        when(pluginConfigurationMock.showSidebarPanel()).thenReturn(true);

        org.mockito.Mockito.doAnswer(invocation -> {
            ((Runnable) invocation.getArgument(0)).run();
            return null;
        }).when(clientThreadMock).invokeLater(any(Runnable.class));

        when(networkClientMock.newCall(any(Request.class))).thenReturn(networkCallMock);

        testPlugin = new DiscordChatboxBroadcasterPlugin();
        testPlugin.gameClient = gameClientMock;
        testPlugin.pluginConfiguration = pluginConfigurationMock;
        testPlugin.sharedNetworkClient = networkClientMock;
        testPlugin.scheduledExecutorService = localExecutorService;
        testPlugin.chatMessageManager = chatMessageManagerMock;
        testPlugin.clientThread = clientThreadMock;
        testPlugin.drawManager = drawManagerMock;
        testPlugin.configManager = configManagerMock;
        testPlugin.clientToolbar = clientToolbarMock;
        testPlugin.gson = new Gson();

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

        ChatMessage wardenMessage = new ChatMessage();
        wardenMessage.setType(ChatMessageType.GAMEMESSAGE);
        wardenMessage.setMessage("Challenge complete: The Wardens. Duration: 7:20<br>Tombs of Amascut: challenge completion time: 22:19 (new personal best)");
        testPlugin.onChatMessage(wardenMessage);

        ChatMessage challengeMessage = new ChatMessage();
        challengeMessage.setType(ChatMessageType.GAMEMESSAGE);
        challengeMessage.setMessage("Challenge completion time: 18:10 (new personal best)");
        testPlugin.onChatMessage(challengeMessage);

        ChatMessage kcMessage = new ChatMessage();
        kcMessage.setType(ChatMessageType.GAMEMESSAGE);
        kcMessage.setMessage("Your completed Tombs of Amascut: Expert Mode count is: 42.");
        testPlugin.onChatMessage(kcMessage);

        when(gameClientMock.getTickCount()).thenReturn(103);
        testPlugin.onGameTick(new GameTick());

        verify(networkClientMock, timeout(1000).times(0)).newCall(any(Request.class));
    }

    @Test
    public void testToaTotalCompletionTimeWithoutColonSimulation() {
        when(gameClientMock.getTickCount()).thenReturn(100);

        ChatMessage simulatedMessage = new ChatMessage();
        simulatedMessage.setType(ChatMessageType.GAMEMESSAGE);
        simulatedMessage.setMessage("Tombs of Amascut total completion time: 28:54 (new personal best)");
        testPlugin.onChatMessage(simulatedMessage);

        verify(networkClientMock, timeout(2000).times(1)).newCall(any(Request.class));
    }

    @Test
    public void testToaTotalCompletionTimeWithColonSimulation() {
        when(gameClientMock.getTickCount()).thenReturn(100);

        ChatMessage simulatedMessage = new ChatMessage();
        simulatedMessage.setType(ChatMessageType.GAMEMESSAGE);
        simulatedMessage.setMessage("Tombs of Amascut: total completion time: 28:54 (new personal best)");
        testPlugin.onChatMessage(simulatedMessage);

        verify(networkClientMock, timeout(2000).times(1)).newCall(any(Request.class));
    }

    @Test
    public void testToaTotalCompletionTimeDeferredKcSimulation() {
        when(gameClientMock.getTickCount()).thenReturn(100);

        ChatMessage simulatedMessage = new ChatMessage();
        simulatedMessage.setType(ChatMessageType.GAMEMESSAGE);
        simulatedMessage.setMessage("Total completion time: 28:54 (new personal best)");
        testPlugin.onChatMessage(simulatedMessage);

        ChatMessage kcMessage = new ChatMessage();
        kcMessage.setType(ChatMessageType.GAMEMESSAGE);
        kcMessage.setMessage("Your completed Tombs of Amascut: Expert Mode count is: 42.");
        testPlugin.onChatMessage(kcMessage);

        verify(networkClientMock, timeout(2000).times(1)).newCall(any(Request.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testToaTotalCompletionTimeCleanRaidNameVerification() {
        Notifier mockNotifier = mock(Notifier.class);
        NewRecordEventProcessor processor = new NewRecordEventProcessor(
            Collections.singletonList(mockNotifier),
            pluginConfigurationMock
        );

        ChatMessage messageWithColon = new ChatMessage();
        messageWithColon.setType(ChatMessageType.GAMEMESSAGE);
        messageWithColon.setMessage("Tombs of Amascut: total completion time: 28:54 (new personal best)");

        processor.evaluateIncomingEvent(messageWithColon, "TestPlayer", "TestClan", null, 100);

        ArgumentCaptor<List<ChatSegment>> captor = ArgumentCaptor.forClass(List.class);
        verify(mockNotifier, times(1)).dispatchNotification(captor.capture());

        List<ChatSegment> segments = captor.getValue();
        boolean hasCleanBossName = segments.stream()
            .anyMatch(s -> "Tombs of Amascut".equals(s.retrieveTextContent()));
        boolean hasTrailingColon = segments.stream()
            .anyMatch(s -> "Tombs of Amascut:".equals(s.retrieveTextContent()));

        assertTrue(hasCleanBossName);
        assertFalse(hasTrailingColon);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testToaSeparatePrefixCleanRaidNameVerification() {
        Notifier mockNotifier = mock(Notifier.class);
        NewRecordEventProcessor processor = new NewRecordEventProcessor(
            Collections.singletonList(mockNotifier),
            pluginConfigurationMock
        );

        ChatMessage prefixMessage = new ChatMessage();
        prefixMessage.setType(ChatMessageType.GAMEMESSAGE);
        prefixMessage.setMessage("Tombs of Amascut: total completion time: 28:54");

        ChatMessage recordMessage = new ChatMessage();
        recordMessage.setType(ChatMessageType.GAMEMESSAGE);
        recordMessage.setMessage("Total completion time: 28:54 (new personal best)");

        processor.evaluateIncomingEvent(prefixMessage, "TestPlayer", "TestClan", null, 100);
        processor.evaluateIncomingEvent(recordMessage, "TestPlayer", "TestClan", null, 100);

        ArgumentCaptor<List<ChatSegment>> captor = ArgumentCaptor.forClass(List.class);
        verify(mockNotifier, times(1)).dispatchNotification(captor.capture());

        List<ChatSegment> segments = captor.getValue();
        boolean hasCleanBossName = segments.stream()
            .anyMatch(s -> "Tombs of Amascut".equals(s.retrieveTextContent()));
        boolean hasTrailingColon = segments.stream()
            .anyMatch(s -> "Tombs of Amascut:".equals(s.retrieveTextContent()));

        assertTrue(hasCleanBossName);
        assertFalse(hasTrailingColon);
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

    @Test
    public void testPayloadJsonAbsentWhenBlank() throws Exception {
        when(pluginConfigurationMock.webhookUsername()).thenReturn("");
        when(pluginConfigurationMock.webhookAvatarUrl()).thenReturn("");

        ChatMessage simulatedMessage = new ChatMessage();
        simulatedMessage.setType(ChatMessageType.GAMEMESSAGE);
        simulatedMessage.setMessage("Congratulations, you've completed a quest: Cook's Assistant");
        testPlugin.onChatMessage(simulatedMessage);

        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(networkClientMock, timeout(2000).times(1)).newCall(requestCaptor.capture());

        Buffer buffer = new Buffer();
        requestCaptor.getValue().body().writeTo(buffer);
        String bodyContent = buffer.readUtf8();

        assertFalse(bodyContent.contains("payload_json"));
        assertTrue(bodyContent.contains("files[0]"));
        assertTrue(bodyContent.contains("notification.png"));
        assertFalse(bodyContent.contains("files[1]"));
    }

    @Test
    public void testPayloadJsonPresentWhenUsernameAndAvatarSet() throws Exception {
        when(pluginConfigurationMock.webhookUsername()).thenReturn("NotificationBot");
        when(pluginConfigurationMock.webhookAvatarUrl()).thenReturn("https://example.com/avatar.png");

        ChatMessage simulatedMessage = new ChatMessage();
        simulatedMessage.setType(ChatMessageType.GAMEMESSAGE);
        simulatedMessage.setMessage("Congratulations, you've completed a quest: Cook's Assistant");
        testPlugin.onChatMessage(simulatedMessage);

        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(networkClientMock, timeout(2000).times(1)).newCall(requestCaptor.capture());

        Buffer buffer = new Buffer();
        requestCaptor.getValue().body().writeTo(buffer);
        String bodyContent = buffer.readUtf8();

        assertTrue(bodyContent.contains("payload_json"));
        assertTrue(bodyContent.contains("NotificationBot"));
        assertTrue(bodyContent.contains("https://example.com/avatar.png"));
        assertTrue(bodyContent.contains("files[0]"));
    }

    @Test
    public void testPayloadJsonTruncatesUsernameTo80Chars() throws Exception {
        String longUsername = "A".repeat(100);
        when(pluginConfigurationMock.webhookUsername()).thenReturn(longUsername);
        when(pluginConfigurationMock.webhookAvatarUrl()).thenReturn("");

        ChatMessage simulatedMessage = new ChatMessage();
        simulatedMessage.setType(ChatMessageType.GAMEMESSAGE);
        simulatedMessage.setMessage("Congratulations, you've completed a quest: Cook's Assistant");
        testPlugin.onChatMessage(simulatedMessage);

        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(networkClientMock, timeout(2000).times(1)).newCall(requestCaptor.capture());

        Buffer buffer = new Buffer();
        requestCaptor.getValue().body().writeTo(buffer);
        String bodyContent = buffer.readUtf8();

        assertTrue(bodyContent.contains("payload_json"));
        assertTrue(bodyContent.contains("A".repeat(80)));
        assertFalse(bodyContent.contains("A".repeat(81)));
        assertFalse(bodyContent.contains("avatar_url"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAttachScreenshotCapturesFrameAndAttachesFiles1() throws Exception {
        when(pluginConfigurationMock.attachScreenshot()).thenReturn(true);

        ChatMessage simulatedMessage = new ChatMessage();
        simulatedMessage.setType(ChatMessageType.GAMEMESSAGE);
        simulatedMessage.setMessage("Congratulations, you've completed a quest: Cook's Assistant");
        testPlugin.onChatMessage(simulatedMessage);

        ArgumentCaptor<Consumer<Image>> frameListenerCaptor = ArgumentCaptor.forClass(Consumer.class);
        verify(drawManagerMock, timeout(2000).times(1)).requestNextFrameListener(frameListenerCaptor.capture());

        BufferedImage testFrame = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        frameListenerCaptor.getValue().accept(testFrame);

        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(networkClientMock, timeout(2000).times(1)).newCall(requestCaptor.capture());

        Buffer buffer = new Buffer();
        requestCaptor.getValue().body().writeTo(buffer);
        String bodyContent = buffer.readUtf8();

        assertTrue(bodyContent.contains("files[0]"));
        assertTrue(bodyContent.contains("notification.png"));
        assertTrue(bodyContent.contains("files[1]"));
        assertTrue(bodyContent.contains("screenshot.png"));
    }

    @Test
    public void testSidebarNavigationAddedOnStartupAndRemovedOnShutdown() {
        verify(clientToolbarMock).addNavigation(any(NavigationButton.class));
        assertNotNull(testPlugin.getPanel());
        assertNotNull(testPlugin.getNavigationButton());

        testPlugin.shutDown();
        verify(clientToolbarMock).removeNavigation(any(NavigationButton.class));
        assertNull(testPlugin.getPanel());
        assertNull(testPlugin.getNavigationButton());
    }

    @Test
    public void testSidebarNavigationToggledOnConfigChanged() {
        NavigationButton navButton = testPlugin.getNavigationButton();
        assertNotNull(navButton);

        // Toggle to false
        when(pluginConfigurationMock.showSidebarPanel()).thenReturn(false);
        ConfigChanged disableEvent = new ConfigChanged();
        disableEvent.setGroup(DiscordChatboxBroadcasterPlugin.CONFIG_GROUP);
        disableEvent.setKey("showSidebarPanel");
        disableEvent.setNewValue("false");
        testPlugin.onConfigChanged(disableEvent);

        verify(clientToolbarMock, timeout(1000).atLeastOnce()).removeNavigation(navButton);

        // Toggle back to true
        when(pluginConfigurationMock.showSidebarPanel()).thenReturn(true);
        ConfigChanged enableEvent = new ConfigChanged();
        enableEvent.setGroup(DiscordChatboxBroadcasterPlugin.CONFIG_GROUP);
        enableEvent.setKey("showSidebarPanel");
        enableEvent.setNewValue("true");
        testPlugin.onConfigChanged(enableEvent);

        verify(clientToolbarMock, timeout(1000).atLeast(2)).addNavigation(navButton);
    }
}