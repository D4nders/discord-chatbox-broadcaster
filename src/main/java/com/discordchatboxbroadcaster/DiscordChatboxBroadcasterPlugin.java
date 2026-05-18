package com.discordchatboxbroadcaster;

import com.discordchatboxbroadcaster.event.AchievementDiaryEventProcessor;
import com.discordchatboxbroadcaster.event.CollectionLogEventProcessor;
import com.discordchatboxbroadcaster.event.CombatAchievementEventProcessor;
import com.discordchatboxbroadcaster.event.DeathEventProcessor;
import com.discordchatboxbroadcaster.event.GameEventProcessor;
import com.discordchatboxbroadcaster.event.KillEventProcessor;
import com.discordchatboxbroadcaster.event.LevelUpEventProcessor;
import com.discordchatboxbroadcaster.event.NewRecordEventProcessor;
import com.discordchatboxbroadcaster.event.PetEventProcessor;
import com.discordchatboxbroadcaster.event.QuestEventProcessor;
import com.discordchatboxbroadcaster.event.ValuableDropEventProcessor;
import com.discordchatboxbroadcaster.notifier.DiscordWebhookNotifier;
import com.discordchatboxbroadcaster.notifier.Notifier;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.clan.ClanChannel;
import net.runelite.api.clan.ClanID;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import okhttp3.OkHttpClient;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

@Slf4j
@PluginDescriptor(
		name = "Discord Chatbox Broadcaster"
)
public class DiscordChatboxBroadcasterPlugin extends Plugin {

	@Inject
	Client gameClient;

	@Inject
	DiscordChatboxBroadcasterConfig pluginConfiguration;

	@Inject
	OkHttpClient sharedNetworkClient;

	@Inject
	ScheduledExecutorService scheduledExecutorService;

	private List<GameEventProcessor> activeEventProcessors;
	private List<Notifier> activeNotifiers;

	@Provides
	DiscordChatboxBroadcasterConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(DiscordChatboxBroadcasterConfig.class);
	}

	@Override
	protected void startUp() {
		SharedEventState sharedEventState = new SharedEventState();

		activeNotifiers = Collections.singletonList(
				new DiscordWebhookNotifier(pluginConfiguration, sharedNetworkClient, scheduledExecutorService)
		);

		for (Notifier currentNotifier : activeNotifiers) {
			currentNotifier.initialize();
		}

		activeEventProcessors = Arrays.asList(
				new PetEventProcessor(activeNotifiers, pluginConfiguration, sharedEventState),
				new CollectionLogEventProcessor(activeNotifiers, pluginConfiguration, sharedEventState),
				new ValuableDropEventProcessor(activeNotifiers, pluginConfiguration, sharedEventState),
				new QuestEventProcessor(activeNotifiers, pluginConfiguration),
				new NewRecordEventProcessor(activeNotifiers, pluginConfiguration),
				new LevelUpEventProcessor(activeNotifiers, pluginConfiguration),
				new KillEventProcessor(activeNotifiers, pluginConfiguration),
				new DeathEventProcessor(activeNotifiers, pluginConfiguration),
				new CombatAchievementEventProcessor(activeNotifiers, pluginConfiguration),
				new AchievementDiaryEventProcessor(activeNotifiers, pluginConfiguration)
		);
	}

	@Override
	protected void shutDown() {
		if (activeNotifiers != null) {
			for (Notifier currentNotifier : activeNotifiers) {
				currentNotifier.terminate();
			}
		}
		activeNotifiers = null;
		activeEventProcessors = null;
	}

	@Subscribe
	public void onChatMessage(ChatMessage incomingChatMessage) {
		Player localPlayerEntity = gameClient.getLocalPlayer();

		if (localPlayerEntity == null) {
			return;
		}

		String activePlayerName = localPlayerEntity.getName();

		ClanChannel activeClanChannel = gameClient.getClanChannel(ClanID.CLAN);
		String activeClanName = (activeClanChannel != null) ? activeClanChannel.getName() : "";

		int currentClientTick = gameClient.getTickCount();

		for (GameEventProcessor targetedProcessor : activeEventProcessors) {
			targetedProcessor.evaluateIncomingEvent(incomingChatMessage, activePlayerName, activeClanName, currentClientTick);
		}
	}

	@Subscribe
	public void onActorDeath(ActorDeath incomingDeathEvent) {
		Player localPlayerEntity = gameClient.getLocalPlayer();

		if (localPlayerEntity == null || incomingDeathEvent.getActor() != localPlayerEntity) {
			return;
		}

		String activePlayerName = localPlayerEntity.getName();

		ClanChannel activeClanChannel = gameClient.getClanChannel(ClanID.CLAN);
		String activeClanName = (activeClanChannel != null) ? activeClanChannel.getName() : "";

		int currentClientTick = gameClient.getTickCount();

		for (GameEventProcessor targetedProcessor : activeEventProcessors) {
			targetedProcessor.evaluateActorDeath(incomingDeathEvent, activePlayerName, activeClanName, currentClientTick);
		}
	}
}