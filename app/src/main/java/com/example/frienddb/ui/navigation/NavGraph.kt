package com.example.frienddb.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.frienddb.data.repository.FriendRepository
import com.example.frienddb.ui.calendar.CalendarScreen
import com.example.frienddb.ui.calendar.CalendarViewModel
import com.example.frienddb.ui.edit.EditScreen
import com.example.frienddb.ui.edit.EditViewModel
import kotlinx.serialization.Serializable

@Serializable object CalendarRoute
@Serializable object EditRoute

@Composable
fun NavGraph(repository: FriendRepository) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = CalendarRoute) {
        composable<CalendarRoute> {
            val vm: CalendarViewModel = viewModel(
                factory = CalendarViewModel.factory(repository)
            )
            CalendarScreen(
                viewModel        = vm,
                onNavigateToEdit = { navController.navigate(EditRoute) }
            )
        }
        composable<EditRoute> {
            val vm: EditViewModel = viewModel(
                factory = EditViewModel.factory(repository)
            )
            EditScreen(
                viewModel = vm,
                onBack    = { navController.popBackStack() }
            )
        }
    }
}
