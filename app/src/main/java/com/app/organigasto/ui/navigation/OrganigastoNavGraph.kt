package com.app.organigasto.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
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

import com.app.organigasto.ui.account.ManageSalaryScreen
import com.app.organigasto.data.local.PreferenceManager

@Composable
fun OrganigastoNavGraph(onThemeChange: (Int) -> Unit) {
    val context = LocalContext.current
    val database = (context.applicationContext as OrganigastoApp).database
    val preferenceManager = remember { PreferenceManager(context) }
    
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(database.usuarioDao(), database.categoriaDao(), preferenceManager)
    )
    
    val navController = rememberNavController()

    // Función auxiliar para obtener el ViewModel con el ID de usuario actual
    @Composable
    fun getMovimientosViewModel(): MovimientosViewModel {
        val userId = preferenceManager.currentUserId
        return viewModel(
            key = if (userId != -1) "user_$userId" else "no_user",
            factory = MovimientosViewModelFactory(
                database.categoriaDao(),
                database.presupuestoPeriodoDao(),
                database.gastoDao(),
                database.suscripcionDao(),
                database.prestamoDao(),
                database.pagoPrestamoDao(),
                database.usuarioDao(),
                userId
            )
        )
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Welcome.route
    ) {
        composable(Screen.Welcome.route) {
            val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
            LaunchedEffect(isLoggedIn) {
                if (isLoggedIn) {
                    val userId = preferenceManager.currentUserId
                    val target = if (preferenceManager.isTutorialCompleted(userId)) Screen.Main.route else Screen.Tutorial.route
                    navController.navigate(target) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            }
            WelcomeScreen(
                onLoginClick = { navController.navigate(Screen.Login.route) },
                onRegisterClick = { navController.navigate(Screen.Register.route) }
            )
        }
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { 
                    val userId = preferenceManager.currentUserId
                    val target = if (preferenceManager.isTutorialCompleted(userId)) Screen.Main.route else Screen.Tutorial.route
                    navController.navigate(target) {
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
                    navController.navigate(Screen.Tutorial.route) {
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
                onAddCategoryClick = { navController.navigate(Screen.AddCategory.route) },
                onCalendarClick = { navController.navigate(Screen.Calendar.route) },
                onAccountClick = { navController.navigate(Screen.Account.route) },
                onSettingsClick = { navController.navigate(Screen.Settings.route) },
                movimientosViewModel = getMovimientosViewModel()
            )
        }
        composable(Screen.AddMovement.route) {
            AddMovementScreen(
                onBackClick = { navController.popBackStack() },
                viewModel = getMovimientosViewModel()
            )
        }
        composable(Screen.Budget.route) {
            BudgetManagementScreen(
                onBackClick = { navController.popBackStack() },
                onManageCategoriesClick = { navController.navigate(Screen.Categories.route) },
                viewModel = getMovimientosViewModel()
            )
        }
        composable(Screen.Categories.route) {
            CategoryListScreen(
                onBackClick = { navController.popBackStack() },
                onAddCategoryClick = { navController.navigate(Screen.AddCategory.route) },
                viewModel = getMovimientosViewModel()
            )
        }
        composable(Screen.AddCategory.route) {
            AddCategoryScreen(
                onBackClick = { navController.popBackStack() },
                viewModel = getMovimientosViewModel()
            )
        }
        composable(Screen.Calendar.route) {
            FinancialCalendarScreen(
                onBackClick = { navController.popBackStack() },
                viewModel = getMovimientosViewModel()
            )
        }
        composable(Screen.Tutorial.route) {
            TutorialScreen(
                onFinish = {
                    preferenceManager.setTutorialCompleted(preferenceManager.currentUserId, true)
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Tutorial.route) { inclusive = true }
                    }
                },
                viewModel = getMovimientosViewModel()
            )
        }
        composable(Screen.Account.route) {
            AccountScreen(
                userName = preferenceManager.userName ?: "Usuario de OrganiGasto",
                userEmail = preferenceManager.userEmail ?: "usuario@organigasto.com",
                onBackClick = { navController.popBackStack() },
                onLogoutClick = {
                    authViewModel.logout()
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onManageSalaryClick = {
                    navController.navigate(Screen.ManageSalary.route)
                },
                onManageCategoriesClick = {
                    navController.navigate(Screen.Categories.route)
                }
            )
        }
        composable(Screen.ManageSalary.route) {
            ManageSalaryScreen(
                onBackClick = { navController.popBackStack() },
                viewModel = getMovimientosViewModel()
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBackClick = { navController.popBackStack() },
                onResetTutorialClick = {
                    navController.navigate(Screen.Tutorial.route)
                },
                authViewModel = authViewModel,
                onThemeChange = onThemeChange,
                preferenceManager = preferenceManager,
                movimientosViewModel = getMovimientosViewModel()
            )
        }
    }
}
