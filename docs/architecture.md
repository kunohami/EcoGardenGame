# Architecture — MVVM

EcoGardenGame is built on **MVVM (Model-View-ViewModel)**, a pattern that enforces a strict separation between the user interface, the business logic, and the data. This document explains what MVVM is, why it exists, and exactly how this project implements it.

---

## The problem MVVM solves

Without any architecture pattern, it's easy to end up with screens that do everything: they fetch data from the network, run game calculations, manage state, and draw UI all in the same place. This works for small prototypes but quickly becomes impossible to maintain. 

MVVM solves this by splitting the codebase into three layers with a clear rule: **each layer can only talk to the layer below it, never above it.**

---

## The three layers

### View — "what the user sees"

The View layer is responsible for **rendering the UI and nothing else**. It does not contain game rules, math, or data access. It reads state from the ViewModel and sends user events back up to it.

In this project, every `*Screen.kt` file is a View. They are written in Jetpack Compose — functions that describe what the screen looks like given the current state.

A screen never calculates a reward, never reads from disk, and never knows whether the user is logged in. It only displays what it is told.

### ViewModel — "the middleman"

The ViewModel sits between the View and the data. Its two jobs are:

1. **Hold state** — everything the UI needs to display, in a form the UI can react to
2. **Handle events** — when the user does something (taps a vegetable, buys an upgrade), the ViewModel receives that event, runs the relevant logic, and updates state

The ViewModel survives configuration changes (like screen rotation) so UI state is never lost. It owns coroutines for asynchronous work (saving to disk, fetching weather) and cancels them automatically when the app is destroyed.

Critically, the ViewModel **does not know what the View looks like**. It exposes state and accepts events, but has no reference to any composable or Android View.

### Model — "the data and the rules"

The Model layer is everything that is not UI and not coordination. It includes:

- **Game entities** — the vegetable classes, achievements, upgrades, rewards
- **Repository** — the interface through which the ViewModel reads and writes data, without caring whether that data comes from disk, a database, or the cloud
- **Data classes** — `GameSaveData`, `UserProfile`, `Reward`, etc.

The Model has no knowledge of the ViewModel or the View. It is pure logic and data.

---

## Unidirectional data flow

The layers communicate in a single direction:

```
  User taps something
        │
        ▼
   [ VIEW ]  ────── calls event function ──────▶  [ VIEWMODEL ]
                                                        │
                                                   runs logic
                                                   updates state
                                                        │
   [ VIEW ]  ◀───── reads state, re-renders ───────────┘
                                                        │
                                              reads / writes
                                                        │
                                                  [ MODEL ]
```

State only flows downward (Model → ViewModel → View). Events only flow upward (View → ViewModel → Model). There are no cycles, no callbacks going the wrong direction.

In Compose, this is implemented through `mutableStateOf`. When the ViewModel updates a `mutableStateOf` variable, Compose automatically identifies which composables read that variable and re-renders only those. The View never calls `notifyDataSetChanged()` or manually refreshes — it simply reads reactive state, and Compose handles the rest.

---

## How this project maps to MVVM

### View — the Compose screens

All screens live in `commonMain/…/ui/`. The root composable is `App.kt`, which owns the navigation structure and applies the active theme.

Screens receive state by reading directly from the ViewModel (passed as a parameter). They call functions on the ViewModel in response to user actions:

```kotlin
// Inside GameScreen — View reads state, calls event
val money = viewModel.money
Button(onClick = { viewModel.onVegetableClick(rewards) }) { ... }
```

The screen itself has no idea how money is calculated or saved.

See [`ui.md`](ui.md) for a full breakdown of screens, navigation, theming, and animations.

### ViewModel — GameViewModel and its managers

`GameViewModel` is the single ViewModel for the entire app. Because the game has substantial complexity, its responsibilities are split across three **manager classes** that `GameViewModel` owns and coordinates:

```
GameViewModel
  ├── EconomyManager      manages money, clicks, fruit counts, harvest totals
  ├── ProfileManager      manages auth state, username, Firebase profile sync, player search
  └── WeatherManager      manages weather API calls, bonus windows, auto-harvester
```

