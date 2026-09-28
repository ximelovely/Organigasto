package com.app.organigasto.ui.suscripciones

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.organigasto.data.local.entity.Suscripcion
import com.app.organigasto.ui.categorias.CategoryIconView
import com.app.organigasto.ui.categorias.parseCategoryColor
import com.app.organigasto.ui.components.AnimatedFinanceBackground
import com.app.organigasto.ui.movimientos.MovimientosViewModel
import com.app.organigasto.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuscripcionesScreen(viewModel: MovimientosViewModel) {
    val suscripciones by viewModel.suscripciones.collectAsState()
    val categorias by viewModel.categorias.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    val catMap = remember(categorias) { categorias.associateBy { it.id } }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        AnimatedFinanceBackground(modifier = Modifier.fillMaxSize())

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Mis Suscripciones",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = PurpuraPrimario,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir Suscripción")
                }
            },
            containerColor = Color.Transparent
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }

                item {
                    TotalSuscripcionesCard(
                        total = suscripciones.sumOf { it.costo },
                        count = suscripciones.size
                    )
                }

                item {
                    Text(
                        "Pagos Recurrentes Activos",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 17.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (suscripciones.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Default.Subscriptions,
                                        contentDescription = null,
                                        tint = PurpuraPrimario.copy(alpha = 0.4f),
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "No tienes suscripciones registradas",
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    items(suscripciones) { suscripcion ->
                        val cat = suscripcion.categoriaId?.let { catMap[it] }
                        SuscripcionItem(
                            suscripcion = suscripcion,
                            categoriaColorHex = cat?.colorHex,
                            categoriaIconoNombre = cat?.iconoNombre,
                            categoriaIconoUri = cat?.iconoUri,
                            onDelete = { viewModel.eliminarSuscripcion(suscripcion.id) }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(40.dp)) }
            }

            if (showAddDialog) {
                AddSuscripcionDialog(
                    categorias = categorias,
                    onDismiss = { showAddDialog = false },
                    onConfirm = { nombre, monto, catId ->
                        viewModel.agregarSuscripcion(nombre, monto)
                        showAddDialog = false
                    }
                )
            }
        }
    }
}

@Composable
fun TotalSuscripcionesCard(total: Double, count: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = PurpuraPrimario),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "GASTO MENSUAL FIJO",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "$${String.format(java.util.Locale.US, "%.2f", total)}",
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                color = Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "$count servicio${if (count != 1) "s" else ""} activo${if (count != 1) "s" else ""}",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun SuscripcionItem(
    suscripcion: Suscripcion,
    categoriaColorHex: String?,
    categoriaIconoNombre: String?,
    categoriaIconoUri: String?,
    onDelete: () -> Unit
) {
    val itemColor = parseCategoryColor(categoriaColorHex, PurpuraPrimario)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(itemColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                CategoryIconView(
                    iconoNombre = categoriaIconoNombre ?: "suscripciones",
                    iconoUri = categoriaIconoUri,
                    tint = Color.White,
                    size = 24.dp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    suscripcion.nombre,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp
                )
                Text(
                    "Próximo cobro: ${suscripcion.fechaProximoCobro}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "$${String.format(java.util.Locale.US, "%.0f", suscripcion.costo)}",
                    fontWeight = FontWeight.ExtraBold,
                    color = itemColor,
                    fontSize = 18.sp
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSuscripcionDialog(
    categorias: List<com.app.organigasto.data.local.entity.Categoria>,
    onDismiss: () -> Unit,
    onConfirm: (String, Double, Int?) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var monto by remember { mutableStateOf("") }
    var selectedCatId by remember { mutableStateOf<Int?>(categorias.firstOrNull()?.id) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Nueva Suscripción",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre del servicio") },
                    placeholder = { Text("Ej: Netflix, Gym, Internet") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = monto,
                    onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) monto = it },
                    label = { Text("Costo mensual") },
                    prefix = { Text("$ ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nombre.isNotBlank()) {
                        onConfirm(nombre, monto.toDoubleOrNull() ?: 0.0, selectedCatId)
                    }
                },
                enabled = nombre.isNotBlank(),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PurpuraPrimario)
            ) {
                Text("GUARDAR", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCELAR") }
        }
    )
}
