# Persistence

This document covers how game state is saved locally, how it is synced to the cloud, and how the weather API is integrated.

---

## GameSaveData — the serialization contract

`GameSaveData` is a plain Kotlin data class that represents a complete snapshot of the game. It is the single source of truth for everything that needs to survive an app restart.

Fields it holds:

- **Economy** — `money`, `totalMoneyEarned`, `totalClicks`, per-vegetable fruit counts and harvest totals
- **Settings** — vibration enabled, active theme ID, language code
- **User profile** — username, avatar image ID
- **Progression** — which vegetables are unlocked, which modifiers are purchased and enabled, global upgrade levels, which library entries are unlocked, which achievements are earned
- **Weather cache** — the last API response and the timestamp it was fetched (used to determine if the 5-hour bonus window is still active without re-fetching)
- **Gallery** — which art pieces are unlocked

### The rule: add fields here first

Any time you want to persist a new piece of state, the first step is adding a field to `GameSaveData`. The second step is reading and writing it in `DataStoreGameRepository`. The third step is hydrating it in `GameViewModel.applySaveData()`. If you add state anywhere without touching all three, it will be lost on restart.

---

## Local persistence — DataStoreGameRepository

`DataStoreGameRepository` implements `GameRepository` using **Jetpack DataStore (Preferences)** — a key-value store backed by a binary file. There is no SQLite database.

### Why DataStore instead of a database

The game state, while large, is a flat snapshot — there are no relational queries, no JOINs, no need to filter rows. DataStore is simpler, has no schema migrations, and handles atomic writes via its built-in transaction model.

### How nested maps are stored

DataStore is a flat key-value store. Nested maps (e.g. `fruitCounts: Map<String, Int>` per vegetable) are stored using a **dynamic key prefix scheme**: `fruitCount_tomato`, `fruitCount_broccoli`, etc. On load, `DataStoreGameRepository` iterates all keys with the prefix and reconstructs the map.

### Write behaviour

Saves are triggered by `GameViewModel` via a coroutine in `viewModelScope` after every state mutation. Writes use `prefs.edit {}` which is atomic — a crash mid-write leaves the previous snapshot intact rather than a corrupted one.

---

## Cloud sync — Firebase Firestore

Players can back up their save to the cloud and restore it on another device. This requires Google Sign-In.

### Upload

```
GameViewModel.uploadSaveToCloud()
  → serializes GameSaveData to a JSON string (kotlinx.serialization)
  → Firebase.firestore
      .collection("users")
      .document(userId)
      .set(parsedJsonMap)
```

A 60-second cooldown is enforced on the ViewModel side to prevent accidental hammering of Firestore.

### Download

The reverse: the Firestore document is fetched, deserialized back into `GameSaveData`, and passed to `applySaveData()`.

### Player search

`ProfileManager` queries the `usernames` Firestore collection to let players look up others by username. The query uses a range filter on the username string to approximate case-insensitive prefix matching (Firestore does not have native case-insensitive search).

---

## Weather API — WeatherService

`WeatherService` is a Ktor HTTP client that fetches current weather from **Open-Meteo** — a free, no-API-key weather service.

**Endpoint:** `https://api.open-meteo.com/v1/forecast`

**Parameters sent:** latitude, longitude, `current=temperature_2m,weather_code`

**Response parsed:**
- `temperature_2m` — current temperature in °C, fed to `RewardCalculator` for crop bonuses
- `weather_code` — a WMO weather code, parsed by `WeatherManager` into an internal `WeatherCondition` enum (Sunny, Cloudy, Rainy, Snowy, Thunderstorm, etc.)

**Error handling:** any network failure returns `null`. `WeatherManager` treats a null response as no update — the previous cached state is preserved.

The last successful response is cached in `GameSaveData` along with its fetch timestamp. On startup, if the cached response is less than 5 hours old, it is used directly without a new API call.
