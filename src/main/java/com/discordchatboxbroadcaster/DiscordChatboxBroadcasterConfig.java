package com.discordchatboxbroadcaster;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("discordchatboxbroadcaster")
public interface DiscordChatboxBroadcasterConfig extends Config {

	@ConfigItem(keyName = "webhookUrl", name = "Discord Webhook URL", description = "", position = 1)
	default String webhookUrl() { return ""; }

	@ConfigItem(keyName = "notifyPet", name = "Notify Pet (Funny Feeling)", description = "", position = 2)
	default boolean notifyPet() { return true; }

	@ConfigItem(keyName = "notifyCollectionLog", name = "Notify Collection Log", description = "", position = 3)
	default boolean notifyCollectionLog() { return true; }

	@ConfigItem(keyName = "notifyValuableDrop", name = "Notify Valuable Drop", description = "", position = 4)
	default boolean notifyValuableDrop() { return true; }
}