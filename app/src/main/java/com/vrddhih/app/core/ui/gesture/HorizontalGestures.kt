package com.vrddhih.app.core.ui.gesture

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Velocity

/**
 * Keeps horizontal scrolling inside a nested scrollable (for example a chip row): scroll and fling left over at
 * its edges are consumed here instead of reaching the main screen pager.
 */
fun Modifier.consumeHorizontalNestedScroll(): Modifier = nestedScroll(ConsumeHorizontalScroll)

private object ConsumeHorizontalScroll : NestedScrollConnection {
    override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset =
        Offset(available.x, 0f)

    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity = Velocity(available.x, 0f)
}

/** Default fling speed, in px/s, that counts as a swipe even when the drag distance is short. */
const val SWIPE_VELOCITY_THRESHOLD = 800f

/**
 * Shared swipe rule for the date strip and the calendar.
 * Returns +1 for a swipe left (go forward), -1 for a swipe right (go back) and 0 when the gesture is too small.
 */
fun swipeDirection(
    totalDragPx: Float,
    velocityPxPerSecond: Float,
    distanceThresholdPx: Float,
    velocityThreshold: Float = SWIPE_VELOCITY_THRESHOLD,
): Int = when {
    totalDragPx <= -distanceThresholdPx || velocityPxPerSecond <= -velocityThreshold -> 1
    totalDragPx >= distanceThresholdPx || velocityPxPerSecond >= velocityThreshold -> -1
    else -> 0
}
