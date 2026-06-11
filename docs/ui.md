# UI Layer

All UI is written in Jetpack Compose Multiplatform. There are no XML layouts, no Fragments, and no traditional View system. The root composable is `App.kt`; everything else is composed inside it.

This document covers navigation, the screen inventory, theming, and the animation systems.

---

## Entry point

`App.kt` is responsible for three things:

1. **Instantiating `GameViewModel`** with its platform dependencies (repository, auth, sensors)
2. **Applying the active theme** — wraps the entire composition in `MaterialTheme` using the color scheme stored in the ViewModel
3. **Owning the navigation structure** — holds the pager state and renders the bottom navigation bar

---

## Navigation

Navigation is built on a `HorizontalPager` with a custom **3D cube transition effect**. As the user swipes between screens, each page applies a `rotationY` transform and a perspective offset, making the pages look like faces of a rotating cube.

There is no NavHost, no back stack library, and no route strings. The active screen is a single integer index held in `GameViewModel`. Bottom bar taps and swipes both update this index; the pager animates to the new page.

### Primary screens (bottom bar)

These five screens are always reachable via the bottom navigation bar:

| Screen | File | Purpose |
|---|---|---|
| Game | `GameScreen.kt` | Main click interaction, active vegetable, money counter, weather shortcut |
| Store | `StoreScreen.kt` | Buy vegetables, per-vegetable modifiers, global upgrades |
| Library | `LibraryScreen.kt` | Purchase educational facts about each vegetable |
| Profile | `ProfileScreen.kt` | Avatar, username, public profile view, cloud sync |
| Misc | `MiscScreen.kt` | Hub that links to all secondary screens |

### Secondary screens (overlays)

Launched from the Misc screen or from contextual buttons. They slide over the primary navigation rather than replacing it.

| Screen | Purpose |
|---|---|
| `SettingsScreen` | Toggle vibration, language, theme |
| `StatsScreen` | Game statistics, click counts, earnings, achievement progress |
| `ThemesScreen` | Select one of four visual themes |
| `LoginScreen` | Google Sign-In interface |
| `GalleryScreen` | Art collection — shows locked/unlocked status of all 20 pieces |
| `WeatherScreen` | Live weather display, location enable/disable, bonus activation |
| `TutorialScreen` | Onboarding walkthrough shown only to new players |
| `AboutScreen` | Credits and app information |

---

## Theming

Four Material 3 color schemes are defined in `Theme.kt`:

| Theme | Character |
|---|---|
| Light | Clean, bright default |
| Dark | Dark mode |
| Wavy | Teal-toned with the animated shader background |
| Autumn Woods | Warm orange and brown palette |

The active theme ID is a `mutableStateOf` in `GameViewModel` and persisted in `GameSaveData`. When it changes, the `MaterialTheme` wrapper at the top of `App.kt` re-renders with the new color scheme and the entire UI picks up the new colors automatically — no manual updates anywhere.

The Wavy theme additionally enables `WavyBackground`, an animated AGSL (Android Graphics Shading Language) shader composable defined in `androidMain`. On iOS, a fallback static background is used instead.

---

## Sprite animation

`SpriteSheet.kt` provides a frame-based sprite animator. It takes a single horizontal bitmap strip (all animation frames in one image) and a frame count, then cycles through frames on a coroutine-driven timer.

This is used for the vegetable idle animations — each vegetable has a sprite sheet that plays while the player is on the Game screen.

---

## Particle effects

Every `GameItem` implementation includes a `ParticleEffect()` composable. When a vegetable is clicked and rewards are generated, the ViewModel pushes `FlyingParticle` instances into a `mutableStateListOf`.

Each particle:

- Displays the vegetable's emoji and the reward amount (e.g. `+5`)
- Floats upward over ~1.2 seconds using `Animatable<Float>`
- Fades in (300 ms) → holds → fades out (400 ms)
- Removes itself from the list when the animation completes

A maximum of 20 particles can be active simultaneously to keep rendering cost bounded.

---

## Achievement toasts

When an achievement is earned for the first time, `GameViewModel` emits it into a `mutableStateListOf<Achievement>`. `App.kt` observes this list and displays a toast composable that slides in, holds for a few seconds, and slides out — all driven by `Animatable`.

---

## Speech bubbles and custom shapes

`SpeechBubbleShape.kt` defines a custom `Shape` used for the tutorial character's dialogue. It draws a rounded rectangle with a triangular pointer tab, implemented via `Path` operations on the Compose canvas.