This is a **composition pattern**: instead of one enormous class, `GameViewModel` delegates specialised work to focused managers while remaining the single point of contact for all screens.

There is also `RewardCalculator` — a stateless singleton (Kotlin `object`) that contains all reward math. It is called by `GameViewModel.onVegetableClick()` and returns a modified reward list based on active upgrades and weather conditions. Because it holds no state, it is easy to reason about and test in isolation.

**What GameViewModel itself is responsible for:**

- Declaring all `mutableStateOf` / `mutableStateListOf` properties that screens observe
- Receiving events from screens and routing them to the right manager
- Triggering the auto-save coroutine after every state mutation
- Checking achievements after each reward event and emitting toast notifications
- Coordinating cloud save and load via Firestore

### Model — entities, repository, data

The Model layer in this project has three parts:

**Game entities** (`commonMain/…/model/`) — the `GameItem` interface and its seven vegetable implementations, `Achievement`, `GlobalUpgrade`, `GameplayModifier`, `LibraryEntry`, `Reward`, and `FlyingParticle`. These encode what exists in the game and how it behaves.

**Repository** (`commonMain/…/data/`) — `GameRepository` is a Kotlin interface with two methods:

```kotlin
interface GameRepository {
    suspend fun loadGameData(): GameSaveData
    suspend fun saveGameData(data: GameSaveData)
}
```

`GameViewModel` depends on this interface, not on any specific implementation. The actual implementation — `DataStoreGameRepository` — uses Jetpack DataStore and is injected at startup. This means the persistence strategy could be swapped (to a database, to a remote API) without touching the ViewModel or any screen.

**GameSaveData** — a plain data class that is the serialization contract. It is the snapshot of the complete game state written to disk and to Firebase. Every piece of persisted state must be a field in `GameSaveData`.

See [`persistence.md`](persistence.md) for implementation details on the data layer.

---

## The Repository pattern

The Repository is an extension of the Model layer that deserves its own mention. Its purpose is to give the ViewModel a **stable, abstract interface to data** regardless of where that data actually lives.

Without a Repository, the ViewModel would call DataStore APIs directly. If you later wanted to add a database or move to a remote backend, you would have to rewrite ViewModel code. With the Repository interface in place, you write a new implementation and swap it in at the injection point — no ViewModel changes required.

In this project, the injection happens in `App.kt`:

```kotlin
val repository = DataStoreGameRepository(dataStore)
val viewModel = GameViewModel(repository, authRepository, ...)
```

---

## The full structure

```
composeApp/src/commonMain/kotlin/com/rafarg/ecogardengame/
│
├── ui/                   ← VIEW
│   ├── App.kt            (root composable, navigation, theme)
│   ├── GameScreen.kt
│   ├── StoreScreen.kt
│   └── … (all other screens)
│
├── viewmodel/            ← VIEWMODEL
│   ├── GameViewModel.kt
│   ├── EconomyManager.kt
│   ├── ProfileManager.kt
│   ├── WeatherManager.kt
│   └── RewardCalculator.kt
│
├── model/                ← MODEL (game entities)
│   ├── GameItem.kt
│   ├── BaseVegetable.kt
│   ├── Tomato.kt … Apple.kt
│   ├── Achievement.kt
│   ├── GlobalUpgrade.kt
│   └── …
│
├── data/                 ← MODEL (data access)
│   ├── GameRepository.kt
│   ├── DataStoreGameRepository.kt
│   ├── GameSaveData.kt
│   └── WeatherService.kt
│
└── auth/                 ← MODEL (auth abstraction)
    └── AuthRepository.kt
```

---

## Further reading

- [`ui.md`](ui.md) — screens, navigation, theming, sprite animation, particles
- [`game-mechanics.md`](game-mechanics.md) — game entities, vegetable mechanics, reward math, upgrades, achievements
- [`persistence.md`](persistence.md) — GameSaveData, DataStore, cloud sync, weather API
- [`platform.md`](platform.md) — Kotlin Multiplatform, expect/actual, sensors, authentication
- [`clean-architecture-path.md`](clean-architecture-path.md) — how this MVVM + Repository setup compares to Clean Architecture, and what extending it further could look like
