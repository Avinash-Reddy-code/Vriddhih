package com.vrddhih.app.features.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import com.vrddhih.app.R
import com.vrddhih.app.core.theme.VrddhihColors
import com.vrddhih.app.core.theme.VrddhihDimens
import com.vrddhih.app.core.theme.VrddhihRadii
import com.vrddhih.app.core.theme.VrddhihSpacing
import com.vrddhih.app.core.theme.VrddhihTheme
import com.vrddhih.app.core.time.DateSelectionState
import com.vrddhih.app.core.ui.components.CalendarToggleButton
import com.vrddhih.app.core.ui.components.DateStrip
import com.vrddhih.app.core.ui.components.StatsToggleButton
import com.vrddhih.app.core.ui.components.VrddhihCalendarDialog
import com.vrddhih.app.core.ui.gesture.consumeHorizontalNestedScroll
import com.vrddhih.app.features.dashboard.mock.DashboardMockData
import java.time.LocalDate
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale

@Composable
fun DashboardRoute(
    modifier: Modifier = Modifier,
    onNavigateToCategories: () -> Unit = {},
    onNavigateToStats: () -> Unit = {},
    viewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory),
) {
    val dateState by viewModel.dateState.collectAsStateWithLifecycle()

    // Re-read the device date on every resume so "today" stays correct past midnight or after a time-zone change.
    LifecycleResumeEffect(viewModel) {
        viewModel.refreshToday()
        onPauseOrDispose { }
    }

    DashboardScreen(
        dateState = dateState,
        onDateSelected = viewModel::selectDate,
        onSelectToday = viewModel::selectToday,
        onNavigateToCategories = onNavigateToCategories,
        onNavigateToStats = onNavigateToStats,
        modifier = modifier,
    )
}

