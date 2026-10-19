package com.discordchatboxbroadcaster.config;

import com.discordchatboxbroadcaster.DiscordChatboxBroadcasterConfig;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.awt.Color;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;

@Slf4j
public final class ConfigShareCodeManager {

	public static final String PREFIX = "DCB1:";
	public static final String CONFIG_GROUP = "discordchatboxbroadcaster";

	private ConfigShareCodeManager() {
	}

	public static String generateShareCode(DiscordChatboxBroadcasterConfig config, Gson gson) {
		JsonObject jsonObject = new JsonObject();

		// Appearance keys
		jsonObject.addProperty("showClanName", config.showClanName());
		jsonObject.addProperty("showAccountIcon", config.showAccountIcon());
		if (config.backgroundStyle() != null) {
			jsonObject.addProperty("backgroundStyle", config.backgroundStyle().name());
		}
		if (config.highlightColor() != null) {
			jsonObject.addProperty("highlightColor", config.highlightColor().getRGB());
		}
		if (config.clanNameColor() != null) {
			jsonObject.addProperty("clanNameColor", config.clanNameColor().getRGB());
		}
		if (config.imageScale() != null) {
			jsonObject.addProperty("imageScale", config.imageScale().name());
		}

		// Event Toggles & Filter keys
		jsonObject.addProperty("disableInTemporaryGameModes", config.disableInTemporaryGameModes());
		jsonObject.addProperty("notifyPet", config.notifyPet());
		jsonObject.addProperty("notifyCollectionLog", config.notifyCollectionLog());
		jsonObject.addProperty("notifyValuableDrop", config.notifyValuableDrop());
		jsonObject.addProperty("valuableDropMinValue", config.valuableDropMinValue());
		jsonObject.addProperty("notifyQuest", config.notifyQuest());
		jsonObject.addProperty("notifyNewRecord", config.notifyNewRecord());
		jsonObject.addProperty("notifyLevelUp", config.notifyLevelUp());
		jsonObject.addProperty("levelUpMinLevel", config.levelUpMinLevel());
		jsonObject.addProperty("levelUpInterval", config.levelUpInterval());
		jsonObject.addProperty("notifyXpMilestone", config.notifyXpMilestone());
		if (config.xpMilestoneInterval() != null) {
			jsonObject.addProperty("xpMilestoneInterval", config.xpMilestoneInterval().name());
		}
		jsonObject.addProperty("notifyCombatAchievement", config.notifyCombatAchievement());
		if (config.combatAchievementMinTier() != null) {
			jsonObject.addProperty("combatAchievementMinTier", config.combatAchievementMinTier().name());
		}
		jsonObject.addProperty("notifyCombatAchievementTierCompletion", config.notifyCombatAchievementTierCompletion());
		jsonObject.addProperty("notifyAchievementDiary", config.notifyAchievementDiary());
		jsonObject.addProperty("notifyKill", config.notifyKill());
		jsonObject.addProperty("notifyDeath", config.notifyDeath());

		String jsonString = gson.toJson(jsonObject);
		String base64Payload = Base64.getUrlEncoder().withoutPadding().encodeToString(jsonString.getBytes(StandardCharsets.UTF_8));
		return PREFIX + base64Payload;
	}

