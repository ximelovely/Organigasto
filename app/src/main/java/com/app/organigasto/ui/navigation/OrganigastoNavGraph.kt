package com.app.organigasto.ui.navigation

import androidx.compose.runtime.Composable
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

import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.organigasto.OrganigastoApp
import com.app.organigasto.ui.auth.AuthViewModel
import com.app.organigasto.ui.auth.AuthViewModelFactory
import com.app.organigasto.ui.movimientos.MovimientosViewModel
import com.app.organigasto.ui.movimientos.MovimientosViewModelFactory

@Composable
fun OrganigastoNavGraph() {
    val context = LocalContext.current
    val database = (context.applicationContext as OrganigastoApp).database
    
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(database.userDao())
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
                onCalendarClick = { navController.navigate(Screen.Calendar.route) }, // Añadido
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
    }
}