@Composable
fun DashboardScreen(
    dateState: DateSelectionState,
    onDateSelected: (LocalDate) -> Unit,
    onSelectToday: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToCategories: () -> Unit = {},
    onNavigateToStats: () -> Unit = {},
    uiState: DashboardUiState = DashboardMockData.initialState(),
    dayLog: DayLogUiState = DayLogUiState.Unavailable,
    occurrences: List<com.vrddhih.app.core.data.quest.QuestOccurrence> = emptyList(),
    habits: List<DashboardHabitItem> = emptyList(),
    goals: List<DashboardGoalItem> = emptyList(),
    dayLogData: com.vrddhih.app.core.data.quest.DayLog? = null,
    hasRealQuests: Boolean = false,
    hasRealHabits: Boolean = false,
    hasRealGoals: Boolean = false,
    onToggleQuest: (com.vrddhih.app.core.data.quest.QuestOccurrence) -> Unit = {},
    onToggleHabit: (Long) -> Unit = {},
    onToggleQuestPreview: (String) -> Unit = {},
    onShiftWindow: (Int) -> Unit = {},
) {
    var showCalendar by rememberSaveable { mutableStateOf(false) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val isDrawerActive = drawerState.isOpen || drawerState.targetValue == DrawerValue.Open

    BackHandler(enabled = isDrawerActive) {
        coroutineScope.launch { drawerState.close() }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = isDrawerActive,
        modifier = modifier
            .fillMaxSize()
            .then(if (isDrawerActive) Modifier.consumeHorizontalNestedScroll() else Modifier),
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = VrddhihColors.Panel,
                drawerShape = RoundedCornerShape(
                    topEnd = VrddhihRadii.md,
                    bottomEnd = VrddhihRadii.md,
                ),
                modifier = Modifier
                    .width(280.dp)
                    .fillMaxHeight()
                    .consumeHorizontalNestedScroll()
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                VrddhihColors.Electric.copy(alpha = 0.5f),
                                VrddhihColors.Electric.copy(alpha = 0.1f),
                            ),
                        ),
                        shape = RoundedCornerShape(
                            topEnd = VrddhihRadii.md,
                            bottomEnd = VrddhihRadii.md,
                        ),
                    ),
            ) {
                val today = dateState.today
                val weekday = remember(today) {
                    today.dayOfWeek.getDisplayName(JavaTextStyle.FULL, Locale.getDefault())
                }
                val formattedDate = remember(today) {
                    val locale = Locale.getDefault()
                    "${today.dayOfMonth} ${today.month.getDisplayName(JavaTextStyle.SHORT, locale).lowercase(locale)} ${today.year}"
                }

                // Sidebar Header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = VrddhihSpacing.lg,
                            end = VrddhihSpacing.lg,
                            top = VrddhihSpacing.md,
                            bottom = VrddhihSpacing.xs,
                        ),
                ) {
                    Text(
                        text = stringResource(R.string.app_name).uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        color = VrddhihColors.TextPrimary,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(VrddhihSpacing.xxs))
                    Text(
                        text = weekday,
                        style = MaterialTheme.typography.bodyMedium,
                        color = VrddhihColors.Ice,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = VrddhihColors.TextMuted,
                    )
                }

                Spacer(Modifier.height(VrddhihSpacing.sm))
                HorizontalDivider(
                    color = VrddhihColors.Outline,
                    modifier = Modifier.padding(horizontal = VrddhihSpacing.lg),
                )
                Spacer(Modifier.height(VrddhihSpacing.md))

                // Navigation Items
                NavigationDrawerItem(
                    label = {
                        Text(
                            text = stringResource(R.string.categories_title),
                            style = MaterialTheme.typography.bodyLarge,
                            color = VrddhihColors.TextPrimary,
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Category,
                            contentDescription = null,
                            tint = VrddhihColors.Ice,
                            modifier = Modifier.size(20.dp),
                        )
                    },
                    selected = false,
                    onClick = {
                        coroutineScope.launch {
                            drawerState.close()
                            onNavigateToCategories()
                        }
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = Color.Transparent,
                        selectedContainerColor = VrddhihColors.Electric.copy(alpha = 0.15f),
                    ),
                    shape = RoundedCornerShape(VrddhihRadii.sm),
                    modifier = Modifier.padding(horizontal = VrddhihSpacing.md),
                )
            }
        },
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = VrddhihDimens.ContentMaxWidth)
                    .fillMaxSize(),
                contentPadding = PaddingValues(
                    start = VrddhihSpacing.lg,
                    end = VrddhihSpacing.lg,
                    top = VrddhihSpacing.md,
                    bottom = 72.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(VrddhihSpacing.lg),
            ) {
                item(key = "top-bar") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = VrddhihSpacing.xs),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(
                            onClick = { coroutineScope.launch { drawerState.open() } },
                            modifier = Modifier.size(48.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(VrddhihColors.PanelRaised)
                                    .border(1.dp, VrddhihColors.OutlineBright, CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Menu,
                                    contentDescription = stringResource(R.string.nav_drawer_open),
                                    tint = VrddhihColors.Cyan,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(VrddhihSpacing.xs),
                        ) {
                            StatsToggleButton(onClick = onNavigateToStats)
                            CalendarToggleButton(onClick = { showCalendar = true })
                        }
                    }
                }
                item(key = "date-strip") {
                    DateStrip(
                        selectedDate = dateState.selectedDate,
                        today = dateState.today,
                        onDateSelected = onDateSelected,
                        onSelectToday = onSelectToday,
                    )
                }
            }

            Text(
                text = stringResource(R.string.dashboard_footer),
                style = MaterialTheme.typography.bodySmall,
                color = VrddhihColors.TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .widthIn(max = VrddhihDimens.ContentMaxWidth)
                    .fillMaxWidth()
                    .padding(horizontal = VrddhihSpacing.lg, vertical = VrddhihSpacing.xl),
            )
        }
    }

    if (showCalendar) {
        VrddhihCalendarDialog(
            selectedDate = dateState.selectedDate,
            today = dateState.today,
            onDateSelected = { date ->
                onDateSelected(date)
                showCalendar = false
            },
            onTodaySelected = {
                onSelectToday()
                showCalendar = false
            },
            onDismiss = { showCalendar = false },
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000, widthDp = 390, heightDp = 800)
@Composable
private fun DashboardScreenPreview() {
    val today = LocalDate.now()
    VrddhihTheme {
        DashboardScreen(
            dateState = DateSelectionState(today = today, selectedDate = today),
            onDateSelected = {},
            onSelectToday = {},
        )
    }
}
