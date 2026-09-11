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

class AuthViewModel(private val userDao: UserDao) : ViewModel() {
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            val user = userDao.getUserByEmail(email)
            if (user != null && user.password == pass) {
                _isLoggedIn.value = true
                _authError.value = null
            } else {
                _authError.value = "Correo o contraseña incorrectos"
            }
        }
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
            }
        }
    }

    fun clearError() {
        _authError.value = null
    }
}

class AuthViewModelFactory(private val userDao: UserDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(userDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
