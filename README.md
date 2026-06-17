# Clicky's Garden

A cross-platform idle/clicker game built with **Kotlin Multiplatform** and **Jetpack Compose Multiplatform**. Players harvest virtual vegetables to earn coins, unlock new crops, and complete achievements — each vegetable has a unique physical interaction mechanic powered by the device's sensors.

> **Platform:** Android (iOS scaffolding in place)
> **Architecture:** MVVM · Repository pattern · KMP
> **UI:** Jetpack Compose Multiplatform · Material 3

---

## Features

### Gameplay
- **7 harvestable vegetables**, each with a distinct mechanic:
  - **Tomato** — precision timing (click during the pulse peak for a critical hit)
  - **Broccoli** — click combo streaks
  - **Bell Pepper** — rain weather bonus
  - **Garlic** — shake the device to harvest
  - **Purple Onion** — proximity sensor bonus (hold phone close)
  - **Squash** — high-speed clicking streak
  - **Apple** — device rotation/tilt bonus
- **Global upgrades**: Lucky Harvest (chance for 10× reward) and Precise Harvest (milestone doubling), each with 5 purchasable levels
- **Per-vegetable modifiers**: unlockable toggles that enhance each crop's special mechanic
- **Achievement system**: 30+ milestones tracking clicks, earnings, sensor interactions, and collection progress
- **Auto-harvester**: activates automatically during rainy weather when the weather bonus is enabled

### Weather integration
- Pulls real-time weather from the **Open-Meteo API** (no API key required)
- Temperature, rain, snow, and sunny conditions each affect specific crops differently
- 5-hour weather bonus window; rain triggers auto-harvest every 10 seconds

### Progression & collection
- **Library**: 70 purchasable educational facts (10 per vegetable) — a knowledge layer on top of the clicker loop
- **Art Gallery**: 20 collectible art pieces unlockable with in-game coins
- Full progression persisted locally via **Jetpack DataStore**

### Social & cloud
- **Google Sign-In** via Firebase Auth
- **Cloud save** — upload/download game state to Firebase Firestore
- **Player search** — find other players by username

### Polish
- 4 themes: Light, Dark, Wavy, Autumn Woods (dynamic Material 3 color schemes)
- 3D cube swipe navigation between main screens
- Frame-synced sprite animations and custom particle effects
- Haptic feedback tied to gameplay events
- Full localization support (string resources, multi-language)
- Animated shader background (Wavy theme, AGSL on Android)

---

## Tech stack

| Area | Technology |
|---|---|
| Language | Kotlin (Multiplatform) |
| UI | Jetpack Compose Multiplatform |
| Architecture | MVVM + Repository pattern |
| Local persistence | Jetpack DataStore (Preferences) |
| Authentication | Firebase Auth + Google Sign-In |
| Cloud storage | Firebase Firestore |
| Networking | Ktor (OkHttp on Android) |
| Serialization | kotlinx.serialization |
| Sensors | Android SensorManager (accelerometer, proximity, rotation) |
| Location | Google Play Services FusedLocationProviderClient |
| Weather API | Open-Meteo (free, no key) |
| Min SDK | API 24 (Android 7.0) |
| Target SDK | API 34 |

---

## Architecture overview

The project follows **MVVM + the Repository pattern**: MVVM separates the UI (View) from business logic (ViewModel) and game/data state (Model), while the Repository pattern hides *where* that data actually lives (DataStore, Firestore) behind a plain interface, so the storage backend can change without touching the ViewModel. It is structured in five layers:

```
┌─────────────────────────────────────────────────────┐
│  UI Layer                                           │
│  Compose screens — renders state, emits events      │
└───────────────────────┬─────────────────────────────┘
                        │ observes / calls
┌───────────────────────▼─────────────────────────────┐
│  ViewModel Layer                                    │
│  GameViewModel                                      │
│    ├── EconomyManager   (currency, inventory)       │
│    ├── ProfileManager   (profile sync, search)      │
│    ├── WeatherManager   (weather API, auto-harvest) │
│    └── RewardCalculator (stateless reward math)     │
└─────────┬──────────────────────────┬────────────────┘
          │ reads / writes           │ auth events
┌─────────▼──────────┐  ┌───────────▼────────────────┐
│  Data Layer        │  │  Auth Layer                 │
│  GameRepository    │  │  AuthRepository (interface) │
│  DataStore (local) │  │  Firebase Auth              │
│  Firestore (cloud) │  │  Google Sign-In             │
│  WeatherService    │  └────────────────────────────┘
└────────────────────┘
┌─────────────────────────────────────────────────────┐
│  Model Layer                                        │
│  GameItem · Achievement · GlobalUpgrade             │
│  GameplayModifier · LibraryEntry · GameSaveData     │
└─────────────────────────────────────────────────────┘
```

Platform-specific code (sensors, shaders, auth) is isolated in `androidMain`/`iosMain` behind Kotlin `expect`/`actual` declarations, keeping all business logic in shared `commonMain`.

See [`docs/architecture.md`](docs/architecture.md) for a full explanation of MVVM + Repository pattern and how each layer is implemented, or [`docs/clean-architecture-path.md`](docs/clean-architecture-path.md) for how it compares to Clean Architecture.

---

## Building

**Android**

```bash
# Debug APK
.\gradlew.bat :composeApp:assembleDebug       # Windows
./gradlew :composeApp:assembleDebug           # macOS / Linux
```

**iOS**

Open `iosApp/` in Xcode and run on simulator or device.

**Requirements**

- Android Studio Hedgehog or later (with KMP plugin)
- JDK 17+
- `google-services.json` placed in `composeApp/` (Firebase project required for auth and cloud features; the game works offline without it)

---

## Firebase setup (optional)

Cloud save, Google Sign-In, and player search require a Firebase project:

1. Create a project at [console.firebase.google.com](https://console.firebase.google.com)
2. Enable **Authentication** (Google provider) and **Firestore**
3. Download `google-services.json` and place it in `composeApp/`
4. Add your Web Client ID where indicated in `AndroidAuthRepository`

The game runs fully offline without these steps — Firebase features will be disabled.

---

## Documentation

- [`docs/architecture.md`](docs/architecture.md) — MVVM explained, how each layer is implemented, data flow walkthroughs
- [`docs/ui.md`](docs/ui.md) — screens, navigation, theming, sprite animation, particles
- [`docs/game-mechanics.md`](docs/game-mechanics.md) — vegetables, reward pipeline, upgrades, achievements, library
- [`docs/persistence.md`](docs/persistence.md) — GameSaveData contract, DataStore, cloud sync, weather API
- [`docs/platform.md`](docs/platform.md) — Kotlin Multiplatform, expect/actual, sensors, auth, permissions
- [`docs/clean-architecture-path.md`](docs/clean-architecture-path.md) — MVVM + Repository pattern compared to Clean Architecture, and how it could be extended

---

## License

**Source code** is licensed under the [GNU General Public License v3.0](LICENSE.txt).
You are free to study, modify, and distribute the code, but any distributed version must also be open source under the same license.

**Art assets** (sprites, illustrations, and UI graphics in `composeApp/src/commonMain/composeResources/drawable/`) are licensed under [Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International](composeApp/src/commonMain/composeResources/drawable/LICENSE.txt).
They may not be used commercially or modified without explicit permission from the author.
