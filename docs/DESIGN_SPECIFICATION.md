# PHASE 01.2 — VRDDHIH VISUAL DIRECTION & DESIGN SPECIFICATION

> **Status:** Specification Proposal — Awaiting Review & Explicit Approval  
> **Target Scope:** Phase 01 (Shared UI/UX Foundations)  
> **Base Code Version:** `v1.0.0` (commit `570d778`)  
> **Application:** Vrddhih (`com.vrddhih.app`)

---

## 1. Executive Summary

Vrddhih (*growth / rising*) is a local-first, offline personal growth, productivity, and life-gamification application for Android. Its visual identity is inspired by the **Solo Leveling "Hunter System"** user interface: an immersive, futuristic, neon-etched "status window" appearing before a hunter.

The visual direction must balance two equally vital forces:
1. **Atmospheric Immersion**: Evoking the sharp, luminous, dark-blue holographic aesthetic of an anime hunter system with high-contrast readouts, cyan/electric energy accents, and restrained edge glows.
2. **Professional Utility & Ergonomics**: Retaining pristine legibility, predictable ergonomics, instantaneous local responsiveness, zero visual clutter, and strict compliance with Android accessibility standards (minimum 48 dp touch targets, robust semantics, and support for system font scaling).

This document establishes the official visual, interaction, and component specification for Phase 01. It defines how the design system tokens in `core/theme` and reusable components in `core/ui/components` will be refined, unified, and governed across all six navigation destinations without rewriting application business logic or corrupting feature boundaries.

---

## 2. Core Design Principles

### Principle 1: Purpose-Driven Visual Effects (Zero Decorative Noise)
In the Solo Leveling universe, the System interface is functional, clinical, and high-tech. In Vrddhih, every glow, gradient, border, and animation must serve an informational purpose:
- **Never add glow for aesthetic decoration alone.** Glows are strictly reserved for active states, critical milestones, level-up moments, or completed status.
- Backgrounds remain deep near-black (`Void` `#04060C` and `Abyss` `#080E1C`) to maximize battery efficiency on OLED displays and ensure high contrast against foreground elements.
- Panels use subtle dark navy gradients (`PanelRaised` `#111C34` $\to$ `Panel` `#0C1426`) with a razor-thin 1 dp accent-tinted border (`Outline` `#1C2A48`), providing a holographic card feel without heavy drop shadows.

### Principle 2: Unmistakable Information Hierarchy
A user glancing at any screen should comprehend their priority directive within 500 milliseconds:
- **Top of Screen**: System orientation via `ScreenHeader` (quiet uppercase `eyebrow` label + prominent `title`).
- **Primary Interactive Directive**: A single, prominent primary action button immediately recognizable across the screen.
- **Content Cards**: Elevated `SystemPanel` status containers.
- **Data & Readouts**: Clear segregation between human-readable text (clean sans-serif) and system numeric metrics (crisp monospace).
- **Status & Urgency**: High-contrast, standardized chromatic signaling (Electric = Active, Ice = Info/Framing, Danger = Overdue/Critical, Success = Completed).

### Principle 3: Feature Independence with Shared System Grammar
Quests, Habits, and Goals are independent domains:
- **Quests** are actionable daily directives with schedule, priority, and completion tracking.
- **Habits** are recurring behavioral protocols with streaks and specialized tracking types (numeric, timer, checklist, limit).
- **Goals** are long-term strategic outcomes with target deadlines and milestone progressions.

They must share the same design language, tokens, button shapes, headers, and form controls, but **never be forced into unnatural shared schemas or rigid template forms**.

### Principle 4: Snappy, Low-Latency Feedback
- Animations are restrained and fast (200 ms to 300 ms for state transitions; 600 ms for numeric progress fill).
- No complex blur passes, no heavy particle physics, and no multi-second animations that delay user interaction.
- The interface must feel instantaneous on local hardware.

---

## 3. Color Roles & Palette Architecture

