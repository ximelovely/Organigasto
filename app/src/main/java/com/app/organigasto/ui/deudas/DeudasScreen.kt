package com.app.organigasto.ui.deudas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.organigasto.ui.theme.*
import com.app.organigasto.ui.movimientos.MovimientosViewModel
import com.app.organigasto.data.local.entity.DeudaEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeudasScreen(viewModel: MovimientosViewModel) {
    val deudas by viewModel.deudas.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Cuentas Pendientes", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }, containerColor = MaterialTheme.colorScheme.primary) {
                Icon(Icons.Default.Add, contentDescription = "Añadir", tint = MaterialTheme.colorScheme.onPrimary)
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    val meDeben = deudas.filter { it.esPrestamo }.sumOf { it.monto }
                    val yoDebo = deudas.filter { !it.esPrestamo }.sumOf { it.monto }
                    
                    DeudaSummaryCard("Me deben", meDeben, Color(0xFF4CAF50), Modifier.weight(1f))
                    DeudaSummaryCard("Yo debo", yoDebo, Color(0xFFF44336), Modifier.weight(1f))
                }
            }

            items(deudas) { deuda ->
                DeudaItem(deuda, onLiquidada = { viewModel.liquidarDeuda(deuda.id) })
            }
        }

        if (showAddDialog) {
            AddDeudaDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { persona, monto, esPrestamo ->
                    viewModel.agregarDeuda(persona, monto, esPrestamo)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun DeudaSummaryCard(label: String, amount: Double, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier, 
        shape = RoundedCornerShape(20.dp), 
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
            Text("$${String.format("%.0f", amount)}", fontSize = 20.sp, fontWeight = FontWeight.Black, color = color)
        }
    }
}

@Composable
fun DeudaItem(deuda: DeudaEntity, onLiquidada: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(40.dp).background((if (deuda.esPrestamo) Color(0xFFE8F5E9) else Color(0xFFFBE9E7)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (deuda.esPrestamo) Icons.Default.CallMade else Icons.Default.CallReceived,
                    contentDescription = null,
                    tint = if (deuda.esPrestamo) Color(0xFF4CAF50) else Color(0xFFF44336)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(deuda.persona, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(if (deuda.esPrestamo) "Préstamo realizado" else "Deuda pendiente", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("$${deuda.monto}", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                TextButton(onClick = onLiquidada, contentPadding = PaddingValues(0.dp)) {
                    Text("Liquidar", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun AddDeudaDialog(onDismiss: () -> Unit, onConfirm: (String, Double, Boolean) -> Unit) {
    var persona by remember { mutableStateOf("") }
    var monto by remember { mutableStateOf("") }
    var esPrestamo by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Deuda/Préstamo", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
        containerColor = MaterialTheme.colorScheme.surface,
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = persona, 
                    onValueChange = { persona = it }, 
                    label = { Text("Persona") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                )
                OutlinedTextField(
                    value = monto, 
                    onValueChange = { monto = it }, 
                    label = { Text("Monto") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = esPrestamo, 
                        onClick = { esPrestamo = true },
                        colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                    )
                    Text("Me deben", color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(
                        selected = !esPrestamo, 
                        onClick = { esPrestamo = false },
                        colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                    )
                    Text("Yo debo", color = MaterialTheme.colorScheme.onSurface)
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(persona, monto.toDoubleOrNull() ?: 0.0, esPrestamo) }) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
