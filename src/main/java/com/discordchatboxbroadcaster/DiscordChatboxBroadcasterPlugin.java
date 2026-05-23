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
import net.runelite.api.GameState;
import net.runelite.api.IconID;
import net.runelite.api.IndexedSprite;
import net.runelite.api.Player;
import net.runelite.api.Varbits;
import net.runelite.api.WorldType;
import net.runelite.api.clan.ClanChannel;
import net.runelite.api.clan.ClanID;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import okhttp3.OkHttpClient;

import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
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

	private BufferedImage cachedPlayerIcon;
	private int cachedAccountType = -1;

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
				new KillEventProcessor(activeNotifiers, pluginConfiguration, gameClient),
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
		cachedPlayerIcon = null;
		cachedAccountType = -1;
	}

	private boolean shouldSkipEvent() {
		if (!pluginConfiguration.disableInTemporaryGameModes()) {
			return false;
		}

		EnumSet<WorldType> worldTypes = gameClient.getWorldType();
		if (worldTypes == null) {
			return false;
		}

		return worldTypes.contains(WorldType.SEASONAL) ||
				worldTypes.contains(WorldType.DEADMAN) ||
				worldTypes.contains(WorldType.TOURNAMENT_WORLD) ||
				worldTypes.contains(WorldType.NOSAVE_MODE) ||
				worldTypes.contains(WorldType.QUEST_SPEEDRUNNING) ||
				worldTypes.contains(WorldType.FRESH_START_WORLD);
	}

	private BufferedImage retrievePlayerIcon() {
		int activeAccountType = gameClient.getVarbitValue(Varbits.ACCOUNT_TYPE);

		if (activeAccountType == 0) {
			return null;
		}

		if (cachedPlayerIcon != null && activeAccountType == cachedAccountType) {
			return cachedPlayerIcon;
		}

		int targetIconIndex = -1;
		switch (activeAccountType) {
			case 1: targetIconIndex = IconID.IRONMAN.getIndex(); break;
			case 2: targetIconIndex = IconID.ULTIMATE_IRONMAN.getIndex(); break;
			case 3: targetIconIndex = IconID.HARDCORE_IRONMAN.getIndex(); break;
			case 4: targetIconIndex = IconID.GROUP_IRONMAN.getIndex(); break;
			case 5: targetIconIndex = IconID.HARDCORE_GROUP_IRONMAN.getIndex(); break;
			case 6: targetIconIndex = IconID.UNRANKED_GROUP_IRONMAN.getIndex(); break;
			default: return null;
		}

		IndexedSprite[] activeModIcons = gameClient.getModIcons();
		if (activeModIcons != null && targetIconIndex >= 0 && targetIconIndex < activeModIcons.length) {
			IndexedSprite targetSprite = activeModIcons[targetIconIndex];
			if (targetSprite != null) {
				cachedPlayerIcon = convertIndexedSpriteToBufferedImage(targetSprite);
				cachedAccountType = activeAccountType;
				return cachedPlayerIcon;
			}
		}

		return null;
	}

	private BufferedImage convertIndexedSpriteToBufferedImage(IndexedSprite sourceSprite) {
		int imageWidth = sourceSprite.getWidth();
		int imageHeight = sourceSprite.getHeight();

		if (imageWidth <= 0 || imageHeight <= 0) {
			return null;
		}

		int[] translatedPixels = new int[imageWidth * imageHeight];
		byte[] rawPixels = sourceSprite.getPixels();
		int[] colorPalette = sourceSprite.getPalette();

		for (int index = 0; index < rawPixels.length; index++) {
			int paletteIndex = rawPixels[index] & 0xFF;
			if (paletteIndex != 0 && paletteIndex < colorPalette.length) {
				translatedPixels[index] = colorPalette[paletteIndex] | 0xFF000000;
			} else {
				translatedPixels[index] = 0;
			}
		}

		BufferedImage convertedImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
		convertedImage.setRGB(0, 0, imageWidth, imageHeight, translatedPixels, 0, imageWidth);
		return convertedImage;
	}

	@Subscribe
	public void onChatMessage(ChatMessage incomingChatMessage) {
		if (shouldSkipEvent()) {
			return;
		}

		Player localPlayerEntity = gameClient.getLocalPlayer();

		if (localPlayerEntity == null) {
			return;
		}

		String activePlayerName = localPlayerEntity.getName();

		ClanChannel activeClanChannel = gameClient.getClanChannel(ClanID.CLAN);
		String activeClanName = (activeClanChannel != null) ? activeClanChannel.getName() : "";

		int currentClientTick = gameClient.getTickCount();
		BufferedImage activePlayerIcon = retrievePlayerIcon();

		for (GameEventProcessor targetedProcessor : activeEventProcessors) {
			targetedProcessor.evaluateIncomingEvent(incomingChatMessage, activePlayerName, activeClanName, activePlayerIcon, currentClientTick);
		}
	}

	@Subscribe
	public void onActorDeath(ActorDeath incomingDeathEvent) {
		if (shouldSkipEvent()) {
			return;
		}

		Player localPlayerEntity = gameClient.getLocalPlayer();

		if (localPlayerEntity == null || incomingDeathEvent.getActor() != localPlayerEntity) {
			return;
		}

		String activePlayerName = localPlayerEntity.getName();

		ClanChannel activeClanChannel = gameClient.getClanChannel(ClanID.CLAN);
		String activeClanName = (activeClanChannel != null) ? activeClanChannel.getName() : "";

		int currentClientTick = gameClient.getTickCount();
		BufferedImage activePlayerIcon = retrievePlayerIcon();

		for (GameEventProcessor targetedProcessor : activeEventProcessors) {
			targetedProcessor.evaluateActorDeath(incomingDeathEvent, activePlayerName, activeClanName, activePlayerIcon, currentClientTick);
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged incomingVarbitEvent) {
		if (shouldSkipEvent()) {
			return;
		}

		Player localPlayerEntity = gameClient.getLocalPlayer();

		if (localPlayerEntity == null) {
			return;
		}

		String activePlayerName = localPlayerEntity.getName();

		ClanChannel activeClanChannel = gameClient.getClanChannel(ClanID.CLAN);
		String activeClanName = (activeClanChannel != null) ? activeClanChannel.getName() : "";

		int currentClientTick = gameClient.getTickCount();
		BufferedImage activePlayerIcon = retrievePlayerIcon();

		for (GameEventProcessor targetedProcessor : activeEventProcessors) {
			targetedProcessor.evaluateVarbitChanged(incomingVarbitEvent.getVarbitId(), incomingVarbitEvent.getValue(), activePlayerName, activeClanName, activePlayerIcon, currentClientTick);
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged incomingGameStateEvent) {
		if (incomingGameStateEvent.getGameState() == GameState.LOGIN_SCREEN || incomingGameStateEvent.getGameState() == GameState.HOPPING) {
			cachedPlayerIcon = null;
			cachedAccountType = -1;
		}

		if (activeEventProcessors == null) {
			return;
		}

		for (GameEventProcessor targetedProcessor : activeEventProcessors) {
			targetedProcessor.evaluateGameStateChanged(incomingGameStateEvent.getGameState());
		}
	}
}