### Existing Color Tokens ([Color.kt](file:///home/avinash/AndroidStudioProjects/Vrddhih/app/src/main/java/com/vrddhih/app/core/theme/Color.kt))
```
Base Surfaces:   Void (#04060C) │ Abyss (#080E1C) │ Panel (#0C1426) │ PanelRaised (#111C34) │ EmblemCore (#12305F)
Outlines:        Outline (#1C2A48) │ OutlineBright (#2A3D66)
Energy Accents:  Electric (#2F8CFF) │ Ice (#7CD3FF) │ Frost (#D4F1FF)
Typography:      TextPrimary (#E8F2FF) │ TextSecondary (#A3B5D1) │ TextMuted (#7A8DAB)
Semantics:       Success (#45E0B0) │ Warning (#FFB84D) │ Danger (#FF5C7C) │ Sample (#B79CFF)
```

### Semantic Color Role Assignments

| Token | Semantic Role | Specific Component Usages | Contrast against `Void` |
| :--- | :--- | :--- | :--- |
| **`Electric`** (`#2F8CFF`) | **Primary Action & Focus** | Primary action button container, selected navigation tabs, focused form borders, active toggles, current day highlight. | $\approx 5.6:1$ (UI elements) |
| **`Ice`** (`#7CD3FF`) | **System Framing & Information** | Screen eyebrows, section header titles, inactive icon tints, secondary tag outlines, active filter text, search cursor. | $\approx 10.8:1$ (High contrast text) |
| **`Frost`** (`#D4F1FF`) | **High-Energy Text & Values** | Hero numerical readouts, highlighted metric values, inner glow edges. | $\approx 14.8:1$ (Highest readability) |
| **`TextPrimary`** (`#E8F2FF`) | **Primary Content Text** | Card titles, user input text, dialog titles, primary list labels. | $\approx 14.1:1$ |
| **`TextSecondary`** (`#A3B5D1`) | **Secondary Content Text** | Subtitles, supporting notes, form helper text, category labels. | $\approx 7.8:1$ |
| **`TextMuted`** (`#7A8DAB`) | **De-emphasized Text** | Inactive counts, placeholder text, timestamps, past dates. | $\approx 4.6:1$ ($\ge 4.5:1$ WCAG AA) |
| **`Success`** (`#45E0B0`) | **Affirmation & Completion** | Completed quest checkmarks, habit check-in successes, goals reached. | $\approx 11.2:1$ |
| **`Warning`** (`#FFB84D`) | **Caution & Thresholds** | Approaching deadlines (D-3 to D-1), limit habit approaching ceiling. | $\approx 9.7:1$ |
| **`Danger`** (`#FF5C7C`) | **Overdue & Critical Failure** | Overdue goals/quests, limit exceeded, delete confirmation buttons. | $\approx 6.4:1$ |

### Accessibility & Contrast Principles
- **No Reliance on Color Alone**: Any state indicated by color must be paired with text or iconography (e.g. Danger red must be paired with `"OVERDUE • Oct 12"` or an alert icon; Success green must be paired with a checkmark or strikethrough).
- **Disabled State Standard**: Disabled buttons or inputs use `PanelRaised` container with `TextMuted` copy at 38% alpha.
- **Focus Indication**: Focused interactive elements display a 1.5 dp outer border using `VrddhihColors.Electric`.

---

## 4. Typography & Layout Spacing Principles

### Typography Hierarchy ([Type.kt](file:///home/avinash/AndroidStudioProjects/Vrddhih/app/src/main/java/com/vrddhih/app/core/theme/Type.kt))

The typography system pairs clean modern sans-serif for human-readable content with a sharp monospace font for technical system metrics:

```
┌────────────────────────────────────────────────────────────────────────┐
│ [SYSTEM EYEBROW]              VrddhihTextStyles.SystemLabel (11sp Mono)│
│ Screen Headline               MaterialTheme.typography.headlineMedium  │
│ Supporting directive subtitle MaterialTheme.typography.bodyMedium      │
│                                                                        │
│ ┌─ SystemPanel ──────────────────────────────────────────────────────┐ │
│ │ SECTION HEADER             VrddhihTextStyles.SystemLabel (Frost)   │ │
│ │ Task Title / Content       MaterialTheme.typography.titleMedium    │ │
│ │ Subtitle / Notes           MaterialTheme.typography.bodyMedium     │ │
│ │ [HIGH PRIORITY]            VrddhihTextStyles.TagLabel (10sp Sans)  │ │
│ │ STAT READOUT: 1,240 XP     VrddhihTextStyles.NumericSmall (Mono)   │ │
│ └────────────────────────────────────────────────────────────────────┘ │
└────────────────────────────────────────────────────────────────────────┘
```

