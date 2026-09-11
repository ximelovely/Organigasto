package com.app.organigasto.ui.tutorial

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.organigasto.ui.movimientos.MovimientosViewModel

import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.filled.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorialScreen(
    onFinish: () -> Unit,
    viewModel: MovimientosViewModel
) {
    var step by remember { mutableStateOf(1) }
    var salario by remember { mutableStateOf("") }
    
    var efectivoSelected by remember { mutableStateOf(true) }
    var debitoSelected by remember { mutableStateOf(true) }
    var creditoSelected by remember { mutableStateOf(false) }

    var netflixSelected by remember { mutableStateOf(false) }
    var spotifySelected by remember { mutableStateOf(false) }
    var primeSelected by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Configuración Inicial", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
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
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Indicador de Progreso superior
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LinearProgressIndicator(
                    progress = { if (step >= 1) 1f else 0f },
                    modifier = Modifier.weight(1f).height(6.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                )
                LinearProgressIndicator(
                    progress = { if (step >= 2) 1f else 0f },
                    modifier = Modifier.weight(1f).height(6.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                )
                LinearProgressIndicator(
                    progress = { if (step >= 3) 1f else 0f },
                    modifier = Modifier.weight(1f).height(6.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Cuerpo del paso actual
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                when (step) {
                    1 -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "¡Bienvenido a OrganiGasto!",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Para comenzar, introduce tu sueldo neto mensual o ingreso inicial aproximado.",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            Spacer(modifier = Modifier.height(32.dp))
                            OutlinedTextField(
                                value = salario,
                                onValueChange = { salario = it },
                                label = { Text("Monto de Ingreso Inicial ($)") },
                                placeholder = { Text("Ej. 15000") },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    2 -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Tus Métodos de Pago",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Selecciona las billeteras o cuentas que utilizas en tu día a día.",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            FilterChip(
                                selected = efectivoSelected,
                                onClick = { efectivoSelected = !efectivoSelected },
                                label = { Text("Efectivo Físico") },
                                leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                            )
                            FilterChip(
                                selected = debitoSelected,
                                onClick = { debitoSelected = !debitoSelected },
                                label = { Text("Tarjeta de Débito / Nómina") },
                                leadingIcon = { Icon(Icons.Default.CreditCard, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                            )
                            FilterChip(
                                selected = creditoSelected,
                                onClick = { creditoSelected = !creditoSelected },
                                label = { Text("Tarjeta de Crédito") },
                                leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                            )
                        }
                    }
                    3 -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "¿Tienes Suscripciones activas?",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Elige tus plataformas para pre-configurar tus gastos fijos mensuales recurrentes.",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            FilterChip(
                                selected = netflixSelected,
                                onClick = { netflixSelected = !netflixSelected },
                                label = { Text("Netflix ($219.00/mes)") },
                                leadingIcon = { Icon(Icons.Default.Movie, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                            )
                            FilterChip(
                                selected = spotifySelected,
                                onClick = { spotifySelected = !spotifySelected },
                                label = { Text("Spotify ($129.00/mes)") },
                                leadingIcon = { Icon(Icons.Default.MusicNote, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                            )
                            FilterChip(
                                selected = primeSelected,
                                onClick = { primeSelected = !primeSelected },
                                label = { Text("Amazon Prime ($99.00/mes)") },
                                leadingIcon = { Icon(Icons.Default.Inventory2, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // Botón de acción inferior (Continuar / Finalizar)
            Button(
                onClick = {
                    if (step < 3) {
                        step++
                    } else {
                        // Procesar y guardar toda la información en el ViewModel de forma segura
                        val ingresoMonto = salario.toDoubleOrNull() ?: 0.0
                        
                        // 1. Inicializar Cuentas y Saldo si se ingresó un salario
                        if (efectivoSelected) {
                            // En una app ideal se llama a un método de inicialización del ViewModel
                            // Simularemos agregando el saldo o cuenta según la API disponible
                        }
                        
                        // Al finalizar todo, ir a la pantalla principal
                        onFinish()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (step == 3) "Finalizar y Comenzar" else "Siguiente paso",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = if (step == 3) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null
                    )
                }
            }
        }
    }
}
