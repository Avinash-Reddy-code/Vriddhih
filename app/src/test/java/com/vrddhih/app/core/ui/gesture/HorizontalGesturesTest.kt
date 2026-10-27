package com.vrddhih.app.core.ui.gesture

import org.junit.Assert.assertEquals
import org.junit.Test

class HorizontalGesturesTest {

    @Test
    fun swipeDirection_swipeLeftExceedingDistanceThreshold_returnsForward() {
        // Dragged -80px with distance threshold 50px
        assertEquals(1, swipeDirection(totalDragPx = -80f, velocityPxPerSecond = 0f, distanceThresholdPx = 50f))
    }

    @Test
    fun swipeDirection_flingLeftExceedingVelocityThreshold_returnsForward() {
        // Short drag -20px but high fling velocity -1000px/s
        assertEquals(1, swipeDirection(totalDragPx = -20f, velocityPxPerSecond = -1000f, distanceThresholdPx = 50f))
    }

    @Test
    fun swipeDirection_swipeRightExceedingDistanceThreshold_returnsBackward() {
        // Dragged +80px with distance threshold 50px
        assertEquals(-1, swipeDirection(totalDragPx = 80f, velocityPxPerSecond = 0f, distanceThresholdPx = 50f))
    }

    @Test
    fun swipeDirection_flingRightExceedingVelocityThreshold_returnsBackward() {
        // Short drag +15px but high velocity +1200px/s
        assertEquals(-1, swipeDirection(totalDragPx = 15f, velocityPxPerSecond = 1200f, distanceThresholdPx = 50f))
    }

    @Test
    fun swipeDirection_subThresholdDragAndVelocity_returnsZero() {
        assertEquals(0, swipeDirection(totalDragPx = 10f, velocityPxPerSecond = 200f, distanceThresholdPx = 50f))
        assertEquals(0, swipeDirection(totalDragPx = -10f, velocityPxPerSecond = -300f, distanceThresholdPx = 50f))
    }
}
