package com.dergoogler.mmrl.ui.navigation.graphs

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.dergoogler.mmrl.ui.navigation.MainScreen
import com.dergoogler.mmrl.ui.screens.home.HomeScreen
import com.dergoogler.mmrl.ui.screens.home.screens.AboutScreen
import com.dergoogler.mmrl.ui.screens.home.screens.ThankYouScreen

enum class HomeScreen(val route: String) {
    Home("Home"),
    About("About"),
    ThankYou("ThankYou")
}

fun NavGraphBuilder.homeScreen() = navigation(
    startDestination = HomeScreen.Home.route,
    route = MainScreen.Home.route
) {
    composable(
        route = HomeScreen.Home.route,
        enterTransition = { fadeIn() },
        exitTransition = { fadeOut() }
    ) {
        HomeScreen()
    }

    composable(
        route = HomeScreen.About.route,
        enterTransition = { fadeIn() },
        exitTransition = { fadeOut() }
    ) {
        AboutScreen()
    }

    composable(
        route = HomeScreen.ThankYou.route,
        enterTransition = { fadeIn() },
        exitTransition = { fadeOut() }
    ) {
        ThankYouScreen()
    }
}