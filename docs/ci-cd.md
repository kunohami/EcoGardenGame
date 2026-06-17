# CI/CD, Git Workflow, and Testing

This project didn't have any of this when development started — no tests, no CI, no
branch rules. This doc covers what exists now and how to work with it day to day.

## What the CI workflow checks

`.github/workflows/android_ci.yml` runs on every push and pull request targeting
`main`, on a single `ubuntu-latest` job:

1. **`ktlintCheck`** — formatting/style. Two of ktlint's standard rules
   (`no-wildcard-imports`, `discouraged-comment-location`) are disabled in
   `.editorconfig` because they fight this codebase's existing conventions (wildcard
   imports for model/sibling types, inline trailing comments on call arguments) rather
   than catching real problems.
2. **`detekt`** — static analysis (unused code, complexity, smells). Runs against
   `composeApp/detekt-baseline.xml`, which excuses every issue that existed when detekt
   was introduced. Only *new* issues fail the build — the baseline is not meant to grow;
   if you fix something that happens to remove a baselined issue, regenerate the
   baseline (see below) rather than leaving stale entries.
3. **Unit tests** — `:composeApp:testDebugUnitTest`, which runs both `commonTest` and
   `androidUnitTest` on the JVM. This intentionally avoids the `allTests` aggregate task,
   which would also try `iosSimulatorArm64Test` and needs a macOS runner.
4. **`assembleDebug`** — confirms the app actually builds, not just that it lints clean.

iOS isn't built or tested in CI yet — this machine and workflow target Android first.
Adding a `macos-latest` job for iOS is a reasonable follow-up but is out of scope for
now.

CI needs `composeApp/google-services.json`, which is gitignored and not committed (see
`CLAUDE.md`). The workflow decodes it from the `GOOGLE_SERVICES_JSON_B64` repo secret
before building. If that file is ever rotated, re-encode and update the secret:

```bash
base64 -w0 composeApp/google-services.json | gh secret set GOOGLE_SERVICES_JSON_B64 --repo kunohami/EcoGardenGame
```

## Pre-PR checklist

Run these locally before opening a PR, in order:

```bash
./gradlew ktlintFormat   # auto-fixes what it can
./gradlew ktlintCheck    # fails on anything it couldn't auto-fix
./gradlew detekt         # static analysis against the baseline
./gradlew :composeApp:testDebugUnitTest
./gradlew :composeApp:assembleDebug
```

If you touched logic that detekt would reasonably flag in code that's already in the
baseline, fix it forward rather than leaving it excused.

### Regenerating the detekt baseline

Only do this when deliberately accepting new pre-existing debt (e.g. after a large
refactor), not as a way to dodge a real finding:

```bash
./gradlew detektBaseline
```

### A local JDK 25 note

detekt 1.23.x bundles a Kotlin compiler that doesn't know how to parse very new JDK
version strings (e.g. plain `25`) and will fail with a cryptic
`IllegalArgumentException: 25`. This only affects local runs on a JDK newer than what
detekt expects — CI pins Temurin 17 via `actions/setup-java`, so it's unaffected. If you
hit this locally, point Gradle at a JDK 17 install for that command:

```bash
./gradlew detekt -Dorg.gradle.java.home="<path to a JDK 17 install>"
```

## Git workflow and PR rules

- Configure git to rebase on pull instead of creating merge commits:
  ```bash
  git config --global pull.rebase true
  ```
- **Required CI check**: `main` is protected — the `build_and_test` status check from
  the workflow above must pass before a PR can merge. This is enforced by GitHub itself
  (branch protection), not just a convention, since the repo is public.
- **Squash merge only**: merge commits and rebase-merge are disabled at the repo level.
  Every PR collapses into a single commit on `main`.
- **Auto-delete branches**: merged PR branches are deleted automatically by GitHub.
- Branch protection currently does *not* require a PR review (this is a solo project)
  and admins can still push directly past the required check in a genuine emergency —
  but treat that as an escape hatch, not the normal path.

## Testing scope today

`RewardCalculator` (`composeApp/src/commonMain/kotlin/com/rafarg/ecogardengame/viewmodel/RewardCalculator.kt`)
is the only manager-layer logic with real unit test coverage
(`composeApp/src/commonTest/kotlin/com/rafarg/ecogardengame/viewmodel/RewardCalculatorTest.kt`).
It's pure logic — no Firebase, Ktor, or Android dependencies — which is exactly why it
was the first target.

`GameViewModel`, `EconomyManager`, `ProfileManager`, and `WeatherManager` all pull in
Firebase Auth/Firestore, Ktor, or platform `expect/actual` code, and aren't covered yet.
Testing them properly needs fake implementations of `AuthRepository`/`GameRepository`
and a fake `HttpClient` engine for `WeatherService` — that's future work, not something
to bolt on ad hoc. Don't add tests for those classes that secretly hit real Firebase or
the network; that would make CI flaky and slow.
