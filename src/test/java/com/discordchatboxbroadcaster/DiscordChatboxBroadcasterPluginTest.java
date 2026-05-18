package com.discordchatboxbroadcaster;

import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.events.ChatMessage;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
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

    @Before
    public void setUp() {
        mockitoSession = MockitoAnnotations.openMocks(this);

        Player localPlayerMock = mock(Player.class);
        when(localPlayerMock.getName()).thenReturn("TestPlayer");
        when(gameClientMock.getLocalPlayer()).thenReturn(localPlayerMock);

        when(pluginConfigurationMock.notifyPet()).thenReturn(true);
        when(pluginConfigurationMock.notifyCollectionLog()).thenReturn(true);
        when(pluginConfigurationMock.notifyValuableDrop()).thenReturn(true);
        when(pluginConfigurationMock.webhookUrl()).thenReturn("https://discord.com/api/webhooks/test");

        when(networkClientMock.newCall(any(Request.class))).thenReturn(networkCallMock);

        testPlugin = new DiscordChatboxBroadcasterPlugin();
        testPlugin.gameClient = gameClientMock;
        testPlugin.pluginConfiguration = pluginConfigurationMock;
        testPlugin.sharedNetworkClient = networkClientMock;

        testPlugin.startUp();
    }

    @After
    public void tearDown() throws Exception {
        if (mockitoSession != null) {
            mockitoSession.close();
        }
        testPlugin.shutDown();
    }

    @Test
    public void testPetEventSimulation() {
        ChatMessage simulatedPetMessage = new ChatMessage();
        simulatedPetMessage.setType(ChatMessageType.GAMEMESSAGE);
        simulatedPetMessage.setMessage("You feel something weird sneaking into your backpack.");

        testPlugin.onChatMessage(simulatedPetMessage);

        verify(networkClientMock, times(1)).newCall(any(Request.class));
    }

    @Test
    public void testCollectionLogEventSimulation() {
        ChatMessage simulatedCollectionLogMessage = new ChatMessage();
        simulatedCollectionLogMessage.setType(ChatMessageType.GAMEMESSAGE);
        simulatedCollectionLogMessage.setMessage("New item added to your collection log: Abyssal whip");

        testPlugin.onChatMessage(simulatedCollectionLogMessage);

        verify(networkClientMock, times(1)).newCall(any(Request.class));
    }

    @Test
    public void testValuableDropEventSimulation() {
        ChatMessage simulatedValuableDropMessage = new ChatMessage();
        simulatedValuableDropMessage.setType(ChatMessageType.GAMEMESSAGE);
        simulatedValuableDropMessage.setMessage("Valuable drop: 1 x Abyssal whip (1,500,000 coins)");

        testPlugin.onChatMessage(simulatedValuableDropMessage);

        verify(networkClientMock, times(1)).newCall(any(Request.class));
    }
}