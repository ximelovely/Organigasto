package com.app.organigasto.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.app.organigasto.data.local.dao.UsuarioDao
import com.app.organigasto.data.local.dao.CategoriaDao
import com.app.organigasto.data.local.entity.Categoria
import com.app.organigasto.data.local.entity.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

import com.app.organigasto.data.local.PreferenceManager

class AuthViewModel(
    private val usuarioDao: UsuarioDao,
    private val categoriaDao: CategoriaDao,
    private val preferenceManager: PreferenceManager
) : ViewModel() {
    private val _isLoggedIn = MutableStateFlow(preferenceManager.isLoggedInSession)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()
    
    val isBiometricEnabled = preferenceManager.isBiometricEnabled
    val hasLoggedInOnce = preferenceManager.hasLoggedInOnce

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            val user = usuarioDao.buscarPorCorreo(email)
            if (user != null && user.passwordHash == pass) { 
                preferenceManager.isLoggedInSession = true
                _isLoggedIn.value = true
                _authError.value = null
                preferenceManager.hasLoggedInOnce = true
                preferenceManager.userEmail = email
                preferenceManager.userName = user.nombre
                preferenceManager.currentUserId = user.id
            } else {
                _authError.value = "Correo o contraseña incorrectos"
            }
        }
    }
    
    fun setBiometricEnabled(enabled: Boolean) {
        preferenceManager.isBiometricEnabled = enabled
    }
    
    fun logout() {
        preferenceManager.isLoggedInSession = false
        _isLoggedIn.value = false
        preferenceManager.currentUserId = -1
    }

    fun register(name: String, email: String, pass: String) {
        viewModelScope.launch {
            val exists = usuarioDao.buscarPorCorreo(email) != null
            if (exists) {
                _authError.value = "El usuario ya existe"
            } else {
                val newUser = Usuario(nombre = name, correo = email, passwordHash = pass, salt = "salt_placeholder")
                val id = usuarioDao.insertar(newUser).toInt()

                preferenceManager.isLoggedInSession = true
                _isLoggedIn.value = true
                _authError.value = null
                preferenceManager.hasLoggedInOnce = true
                preferenceManager.userEmail = email
                preferenceManager.userName = name
                preferenceManager.currentUserId = id
            }
        }
    }

    fun clearError() {
        _authError.value = null
    }
}

class AuthViewModelFactory(
    private val usuarioDao: UsuarioDao,
    private val categoriaDao: CategoriaDao,
    private val preferenceManager: PreferenceManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(usuarioDao, categoriaDao, preferenceManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
