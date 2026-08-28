# Discord Chatbox Broadcaster

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
* 💰 **Valuable Drops** (High-value and untradeable loot)
* 📜 **Quests** (Quest completions)
* ⏱️ **Personal Bests** (New boss/activity records)
* 🎉 **Level Ups** (Skill advancements)
* ⚔️ **Player Kills** (PvP victories)
* 💀 **Deaths** (When your character dies)
* 🛡️ **Combat Achievements** (Task completions)
* 🗺️ **Achievement Diaries** (Area tier completions)

### Smart Features
* **Intelligent Deduplication:** Prevents spam by silently ignoring "Valuable drop" messages if they trigger on the exact same tick as a "Collection Log" message for the same item.
* **Rate-Limit Protection:** Employs an asynchronous background queue to respect Discord's strict webhook rate limits (5 requests per 2 seconds). This ensures no broadcasts are lost during mass clue-scroll openings or back-to-back achievements.
* **Streamer Safe:** The Webhook URL configuration field is marked as a secret, masking the URL with asterisks to prevent accidental leaks on stream.

## Setup Instructions

1. Open your Discord Server settings.
2. Navigate to **Integrations** -> **Webhooks**.
3. Click **New Webhook**, customize the name/avatar, and click **Copy Webhook URL**.
4. Open the RuneLite configuration panel and search for **Discord Chatbox Broadcaster**.
5. Paste your copied URL into the **Discord Webhook URL** field.
6. Toggle the specific events you wish to broadcast!

## Credit

* Achievement Diary varbit mapping logic inspired by [m0bilebtw's C Engineer Completed plugin](https://github.com/m0bilebtw/c-engineer-completed).