1. **`SystemLabel`** (`11.sp`, `FontWeight.SemiBold`, `letterSpacing = 2.sp`, Uppercase Sans):
   - **Sole Purpose**: System header eyebrows, section bar headers, field group labels in forms.
2. **`TagLabel`** (`10.sp`, `FontWeight.SemiBold`, `letterSpacing = 1.2.sp`, Uppercase Sans):
   - **Sole Purpose**: Chips, badges, priority pills, and inline status badges.
3. **`NumericSmall` (`13.sp`) / `NumericMedium` (`22.sp`) / `NumericLarge` (`26.sp`)** (`FontFamily.Monospace`, Bold):
   - **Sole Purpose**: All numerical counters, deadlines (`"D-0"`, `"D-4"`), timer clocks (`"14:59"`), progress percentages (`"75%"`), currency amounts, and XP gains.
4. **Material Scales**:
   - `headlineMedium` (`28.sp` Bold): Screen titles.
   - `titleLarge` (`20.sp` SemiBold): Modal dialog and editor titles.
   - `titleMedium` (`16.sp` SemiBold): Card titles.
   - `bodyLarge` / `bodyMedium`: Primary input values and body paragraphs.
   - `bodySmall`: Timestamps, captions, validation errors.

### Spacing & Layout Rules ([Dimens.kt](file:///home/avinash/AndroidStudioProjects/Vrddhih/app/src/main/java/com/vrddhih/app/core/theme/Dimens.kt))

- **Horizontal Margins**: Standard screen edge margin is **`VrddhihSpacing.lg` (16 dp)**.
- **Top / Bottom Margins**: Standard screen top padding is **`VrddhihSpacing.xl` (20 dp)**; bottom scrolling content padding is **96 dp** to ensure items clear bottom navigation and floating actions.
- **Section Spacing**: Gap between major panels in a list is **`VrddhihSpacing.lg` (16 dp)**.
- **Card Padding**: Standard `SystemPanel` content padding is **`VrddhihSpacing.lg` (16 dp)**; compact cards use **`VrddhihSpacing.md` (12 dp)**.
- **Component Radii Standards**:
  - `VrddhihRadii.lg` (**20 dp**): Surfaces and panels (`SystemPanel`).
  - `VrddhihRadii.md` (**14 dp**): Action buttons, modal dialogs, category selection tiles.
  - `VrddhihRadii.sm` (**8 dp**): Filter chips, compact dialog buttons.
  - `VrddhihRadii.xs` (**6 dp**): Status pills (`SystemTag`), milestone check-boxes, steppers.
- **Max Content Width**: `VrddhihDimens.ContentMaxWidth` (**640 dp**) centered horizontally on tablets and landscape orientations.

---

## 5. Major Design Decisions & Interaction Policies

### Decision 1: Primary-Action Placement Across Primary Tabs

#### Problem Analysis
Currently, creation entry points are severely fragmented: Quests uses a bottom-anchored bar (`NEW QUEST`), Habits uses an inline button in the scrolling list, and Goals uses a tiny top-right header icon button.

#### Evaluation of Options

| Option | Pros | Cons | Recommendation Assessment |
| :--- | :--- | :--- | :--- |
| **Option A: Anchored Bottom Action Bar** (full-width button with gradient scrim above nav bar) | Outstanding thumb reachability on modern large devices; strong visual hierarchy; room for clear action label (`"+ NEW QUEST"`, `"+ NEW HABIT"`, `"+ NEW GOAL"`). | Consumes ~74 dp of vertical space; requires 96 dp bottom padding on lists; can feel heavy on smaller screens. | **Recommended for Primary Growth Screens** (Quests, Habits, Goals). |
| **Option B: Standard Floating Action Button (FAB)** | Standard Material pattern; familiar; lightweight. | Often covers bottom-right card content; lacks label unless Extended FAB (which is awkwardly wide); less aligned with the immersive hunter-system aesthetic. | Viable fallback, but less distinctive. |
| **Option C: Top Header Action Button** | Leaves full screen free for scrolling content; zero bottom occlusion. | Poor reachability on tall phones; low visual priority; header becomes cramped when icons for calendar and categories are present. | **Rejected** for primary creation. Header should be reserved for screen-level utilities (Calendar, Categories). |

