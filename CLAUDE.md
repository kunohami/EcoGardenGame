# EcoGardenGame — Claude Code Context

## What this project is

EcoGardenGame (internal codename: "Clicky's Garden") is a Kotlin Multiplatform idle/clicker game targeting Android and iOS. Players harvest virtual vegetables to earn coins, unlock new crops, purchase upgrades, and complete achievements. Each vegetable has a unique sensor or timing-based interaction mechanic.

## Architecture in one paragraph

MVVM + Repository pattern (not full clean architecture — see `docs/architecture.md` for why). The UI layer is Jetpack Compose Multiplatform (`ui/` screens). Business logic lives in `GameViewModel`, which delegates to three manager classes: `EconomyManager` (currency/inventory), `ProfileManager` (auth, Firebase profile sync), and `WeatherManager` (Open-Meteo API, auto-harvest). The data layer uses `GameRepository` (interface) backed by `DataStoreGameRepository` (Jetpack DataStore). Game entities implement the `GameItem` interface; all reward math flows through the `RewardCalculator` singleton. Firebase Firestore is used for cloud save and player-search. Platform-specific code (sensors, location, auth, shaders) lives in `androidMain`/`iosMain` behind `expect/actual` declarations.

## Key directories

```
composeApp/src/
  commonMain/kotlin/com/rafarg/ecogardengame/
    auth/          # AuthRepository interface + UserProfile
    data/          # GameRepository, DataStoreGameRepository, WeatherService, GameSaveData
    model/         # GameItem, BaseVegetable, 7 vegetable impls, Achievement, GlobalUpgrade, LibraryEntry
    ui/            # All Compose screens + theme, sprite sheet, wavy background
    util/          # Vibrator, ShakeDetector, LocationProvider, etc. (expect declarations)
    viewmodel/     # GameViewModel, EconomyManager, ProfileManager, WeatherManager, RewardCalculator
  androidMain/     # Concrete platform implementations
  iosMain/         # iOS implementations
```

## How to build

```bash
# Android debug APK (Windows)
.\gradlew.bat :composeApp:assembleDebug

# Android debug APK (macOS/Linux)
./gradlew :composeApp:assembleDebug

# iOS: open iosApp/ in Xcode and run
```

## Coding conventions

- All shared game logic goes in `commonMain`; never put business logic in `androidMain`/`iosMain`
- Platform-specific capabilities use `expect`/`actual` (see `util/`)
- Game state is reactive via Compose `mutableStateOf`; avoid side effects outside `viewModelScope`
- New vegetables must implement `GameItem` (or extend `BaseVegetable`) and be registered in `App.kt`'s item list
- `GameSaveData` is the serialization contract — add fields there when adding persisted state
- No Room database; persistence is Jetpack DataStore (key-value)

## External services

| Service | Purpose | Config location |
|---|---|---|
| Firebase Auth | Google Sign-In | `google-services.json` (not committed) |
| Firebase Firestore | Cloud save, player search | same |
| Open-Meteo API | Weather data | `WeatherService.kt`, no API key needed |
| Google Play Services | Location, Google Sign-In credential | Manifest + `AndroidAuthRepository` |

## Docs

| File | Contents |
|---|---|
| `docs/architecture.md` | MVVM pattern — explained from first principles, how this project maps to it |
| `docs/ui.md` | Screens, navigation, theming, sprite animation, particles |
| `docs/game-mechanics.md` | Vegetables, reward flow, RewardCalculator, upgrades, achievements, library |
| `docs/persistence.md` | GameSaveData contract, DataStore, cloud sync, weather API |
| `docs/platform.md` | Kotlin Multiplatform, expect/actual, sensors, auth, permissions |
| `docs/clean-architecture-path.md` | MVVM + Repository pattern compared to Clean Architecture, and how it could be extended |
| `docs/ci-cd.md` | GitHub Actions CI, ktlint/detekt, pre-PR checklist, git/PR workflow, testing scope |
