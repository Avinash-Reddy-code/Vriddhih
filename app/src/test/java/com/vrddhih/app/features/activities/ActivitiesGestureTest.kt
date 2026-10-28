package com.vrddhih.app.features.activities

import androidx.compose.foundation.pager.PagerState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ActivitiesGestureTest {

    private class TestPagerState(
        initialPage: Int,
        private val count: Int,
        initialOffsetFraction: Float = 0f,
    ) : PagerState(currentPage = initialPage, currentPageOffsetFraction = initialOffsetFraction) {
        override val pageCount: Int get() = count
        var dispatchedDelta: Float = 0f

        override fun dispatchRawDelta(delta: Float): Float {
            dispatchedDelta += delta
            return delta
        }
    }

    @Test
    fun boundarySwipeRight_onTasks_dispatchesToParent() {
        val parent = TestPagerState(initialPage = 1, count = 4)
        val child = TestPagerState(initialPage = 0, count = 2)
        val connection = createNestedPagerConnection(parent, child)

        // Dragging right on Tasks (dx > 0)
        val consumed = connection.onPostScroll(
            consumed = Offset.Zero,
            available = Offset(50f, 0f),
            source = NestedScrollSource.UserInput,
        )

        assertEquals(50f, consumed.x)
        assertEquals(50f, parent.dispatchedDelta)
    }

    @Test
    fun boundarySwipeLeft_onHabits_dispatchesToParent() {
        val parent = TestPagerState(initialPage = 1, count = 4)
        val child = TestPagerState(initialPage = 1, count = 2)
        val connection = createNestedPagerConnection(parent, child)

        // Dragging left on Habits (dx < 0)
        val consumed = connection.onPostScroll(
            consumed = Offset.Zero,
            available = Offset(-50f, 0f),
            source = NestedScrollSource.UserInput,
        )

        assertEquals(-50f, consumed.x)
        assertEquals(-50f, parent.dispatchedDelta)
    }

    @Test
    fun internalSwipe_doesNotDispatchToParent() {
        val parent = TestPagerState(initialPage = 1, count = 4)
        // Child on Tasks (0), dragging left towards Habits (dx < 0)
        val child = TestPagerState(initialPage = 0, count = 2)
        val connection = createNestedPagerConnection(parent, child)

        val consumed = connection.onPostScroll(
            consumed = Offset.Zero,
            available = Offset(-50f, 0f),
            source = NestedScrollSource.UserInput,
        )

        assertEquals(0f, consumed.x)
        assertEquals(0f, parent.dispatchedDelta)
    }
}