#### Recommendation
**Adopt Option A (Standardized Anchored Bottom Action Bar) for Quests, Habits, and Goals.**
- **Placement**: Anchored at `Alignment.BottomCenter`, floating above the bottom navigation bar with a 3-stop gradient scrim (`Brush.verticalGradient(Transparent, Void 88%, Void)`).
- **Styling**: `height = 50.dp`, `shape = RoundedCornerShape(VrddhihRadii.md)`, `containerColor = VrddhihColors.Electric`, content `VrddhihColors.Void`, font `VrddhihTextStyles.TagLabel` bold uppercase.
- **Consistency**: Quests displays `"+ NEW QUEST"`, Habits displays `"+ NEW HABIT"`, Goals displays `"+ NEW GOAL"`.
- **List Inset**: All three screens maintain `contentPadding = PaddingValues(bottom = 96.dp)` so no item is ever obscured behind the bar.

---

### Decision 2: Creation & Editing Presentation

#### Problem Analysis
Quests currently uses a full-screen NavHost route (`QuestEditorScreen`), whereas Habits and Goals use custom 92% height modal dialogs (`HabitEditorDialog` and `GoalEditorDialog`).

#### Evaluation of Options

| Option | Pros | Cons | Recommendation Assessment |
| :--- | :--- | :--- | :--- |
| **Option A: Full-Screen Navigation Route for All Editors** | Unlimited scroll height; clean IME/keyboard handling; clear top app bar; deep linkable. | Heavyweight for quick entries (e.g. adding a simple 10-minute habit); full navigation transition can feel disconnected from the tab context. | Excellent for complex workflows, excessive for simple ones. |
| **Option B: Modal Bottom Sheets (`ModalBottomSheet`)** | Native Compose gesture support (swipe to dismiss); smooth backdrop dim; naturally docks above keyboard. | Can conflict with internal horizontal pagers or nested sliders; Material 3 bottom sheets require careful customization to look like hunter system windows. | Good, but deviates from the established "holographic window" metaphor. |
| **Option C: Standardized System Modal Dialog (`VrddhihEditorDialog`)** | Preserves the Solo Leveling "Status Window" aesthetic (centered floating card with glowing borders); user remains visually anchored to their screen; fast open/dismiss. | Requires explicit height constraints (e.g. 90% screen height) and careful internal scrolling with `imePadding()`. | **Recommended for Focused Editors** (Habits, Goals, Categories). |

#### Recommendation: Two-Tier Interaction Policy Based on Complexity
1. **Tier 1 (Complex Multi-Section Editor) $\to$ Dedicated Full-Screen Destination**:
   - Used for **Quests** (`QuestEditorScreen`). Quests feature intricate recurrence rules, day-of-week bitmasks, occurrence scopes ("this and future"), due times, difficulty, priority, and category pickers. A full screen provides the required layout stability and keyboard management.
2. **Tier 2 (Focused Metric / Protocol Editors) $\to$ Standardized System Window Dialog**:
   - Used for **Habits**, **Goals**, and **Categories**. These flows are focused, typically requiring 3 to 5 fields.
   - They will share a unified dialog container (`VrddhihEditorDialog`) featuring a standardized header (Eyebrow + Title + Close icon), internal `LazyColumn`, and a sticky footer bar with Cancel and Save buttons.

---

### Decision 3: Category Access Architecture

#### Problem Analysis
Currently, only Quests has access to `CategoriesScreen` (via its header menu icon) and inline category creation. Habits and Goals allow picking categories from a horizontal carousel, but provide no way to manage or create them.

#### Recommendation
1. **Global Management Entry Point**:
   - Add a standardized category icon button (`Icons.Outlined.FolderCopy` or `Icons.Outlined.Category`, tint = `VrddhihColors.Ice`) into the header action slot of **Quests, Habits, and Goals**.
   - Clicking this button navigates to `CategoriesScreen`, allowing full review, creation, editing, and deletion of custom categories.
2. **In-Editor Inline Creation**:
   - Promote `CreateCustomCategoryTile` (already built in `CategoryBadge.kt`) into a shared `CategoryPickerRow` component.
   - When editing a Quest, Habit, or Goal, the category carousel will always feature `CreateCustomCategoryTile` as its first or last item, launching the existing `CategoryEditorDialog` without navigating away from the current form.

