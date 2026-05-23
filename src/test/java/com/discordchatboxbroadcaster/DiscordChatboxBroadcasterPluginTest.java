package com.discordchatboxbroadcaster;

import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.ChatMessage;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
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
        when(gameClientMock.getNpcs()).thenReturn(Collections.emptyList());

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

        when(pluginConfigurationMock.webhookUrl()).thenReturn("https://discord.com/api/webhooks/test");

        when(networkClientMock.newCall(any(Request.class))).thenReturn(networkCallMock);

        testPlugin = new DiscordChatboxBroadcasterPlugin();
        testPlugin.gameClient = gameClientMock;
        testPlugin.pluginConfiguration = pluginConfigurationMock;
        testPlugin.sharedNetworkClient = networkClientMock;
        testPlugin.scheduledExecutorService = localExecutorService;

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
        ChatMessage simulatedMessage = new ChatMessage();
        simulatedMessage.setType(ChatMessageType.GAMEMESSAGE);
        simulatedMessage.setMessage("You feel something weird sneaking into your backpack.");
        testPlugin.onChatMessage(simulatedMessage);
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
        ChatMessage simulatedMessage = new ChatMessage();
        simulatedMessage.setType(ChatMessageType.GAMEMESSAGE);
        simulatedMessage.setMessage("Valuable drop: 1 x Abyssal whip (1,500,000 coins)");
        testPlugin.onChatMessage(simulatedMessage);
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
        ChatMessage simulatedMessage = new ChatMessage();
        simulatedMessage.setType(ChatMessageType.GAMEMESSAGE);
        simulatedMessage.setMessage("Fight Cave duration: 33:23 (new personal best).");
        testPlugin.onChatMessage(simulatedMessage);
        verify(networkClientMock, timeout(2000).times(1)).newCall(any(Request.class));
    }

    @Test
    public void testLevelUpEventSimulation() {
        ChatMessage simulatedMessage = new ChatMessage();
        simulatedMessage.setType(ChatMessageType.GAMEMESSAGE);
        simulatedMessage.setMessage("Congratulations, you've just advanced your Construction level. You are now level 81.");
        testPlugin.onChatMessage(simulatedMessage);
        verify(networkClientMock, timeout(2000).times(1)).newCall(any(Request.class));
    }

    @Test
    public void testKillEventSimulation() {
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
        simulatedMessage.setMessage("Congratulations, you've completed an Easy combat task: Defence? What Defence?");
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
    public void testValuableDropIgnoredWhenCollectionLogTriggers() {
        ChatMessage simulatedCollectionLogMessage = new ChatMessage();
        simulatedCollectionLogMessage.setType(ChatMessageType.GAMEMESSAGE);
        simulatedCollectionLogMessage.setMessage("New item added to your collection log: Abyssal whip");

        ChatMessage simulatedValuableDropMessage = new ChatMessage();
        simulatedValuableDropMessage.setType(ChatMessageType.GAMEMESSAGE);
        simulatedValuableDropMessage.setMessage("Valuable drop: 1 x Abyssal whip (1,500,000 coins)");

        testPlugin.onChatMessage(simulatedCollectionLogMessage);
        testPlugin.onChatMessage(simulatedValuableDropMessage);

        verify(networkClientMock, timeout(2000).times(1)).newCall(any(Request.class));
    }

    @Test
    public void testNpcKillIgnored() {
        NPC krukNpcMock = mock(NPC.class);
        when(krukNpcMock.getName()).thenReturn("Kruk");
        when(gameClientMock.getNpcs()).thenReturn(Collections.singletonList(krukNpcMock));

        ChatMessage simulatedMessage = new ChatMessage();
        simulatedMessage.setType(ChatMessageType.GAMEMESSAGE);
        simulatedMessage.setMessage("You have defeated Kruk.");
        testPlugin.onChatMessage(simulatedMessage);

        verify(networkClientMock, timeout(2000).times(0)).newCall(any(Request.class));
    }
}