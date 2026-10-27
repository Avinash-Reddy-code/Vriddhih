# Vrddhih Roadmap

Every phase stays within the local-only rule: no accounts, no cloud, no sync, and all data on the device. Each phase ends with a review before the next one starts.

## Phase 0: Foundation and UI *(done)*

- Feature-oriented package structure in the existing `:app` module.
- A dark hunter-system design system with centralized tokens and reusable components.
- A dashboard built on clearly labelled mock data.
- Working six-tab bottom navigation with consistent placeholder screens.
- Project documentation (README, AGENTS, architecture, roadmap).
- Out of scope: persistence, a database, backup and restore.

## Phase 1: Room database and local data architecture *(implemented, awaiting review)*

- Add Room (and KSP) with entities for quests, habits, goals, finance entries and player progress.
- Repositories expose `Flow`s to ViewModels. Replace `DashboardMockData` with real local state.
- Provide simple manual dependency wiring, adopting a DI framework only if wiring becomes painful.
- Plan schema versioning and migrations from the start.
- Android Auto Backup and device transfer are disabled (decided in Phase 1).
- A dashboard date strip and calendar dialog, plus a selected-day "Day log" read from Room.

## Phase 2: Tasks and recurring quests *(implemented, awaiting review)*

- Real task & quest CRUD engine backed by Room (definitions, occurrences, overrides, completions and activity log).
- Scope-aware recurring quest management (Single occurrence, This & future, All occurrences).
- Dynamic recurrence derivation (Daily, Weekdays, Weekly, Monthly with month-length & leap year clamping).
- Horizontal swipe navigation between all 6 primary destinations with synchronized bottom bar and back navigation.
- Swipeable 7-day date strip with forward/backward shifting and accessible navigation.
- Swipeable monthly calendar dialog with animated month transitions and arrow navigation.
- Dashboard integration with real active quests, date-aware headings, and distinct Day Log activity timeline.
- Quests screen with filter chips, category/title search, sorting, and hunter-themed dialogs.

## Phase 3: Goals and habits

- Habits with check-ins and streak calculation.
- Long-term goals with milestones and progress linked to quests and habits.

## Phase 4: XP, levels and achievements

- An XP ledger, a level curve, player titles and ranks.
- Achievements unlocked by local activity.

## Phase 5: Personal finance tracker

- A manual income and expense ledger with categories and monthly budgets.
- Spending summaries. No bank connections and no cloud.

## Phase 6: Reminders and notifications

- Local notifications for due quests and habits, using WorkManager and/or AlarmManager as needed.
- Notification permission handling and quiet hours.

## Phase 7: Statistics, reliability and release preparation

- Trends and reports across quests, habits, goals, XP and finance.
- Performance, accessibility and testing passes.
- A release build, signing, app icon and versioning.
