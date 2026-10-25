# Vrddhih Roadmap

Every phase stays within the local-only rule: no accounts, no cloud, no sync, and all data on the device. Each phase ends with a review before the next one starts.

## Phase 0: Foundation and UI *(current)*

- Feature-oriented package structure in the existing `:app` module.
- A dark hunter-system design system with centralized tokens and reusable components.
- A dashboard built on clearly labelled mock data.
- Working six-tab bottom navigation with consistent placeholder screens.
- Project documentation (README, AGENTS, architecture, roadmap).
- Out of scope: persistence, a database, backup and restore.

## Phase 1: Room database and local data architecture

- Add Room (and KSP) with entities for quests, habits, goals, finance entries and player progress.
- Repositories expose `Flow`s to ViewModels. Replace `DashboardMockData` with real local state.
- Provide simple manual dependency wiring, adopting a DI framework only if wiring becomes painful.
- Plan schema versioning and migrations from the start.
- Decide the Android Auto Backup policy for the database: currently `allowBackup="true"` from the template.

## Phase 2: Tasks and recurring quests

- Create, edit, complete and delete quests, with priority, due date and XP reward.
- Recurring quests (daily, weekly, custom) and a daily quest board.

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
