package com.vrddhih.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.vrddhih.app.core.navigation.TopLevelDestination
import com.vrddhih.app.core.navigation.VrddhihBottomBar
import com.vrddhih.app.core.navigation.VrddhihNavHost
import com.vrddhih.app.core.navigation.isInHierarchy
import com.vrddhih.app.core.navigation.navigateToTopLevel
import com.vrddhih.app.core.theme.VrddhihBrushes

@Composable
fun VrddhihApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(VrddhihBrushes.Backdrop),
        containerColor = Color.Transparent,
        bottomBar = {
            VrddhihBottomBar(
                destinations = TopLevelDestination.entries,
                isSelected = { currentDestination.isInHierarchy(it) },
                onNavigate = navController::navigateToTopLevel,
            )
        },
    ) { innerPadding ->
        VrddhihNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
        )
    }
}
