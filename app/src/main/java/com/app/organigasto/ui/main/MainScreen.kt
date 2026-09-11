package com.app.organigasto.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.app.organigasto.ui.home.HomeScreen
import com.app.organigasto.ui.movimientos.MovimientosViewModel
import com.app.organigasto.ui.stats.StatsScreen
import com.app.organigasto.ui.suscripciones.SuscripcionesScreen
import com.app.organigasto.ui.deudas.DeudasScreen
import com.app.organigasto.ui.navigation.Screen
import com.app.organigasto.ui.theme.PurpuraPrimario
import com.app.organigasto.ui.theme.PurpuraSecundario

@Composable
fun MainScreen(
    onAddMovementClick: () -> Unit,
    onBudgetEditClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onAccountClick: () -> Unit,
    onSettingsClick: () -> Unit,
    movimientosViewModel: MovimientosViewModel
) {
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Main.Home.route,
            modifier = Modifier.padding(bottom = padding.calculateBottomPadding())
        ) {
            composable(Screen.Main.Home.route) {
                HomeScreen(
                    onAddMovementClick = onAddMovementClick,
                    onBudgetEditClick = onBudgetEditClick,
                    onCalendarClick = onCalendarClick,
                    onAccountClick = onAccountClick,
                    onSettingsClick = onSettingsClick,
                    viewModel = movimientosViewModel
                )
            }
            composable(Screen.Main.Stats.route) {
                StatsScreen(viewModel = movimientosViewModel)
            }
            composable(Screen.Main.Subscriptions.route) {
                SuscripcionesScreen(viewModel = movimientosViewModel)
            }
            composable(Screen.Main.Loans.route) {
                DeudasScreen(viewModel = movimientosViewModel)
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        BottomNavItem("Inicio", Screen.Main.Home.route, Icons.Default.Home),
        BottomNavItem("Estadísticas", Screen.Main.Stats.route, Icons.Default.PieChart),
        BottomNavItem("Suscripciones", Screen.Main.Subscriptions.route, Icons.Default.Subscriptions),
        BottomNavItem("Créditos", Screen.Main.Loans.route, Icons.Default.AccountBalance)
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        navController.graph.startDestinationRoute?.let { route ->
                            popUpTo(route) { saveState = true }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            )
        }
    }
}

data class BottomNavItem(val title: String, val route: String, val icon: ImageVector)
