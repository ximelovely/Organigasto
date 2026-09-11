package com.app.organigasto.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.material.icons.filled.Fingerprint
import com.app.organigasto.ui.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onResetTutorialClick: () -> Unit,
    authViewModel: AuthViewModel
) {
    var darkModeEnabled by remember { mutableStateOf(false) }
    var biometricEnabled by remember { mutableStateOf(authViewModel.isBiometricEnabled) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Preferencias Generales",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Fila de Modo Oscuro
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ColorLens, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Modo Oscuro Permanente", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            Text("Forzar apariencia de tema oscuro", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                    }
                    Switch(
                        checked = darkModeEnabled,
                        onCheckedChange = { darkModeEnabled = it }
                    )
                }
            }
            
            // Fila de Biometría
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Fingerprint, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Usar Huella Digital", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            Text("Desbloqueo biométrico rápido", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                    }
                    Switch(
                        checked = biometricEnabled,
                        onCheckedChange = { 
                            biometricEnabled = it
                            authViewModel.setBiometricEnabled(it)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                "Mantenimiento y Datos",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Opción de relanzar el tutorial de Onboarding
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onResetTutorialClick() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.RestartAlt, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Volver a jugar Tutorial", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        Text("Iniciar guía de configuración inicial", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }
            }

            // Opción de restablecer base de datos completa
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Borrar todos los Datos", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.error)
                        Text("Restablecer cuentas, movimientos y presupuestos", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }
            }
        }
    }
}
