package com.discordchatboxbroadcaster;

import com.discordchatboxbroadcaster.config.BackgroundStyle;
import com.discordchatboxbroadcaster.config.CombatAchievementTier;
import com.discordchatboxbroadcaster.config.ConfigShareCodeManager;
import com.discordchatboxbroadcaster.config.ImageScale;
import com.discordchatboxbroadcaster.config.XpMilestoneInterval;
import com.discordchatboxbroadcaster.ui.DiscordChatboxBroadcasterPanel;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.runelite.client.config.ConfigManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ConfigShareCodeTest {

	@Mock
	private DiscordChatboxBroadcasterConfig configMock;

	@Mock
	private ConfigManager configManagerMock;

	private Gson gson;

	@Before
	public void setUp() {
		MockitoAnnotations.openMocks(this);
		gson = new Gson();
	}

	@Test
	public void testRoundTripExportAndImportWithEvents() {
		when(configMock.showClanName()).thenReturn(true);
		when(configMock.showAccountIcon()).thenReturn(false);
		when(configMock.backgroundStyle()).thenReturn(BackgroundStyle.TRANSPARENT);
		when(configMock.highlightColor()).thenReturn(new Color(10, 20, 30));
		when(configMock.clanNameColor()).thenReturn(new Color(40, 50, 60));
		when(configMock.imageScale()).thenReturn(ImageScale.X2);

		when(configMock.disableInTemporaryGameModes()).thenReturn(true);
		when(configMock.notifyPet()).thenReturn(true);
		when(configMock.notifyCollectionLog()).thenReturn(false);
		when(configMock.notifyValuableDrop()).thenReturn(true);
		when(configMock.valuableDropMinValue()).thenReturn(100_000);
		when(configMock.notifyQuest()).thenReturn(true);
		when(configMock.notifyNewRecord()).thenReturn(false);
		when(configMock.notifyLevelUp()).thenReturn(true);
		when(configMock.levelUpMinLevel()).thenReturn(50);
		when(configMock.levelUpInterval()).thenReturn(5);
		when(configMock.notifyXpMilestone()).thenReturn(true);
		when(configMock.xpMilestoneInterval()).thenReturn(XpMilestoneInterval.FIVE_MILLION);
		when(configMock.notifyCombatAchievement()).thenReturn(true);
		when(configMock.combatAchievementMinTier()).thenReturn(CombatAchievementTier.HARD);
		when(configMock.notifyCombatAchievementTierCompletion()).thenReturn(false);
		when(configMock.notifyAchievementDiary()).thenReturn(true);
		when(configMock.notifyKill()).thenReturn(false);
		when(configMock.notifyDeath()).thenReturn(true);

		String shareCode = ConfigShareCodeManager.generateShareCode(configMock, gson);
		assertTrue(shareCode.startsWith(ConfigShareCodeManager.PREFIX));

		boolean applied = ConfigShareCodeManager.applyShareCode(shareCode, configManagerMock, gson, true);
		assertTrue(applied);

		// Verify Appearance settings applied
		verify(configManagerMock).setConfiguration(ConfigShareCodeManager.CONFIG_GROUP, "showClanName", true);
		verify(configManagerMock).setConfiguration(ConfigShareCodeManager.CONFIG_GROUP, "showAccountIcon", false);
		verify(configManagerMock).setConfiguration(ConfigShareCodeManager.CONFIG_GROUP, "backgroundStyle", BackgroundStyle.TRANSPARENT);
		verify(configManagerMock).setConfiguration(ConfigShareCodeManager.CONFIG_GROUP, "highlightColor", new Color(10, 20, 30));
		verify(configManagerMock).setConfiguration(ConfigShareCodeManager.CONFIG_GROUP, "clanNameColor", new Color(40, 50, 60));
		verify(configManagerMock).setConfiguration(ConfigShareCodeManager.CONFIG_GROUP, "imageScale", ImageScale.X2);

		// Verify Event settings applied
		verify(configManagerMock).setConfiguration(ConfigShareCodeManager.CONFIG_GROUP, "disableInTemporaryGameModes", true);
		verify(configManagerMock).setConfiguration(ConfigShareCodeManager.CONFIG_GROUP, "notifyPet", true);
		verify(configManagerMock).setConfiguration(ConfigShareCodeManager.CONFIG_GROUP, "notifyCollectionLog", false);
		verify(configManagerMock).setConfiguration(ConfigShareCodeManager.CONFIG_GROUP, "notifyValuableDrop", true);
		verify(configManagerMock).setConfiguration(ConfigShareCodeManager.CONFIG_GROUP, "valuableDropMinValue", 100_000);
		verify(configManagerMock).setConfiguration(ConfigShareCodeManager.CONFIG_GROUP, "notifyQuest", true);
		verify(configManagerMock).setConfiguration(ConfigShareCodeManager.CONFIG_GROUP, "notifyNewRecord", false);
		verify(configManagerMock).setConfiguration(ConfigShareCodeManager.CONFIG_GROUP, "notifyLevelUp", true);
		verify(configManagerMock).setConfiguration(ConfigShareCodeManager.CONFIG_GROUP, "levelUpMinLevel", 50);
		verify(configManagerMock).setConfiguration(ConfigShareCodeManager.CONFIG_GROUP, "levelUpInterval", 5);
		verify(configManagerMock).setConfiguration(ConfigShareCodeManager.CONFIG_GROUP, "notifyXpMilestone", true);
		verify(configManagerMock).setConfiguration(ConfigShareCodeManager.CONFIG_GROUP, "xpMilestoneInterval", XpMilestoneInterval.FIVE_MILLION);
		verify(configManagerMock).setConfiguration(ConfigShareCodeManager.CONFIG_GROUP, "notifyCombatAchievement", true);
		verify(configManagerMock).setConfiguration(ConfigShareCodeManager.CONFIG_GROUP, "combatAchievementMinTier", CombatAchievementTier.HARD);
		verify(configManagerMock).setConfiguration(ConfigShareCodeManager.CONFIG_GROUP, "notifyCombatAchievementTierCompletion", false);
		verify(configManagerMock).setConfiguration(ConfigShareCodeManager.CONFIG_GROUP, "notifyAchievementDiary", true);
		verify(configManagerMock).setConfiguration(ConfigShareCodeManager.CONFIG_GROUP, "notifyKill", false);
		verify(configManagerMock).setConfiguration(ConfigShareCodeManager.CONFIG_GROUP, "notifyDeath", true);
	}

	@Test
	public void testImportWithEventsDisabledAppliesOnlyAppearance() {
		when(configMock.showClanName()).thenReturn(true);
		when(configMock.showAccountIcon()).thenReturn(true);
		when(configMock.backgroundStyle()).thenReturn(BackgroundStyle.DARK);
		when(configMock.highlightColor()).thenReturn(Color.RED);
		when(configMock.clanNameColor()).thenReturn(Color.GREEN);
		when(configMock.imageScale()).thenReturn(ImageScale.X1);

		when(configMock.notifyPet()).thenReturn(true);
		when(configMock.valuableDropMinValue()).thenReturn(50_000);

		String shareCode = ConfigShareCodeManager.generateShareCode(configMock, gson);
		boolean applied = ConfigShareCodeManager.applyShareCode(shareCode, configManagerMock, gson, false);
		assertTrue(applied);

		// Appearance keys applied
		verify(configManagerMock).setConfiguration(ConfigShareCodeManager.CONFIG_GROUP, "showClanName", true);
		verify(configManagerMock).setConfiguration(ConfigShareCodeManager.CONFIG_GROUP, "showAccountIcon", true);
		verify(configManagerMock).setConfiguration(ConfigShareCodeManager.CONFIG_GROUP, "backgroundStyle", BackgroundStyle.DARK);
		verify(configManagerMock).setConfiguration(ConfigShareCodeManager.CONFIG_GROUP, "highlightColor", Color.RED);
		verify(configManagerMock).setConfiguration(ConfigShareCodeManager.CONFIG_GROUP, "clanNameColor", Color.GREEN);
		verify(configManagerMock).setConfiguration(ConfigShareCodeManager.CONFIG_GROUP, "imageScale", ImageScale.X1);

		// Event keys NEVER applied
		verify(configManagerMock, never()).setConfiguration(eq(ConfigShareCodeManager.CONFIG_GROUP), eq("notifyPet"), any());
		verify(configManagerMock, never()).setConfiguration(eq(ConfigShareCodeManager.CONFIG_GROUP), eq("valuableDropMinValue"), any());
		verify(configManagerMock, never()).setConfiguration(eq(ConfigShareCodeManager.CONFIG_GROUP), eq("notifyCollectionLog"), any());
		verify(configManagerMock, never()).setConfiguration(eq(ConfigShareCodeManager.CONFIG_GROUP), eq("notifyValuableDrop"), any());
		verify(configManagerMock, never()).setConfiguration(eq(ConfigShareCodeManager.CONFIG_GROUP), eq("notifyLevelUp"), any());
	}

	@Test
	public void testSecurityWebhookNeverExportedOrImported() {
		when(configMock.webhookUrl()).thenReturn("https://discord.com/api/webhooks/super-secret-token");
		when(configMock.webhookUsername()).thenReturn("AttackerBot");
		when(configMock.webhookAvatarUrl()).thenReturn("https://attacker.example/avatar.png");
		when(configMock.attachScreenshot()).thenReturn(true);

		when(configMock.showClanName()).thenReturn(true);
		when(configMock.showAccountIcon()).thenReturn(true);
		when(configMock.backgroundStyle()).thenReturn(BackgroundStyle.CLASSIC);
		when(configMock.highlightColor()).thenReturn(Color.RED);
		when(configMock.clanNameColor()).thenReturn(Color.BLUE);
		when(configMock.imageScale()).thenReturn(ImageScale.X1);

		String shareCode = ConfigShareCodeManager.generateShareCode(configMock, gson);
		assertFalse(shareCode.contains("super-secret-token"));
		assertFalse(shareCode.contains("AttackerBot"));
		assertFalse(shareCode.contains("attacker.example"));

		String payload = shareCode.substring(ConfigShareCodeManager.PREFIX.length());
		byte[] decoded = Base64.getUrlDecoder().decode(payload);
		String decodedJson = new String(decoded, StandardCharsets.UTF_8);

		assertFalse(decodedJson.contains("webhookUrl"));
		assertFalse(decodedJson.contains("webhookUsername"));
		assertFalse(decodedJson.contains("webhookAvatarUrl"));
		assertFalse(decodedJson.contains("attachScreenshot"));
		assertFalse(decodedJson.contains("super-secret-token"));

		// Craft malicious JSON attempting to inject sensitive webhook settings
		JsonObject malicious = new JsonObject();
		malicious.addProperty("webhookUrl", "https://discord.com/api/webhooks/evil-injected-token");
		malicious.addProperty("webhookUsername", "EvilWebhook");
		malicious.addProperty("webhookAvatarUrl", "https://evil.example/pic.jpg");
		malicious.addProperty("attachScreenshot", true);
		malicious.addProperty("shareCode", "evil-share-code");
		malicious.addProperty("importEventSettings", false);
		malicious.addProperty("unrecognizedProperty", "malicious-data");
		malicious.addProperty("showClanName", false);

		String maliciousPayload = Base64.getUrlEncoder().encodeToString(malicious.toString().getBytes(StandardCharsets.UTF_8));
		String maliciousCode = ConfigShareCodeManager.PREFIX + maliciousPayload;

		boolean applied = ConfigShareCodeManager.applyShareCode(maliciousCode, configManagerMock, gson, true);
		assertTrue(applied);

		verify(configManagerMock, never()).setConfiguration(eq(ConfigShareCodeManager.CONFIG_GROUP), eq("webhookUrl"), any());
		verify(configManagerMock, never()).setConfiguration(eq(ConfigShareCodeManager.CONFIG_GROUP), eq("webhookUsername"), any());
		verify(configManagerMock, never()).setConfiguration(eq(ConfigShareCodeManager.CONFIG_GROUP), eq("webhookAvatarUrl"), any());
		verify(configManagerMock, never()).setConfiguration(eq(ConfigShareCodeManager.CONFIG_GROUP), eq("attachScreenshot"), any());
		verify(configManagerMock, never()).setConfiguration(eq(ConfigShareCodeManager.CONFIG_GROUP), eq("shareCode"), any());
		verify(configManagerMock, never()).setConfiguration(eq(ConfigShareCodeManager.CONFIG_GROUP), eq("importEventSettings"), any());
		verify(configManagerMock, never()).setConfiguration(eq(ConfigShareCodeManager.CONFIG_GROUP), eq("unrecognizedProperty"), any());

		// Verify allowed appearance property was imported
		verify(configManagerMock).setConfiguration(ConfigShareCodeManager.CONFIG_GROUP, "showClanName", false);
	}

	@Test
	public void testOutOfRangeNumbersClamped() {
		JsonObject customJson = new JsonObject();
		customJson.addProperty("valuableDropMinValue", -50);
		customJson.addProperty("levelUpMinLevel", 500);
		customJson.addProperty("levelUpInterval", 0);

		String payload = Base64.getUrlEncoder().encodeToString(customJson.toString().getBytes(StandardCharsets.UTF_8));
		boolean applied = ConfigShareCodeManager.applyShareCode(ConfigShareCodeManager.PREFIX + payload, configManagerMock, gson, true);
		assertTrue(applied);

		verify(configManagerMock).setConfiguration(ConfigShareCodeManager.CONFIG_GROUP, "valuableDropMinValue", 0);
		verify(configManagerMock).setConfiguration(ConfigShareCodeManager.CONFIG_GROUP, "levelUpMinLevel", 99);
		verify(configManagerMock).setConfiguration(ConfigShareCodeManager.CONFIG_GROUP, "levelUpInterval", 1);

		// Also test lower-bound clamping for levelUpMinLevel and upper-bound for levelUpInterval
		ConfigManager secondMock = mock(ConfigManager.class);
		JsonObject customJson2 = new JsonObject();
		customJson2.addProperty("levelUpMinLevel", -10);
		customJson2.addProperty("levelUpInterval", 200);

		String payload2 = Base64.getUrlEncoder().encodeToString(customJson2.toString().getBytes(StandardCharsets.UTF_8));
		boolean applied2 = ConfigShareCodeManager.applyShareCode(ConfigShareCodeManager.PREFIX + payload2, secondMock, gson, true);
		assertTrue(applied2);

		verify(secondMock).setConfiguration(ConfigShareCodeManager.CONFIG_GROUP, "levelUpMinLevel", 1);
		verify(secondMock).setConfiguration(ConfigShareCodeManager.CONFIG_GROUP, "levelUpInterval", 98);
	}

	@Test
	public void testInvalidAndCorruptedShareCodes() {
		assertFalse(ConfigShareCodeManager.applyShareCode(null, configManagerMock, gson, true));
		assertFalse(ConfigShareCodeManager.applyShareCode("", configManagerMock, gson, true));
		assertFalse(ConfigShareCodeManager.applyShareCode("   ", configManagerMock, gson, true));
		assertFalse(ConfigShareCodeManager.applyShareCode("DCB1:", configManagerMock, gson, true));
		assertFalse(ConfigShareCodeManager.applyShareCode("DCB1:   ", configManagerMock, gson, true));
		assertFalse(ConfigShareCodeManager.applyShareCode("INVALID:eyJ0ZXN0IjoxfQ", configManagerMock, gson, true));
		assertFalse(ConfigShareCodeManager.applyShareCode("DCB1:not-valid-base64!@#$%", configManagerMock, gson, true));

		// Non-JSON payload
		String nonJsonBase64 = Base64.getEncoder().encodeToString("hello world not json".getBytes(StandardCharsets.UTF_8));
		assertFalse(ConfigShareCodeManager.applyShareCode("DCB1:" + nonJsonBase64, configManagerMock, gson, true));

		// Json primitive instead of object
		String jsonPrimitiveBase64 = Base64.getEncoder().encodeToString("12345".getBytes(StandardCharsets.UTF_8));
		assertFalse(ConfigShareCodeManager.applyShareCode("DCB1:" + jsonPrimitiveBase64, configManagerMock, gson, true));

		// Json array instead of object
		String jsonArrayBase64 = Base64.getEncoder().encodeToString("[1,2,3]".getBytes(StandardCharsets.UTF_8));
		assertFalse(ConfigShareCodeManager.applyShareCode("DCB1:" + jsonArrayBase64, configManagerMock, gson, true));
	}

	@Test
	public void testStandardAndUrlSafeBase64Accepted() {
		JsonObject obj = new JsonObject();
		obj.addProperty("showClanName", false);
		String jsonStr = obj.toString();

		// Standard Base64
		String standardEncoded = Base64.getEncoder().encodeToString(jsonStr.getBytes(StandardCharsets.UTF_8));
		ConfigManager standardMock = mock(ConfigManager.class);
		assertTrue(ConfigShareCodeManager.applyShareCode("DCB1:" + standardEncoded, standardMock, gson, true));
		verify(standardMock).setConfiguration(ConfigShareCodeManager.CONFIG_GROUP, "showClanName", false);

		// URL-Safe Base64 without padding
		String urlSafeEncoded = Base64.getUrlEncoder().withoutPadding().encodeToString(jsonStr.getBytes(StandardCharsets.UTF_8));
		ConfigManager urlSafeMock = mock(ConfigManager.class);
		assertTrue(ConfigShareCodeManager.applyShareCode("DCB1:" + urlSafeEncoded, urlSafeMock, gson, true));
		verify(urlSafeMock).setConfiguration(ConfigShareCodeManager.CONFIG_GROUP, "showClanName", false);
	}

	@Test
	public void testPanelExportGeneratesCodeAndSetsPositiveStatus() {
		when(configMock.showClanName()).thenReturn(true);
		when(configMock.showAccountIcon()).thenReturn(false);
		when(configMock.backgroundStyle()).thenReturn(BackgroundStyle.CLASSIC);
		when(configMock.highlightColor()).thenReturn(Color.RED);
		when(configMock.clanNameColor()).thenReturn(Color.BLUE);
		when(configMock.imageScale()).thenReturn(ImageScale.X1);

		DiscordChatboxBroadcasterPanel panel = new DiscordChatboxBroadcasterPanel(configMock, configManagerMock, gson);
		panel.getExportButton().doClick();

		assertTrue(panel.getStatusLabel().getText().contains("Copied share code to clipboard!"));
	}

	@Test
	public void testPanelImportValidCodeAppliesClearsInputAndSetsSuccessStatus() {
		DiscordChatboxBroadcasterPanel panel = new DiscordChatboxBroadcasterPanel(configMock, configManagerMock, gson);

		JsonObject validSettings = new JsonObject();
		validSettings.addProperty("showClanName", false);
		validSettings.addProperty("notifyPet", true);
		String code = "DCB1:" + Base64.getUrlEncoder().encodeToString(validSettings.toString().getBytes(StandardCharsets.UTF_8));

		panel.getShareCodeInputField().setText(code);
		assertTrue(panel.getImportEventsCheckbox().isSelected());

		panel.getImportButton().doClick();

		verify(configManagerMock).setConfiguration(ConfigShareCodeManager.CONFIG_GROUP, "showClanName", false);
		verify(configManagerMock).setConfiguration(ConfigShareCodeManager.CONFIG_GROUP, "notifyPet", true);
		assertEquals("", panel.getShareCodeInputField().getText());
		assertTrue(panel.getStatusLabel().getText().contains("Share code imported successfully!"));
	}

	@Test
	public void testPanelImportWithEventsUncheckedAppliesOnlyAppearance() {
		DiscordChatboxBroadcasterPanel panel = new DiscordChatboxBroadcasterPanel(configMock, configManagerMock, gson);

		JsonObject validSettings = new JsonObject();
		validSettings.addProperty("showClanName", false);
		validSettings.addProperty("notifyPet", true);
		String code = "DCB1:" + Base64.getUrlEncoder().encodeToString(validSettings.toString().getBytes(StandardCharsets.UTF_8));

		panel.getShareCodeInputField().setText(code);
		panel.getImportEventsCheckbox().setSelected(false);

		panel.getImportButton().doClick();

		verify(configManagerMock).setConfiguration(ConfigShareCodeManager.CONFIG_GROUP, "showClanName", false);
		verify(configManagerMock, never()).setConfiguration(eq(ConfigShareCodeManager.CONFIG_GROUP), eq("notifyPet"), any());
		assertEquals("", panel.getShareCodeInputField().getText());
		assertTrue(panel.getStatusLabel().getText().contains("Appearance settings imported!"));
	}

	@Test
	public void testPanelImportInvalidCodeSetsErrorStatusAndPreservesText() {
		DiscordChatboxBroadcasterPanel panel = new DiscordChatboxBroadcasterPanel(configMock, configManagerMock, gson);

		panel.getShareCodeInputField().setText("DCB1:invalid-payload");
		panel.getImportButton().doClick();

		assertEquals("DCB1:invalid-payload", panel.getShareCodeInputField().getText());
		assertTrue(panel.getStatusLabel().getText().contains("Invalid share code. Expected a code starting with DCB1:."));

		// Also test blank input
		panel.getShareCodeInputField().setText("");
		panel.getImportButton().doClick();
		assertTrue(panel.getStatusLabel().getText().contains("Please paste a DCB1:... share code first."));
	}

	@Test
	public void testAuthenticIronmanHelmResourceLoads() {
		BufferedImage helm = DiscordChatboxBroadcasterPanel.getSampleIronmanHelm();
		assertNotNull("Authentic ironman helm must not be null", helm);
		assertTrue(helm.getWidth() > 0);
		assertTrue(helm.getHeight() > 0);
	}

	@Test
	public void testPreviewLayoutAndCaptionProperties() {
		when(configMock.showClanName()).thenReturn(true);
		when(configMock.showAccountIcon()).thenReturn(true);
		when(configMock.backgroundStyle()).thenReturn(BackgroundStyle.CLASSIC);
		when(configMock.highlightColor()).thenReturn(Color.RED);
		when(configMock.clanNameColor()).thenReturn(Color.BLUE);
		when(configMock.imageScale()).thenReturn(ImageScale.X2);

		DiscordChatboxBroadcasterPanel panel = new DiscordChatboxBroadcasterPanel(configMock, configManagerMock, gson);

		assertEquals(SwingConstants.LEFT, panel.getPreviewLabel().getHorizontalAlignment());
		assertNotNull(panel.getPreviewScrollPane());
		assertEquals(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED, panel.getPreviewScrollPane().getHorizontalScrollBarPolicy());
		assertEquals(JScrollPane.VERTICAL_SCROLLBAR_NEVER, panel.getPreviewScrollPane().getVerticalScrollBarPolicy());

		assertNotNull(panel.getPreviewLabel().getIcon());
		assertNotNull(panel.getPreviewDimensionsLabel());
		assertTrue("Caption must include dimensions and scale (2x)",
				panel.getPreviewDimensionsLabel().getText().contains("(2x)"));
		assertTrue("Caption must start with 'Output size:'",
				panel.getPreviewDimensionsLabel().getText().startsWith("Output size:"));
	}

	@Test
	public void testUpdatePlayerContextDynamicSegments() {
		when(configMock.showClanName()).thenReturn(true);
		when(configMock.showAccountIcon()).thenReturn(true);
		when(configMock.backgroundStyle()).thenReturn(BackgroundStyle.CLASSIC);
		when(configMock.highlightColor()).thenReturn(Color.RED);
		when(configMock.clanNameColor()).thenReturn(Color.BLUE);
		when(configMock.imageScale()).thenReturn(ImageScale.X1);

		DiscordChatboxBroadcasterPanel panel = new DiscordChatboxBroadcasterPanel(configMock, configManagerMock, gson);

		BufferedImage customIcon = new BufferedImage(15, 15, BufferedImage.TYPE_INT_ARGB);
		panel.updatePlayerContext("TestClan", "MyIronman", customIcon);

		assertEquals(5, panel.getSampleSegments().size());
		assertEquals("[TestClan] ", panel.getSampleSegments().get(0).retrieveTextContent());
		assertTrue(panel.getSampleSegments().get(1).isImage());
		assertEquals(customIcon, panel.getSampleSegments().get(1).retrieveInlineImage());
		assertEquals("MyIronman ", panel.getSampleSegments().get(2).retrieveTextContent());

		// Fallback when null
		panel.updatePlayerContext(null, null, null);
		assertEquals("[Clan] ", panel.getSampleSegments().get(0).retrieveTextContent());
		assertTrue(panel.getSampleSegments().get(1).isImage());
		assertEquals("Player ", panel.getSampleSegments().get(2).retrieveTextContent());
	}

	@Test
	public void testExportButtonText() {
		DiscordChatboxBroadcasterPanel panel = new DiscordChatboxBroadcasterPanel(configMock, configManagerMock, gson);
		assertEquals("Export to Clipboard", panel.getExportButton().getText());
	}
}
