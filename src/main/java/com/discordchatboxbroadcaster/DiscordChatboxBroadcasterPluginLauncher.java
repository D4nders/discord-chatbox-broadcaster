package com.discordchatboxbroadcaster;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class DiscordChatboxBroadcasterPluginLauncher {
    public static void main(String[] args) throws Exception {
        ExternalPluginManager.loadBuiltin(DiscordChatboxBroadcasterPlugin.class);
        RuneLite.main(args);
    }
}