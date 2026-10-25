# AGENTS.md — Vrddhih

Guidance for coding agents (Devin and others) working in this repository.

## Application identity

- Name: **Vrddhih**, a native Android personal-growth app with a Solo Leveling-inspired "hunter system" look.
- Package / namespace / applicationId: `com.vrddhih.app`
- Single module: `:app`
- Stack: Kotlin, Jetpack Compose, Material 3, Navigation Compose, AndroidX ViewModel, Gradle Kotlin DSL with a version catalog (`gradle/libs.versions.toml`).
- Toolchain: AGP 9.4.1 (built-in Kotlin, KGP 2.2.10), Gradle 9.6.0 wrapper, Gradle daemon JVM toolchain 25, compileSdk/targetSdk 37, minSdk 26, Java source/target 11.

## Data requirement: local only (non-negotiable)

Vrddhih is a personal app. Two people each run their own independent install.

- No login, signup, accounts or authentication.
- No Firebase, cloud backend, remote database, sync or analytics SDKs.
- No shared tasks, expenses, profiles, leaderboards or progress between installs.
- All personal data stays on the device. The app must work fully offline.
- Do not add the `INTERNET` permission or network libraries without explicit approval.
- Backup and restore are out of scope until explicitly planned.

## Architecture conventions

- Feature-oriented packages under `com.vrddhih.app`:
  - `core/theme` holds design tokens (colors, brushes, typography, spacing, radii, shapes).
  - `core/navigation` holds the top-level destinations, NavHost and bottom bar.
  - `core/ui/components` holds reusable Compose components (panels, headers, progress, tags, placeholders).
  - `features/<feature>` holds the screens, UI state and ViewModels for one feature.
- MVVM: a screen is split into a `*Route` (gets the ViewModel and collects state) and a stateless `*Screen` (UI only, previewable).
- Keep business logic and derived values in UI-state classes or ViewModels, not in composables.
- Expose state as `StateFlow` and collect with `collectAsStateWithLifecycle()`.
- Add a ViewModel only when a screen has state worth holding. Add repositories once real data exists (Phase 1). Do not add use cases, DI frameworks or interfaces for hypothetical services ahead of need.
- Mock data lives only in clearly named `mock` packages (currently `features/dashboard/mock/DashboardMockData.kt`), with IDs prefixed `sample-`. UI backed by mock data must show `MockDataBadge`.

## Coding standards

- Use theme tokens (`VrddhihColors`, `VrddhihBrushes`, `VrddhihSpacing`, `VrddhihRadii`, `VrddhihDimens`, `VrddhihTextStyles`, `MaterialTheme.typography`). Do not hard-code colors or spacing in screens.
- Put user-facing copy in `res/values/strings.xml`. Mock data content is the exception.
- Composables take a `modifier: Modifier = Modifier` parameter, and state is hoisted.
- Touch targets must be at least 48dp. Give interactive rows proper semantics (for example `toggleable(role = Role.Checkbox)`).
- Default to no comments. Write KDoc only where intent is not obvious from the names.
- Do not upgrade AGP, Kotlin, Gradle or SDK versions without a demonstrated compatibility need. The `kotlin` catalog version must match the KGP that AGP bundles.

## Design direction

- Dark only: near-black and deep-navy backgrounds, with electric-blue and icy-blue accents.
- "Status window" panels have gradient surfaces, thin accent-gradient borders and an optional soft corner glow (`SystemPanel(glow = true)`, used sparingly).
- Uppercase, letter-spaced system labels; monospace numeric readouts.
- Animations stay restrained: fades between tabs and short progress/colour transitions. No blur and no heavy effects.
- Use original compositions only. No anime screenshots, extracted art or remote images.

## Development workflow

Commands run from the project root. Android Studio's bundled JBR works as `JAVA_HOME`:

```sh
export JAVA_HOME=~/Applications/android-studio/jbr   # fish: set -x JAVA_HOME ~/Applications/android-studio/jbr
./gradlew assembleDebug
./gradlew test
./gradlew installDebug   # needs a connected device or emulator
```

- Check `git status` before editing. Never reset, clean, force-push or rewrite history. Commit only when asked.
- Work phase by phase as listed in `docs/ROADMAP.md`, and stop for review at the end of each phase.
- Report build and test results accurately. Never claim device or emulator verification that did not happen.
