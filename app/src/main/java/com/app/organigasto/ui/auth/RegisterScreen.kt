package com.app.organigasto.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.organigasto.ui.theme.PurpuraPrimario
import com.app.organigasto.ui.theme.PurpuraSecundario

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onLoginClick: () -> Unit,
    onBackClick: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Crea tu cuenta",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = PurpuraSecundario
            )
            Text(
                text = "Empieza a organizar tus gastos hoy",
                fontSize = 13.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre completo") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PurpuraPrimario,
                    unfocusedBorderColor = PurpuraPrimario.copy(alpha = 0.3f)
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PurpuraPrimario,
                    unfocusedBorderColor = PurpuraPrimario.copy(alpha = 0.3f)
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PurpuraPrimario,
                    unfocusedBorderColor = PurpuraPrimario.copy(alpha = 0.3f)
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirmar contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PurpuraPrimario,
                    unfocusedBorderColor = PurpuraPrimario.copy(alpha = 0.3f)
                )
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            }

            if (successMessage != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = successMessage!!, color = Color(0xFF4CAF50), fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    successMessage = null
                    if (name.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                        errorMessage = "Todos los campos son obligatorios"
                    } else if (!email.contains("@") || !email.contains(".")) {
                        errorMessage = "El correo electrónico no es válido"
                    } else if (password != confirmPassword) {
                        errorMessage = "Las contraseñas no coinciden"
                    } else {
                        errorMessage = null
                        successMessage = "¡Cuenta creada con éxito! Redirigiendo..."
                        onRegisterSuccess()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PurpuraPrimario)
            ) {
                Text(text = "Registrarse", fontSize = 16.sp, color = Color.White)
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = onLoginClick) {
                Text(text = "¿Ya tienes cuenta? Inicia sesión", color = PurpuraSecundario)
            }

            TextButton(onClick = onBackClick) {
                Text(text = "Volver", color = Color.Gray)
            }
        }
    }
}
