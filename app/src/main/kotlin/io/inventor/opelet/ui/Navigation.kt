package io.inventor.opelet.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.inventor.opelet.ui.apps.AppListScreen
import io.inventor.opelet.ui.detail.DetailScreen
import io.inventor.opelet.ui.settings.SettingsScreen
import java.net.URLDecoder
import java.net.URLEncoder

object Routes {
    const val APP_LIST = "apps"
    const val DETAIL = "detail/{repoFullName}"
    const val SETTINGS = "settings"

    fun detail(repoFullName: String): String =
        "detail/${URLEncoder.encode(repoFullName, "UTF-8")}"
}

@Composable
fun OpeletNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.APP_LIST,
        modifier = modifier,
    ) {
        composable(Routes.APP_LIST) {
            AppListScreen(
                onAppClick = { repoFullName ->
                    navController.navigate(Routes.detail(repoFullName))
                },
                onSettingsClick = {
                    navController.navigate(Routes.SETTINGS)
                },
            )
        }

        composable(
            route = Routes.DETAIL,
            arguments = listOf(
                navArgument("repoFullName") { type = NavType.StringType }
            ),
        ) { backStackEntry ->
            val encoded = backStackEntry.arguments?.getString("repoFullName") ?: ""
            // SavedStateHandle receives the decoded value automatically
            DetailScreen(
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
            )
        }
    }
}
