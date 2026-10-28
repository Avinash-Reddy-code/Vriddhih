package com.vrddhih.app.core.navigation

import com.vrddhih.app.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class TopLevelDestinationTest {

    @Test
    fun primaryDestinationsAreDashboardActivitiesGoalsFinance() {
        val destinations = TopLevelDestination.entries
        assertEquals(4, destinations.size)
        assertEquals(TopLevelDestination.DASHBOARD, destinations[0])
        assertEquals(TopLevelDestination.ACTIVITIES, destinations[1])
        assertEquals(TopLevelDestination.GOALS, destinations[2])
        assertEquals(TopLevelDestination.FINANCE, destinations[3])
    }

    @Test
    fun activitiesDestinationProperties() {
        val activities = TopLevelDestination.ACTIVITIES
        assertEquals("activities", activities.route)
        assertEquals(R.string.nav_activities, activities.labelRes)
        assertNotNull(activities.selectedIcon)
        assertNotNull(activities.unselectedIcon)
    }
}
