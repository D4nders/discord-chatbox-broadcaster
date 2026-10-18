# Discord Chatbox Broadcaster

[![Active Installs](https://img.shields.io/endpoint?url=https://api.runelite.net/pluginhub/shields/installs/plugin/discord-chatbox-broadcaster&style=for-the-badge)](https://runelite.net/plugin-hub/show/discord-chatbox-broadcaster)
[![Report a Bug](https://img.shields.io/badge/%F0%9F%90%9B_Report_a_Bug-d73a4a?style=for-the-badge)](https://github.com/D4nders/discord-chatbox-broadcaster/issues/new?template=bug_report.yaml)
[![New Broadcast Event](https://img.shields.io/badge/%F0%9F%93%A2_New_Broadcast_Event-0075ca?style=for-the-badge)](https://github.com/D4nders/discord-chatbox-broadcaster/issues/new?template=new_broadcast.yaml)
[![Feature Request](https://img.shields.io/badge/%F0%9F%92%A1_Feature_Request-a2eeef?style=for-the-badge&logoColor=black)](https://github.com/D4nders/discord-chatbox-broadcaster/issues/new?template=feature_request.yaml)
[![Blank Issue](https://img.shields.io/badge/%F0%9F%93%9D_Quick_Note_%2F_Blank_Issue-6e7781?style=for-the-badge)](https://github.com/D4nders/discord-chatbox-broadcaster/issues/new?template=BLANK_ISSUE)

A RuneLite plugin that automatically broadcasts significant Old School RuneScape achievements and events to a Discord Webhook. Unlike standard text webhooks, this plugin generates a high-quality, authentic-looking OSRS chatbox image for every broadcast!

## Examples from Discord

<img width="271" height="30" alt="image" src="https://github.com/user-attachments/assets/9cbf31c8-49bb-4538-80c9-061fdc80d552" />
<br>
<img width="380" height="30" alt="image" src="https://github.com/user-attachments/assets/f87422e9-d327-443a-8be7-adde9bb1ceb9" />
<br>
<img width="514" height="30" alt="image" src="https://github.com/user-attachments/assets/cdf105fc-4432-41c2-b9f2-b6cb9e48a546" />
<br>
<img width="382" height="30" alt="image" src="https://github.com/user-attachments/assets/1f1eb78f-1dbf-487d-87a2-0fa05920289b" />
<br>

## Features

The plugin currently supports broadcasting the following events:

* 🐾 **Pets** (Funny feelings & backpack sneaks)
* 📖 **Collection Log** (New item additions)
* 💰 **Valuable Drops** (High-value loot drops)
* 📜 **Quests** (Quest completions)
* ⏱️ **Personal Bests** (New boss/activity records, including boss/activity name)
* 🎉 **Level Ups** (Skill advancements)
* 📈 **XP Milestones** (Every whole 1M XP reached past level 99)
* ⚔️ **Player Kills** (PvP victories)
* 💀 **Deaths** (When your character dies)
* 🛡️ **Combat Achievements** (Individual task & full tier completions)
* 🗺️ **Achievement Diaries** (Area tier completions)

### Smart Features
* **Intelligent Deduplication:** Prevents spam by silently suppressing duplicate notifications on the same tick — e.g. a "Valuable drop" is ignored when it coincides with a "Collection Log" message for the same item, and pet drops suppress their corresponding collection log entry.
* **Pet Name Extraction:** Identifies the specific pet name by correlating the "funny feeling" chatbox message with the same-tick "Untradeable drop" message. Duplicate pets show the generic message.
* **In-Game Settings Warnings:** Automatically warns you in the chatbox on login or config toggle if an enabled notification requires an OSRS in-game setting (such as *Collection log - New addition notification* or *Loot drop notifications*) that is currently disabled.
* **Rate-Limit Protection:** Employs an asynchronous background queue to respect Discord's strict webhook rate limits (5 requests per 2 seconds). This ensures no broadcasts are lost during mass clue-scroll openings or back-to-back achievements.
* **Streamer Safe:** The Webhook URL configuration field is marked as a secret, masking the URL with asterisks to prevent accidental leaks on stream.

## Setup Instructions

1. Open your Discord Server settings.
2. Navigate to **Integrations** -> **Webhooks**.
3. Click **New Webhook**, customize the name/avatar, and click **Copy Webhook URL**.
4. Open the RuneLite configuration panel and search for **Discord Chatbox Broadcaster**.
5. Paste your copied URL into the **Discord Webhook URL** field.
6. Toggle the specific events you wish to broadcast!

## Changelog

### v1.2

* **XP Milestones (#6):** Added an opt-in setting to broadcast every whole 1M XP milestone reached past level 99 (`"[Player] has reached [14,000,000] XP in [Skill]!"`).
* **In-Game Settings Warnings (#7):** Added an in-game chatbox warning on login and config toggle when Collection Log or Valuable Drop notifications are enabled in the plugin while the corresponding OSRS in-game notification settings are disabled.
* **Achievement Diaries (#8):** Fixed diary completions not broadcasting when displayed in a popup dialog (`MESBOX`/`DIALOG`) or containing `<br>` line breaks.
* **Personal Bests (#12, #14):** Fixed Tombs of Amascut and raid/boss completion times where inline raid names or same-tick deferred killcount messages caused missing raid names or truncated `"Challenge "` / `"total "` completion labels.
* **Combat Achievements (#15):** Added broadcasting support for unlocking full Combat Achievement Tier rewards (`"[Player] has completed the [Tier] Tier of Combat Achievements!"`).

### v1.1

* **Pets:** Decoupled from Valuable Drops. Pet name is now extracted by correlating the "funny feeling" message with the same-tick "Untradeable drop" message. Duplicate pets show the generic message.
* **Valuable Drops:** Now only triggers on "Valuable drop:" messages — no longer fires on untradeable drops.
* **Personal Bests:** Now displays the boss/activity name alongside the PB time.
* **Achievement Diaries:** Refactored from Varbit tracking (which spammed all completed diaries on login) to parsing the standard chatbox completion message.
* **Combat Tasks & Quests:** Fixed formatting where raw color tags were appearing in Discord messages.
* **Player Kills:** Fixed a bug where PvM kills were misidentified as PvP kills. Now uses player death tracking.
* **Config:** All notifications now default to disabled (opt-in) per RuneLite guidelines.
* Various code-quality improvements.
