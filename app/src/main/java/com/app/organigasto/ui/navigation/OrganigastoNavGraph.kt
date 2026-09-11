package com.app.organigasto.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.material3.Text

import com.app.organigasto.ui.auth.WelcomeScreen
import com.app.organigasto.ui.auth.LoginScreen
import com.app.organigasto.ui.auth.RegisterScreen
import com.app.organigasto.ui.home.HomeScreen
import com.app.organigasto.ui.movimientos.AddMovementScreen

import com.app.organigasto.ui.main.MainScreen
import com.app.organigasto.ui.presupuesto.BudgetManagementScreen
import com.app.organigasto.ui.categorias.CategoryListScreen
import com.app.organigasto.ui.categorias.AddCategoryScreen
import com.app.organigasto.ui.calendario.FinancialCalendarScreen
import com.app.organigasto.ui.tutorial.TutorialScreen
import com.app.organigasto.ui.account.AccountScreen
import com.app.organigasto.ui.settings.SettingsScreen

import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.organigasto.OrganigastoApp
import com.app.organigasto.ui.auth.AuthViewModel
import com.app.organigasto.ui.auth.AuthViewModelFactory
import com.app.organigasto.ui.movimientos.MovimientosViewModel
import com.app.organigasto.ui.movimientos.MovimientosViewModelFactory

import com.app.organigasto.data.local.PreferenceManager

@Composable
fun OrganigastoNavGraph() {
    val context = LocalContext.current
    val database = (context.applicationContext as OrganigastoApp).database
    val preferenceManager = remember { PreferenceManager(context) }
    
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(database.userDao(), preferenceManager)
    )
    
    val movimientosViewModel: MovimientosViewModel = viewModel(
        factory = MovimientosViewModelFactory(
            database.movimientoDao(),
            database.categoriaDao(),
            database.cuentaDao(),
            database.metaAhorroDao(),
            database.suscripcionDao(),
            database.deudaDao()
        )
    )
    
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Welcome.route
    ) {
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onLoginClick = { navController.navigate(Screen.Login.route) },
                onRegisterClick = { navController.navigate(Screen.Register.route) }
            )
        }
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { 
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                onRegisterClick = { navController.navigate(Screen.Register.route) },
                onForgotPasswordClick = { /* TODO */ },
                onBackClick = { navController.popBackStack() },
                authViewModel = authViewModel
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                onLoginClick = { navController.navigate(Screen.Login.route) },
                onBackClick = { navController.popBackStack() },
                authViewModel = authViewModel
            )
        }
        composable(Screen.Main.route) {
            MainScreen(
                onAddMovementClick = { navController.navigate(Screen.AddMovement.route) },
                onBudgetEditClick = { navController.navigate(Screen.Budget.route) },
                onCalendarClick = { navController.navigate(Screen.Calendar.route) },
                onAccountClick = { navController.navigate(Screen.Account.route) },
                onSettingsClick = { navController.navigate(Screen.Settings.route) },
                movimientosViewModel = movimientosViewModel
            )
        }
        composable(Screen.AddMovement.route) {
            AddMovementScreen(
                onBackClick = { navController.popBackStack() },
                viewModel = movimientosViewModel
            )
        }
        composable(Screen.Budget.route) {
            BudgetManagementScreen(
                onBackClick = { navController.popBackStack() },
                onManageCategoriesClick = { navController.navigate(Screen.Categories.route) }, // Añadido
                viewModel = movimientosViewModel
            )
        }
        composable(Screen.Categories.route) {
            CategoryListScreen(
                onBackClick = { navController.popBackStack() },
                onAddCategoryClick = { navController.navigate(Screen.AddCategory.route) },
                viewModel = movimientosViewModel
            )
        }
        composable(Screen.AddCategory.route) {
            AddCategoryScreen(
                onBackClick = { navController.popBackStack() },
                viewModel = movimientosViewModel
            )
        }
        composable(Screen.Calendar.route) {
            FinancialCalendarScreen(
                onBackClick = { navController.popBackStack() },
                viewModel = movimientosViewModel
            )
        }
        composable(Screen.Tutorial.route) {
            TutorialScreen(
                onFinish = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Tutorial.route) { inclusive = true }
                    }
                },
                viewModel = movimientosViewModel
            )
        }
        composable(Screen.Account.route) {
            AccountScreen(
                onBackClick = { navController.popBackStack() },
                onLogoutClick = {
                    authViewModel.logout()
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBackClick = { navController.popBackStack() },
                onResetTutorialClick = {
                    navController.navigate(Screen.Tutorial.route)
                },
                authViewModel = authViewModel
            )
        }
    }
}