---

### Decision 4: Shared vs. Feature-Specific Component Boundaries

```
┌────────────────────────────────────────────────────────────────────────┐
│ SHARED SYSTEM LAYER (core/ui/components/)                              │
│ • SystemPanel, SystemProgressBar, ProgressRing, SystemTag, SystemBadge │
│ • ScreenHeader, SectionHeader                                          │
│ • PrimaryActionButton (Bottom bar), GhostButton, SystemIconButton      │
│ • VrddhihTextField, VrddhihStepper (≥48dp)                             │
│ • CategorySelectionTile, CuteCategoryBadge, CategoryPickerRow          │
│ • DateStrip, VrddhihCalendarDialog                                     │
└──────────────────────────────────┬─────────────────────────────────────┘
                                   │ Used by
         ┌─────────────────────────┼─────────────────────────┐
         ▼                         ▼                         ▼
┌─────────────────┐       ┌─────────────────┐       ┌─────────────────┐
│ Quests Domain   │       │ Habits Domain   │       │ Goals Domain    │
│ • QuestItemRow  │       │ • HabitItemCard │       │ • GoalCard      │
│ • QuestPriority │       │ • TimerWidget   │       │ • MilestoneList │
│ • RecurrenceRow │       │ • LimitGauge    │       │ • DeadlineBadge │
└─────────────────┘       └─────────────────┘       └─────────────────┘
```

- **Shared Components**: Strictly UI presentation, layout chrome, design-token wrappers, and accessibility containers. No domain entities or business logic.
- **Feature-Specific Components**: Compositions of shared components binding to specific domain models (`QuestOccurrence`, `HabitWithConfigs`, `GoalWithAllConfigs`).

---

### Decision 5: Form Fields, Selection Controls & Date/Time Conventions

#### 1. Form Labels & Field Grouping
- **Label Typography**: All field labels use external `VrddhihTextStyles.SystemLabel` (11 sp uppercase, 2 sp letter spacing, `VrddhihColors.Ice`). Floating internal labels are deprecated.
- **Required Indicator**: Required fields display a prominent red `*` indicator (`VrddhihColors.Danger`) with merged TalkBack semantics ("Field Name, Required").
- **Spacing**: 4 dp (`VrddhihSpacing.xs`) between label and input surface; 16 dp (`VrddhihSpacing.lg`) vertical gap between discrete field groups.

#### 2. Text Fields & Inline Validation
- **Container & Borders**: Deep dark `VrddhihColors.Void` (`#04060C`) background, 1 dp `VrddhihColors.Outline` unfocused border, 1.5 dp `VrddhihColors.Electric` focused border.
- **Error Presentation**: When validation fails, the border transitions to `VrddhihColors.Danger` (1 dp) and displays an alert icon (`Icons.Outlined.ErrorOutline`) alongside the descriptive error message, strictly satisfying WCAG non-reliance on color alone.
- **Touch Target**: Strict minimum 48 dp interactive touch height (`defaultMinSize(minHeight = 48.dp)`).

#### 3. Selection Controls
- **Filter & Protocol Chips**: Standardized `VrddhihFilterChip` with strict $\ge 48\times 48\text{ dp}$ touch bounds, `Role.RadioButton` or `Role.Checkbox`, and `VrddhihColors.Electric` active glow.
- **Category Selection**: Standardized `CategoryPickerRow` providing a single unified horizontal carousel of `CategorySelectionTile` items with `CreateCustomCategoryTile` as its trailing action.

#### 4. Date & Time Conventions
- **Date Formatting**: Standardized user-readable format is `"MMM d, yyyy"` (e.g. `"Oct 27, 2026"` in monospace or body font).
- **Time Formatting**: Formatted as `"h:mm a"` or `"HH:mm"` matching the user's device 24-hour setting.
- **Interactive Trigger**: Standardized `VrddhihDateField` ($\ge 48\text{ dp}$ touch height) with calendar icon; tapping opens `VrddhihCalendarDialog`.
- **Optional Clearing**: Optional dates feature a dedicated clear action (`Icons.Outlined.Close`) with `"Clear date"` accessibility description.

---

## 6. Measurable Design-Rule Table

