# Game Mechanics

This document covers the game's entities and rules: how vegetables are structured, what makes each one unique, how rewards are calculated, and how progression (upgrades, achievements, library) is tracked.

---

## The GameItem interface

Every harvestable crop in the game implements the `GameItem` interface. This is the contract that makes the crop list extensible — `GameViewModel` works with a `List<GameItem>` and never references a specific vegetable class.

```kotlin
interface GameItem {
    val id: String
    val name: StringResource
    val resource: DrawableResource       // sprite sheet image
    val price: Int                       // coin cost to unlock in the Store
    val unlockCost: Int                  // alias used in some contexts
    val baseRewards: List<Reward>        // what one click yields before modifiers
    val modifiers: List<GameplayModifier>
    val particleEmoji: String
    val tutorialRes: DrawableResource

    @Composable fun Content(...)         // renders the vegetable in GameScreen
    @Composable fun ParticleEffect(...)  // renders flying reward particles
}
```

### BaseVegetable

`BaseVegetable` is an abstract class that provides sensible defaults so vegetable subclasses only need to override what makes them unique:

- `Content()` — a clickable composable with a scale-down-on-press animation
- `ParticleEffect()` — spawns `FlyingParticle` instances from the reward list
- Default base rewards: 1 fruit + 1 coin per click

All seven vegetables extend `BaseVegetable` and override `Content()` to implement their special mechanic.

---

## Vegetables

### Tomato — precision timing

The tomato pulses on a 5-second cycle. The pulse phase is tracked frame-by-frame using `withFrameMillis`. When the player clicks during the final 10% of the cycle (≥ 90% completion), it counts as a **critical hit** and multiplies the reward. Clicking outside this window gives a normal reward.

This mechanic rewards patience and attention. The Tomato Haptic Timing modifier (purchasable) adds a vibration cue when the critical window opens.

**Sensor / API:** `withFrameMillis` (Compose frame callback, no hardware sensor)

---

### Broccoli — click combo

The broccoli tracks consecutive clicks. Each click within a short time window of the previous one increments a combo counter; the reward scales with the combo. Waiting too long resets the counter to zero.

This rewards rhythmic, sustained clicking rather than raw speed.

**Sensor / API:** Kotlin `Clock.System.now()` timestamp comparison

---

### Bell Pepper — rain bonus

The bell pepper has no active mechanic on its own. When the weather bonus is enabled and current weather is rainy, the bell pepper gets a passive reward multiplier on every click.

The rain state is read from `WeatherManager` by `RewardCalculator` at reward-calculation time.

**Sensor / API:** `WeatherManager` / Open-Meteo API

---

### Garlic — shake to harvest

Shaking the device triggers a bonus harvest on the garlic. The `ShakeDetector` monitors accelerometer data and fires a callback when the shake threshold is exceeded. `GameViewModel` wires this callback to `onVegetableClick()` when garlic is the active crop.

**Sensor / API:** Android `SensorManager` (TYPE_ACCELEROMETER) via `ShakeDetector`

---

### Purple Onion — proximity bonus

