package com.app.organigasto.ui.navigation

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object Login : Screen("login")
    object Register : Screen("register")
    object Main : Screen("main") {
        object Home : Screen("home")
        object Stats : Screen("stats")
        object Subscriptions : Screen("subscriptions")
        object Loans : Screen("loans")
    }
    object AddMovement : Screen("add_movement")
    object Budget : Screen("budget")
    object Categories : Screen("categories")
    object AddCategory : Screen("add_category")
    object Calendar : Screen("calendar")
    object Tutorial : Screen("tutorial")
    object Account : Screen("account")
    object Settings : Screen("settings")
}