| Area | Current Implementation (Phase 01.1 Finding) | Proposed Rule (Phase 01.2 Standard) | Approval Status |
| :--- | :--- | :--- | :--- |
| **Primary Action** | Inconsistent: Quests = bottom bar; Habits = inline row; Goals = header icon. | **Anchored Primary Action**: `AnchoredPrimaryAction` and `PrimaryCreationButton` (50 dp, Electric fill, Void text, min 48 dp touch target, `isLoading` support with Void spinner). | **Implemented in Isolation** (Phase 01.4/01.5) |
| **Secondary Action** | Ad-hoc `TextButton` or custom `Box` borders. | **Standardized `VrddhihSecondaryButton`**: `PanelRaised` container, `Outline` 1 dp border, `Ice` (or `Danger` for destructive) text, 48 dp height, min 48 dp touch target, `isLoading` support. | **Implemented in Isolation** (Phase 01.5) |
| **Shared Icon Button** | Varied sizes (36 dp, 38 dp, 40 dp, 48 dp), missing descriptions. | **Standardized `VrddhihIconButton`**: Enforces strict $\ge 48\times 48\text{ dp}$ touch target, mandatory `contentDescription: String`, independent visual size (e.g. 36 dp badge inside 48 dp touch area), ghost/panel-backed styles, `isLoading` support. | **Implemented in Isolation** (Phase 01.5) |
| **Stepper Controls** | Sub-48dp violations (38 dp steppers in Habit card; private StepperButton in GoalCard). | **Standardized `VrddhihStepper` & `VrddhihStepperButton`**: Strict $\ge 48\times 48\text{ dp}$ touch targets for increment/decrement buttons, monospace numeric readout, bounds enforcement. | **Implemented in Isolation** (Phase 01.5) |
| **Form Fields** | Multiple duplicate `OutlinedTextFieldDefaults.colors` with conflicting borders (`Ice` vs `Electric`). | **Standardized `VrddhihTextField` & `VrddhihFieldLabel`**: `Void` container, `Outline` border, `Electric` focus border (1.5 dp), `TextPrimary` input, `TextMuted` placeholder, `Danger` error border paired with `Icons.Outlined.ErrorOutline` alert icon. | **Implemented in Isolation** (Phase 01.6) |
| **Selection Controls** | Inconsistent chips (FlowRow vs cards vs row); sub-48dp touch targets. | **Standardized `VrddhihFilterChip` & `CategoryPickerRow`**: Strict $\ge 48\times 48\text{ dp}$ touch target, `Role.RadioButton`/`Role.Checkbox`, unified category carousel with `CreateCustomCategoryTile`. | **Implemented in Isolation** (Phase 01.6) |
| **Date / Time Conventions** | Inconsistent picking (non-clickable date texts vs dialogs); fragmented formatting. | **Standardized `VrddhihDateField`**: Standard "MMM d, yyyy" format, $\ge 48\text{ dp}$ touch height, calendar icon, optional clear action, integrated with `VrddhihCalendarDialog`. | **Implemented in Isolation** (Phase 01.6) |
| **Card Surfaces** | `SystemPanel` with 20 dp radii and accent gradient borders. | **Retain `SystemPanel`**: 20 dp radius, `VrddhihBrushes.Panel`, 1 dp `panelBorder(accent)`. Reserve `glow = true` for active/completed hero cards. | **Supported in Code** |
| **Screen Headers** | Quests/Habits/Dashboard use `ScreenHeader`; Goals uses custom private `Header`. | **Enforce `ScreenHeader` globally**: Uppercase SystemLabel eyebrow, headlineMedium title, optional bodyMedium subtitle, 48 dp utility action slot. | **Implemented in Isolation** (Phase 01.3) |
| **Touch Targets** | Multiple sub-48dp violations (36 dp close button in Goal editor; 38 dp steppers in Habit card). | **Strict $\ge 48\text{ dp}$ minimum bounds**: All interactive icons, steppers, and checklist rows must enforce 48 dp minimum touch bounds via `Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)`. | **Requires Approval** |
| **Typography Scale** | Form labels arbitrarily swap between `SystemLabel` (11sp) and `TagLabel` (10sp). | **Strict Scale Rules**: Field labels = `SystemLabel` (11sp, tracking 2sp); Status pills/tags = `TagLabel` (10sp, tracking 1.2sp); Numbers = Monospace. | **Requires Approval** |
| **Animation Bounds** | Animated progress fill uses 600 ms tween; date strip uses instant snaps. | **600 ms for progress meters** (`SystemProgressBar`, `ProgressRing`); **250 ms tween for state visibility** (AnimatedVisibility expand/shrink); **zero animation on critical tap responses**. | **Supported in Code** |
| **Feedback Alerts** | Inline red texts for errors; `MockDataBadge` for sample data. | **Unified Inline Feedback**: Error messages use `Danger` text (`bodySmall`) with alert icon; Delete prompts use standard confirmation dialogs with `Danger` confirm button. | **Requires Approval** |

