# Platform Layer

EcoGardenGame targets Android and iOS from a single shared codebase using **Kotlin Multiplatform (KMP)**. This document explains how platform differences are handled, what sensors and hardware capabilities are used, and how authentication works.

---

## Kotlin Multiplatform — shared vs platform-specific

The project source is split into three source sets:

| Source set | Compiled for | Contains |
|---|---|---|
| `commonMain` | Both platforms | All game logic, UI, ViewModels, data layer |
| `androidMain` | Android only | Android-specific implementations |
| `iosMain` | iOS only | iOS-specific implementations |

The guiding rule: **business logic and UI live in `commonMain`**. Only code that genuinely cannot be shared (hardware sensor APIs, platform auth SDKs, shader languages) lives in platform source sets.

---

## expect / actual — the abstraction mechanism

When `commonMain` needs to use a capability that is implemented differently per platform, Kotlin's `expect`/`actual` mechanism is used.

In `commonMain`, you declare what you need (the `expect`):

```kotlin
expect class ShakeDetector {
    fun start(onShake: () -> Unit)
    fun stop()
}
```

In `androidMain` and `iosMain`, you provide the implementation (the `actual`):

```kotlin
// androidMain
actual class ShakeDetector(private val sensorManager: SensorManager) {
    actual fun start(onShake: () -> Unit) {
        // register accelerometer listener
    }
    actual fun stop() { ... }
}
```

From `commonMain`'s point of view, `ShakeDetector` just works. The platform detail is fully contained in the platform source set. This is the same principle as the Repository pattern applied to hardware.

---

## Platform abstractions in this project

| expect declaration | Android implementation | Purpose |
|---|---|---|
| `Vibrator` | `VibratorManager` + `VibrationEffect` | Haptic feedback on vegetable clicks and events |
| `ShakeDetector` | `SensorManager` (TYPE_ACCELEROMETER) | Garlic shake-to-harvest mechanic |
| `ProximityDetector` | `SensorManager` (TYPE_PROXIMITY) | Purple Onion proximity bonus |
| `RotationDetector` | `SensorManager` (TYPE_ROTATION_VECTOR) | Apple tilt bonus |
| `LocationProvider` | `FusedLocationProviderClient` | Fetches lat/lon for weather API |
| `WavyBackground` | AGSL shader composable | Animated background for the Wavy theme |
| DataStore factory | `Context`-based DataStore path | Platform-specific file path for DataStore |

---

## Android entry point — MainActivity

`MainActivity` extends `ComponentActivity` and is the Android-side entry point. It is responsible for everything that needs an Android `Context` before the Compose tree exists:

- Initialising Firebase
- Creating the `DataStore` instance
- Instantiating hardware detectors (`ShakeDetector`, `ProximityDetector`, `RotationDetector`)
- Registering the Google Sign-In Activity Result launcher
- Launching the root `App()` composable via `setContent`

All of these are then passed as constructor parameters into `GameViewModel`, keeping platform initialisation out of shared code.

---

## Authentication

### AuthRepository (interface, `commonMain`)

```kotlin
interface AuthRepository {
    val currentUser: Flow<UserProfile?>
    suspend fun signInWithGoogle(): Result<UserProfile>
    suspend fun signOut()
}
```

`ProfileManager` (inside `GameViewModel`) depends on this interface. The shared code only knows about `UserProfile` — it never touches Firebase SDK types directly.

### AndroidAuthRepository (`androidMain`)

The Android implementation:

1. Calls the Google Sign-In credential picker via the Activity Result API registered in `MainActivity`
2. On success, creates a `GoogleAuthProvider` credential and links it to Firebase Auth
3. Exposes the current user as a `Flow<UserProfile?>` by combining Firebase's own auth state flow with a manual `MutableStateFlow` — the manual flow bridges the gap between the Activity Result callback and Firebase's async state update

`UserProfile` carries: `id` (Firebase UID), `name`, `email`, `photoUrl`.

---

## Android permissions

Declared in `AndroidManifest.xml`:

| Permission | Required for |
|---|---|
| `INTERNET` | Weather API, Firebase Firestore, Firebase Auth |
| `VIBRATE` | Haptic feedback |
| `ACCESS_COARSE_LOCATION` | Weather lookup (cell-tower precision is sufficient) |
| `ACCESS_FINE_LOCATION` | More precise GPS coordinates for weather |

Location permissions are requested at runtime before `LocationProvider.getLocation()` is called. The app functions without location — weather features are simply unavailable.
