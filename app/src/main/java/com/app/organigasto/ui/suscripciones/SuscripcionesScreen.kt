package com.app.organigasto.ui.suscripciones

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
import com.app.organigasto.data.local.entity.SuscripcionEntity
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuscripcionesScreen(viewModel: MovimientosViewModel) {
    val suscripciones by viewModel.suscripciones.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Mis Suscripciones", fontWeight = FontWeight.Bold, color = PurpuraSecundario) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = CremaFondo)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }, containerColor = PurpuraPrimario) {
                Icon(Icons.Default.Add, contentDescription = "Añadir", tint = Color.White)
            }
        },
        containerColor = CremaFondo
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                TotalSuscripcionesCard(suscripciones.sumOf { it.monto })
            }

            items(suscripciones) { suscripcion ->
                SuscripcionItem(suscripcion)
            }
        }

        if (showAddDialog) {
            AddSuscripcionDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { nombre, monto ->
                    viewModel.agregarSuscripcion(nombre, monto, LocalDate.now().plusMonths(1), 1)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun TotalSuscripcionesCard(total: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = PurpuraSecundario)
    ) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("TOTAL MENSUAL", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text("$${String.format("%.2f", total)}", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun SuscripcionItem(suscripcion: SuscripcionEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).background(PurpuraPrimario.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Subscriptions, contentDescription = null, tint = PurpuraPrimario)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(suscripcion.nombre, fontWeight = FontWeight.Bold, color = PurpuraSecundario)
                Text("Próximo cobro: ${suscripcion.proximoPago}", fontSize = 11.sp, color = Color.Gray)
            }
            Text("$${suscripcion.monto}", fontWeight = FontWeight.Black, color = PurpuraSecundario)
        }
    }
}

@Composable
fun AddSuscripcionDialog(onDismiss: () -> Unit, onConfirm: (String, Double) -> Unit) {
    var nombre by remember { mutableStateOf("") }
    var monto by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Suscripción", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") })
                OutlinedTextField(value = monto, onValueChange = { monto = it }, label = { Text("Monto mensual") })
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(nombre, monto.toDoubleOrNull() ?: 0.0) }) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
