package com.app.organigasto.ui.auth

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthViewModel : ViewModel() {
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    fun login(email: String, pass: String) {
        // Mock login
        if (email.isNotEmpty() && pass.length >= 6) {
            _isLoggedIn.value = true
        }
    }

    fun register(name: String, email: String, pass: String) {
        // Mock register
        if (name.isNotEmpty() && email.isNotEmpty() && pass.length >= 6) {
            _isLoggedIn.value = true
        }
    }
}