When the player holds the phone close to their face (closer than the proximity sensor's threshold), the purple onion activates a reward multiplier. The `ProximityDetector` emits a boolean state — near or far — and `GameViewModel` uses it as a bonus flag during reward calculation.

**Sensor / API:** Android `SensorManager` (TYPE_PROXIMITY) via `ProximityDetector`

---

### Squash — speed streak

The squash tracks click velocity. If clicks arrive faster than a threshold, a streak counter increments. Sustaining the streak keeps the multiplier active; slowing down resets it. This rewards the opposite of the broccoli — raw speed over rhythm.

**Sensor / API:** Timestamp delta between consecutive clicks

---

### Apple — rotation bonus

Tilting the device past an angle threshold activates a bonus multiplier for the apple. The `RotationDetector` reads the rotation vector sensor and emits the current tilt angle. `GameViewModel` checks this angle during reward calculation.

**Sensor / API:** Android `SensorManager` (TYPE_ROTATION_VECTOR) via `RotationDetector`

---

## Reward flow

Every click goes through this pipeline:

```
User taps vegetable
  → GameItem.baseRewards         (base: 1 fruit + 1 coin)
  → RewardCalculator.calculate() (applies all modifiers)
  → EconomyManager.addRewards()  (increments state)
```

### RewardCalculator

`RewardCalculator` is a Kotlin `object` (singleton). It is stateless — it takes inputs and returns a modified reward list. The modifiers it applies, in order:

1. **Lucky Harvest upgrade** — rolls a random number against a 1–5% chance (scales with upgrade level). On success, multiplies the entire reward by 10. The resulting particle displays with a special "lucky" flag.

2. **Precise Harvest upgrade** — every N clicks (N decreases as upgrade level increases), the reward is doubled. The ViewModel tracks the total click count; `RewardCalculator` checks whether the current click is a milestone.

3. **Temperature bonus** — reads the current temperature from `WeatherManager`. Certain crops have hardcoded temperature ranges that grant a multiplier (e.g. garlic bonuses in cold weather).

4. **Sunny bonus** — if weather is clear/sunny and the weather bonus is active, all crops get a small photosynthesis multiplier.

5. **Snow bonus** — garlic gets an additional multiplier when weather is snowy.

6. **Rain bonus** — bell pepper gets its multiplier when weather is rainy.

---

## Upgrades

### GlobalUpgrade

Global upgrades are cross-vegetable improvements purchasable in the Store. Each has a maximum of 5 levels; cost scales as `baseCost × currentLevel`.

| Upgrade | Effect | Max level |
|---|---|---|
| Precise Harvest | Doubles reward at every N clicks (N decreases per level) | 5 |
| Lucky Harvest | 1–5% chance for 10× reward on any click | 5 |
| Weather Bonus | Unlocks weather mechanics entirely | 1 |

### GameplayModifier

Per-vegetable purchasable enhancements. Each modifier targets a specific vegetable and adds a toggleable behaviour. Examples:

- **Tomato Haptic Timing** — vibrates when the critical window opens
- **Broccoli Touch Precision** — widens the combo time window
- **Garlic Sensor Sensitivity** — lowers the shake threshold

Modifiers have an `isUnlocked` flag (whether purchased) and an `isEnabled` flag (whether active). Both are persisted in `GameSaveData`.

---

## Achievements

Achievements are defined as a list of `Achievement` objects. Each one carries:

- A display name and description
- A `checkEarned(GameSaveData) -> Boolean` lambda that inspects save data to determine if the milestone is met

After every reward event, `GameViewModel` iterates the achievement list, calls `checkEarned()` on each unearned achievement, and if one now returns `true`, marks it earned, triggers a toast notification, and saves.

### Achievement categories

| Category | Examples |
|---|---|
| Collection | Master Gardener (all vegetables), Geneticist (all modifiers), Shopaholic (all upgrades), Art Collector (all gallery pieces) |
| Click milestones | 5 K, 10 K, 25 K, 50 K total clicks |
| Earnings | 50 K, 100 K total coins earned |
| Skill | Sonic Squash (sustain 10+ speed streak), Tomato Sniper (50+ critical hits) |

---

## Library

The Library screen offers 70 purchasable educational facts — 10 per vegetable — at escalating coin costs (100 to 2500 coins each). Each `LibraryEntry` has an `isUnlocked` boolean persisted in `GameSaveData`. This is a pure progression/collection layer with no effect on gameplay.

---

## Art Gallery

`ArtRepository` (a singleton `object`) defines 20 collectible art pieces, each with a drawable resource, a string key, and a coin cost (500–5000). Purchasing a piece sets its unlocked flag in `GameSaveData`. Unlocking all 20 earns the Art Collector achievement.
