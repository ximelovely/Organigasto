package com.app.organigasto.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.organigasto.MainActivity
import com.app.organigasto.ui.theme.PurpuraPrimario
import com.app.organigasto.ui.theme.PurpuraSecundario

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onBackClick: () -> Unit,
    authViewModel: AuthViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val authError by authViewModel.authError.collectAsState()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    
    val context = LocalContext.current
    val isBiometricEnabled = authViewModel.isBiometricEnabled
    val hasLoggedInOnce = authViewModel.hasLoggedInOnce

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            onLoginSuccess()
        }
    }
    
    // Auto-biometric on start if enabled
    LaunchedEffect(Unit) {
        if (isBiometricEnabled && hasLoggedInOnce) {
            (context as? MainActivity)?.showBiometricPrompt {
                onLoginSuccess()
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(PurpuraPrimario, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.size(54.dp)) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(Color.White, CircleShape)
                            .align(Alignment.BottomEnd),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$",
                            color = PurpuraPrimario,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Bienvenido de nuevo",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = PurpuraSecundario
            )
            Text(
                text = "Inicia sesión para seguir organizando tus gastos",
                fontSize = 13.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { 
                    email = it
                    authViewModel.clearError() 
                },
                label = { Text("Correo electrónico") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PurpuraPrimario,
                    unfocusedBorderColor = PurpuraPrimario.copy(alpha = 0.3f),
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { 
                    password = it
                    authViewModel.clearError()
                },
                label = { Text("Contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PurpuraPrimario,
                    unfocusedBorderColor = PurpuraPrimario.copy(alpha = 0.3f),
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                )
            )

            if (authError != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = authError!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { authViewModel.login(email, password) },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PurpuraPrimario)
                ) {
                    Text(text = "Iniciar Sesión", fontSize = 16.sp, color = Color.White)
                }
                
                if (isBiometricEnabled && hasLoggedInOnce) {
                    Spacer(modifier = Modifier.width(16.dp))
                    IconButton(
                        onClick = {
                            (context as? MainActivity)?.showBiometricPrompt {
                                onLoginSuccess()
                            }
                        },
                        modifier = Modifier
                            .size(50.dp)
                            .background(PurpuraPrimario.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(
                            Icons.Default.Fingerprint, 
                            contentDescription = "Biometría",
                            tint = PurpuraPrimario
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onRegisterClick) {
                Text(text = "¿No tienes cuenta? Regístrate", color = PurpuraSecundario)
            }

            TextButton(onClick = onBackClick) {
                Text(text = "Volver", color = Color.Gray)
            }
        }
    }
}
