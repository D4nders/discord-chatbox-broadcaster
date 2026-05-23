package com.discordchatboxbroadcaster.event;

import com.discordchatboxbroadcaster.DiscordChatboxBroadcasterConfig;
import com.discordchatboxbroadcaster.notifier.Notifier;
import com.discordchatboxbroadcaster.render.ChatSegment;
import net.runelite.api.GameState;
import net.runelite.api.gameval.VarbitID;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AchievementDiaryEventProcessor extends GameEventProcessor {

    private static class DiaryMetadata {
        private final String tier;
        private final String area;
        private final int completionValue;

        public DiaryMetadata(String tier, String area, int completionValue) {
            this.tier = tier;
            this.area = area;
            this.completionValue = completionValue;
        }

        public String getTier() {
            return tier;
        }

        public String getArea() {
            return area;
        }

        public int getCompletionValue() {
            return completionValue;
        }
    }

    private final DiscordChatboxBroadcasterConfig pluginConfiguration;
    private final Map<Integer, DiaryMetadata> diaryVarbitMapping;
    private final Map<Integer, Integer> previousVarbitStates;

    public AchievementDiaryEventProcessor(List<Notifier> registeredNotifiers, DiscordChatboxBroadcasterConfig pluginConfiguration) {
        super(registeredNotifiers);
        this.pluginConfiguration = pluginConfiguration;
        this.diaryVarbitMapping = new HashMap<>();
        this.previousVarbitStates = new HashMap<>();
        initializeDiaryMapping();
    }

    private void initializeDiaryMapping() {
        registerDiary(VarbitID.ARDOUGNE_DIARY_EASY_COMPLETE, "Easy", "Ardougne", 1);
        registerDiary(VarbitID.ARDOUGNE_DIARY_MEDIUM_COMPLETE, "Medium", "Ardougne", 1);
        registerDiary(VarbitID.ARDOUGNE_DIARY_HARD_COMPLETE, "Hard", "Ardougne", 1);
        registerDiary(VarbitID.ARDOUGNE_DIARY_ELITE_COMPLETE, "Elite", "Ardougne", 1);

        registerDiary(VarbitID.DESERT_DIARY_EASY_COMPLETE, "Easy", "Desert", 1);
        registerDiary(VarbitID.DESERT_DIARY_MEDIUM_COMPLETE, "Medium", "Desert", 1);
        registerDiary(VarbitID.DESERT_DIARY_HARD_COMPLETE, "Hard", "Desert", 1);
        registerDiary(VarbitID.DESERT_DIARY_ELITE_COMPLETE, "Elite", "Desert", 1);

        registerDiary(VarbitID.FALADOR_DIARY_EASY_COMPLETE, "Easy", "Falador", 1);
        registerDiary(VarbitID.FALADOR_DIARY_MEDIUM_COMPLETE, "Medium", "Falador", 1);
        registerDiary(VarbitID.FALADOR_DIARY_HARD_COMPLETE, "Hard", "Falador", 1);
        registerDiary(VarbitID.FALADOR_DIARY_ELITE_COMPLETE, "Elite", "Falador", 1);

        registerDiary(VarbitID.FREMENNIK_DIARY_EASY_COMPLETE, "Easy", "Fremennik", 1);
        registerDiary(VarbitID.FREMENNIK_DIARY_MEDIUM_COMPLETE, "Medium", "Fremennik", 1);
        registerDiary(VarbitID.FREMENNIK_DIARY_HARD_COMPLETE, "Hard", "Fremennik", 1);
        registerDiary(VarbitID.FREMENNIK_DIARY_ELITE_COMPLETE, "Elite", "Fremennik", 1);

        registerDiary(VarbitID.KANDARIN_DIARY_EASY_COMPLETE, "Easy", "Kandarin", 1);
        registerDiary(VarbitID.KANDARIN_DIARY_MEDIUM_COMPLETE, "Medium", "Kandarin", 1);
        registerDiary(VarbitID.KANDARIN_DIARY_HARD_COMPLETE, "Hard", "Kandarin", 1);
        registerDiary(VarbitID.KANDARIN_DIARY_ELITE_COMPLETE, "Elite", "Kandarin", 1);

        registerDiary(VarbitID.ATJUN_EASY_DONE, "Easy", "Karamja", 2);
        registerDiary(VarbitID.ATJUN_MED_DONE, "Medium", "Karamja", 2);
        registerDiary(VarbitID.ATJUN_HARD_DONE, "Hard", "Karamja", 2);
        registerDiary(VarbitID.KARAMJA_DIARY_ELITE_COMPLETE, "Elite", "Karamja", 1);

        registerDiary(VarbitID.KOUREND_DIARY_EASY_COMPLETE, "Easy", "Kourend & Kebos", 1);
        registerDiary(VarbitID.KOUREND_DIARY_MEDIUM_COMPLETE, "Medium", "Kourend & Kebos", 1);
        registerDiary(VarbitID.KOUREND_DIARY_HARD_COMPLETE, "Hard", "Kourend & Kebos", 1);
        registerDiary(VarbitID.KOUREND_DIARY_ELITE_COMPLETE, "Elite", "Kourend & Kebos", 1);

        registerDiary(VarbitID.LUMBRIDGE_DIARY_EASY_COMPLETE, "Easy", "Lumbridge & Draynor", 1);
        registerDiary(VarbitID.LUMBRIDGE_DIARY_MEDIUM_COMPLETE, "Medium", "Lumbridge & Draynor", 1);
        registerDiary(VarbitID.LUMBRIDGE_DIARY_HARD_COMPLETE, "Hard", "Lumbridge & Draynor", 1);
        registerDiary(VarbitID.LUMBRIDGE_DIARY_ELITE_COMPLETE, "Elite", "Lumbridge & Draynor", 1);

        registerDiary(VarbitID.MORYTANIA_DIARY_EASY_COMPLETE, "Easy", "Morytania", 1);
        registerDiary(VarbitID.MORYTANIA_DIARY_MEDIUM_COMPLETE, "Medium", "Morytania", 1);
        registerDiary(VarbitID.MORYTANIA_DIARY_HARD_COMPLETE, "Hard", "Morytania", 1);
        registerDiary(VarbitID.MORYTANIA_DIARY_ELITE_COMPLETE, "Elite", "Morytania", 1);

        registerDiary(VarbitID.VARROCK_DIARY_EASY_COMPLETE, "Easy", "Varrock", 1);
        registerDiary(VarbitID.VARROCK_DIARY_MEDIUM_COMPLETE, "Medium", "Varrock", 1);
        registerDiary(VarbitID.VARROCK_DIARY_HARD_COMPLETE, "Hard", "Varrock", 1);
        registerDiary(VarbitID.VARROCK_DIARY_ELITE_COMPLETE, "Elite", "Varrock", 1);

        registerDiary(VarbitID.WESTERN_DIARY_EASY_COMPLETE, "Easy", "Western Provinces", 1);
        registerDiary(VarbitID.WESTERN_DIARY_MEDIUM_COMPLETE, "Medium", "Western Provinces", 1);
        registerDiary(VarbitID.WESTERN_DIARY_HARD_COMPLETE, "Hard", "Western Provinces", 1);
        registerDiary(VarbitID.WESTERN_DIARY_ELITE_COMPLETE, "Elite", "Western Provinces", 1);

        registerDiary(VarbitID.WILDERNESS_DIARY_EASY_COMPLETE, "Easy", "Wilderness", 1);
        registerDiary(VarbitID.WILDERNESS_DIARY_MEDIUM_COMPLETE, "Medium", "Wilderness", 1);
        registerDiary(VarbitID.WILDERNESS_DIARY_HARD_COMPLETE, "Hard", "Wilderness", 1);
        registerDiary(VarbitID.WILDERNESS_DIARY_ELITE_COMPLETE, "Elite", "Wilderness", 1);
    }

    private void registerDiary(int varbitId, String tier, String area, int completionValue) {
        diaryVarbitMapping.put(varbitId, new DiaryMetadata(tier, area, completionValue));
    }

    @Override
    protected boolean isFeatureEnabled() {
        return pluginConfiguration.notifyAchievementDiary();
    }

    @Override
    protected void processSanitizedMessage(String sanitizedMessageContent, String activePlayerName, String activeClanName, int currentTick) {
    }

    @Override
    public void evaluateVarbitChanged(int varbitId, int newValue, String activePlayerName, String activeClanName, int currentTick) {
        if (!isFeatureEnabled()) {
            return;
        }

        DiaryMetadata diaryMetadata = diaryVarbitMapping.get(varbitId);

        if (diaryMetadata == null) {
            return;
        }

        int previousValue = previousVarbitStates.getOrDefault(varbitId, -1);
        previousVarbitStates.put(varbitId, newValue);

        if (previousValue != -1 && previousValue != newValue && newValue == diaryMetadata.getCompletionValue()) {
            executeNotificationSequence(activePlayerName, activeClanName, diaryMetadata.getTier(), diaryMetadata.getArea());
        }
    }

    @Override
    public void evaluateGameStateChanged(GameState newGameState) {
        if (newGameState == GameState.LOGIN_SCREEN || newGameState == GameState.HOPPING) {
            previousVarbitStates.clear();
        }
    }

    private void executeNotificationSequence(String activePlayerName, String activeClanName, String tier, String area) {
        List<ChatSegment> notificationSegments = buildPlayerClanPrefixSegments(activePlayerName, activeClanName);
        notificationSegments.add(new ChatSegment("completed the ", Color.BLACK));
        notificationSegments.add(new ChatSegment(tier + " " + area, HIGHLIGHT_COLOR));
        notificationSegments.add(new ChatSegment(" diary.", Color.BLACK));

        dispatchNotificationSegments(notificationSegments);
    }
}