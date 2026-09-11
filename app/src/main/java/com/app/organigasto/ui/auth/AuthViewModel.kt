package com.app.organigasto.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.app.organigasto.data.local.dao.UserDao
import com.app.organigasto.data.local.entity.UserEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import com.app.organigasto.data.local.PreferenceManager

class AuthViewModel(
    private val userDao: UserDao,
    private val preferenceManager: PreferenceManager
) : ViewModel() {
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()
    
    val isBiometricEnabled = preferenceManager.isBiometricEnabled
    val hasLoggedInOnce = preferenceManager.hasLoggedInOnce

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            val user = userDao.getUserByEmail(email)
            if (user != null && user.password == pass) {
                _isLoggedIn.value = true
                _authError.value = null
                preferenceManager.hasLoggedInOnce = true
                preferenceManager.userEmail = email
                preferenceManager.userName = user.name
            } else {
                _authError.value = "Correo o contraseña incorrectos"
            }
        }
    }
    
    fun setBiometricEnabled(enabled: Boolean) {
        preferenceManager.isBiometricEnabled = enabled
    }
    
    fun logout() {
        _isLoggedIn.value = false
    }

    fun register(name: String, email: String, pass: String) {
        viewModelScope.launch {
            val exists = userDao.checkUserExists(email) > 0
            if (exists) {
                _authError.value = "El usuario ya existe"
            } else {
                val newUser = UserEntity(name = name, email = email, password = pass)
                userDao.insert(newUser)
                _isLoggedIn.value = true
                _authError.value = null
                preferenceManager.hasLoggedInOnce = true
                preferenceManager.userEmail = email
                preferenceManager.userName = name
            }
        }
    }

    fun clearError() {
        _authError.value = null
    }
}

class AuthViewModelFactory(
    private val userDao: UserDao,
    private val preferenceManager: PreferenceManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(userDao, preferenceManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
