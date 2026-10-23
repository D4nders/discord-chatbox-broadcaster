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
import com.discordchatboxbroadcaster.event.XpMilestoneEventProcessor;
import com.discordchatboxbroadcaster.notifier.DiscordWebhookNotifier;
import com.discordchatboxbroadcaster.notifier.Notifier;
import com.discordchatboxbroadcaster.ui.DiscordChatboxBroadcasterPanel;
import com.google.gson.Gson;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
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
import net.runelite.api.events.GameTick;
import net.runelite.api.events.StatChanged;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.DrawManager;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.ImageUtil;
import okhttp3.OkHttpClient;

import javax.inject.Inject;
import javax.swing.SwingUtilities;
import java.awt.Color;
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

	static final String CONFIG_GROUP = "discordchatboxbroadcaster";
	static final String COLLECTION_LOG_WARNING = "[Discord Broadcaster] Warning: Collection Log notifications require the in-game setting 'Collection log - New addition notification' to be enabled (Chat or Both).";
	static final String VALUABLE_DROP_WARNING = "[Discord Broadcaster] Warning: Valuable Drop notifications require the in-game setting 'Loot drop notifications' to be enabled.";

	@Inject
	Client gameClient;

	@Inject
	DiscordChatboxBroadcasterConfig pluginConfiguration;

	@Inject
	OkHttpClient sharedNetworkClient;

	@Inject
	ScheduledExecutorService scheduledExecutorService;

	@Inject
	ConfigManager configManager;

	@Inject
	ClientThread clientThread;

	@Inject
	ChatMessageManager chatMessageManager;

	@Inject
	Gson gson;

	@Inject
	DrawManager drawManager;

	@Inject
	ClientToolbar clientToolbar;

	private List<GameEventProcessor> activeEventProcessors;
	private List<Notifier> activeNotifiers;

	private BufferedImage cachedPlayerIcon;
	private int cachedAccountType = -1;

	private boolean pendingLoginSettingsCheck = false;
	private GameState lastGameState = null;

	private DiscordChatboxBroadcasterPanel panel;
	private NavigationButton navigationButton;

	public DiscordChatboxBroadcasterPanel getPanel() {
		return panel;
	}

	public NavigationButton getNavigationButton() {
		return navigationButton;
	}

	@Provides
	DiscordChatboxBroadcasterConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(DiscordChatboxBroadcasterConfig.class);
	}

	@Override
	protected void startUp() {
		SharedEventState sharedEventState = new SharedEventState();

		activeNotifiers = Collections.singletonList(
				new DiscordWebhookNotifier(pluginConfiguration, sharedNetworkClient, scheduledExecutorService, gson, drawManager)
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
				new AchievementDiaryEventProcessor(activeNotifiers, pluginConfiguration),
				new XpMilestoneEventProcessor(activeNotifiers, pluginConfiguration)
		);

		panel = new DiscordChatboxBroadcasterPanel(pluginConfiguration, configManager, gson);

		BufferedImage icon = ImageUtil.loadImageResource(getClass(), "panel_icon.png");
		if (icon == null) {
			icon = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		}

		navigationButton = NavigationButton.builder()
				.tooltip("Discord Chatbox Broadcaster")
				.icon(icon)
				.priority(5)
				.panel(panel)
				.build();

		if (clientToolbar != null && pluginConfiguration.showSidebarPanel()) {
			clientToolbar.addNavigation(navigationButton);
		}

		if (gameClient.getGameState() == GameState.LOGGED_IN) {
			pendingLoginSettingsCheck = true;
			updatePanelPlayerContext();
		}
	}

	@Override
	protected void shutDown() {
		if (clientToolbar != null && navigationButton != null) {
			clientToolbar.removeNavigation(navigationButton);
		}
		navigationButton = null;
		panel = null;

		if (activeNotifiers != null) {
			for (Notifier currentNotifier : activeNotifiers) {
				currentNotifier.terminate();
			}
		}
		activeNotifiers = null;
		activeEventProcessors = null;
		cachedPlayerIcon = null;
		cachedAccountType = -1;
		pendingLoginSettingsCheck = false;
		lastGameState = null;
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

		if (localPlayerEntity == null) {
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
	public void onGameStateChanged(GameStateChanged incomingGameStateEvent) {
		GameState newState = incomingGameStateEvent.getGameState();
		if (newState == GameState.LOGGED_IN && lastGameState != GameState.HOPPING && lastGameState != GameState.LOGGED_IN) {
			pendingLoginSettingsCheck = true;
			updatePanelPlayerContext();
		}
		if (newState != GameState.LOADING) {
			lastGameState = newState;
		}

		if (newState == GameState.LOGIN_SCREEN || newState == GameState.HOPPING) {
			cachedPlayerIcon = null;
			cachedAccountType = -1;
			if (panel != null) {
				SwingUtilities.invokeLater(() -> {
					if (panel != null) {
						panel.updatePlayerContext(null, null, null);
					}
				});
			}
		}

		if (activeEventProcessors == null) {
			return;
		}

		for (GameEventProcessor targetedProcessor : activeEventProcessors) {
			targetedProcessor.evaluateGameStateChanged(newState);
		}
	}

	@Subscribe
	public void onGameTick(GameTick incomingGameTickEvent) {
		if (pendingLoginSettingsCheck && gameClient.getGameState() == GameState.LOGGED_IN) {
			pendingLoginSettingsCheck = false;
			checkAndWarnGameSettings(pluginConfiguration.notifyCollectionLog(), pluginConfiguration.notifyValuableDrop());
			updatePanelPlayerContext();
		}

		if (activeEventProcessors == null) {
			return;
		}

		int currentClientTick = gameClient.getTickCount();

		for (GameEventProcessor targetedProcessor : activeEventProcessors) {
			targetedProcessor.evaluateGameTick(currentClientTick);
		}
	}

	@Subscribe
	public void onStatChanged(StatChanged incomingStatEvent) {
		if (shouldSkipEvent() || activeEventProcessors == null) {
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
			targetedProcessor.evaluateStatChanged(incomingStatEvent, activePlayerName, activeClanName, activePlayerIcon, currentClientTick);
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event) {
		if (!CONFIG_GROUP.equals(event.getGroup())) {
			return;
		}

		if ("showSidebarPanel".equals(event.getKey())) {
			SwingUtilities.invokeLater(() -> {
				if (clientToolbar == null || navigationButton == null) {
					return;
				}
				clientToolbar.removeNavigation(navigationButton);
				if (pluginConfiguration.showSidebarPanel()) {
					clientToolbar.addNavigation(navigationButton);
				}
			});
		}

		SwingUtilities.invokeLater(() -> {
			if (panel != null) {
				panel.updatePreview();
			}
		});

		if (!"true".equals(event.getNewValue())) {
			return;
		}
		boolean checkCollectionLog = "notifyCollectionLog".equals(event.getKey());
		boolean checkValuableDrop = "notifyValuableDrop".equals(event.getKey());
		if (checkCollectionLog || checkValuableDrop) {
			if (clientThread != null) {
				clientThread.invokeLater(() -> {
					if (gameClient != null && gameClient.getGameState() == GameState.LOGGED_IN) {
						checkAndWarnGameSettings(checkCollectionLog, checkValuableDrop);
					}
				});
			}
		}
	}

	private void checkAndWarnGameSettings(boolean checkCollectionLog, boolean checkValuableDrop) {
		if (checkCollectionLog && (gameClient.getVarbitValue(VarbitID.OPTION_COLLECTION_NEW_ITEM) & 1) == 0) {
			sendChatWarning(COLLECTION_LOG_WARNING);
		}
		if (checkValuableDrop && gameClient.getVarbitValue(VarbitID.OPTION_LOOTNOTIFICATION_ON) == 0) {
			sendChatWarning(VALUABLE_DROP_WARNING);
		}
	}

	private void sendChatWarning(String message) {
		if (chatMessageManager == null) {
			return;
		}
		String formatted = ColorUtil.wrapWithColorTag(message, Color.RED);
		chatMessageManager.queue(QueuedMessage.builder()
				.type(ChatMessageType.CONSOLE)
				.runeLiteFormattedMessage(formatted)
				.build());
	}

	private void updatePanelPlayerContext() {
		if (panel == null || gameClient == null) {
			return;
		}

		Runnable queryPlayerInfo = () -> {
			if (gameClient.getGameState() != GameState.LOGGED_IN) {
				return;
			}
			Player localPlayer = gameClient.getLocalPlayer();
			String playerName = (localPlayer != null) ? localPlayer.getName() : null;

			ClanChannel clanChannel = gameClient.getClanChannel(ClanID.CLAN);
			String clanName = (clanChannel != null) ? clanChannel.getName() : null;

			BufferedImage playerIcon = retrievePlayerIcon();

			SwingUtilities.invokeLater(() -> {
				if (panel != null) {
					panel.updatePlayerContext(clanName, playerName, playerIcon);
				}
			});
		};

		if (clientThread != null) {
			clientThread.invokeLater(queryPlayerInfo);
		} else {
			queryPlayerInfo.run();
		}
	}
}