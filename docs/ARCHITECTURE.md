# Vrddhih Architecture

## Scope statement

Vrddhih is a single-user, on-device application. **Cloud synchronization, remote storage, accounts and backup/restore are not part of the current scope.** Each install is fully independent.

## Package structure

```
com.vrddhih.app
├── MainActivity            # ComponentActivity: enableEdgeToEdge + setContent
├── VrddhihApp              # App shell: Scaffold + bottom bar + NavHost
├── core
│   ├── theme               # VrddhihColors, VrddhihBrushes, VrddhihTypography, VrddhihTextStyles,
│   │                       # VrddhihSpacing, VrddhihRadii, VrddhihDimens, VrddhihTheme
│   ├── navigation          # TopLevelDestination, VrddhihNavHost, navigateToTopLevel, VrddhihBottomBar
│   └── ui/components       # SystemPanel, ScreenHeader, SectionHeader, SystemProgressBar,
│                           # ProgressRing, SystemTag, MockDataBadge, SystemIconBadge, FeaturePlaceholder
└── features
    ├── dashboard           # DashboardRoute/Screen, DashboardPanels, DashboardViewModel, DashboardUiState
    │   └── mock            # DashboardMockData (mock-data boundary)
    ├── quests | habits | goals | finance | statistics   # Placeholder screens
```

Dependencies point inward: features use `core`, and `core/navigation` is the only `core` package that references feature screens, because it wires the graph.

A `profile` feature was suggested but not created yet. The player title and level currently appear on the dashboard, and an empty package would add nothing.

## UI-state approach

- **Route/Screen split.** `DashboardRoute` obtains `DashboardViewModel` through `viewModel()` and collects `uiState` with `collectAsStateWithLifecycle()`. `DashboardScreen` is stateless and previewable.
- **Immutable state.** `DashboardUiState` is a data class. Derived values (completed and remaining counts, habit progress, level progress, goal percent, deadline urgency) are computed properties there, so composables contain no business logic and the values are unit-tested.
- **Events.** UI events are plain lambdas (`onToggleQuestPreview`) that call ViewModel functions.
- **ViewModels only where useful.** Only the dashboard has state today. The placeholder screens are plain composables.
- **Design system.** All colors, spacing, radii and type come from `core/theme`, and screens compose `core/ui/components`.

## Navigation strategy

- Navigation Compose with string routes defined once in the `TopLevelDestination` enum, which also holds labels and icons.
- `VrddhihNavHost` maps each route to exactly one screen. Transitions are short cross-fades.
- `navigateToTopLevel` pops to the start destination with `saveState = true`, uses `launchSingleTop = true` and `restoreState = true`. This prevents duplicate copies of a tab on the back stack, and each tab keeps its state, including the dashboard ViewModel and scroll position, when you switch away and back.
- The selected tab comes from the current back-stack entry's destination hierarchy (`isInHierarchy`).
- Nested graphs per feature can be added later when features gain detail screens. The top-level contract stays the same.

## Mock-data boundary

- `features/dashboard/mock/DashboardMockData` is the **only** source of sample values. Every ID is prefixed `sample-`.
- The `*Preview` models (`QuestPreview`, `HabitPreview`, `GoalPreview`, `DeadlinePreview`) are display models, not persistence entities.
- Every dashboard panel shows a `MockDataBadge`, and a preview notice sits at the top of the dashboard.
- Quest toggling is held in memory by `DashboardViewModel` and is labelled as temporary. Nothing is written to disk.
- Phase 1 removes the `mock` package and feeds `DashboardUiState` from repositories. The screen layer should not need to change.

## Future local-only database plan (Phase 1+)

- **Room** (SQLite) inside the app's private storage as the single source of truth, with entities for quests (including recurrence), habits and check-ins, goals and milestones, finance entries, and an XP ledger.
- **Repositories** expose `Flow`s and are injected into ViewModels through simple manual wiring.
- **Migrations** are versioned from the first schema. Schema JSON is exported for review.
- **No network.** The app declares no `INTERNET` permission today, and the data layer will not add one.
- **Backup.** The template's `android:allowBackup="true"` and backup rules are still in place. Before real data exists (Phase 1), decide whether to disable Android Auto Backup or restrict it, so personal data never leaves the device unintentionally.
