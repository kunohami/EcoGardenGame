# From MVVM + Repository to Clean Architecture

[`architecture.md`](architecture.md) covers what this project actually does: MVVM with the Repository pattern. This document is a side-by-side look at how that compares to **Clean Architecture** (Robert C. Martin), and what it would take to move further in that direction if the project keeps growing. 

---

## What Clean Architecture adds on top

Clean Architecture defines a strict **Dependency Rule**: source code dependencies may only point inward, across four layers:

```
Frameworks & Drivers  (UI, databases, network, Android APIs)
    ↓ depends on
Interface Adapters    (ViewModels, Repositories, data mappers)
    ↓ depends on
Use Cases             (application-specific business rules)
    ↓ depends on
Entities              (core business objects and rules)
```

Nothing in an inner layer references anything from an outer layer — entities don't know about databases, use cases don't know about Compose. The payoff is that the core business rules can be tested, reused, or ported to a different UI framework without touching them at all.

## What's already in place

This project already has the piece of Clean Architecture that tends to matter most in practice: **the data layer sits behind interfaces.** `GameRepository` and `AuthRepository` are abstractions in `commonMain`; `GameViewModel` depends on those, not on DataStore or Firebase directly. Swapping the storage backend means writing a new implementation and changing one injection point — no ViewModel changes required.

The `commonMain`/`androidMain` split does similar work for platform code: sensors, shaders, and Android auth SDKs are isolated behind `expect`/`actual` by construction, so they can't leak into shared logic.

## Where the dependency direction differs

A few spots in the current code take a more direct route than Clean Architecture would prescribe. These are worth knowing about, less because they're wrong and more because they're the exact places a future refactor would touch:

- **Model classes call Compose directly.** `GameItem` declares `@Composable fun Content()` and `@Composable fun ParticleEffect()`, and every vegetable implements them. In Clean Architecture terms, the entity layer would describe data only, and a separate composable in `ui/` would render it. For a project this size, having the vegetable own its own rendering keeps things colocated and easy to follow — the trade-off is that the model can't be reused outside a Compose context.
- **No dedicated Use Case layer.** Business logic — reward calculation, achievement checks, auto-save, weather bonuses — lives directly in `GameViewModel` and its managers rather than in standalone use-case classes. This keeps the call chain short and easy to trace for a single-ViewModel app, at the cost of the ViewModel doing more than just coordinating.
- **A couple of dependencies skip the interface step.** `WeatherManager` instantiates `WeatherService` directly, and `RewardCalculator` / `ArtRepository` are Kotlin `object` singletons. Fine for a codebase with one implementation of each; less convenient if you ever want to swap or mock them.

## If the project keeps growing

None of this needs to change for the game to keep working — these are options to reach for if the codebase outgrows its current shape, roughly in order of payoff:

1. **Move `@Composable` out of the model.** Let vegetable classes describe their data; add matching composables in `ui/` that know how to render each one. This flips the dependency so UI depends on model, not the reverse.
2. **Extract a Use Case layer.** Pull operations like "harvest vegetable," "purchase upgrade," and "check achievements" into their own classes (`HarvestVegetableUseCase`, `PurchaseUpgradeUseCase`, …). `GameViewModel` becomes a thin coordinator that calls use cases and maps results to UI state — easier to unit test in isolation.
3. **Add a `WeatherRepository` interface.** Give `WeatherManager` an abstraction over `WeatherService`, the same way `GameRepository` already abstracts persistence.
4. **Inject `RewardCalculator`.** Pass it in rather than reaching for the singleton, making its dependencies explicit and the class mockable in tests.

Each step is independent and additive — there's no need to do all four, or any of them, unless a concrete need (testing, reuse, a second UI target) shows up first.