	public static boolean applyShareCode(String rawCode, ConfigManager configManager, Gson gson, boolean importEvents) {
		if (rawCode == null) {
			return false;
		}

		String trimmed = rawCode.trim();
		if (trimmed.length() <= PREFIX.length() || !trimmed.regionMatches(true, 0, PREFIX, 0, PREFIX.length())) {
			return false;
		}

		String payload = trimmed.substring(PREFIX.length()).trim();
		if (payload.isEmpty()) {
			return false;
		}

		byte[] jsonBytes;
		try {
			jsonBytes = Base64.getUrlDecoder().decode(payload);
		} catch (IllegalArgumentException e) {
			try {
				jsonBytes = Base64.getDecoder().decode(payload);
			} catch (IllegalArgumentException ex) {
				log.debug("Failed to decode base64 share code payload: {}", ex.getMessage());
				return false;
			}
		}

		JsonObject obj;
		try {
			obj = gson.fromJson(new String(jsonBytes, StandardCharsets.UTF_8), JsonObject.class);
		} catch (Exception e) {
			log.debug("Failed to parse share code JSON: {}", e.getMessage());
			return false;
		}

		if (obj == null || !obj.isJsonObject()) {
			return false;
		}

		// Appearance keys
		JsonElement showClanName = obj.get("showClanName");
		if (isBoolean(showClanName)) {
			configManager.setConfiguration(CONFIG_GROUP, "showClanName", showClanName.getAsBoolean());
		}

		JsonElement showAccountIcon = obj.get("showAccountIcon");
		if (isBoolean(showAccountIcon)) {
			configManager.setConfiguration(CONFIG_GROUP, "showAccountIcon", showAccountIcon.getAsBoolean());
		}

		JsonElement backgroundStyle = obj.get("backgroundStyle");
		if (isString(backgroundStyle)) {
			try {
				BackgroundStyle style = BackgroundStyle.valueOf(backgroundStyle.getAsString());
				configManager.setConfiguration(CONFIG_GROUP, "backgroundStyle", style);
			} catch (IllegalArgumentException | NullPointerException ignored) {
			}
		}

		JsonElement highlightColor = obj.get("highlightColor");
		if (isNumber(highlightColor)) {
			try {
				configManager.setConfiguration(CONFIG_GROUP, "highlightColor", new Color(highlightColor.getAsInt(), false));
			} catch (IllegalArgumentException ignored) {
			}
		}

		JsonElement clanNameColor = obj.get("clanNameColor");
		if (isNumber(clanNameColor)) {
			try {
				configManager.setConfiguration(CONFIG_GROUP, "clanNameColor", new Color(clanNameColor.getAsInt(), false));
			} catch (IllegalArgumentException ignored) {
			}
		}

		JsonElement imageScale = obj.get("imageScale");
		if (isString(imageScale)) {
			try {
				ImageScale scale = ImageScale.valueOf(imageScale.getAsString());
				configManager.setConfiguration(CONFIG_GROUP, "imageScale", scale);
			} catch (IllegalArgumentException | NullPointerException ignored) {
			}
		}

		// Event keys
		if (importEvents) {
			applyBoolean(configManager, obj, "disableInTemporaryGameModes");
			applyBoolean(configManager, obj, "notifyPet");
			applyBoolean(configManager, obj, "notifyCollectionLog");
			applyBoolean(configManager, obj, "notifyValuableDrop");
			applyBoolean(configManager, obj, "notifyQuest");
			applyBoolean(configManager, obj, "notifyNewRecord");
			applyBoolean(configManager, obj, "notifyLevelUp");
			applyBoolean(configManager, obj, "notifyXpMilestone");
			applyBoolean(configManager, obj, "notifyCombatAchievement");
			applyBoolean(configManager, obj, "notifyCombatAchievementTierCompletion");
			applyBoolean(configManager, obj, "notifyAchievementDiary");
			applyBoolean(configManager, obj, "notifyKill");
			applyBoolean(configManager, obj, "notifyDeath");

			JsonElement valuableDropMinValue = obj.get("valuableDropMinValue");
			if (isNumber(valuableDropMinValue)) {
				try {
					int val = Math.max(0, valuableDropMinValue.getAsInt());
					configManager.setConfiguration(CONFIG_GROUP, "valuableDropMinValue", val);
				} catch (NumberFormatException ignored) {
				}
			}

			JsonElement levelUpMinLevel = obj.get("levelUpMinLevel");
			if (isNumber(levelUpMinLevel)) {
				try {
					int val = Math.max(1, Math.min(99, levelUpMinLevel.getAsInt()));
					configManager.setConfiguration(CONFIG_GROUP, "levelUpMinLevel", val);
				} catch (NumberFormatException ignored) {
				}
			}

			JsonElement levelUpInterval = obj.get("levelUpInterval");
			if (isNumber(levelUpInterval)) {
				try {
					int val = Math.max(1, Math.min(98, levelUpInterval.getAsInt()));
					configManager.setConfiguration(CONFIG_GROUP, "levelUpInterval", val);
				} catch (NumberFormatException ignored) {
				}
			}

			JsonElement xpMilestoneInterval = obj.get("xpMilestoneInterval");
			if (isString(xpMilestoneInterval)) {
				try {
					XpMilestoneInterval val = XpMilestoneInterval.valueOf(xpMilestoneInterval.getAsString());
					configManager.setConfiguration(CONFIG_GROUP, "xpMilestoneInterval", val);
				} catch (IllegalArgumentException | NullPointerException ignored) {
				}
			}

			JsonElement combatAchievementMinTier = obj.get("combatAchievementMinTier");
			if (isString(combatAchievementMinTier)) {
				try {
					CombatAchievementTier val = CombatAchievementTier.valueOf(combatAchievementMinTier.getAsString());
					configManager.setConfiguration(CONFIG_GROUP, "combatAchievementMinTier", val);
				} catch (IllegalArgumentException | NullPointerException ignored) {
				}
			}
		}

		return true;
	}

	private static void applyBoolean(ConfigManager configManager, JsonObject obj, String key) {
		JsonElement elem = obj.get(key);
		if (isBoolean(elem)) {
			configManager.setConfiguration(CONFIG_GROUP, key, elem.getAsBoolean());
		}
	}

	private static boolean isBoolean(JsonElement elem) {
		return elem != null && elem.isJsonPrimitive() && elem.getAsJsonPrimitive().isBoolean();
	}

	private static boolean isNumber(JsonElement elem) {
		return elem != null && elem.isJsonPrimitive() && elem.getAsJsonPrimitive().isNumber();
	}

	private static boolean isString(JsonElement elem) {
		return elem != null && elem.isJsonPrimitive() && elem.getAsJsonPrimitive().isString();
	}
}
