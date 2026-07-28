package com.discordchatboxbroadcaster;

import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;

public class StartupConfigurationValidator {

    private static final String CONFIGURATION_GROUP_NAME = "discordchatboxbroadcaster";
    private static final String UNTRADEABLE_WARNING_KEY = "untradeableWarningShown";
    private static final String STARTUP_WARNING_MESSAGE = "Discord Broadcaster: Please enable 'Untradeable loot notifications' in your OSRS settings for pet names to broadcast correctly.";

    private final Client gameClient;
    private final ClientThread clientThread;
    private final ConfigManager configManager;

    public StartupConfigurationValidator(Client gameClient, ClientThread clientThread, ConfigManager configManager) {
        this.gameClient = gameClient;
        this.clientThread = clientThread;
        this.configManager = configManager;
    }

    public void evaluateFirstTimePetWarning() {
        Boolean hasUserSeenWarning = configManager.getConfiguration(CONFIGURATION_GROUP_NAME, UNTRADEABLE_WARNING_KEY, Boolean.class);

        if (hasUserSeenWarning == null || !hasUserSeenWarning) {
            clientThread.invokeLater(this::dispatchStartupWarningMessage);
            configManager.setConfiguration(CONFIGURATION_GROUP_NAME, UNTRADEABLE_WARNING_KEY, true);
        }
    }

    private void dispatchStartupWarningMessage() {
        gameClient.addChatMessage(ChatMessageType.GAMEMESSAGE, "", STARTUP_WARNING_MESSAGE, null);
    }
}