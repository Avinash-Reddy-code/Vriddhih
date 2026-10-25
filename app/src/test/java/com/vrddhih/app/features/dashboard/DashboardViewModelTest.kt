package com.vrddhih.app.features.dashboard

import com.vrddhih.app.features.dashboard.mock.DashboardMockData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class DashboardViewModelTest {

    @Test
    fun initialState_comesFromMockData() {
        val viewModel = DashboardViewModel()
        assertEquals(DashboardMockData.initialState(), viewModel.uiState.value)
    }

    @Test
    fun toggleQuestPreview_flipsOnlyTheTargetQuest() {
        val viewModel = DashboardViewModel()
        val before = viewModel.uiState.value
        val target = before.quests.first()

        viewModel.toggleQuestPreview(target.id)

        val after = viewModel.uiState.value
        assertEquals(!target.isCompleted, after.quests.first().isCompleted)
        assertEquals(before.quests.drop(1), after.quests.drop(1))
        assertNotEquals(before.completedQuestCount, after.completedQuestCount)
    }

    @Test
    fun toggleQuestPreview_twiceRestoresOriginalState() {
        val viewModel = DashboardViewModel()
        val before = viewModel.uiState.value
        val id = before.quests.last().id

        viewModel.toggleQuestPreview(id)
        viewModel.toggleQuestPreview(id)

        assertEquals(before, viewModel.uiState.value)
    }

    @Test
    fun toggleQuestPreview_unknownIdIsIgnored() {
        val viewModel = DashboardViewModel()
        val before = viewModel.uiState.value

        viewModel.toggleQuestPreview("does-not-exist")

        assertEquals(before, viewModel.uiState.value)
    }
}