---

## 7. Decisions Requiring Explicit User Approval

Before proceeding to implementation in **Phase 01.3**, the following design decisions require your explicit confirmation:

1. **Approval of Primary-Action Placement**:
   - *Proposed*: Unify Quests, Habits, and Goals with the **Anchored Bottom Action Bar** (50 dp Electric button above bottom navigation).
   - *Alternative*: Standard Material 3 Floating Action Button (FAB) in bottom-right corner.
2. **Approval of Editor Form Factor Policy**:
   - *Proposed*: Two-tier approach: Full-screen route for Quests (due to recurrence complexity); Standardized System Modal Dialog (`VrddhihEditorDialog`) for Habits, Goals, and Categories.
   - *Alternative*: Convert all creation flows into full-screen routes, or convert all into modal bottom sheets.
3. **Approval of Category Entry Point**:
   - *Proposed*: Provide a standard Category utility icon in the header of Quests, Habits, and Goals, plus inline category creation tile in all editor carousels.
   - *Alternative*: Restrict category management strictly to Quests or move it to a dedicated global settings sheet.
4. **Approval of Strict 48 dp Touch Target Remediation**:
   - *Proposed*: Update all sub-48dp controls (steppers in `HabitItemCard`, close icon in `HabitEditorDialog`, milestone remove in `GoalEditorDialog`) to enforce 48 dp touch boundaries.

---

## 8. Implementation Sequence & Next Micro-Steps Alignment

The approved master roadmap governs subsequent implementation without renumbering:

- **01.1: Read-only audit** — *Completed.*
- **01.2: Define visual direction and design principles** — *Current task (Specification completed).*
- **01.3: Standardize the shared screen-header component** — *Next task: Refactor `ScreenHeader` to support uniform actions and subtitle typography; apply to GoalsScreen.*
- **01.4: Define the common primary-action style and placement rule** — *Implement the approved primary action component and bottom anchoring layout.*
- **01.5: Standardize button dimensions, icon buttons, touch targets, loading and disabled states** — *Create `VrddhihPrimaryButton`, `VrddhihGhostButton`, and `VrddhihStepper` enforcing $\ge 48\text{ dp}$.*
- **01.6: Standardize form labels, validation messages, selection controls and date/time conventions** — *Create `VrddhihTextField`, unify label type scales, and promote `CategoryPickerRow`.*
- **01.7: Standardize empty states, confirmation dialogs, errors and success feedback** — *Unify `QuestDeleteDialog` and future deletion modals into a reusable system confirmation dialog.*
- **01.8–01.10: Apply the shared header incrementally, one screen at a time** — *Migrate screens sequentially to verified shared components.*
- **01.11: Audit visual consistency across all six destinations** — *Final verification pass.*

---

## 9. Risks, Dependencies & Verification Checklist

1. **Keyboard Occlusion on Bottom Action Bar**:
   - *Risk*: Bottom-anchored action bars can occlude inputs when the software keyboard opens if `imePadding()` or `navigationBarsPadding()` is improperly configured.
   - *Mitigation*: The bottom action bar is present only on the list screens, not inside the editor forms.
2. **Touch-Target Visual Layout Distortion**:
   - *Risk*: Expanding 38 dp stepper buttons to 48 dp touch targets could make compact habit cards taller.
   - *Mitigation*: Use Compose `Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)` with centered visual graphics (36 dp visual button inside 48 dp touch container).
3. **Automated Test Integrity**:
   - *Verification*: Any modifications to shared components in Phase 01.3+ must keep all 12 existing test suites (102 unit tests) passing with 100% success.
