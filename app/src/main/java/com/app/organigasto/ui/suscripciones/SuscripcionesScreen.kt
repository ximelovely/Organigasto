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
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.draw.clip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuscripcionesScreen(viewModel: MovimientosViewModel) {
    val suscripciones by viewModel.suscripciones.collectAsState()
    val suscripcionesPausadas by viewModel.suscripcionesPausadas.collectAsState()
    val categorias by viewModel.categorias.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingSuscripcion by remember { mutableStateOf<Suscripcion?>(null) }
    var mostrarPausadas by remember { mutableStateOf(false) }

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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
                            .padding(3.dp)
                    ) {
                        listOf(false to "Activas", true to "Pausadas (${suscripcionesPausadas.size})")
                            .forEach { (esPausadas, label) ->
                                val seleccionado = mostrarPausadas == esPausadas
                                Text(
                                    label,
                                    textAlign = TextAlign.Center,
                                    fontSize = 13.sp,
                                    fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal,
                                    color = if (seleccionado) MaterialTheme.colorScheme.onSurface
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (seleccionado) MaterialTheme.colorScheme.surface else Color.Transparent)
                                        .clickable { mostrarPausadas = esPausadas }
                                        .padding(vertical = 6.dp)
                                )
                            }
                    }
                }

                item {
                    Text(
                        if (mostrarPausadas) "Pagos Pausados" else "Pagos Recurrentes Activos",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 17.sp,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }

                val listaActual = if (mostrarPausadas) suscripcionesPausadas else suscripciones

                if (listaActual.isEmpty()) {
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
                    items(listaActual) { suscripcion ->
                        val cat = suscripcion.categoriaId?.let { catMap[it] }
                        SuscripcionItem(
                            suscripcion = suscripcion,
                            categoriaColorHex = cat?.colorHex,
                            categoriaIconoNombre = cat?.iconoNombre,
                            categoriaIconoUri = cat?.iconoUri,
                            onDelete = { viewModel.eliminarSuscripcion(suscripcion.id) },
                            onEdit = { editingSuscripcion = suscripcion },
                            onTogglePausa = { viewModel.togglePausaSuscripcion(suscripcion) },
                            estaPausada = mostrarPausadas
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(40.dp)) }
            }

            if (showAddDialog) {
                AddSuscripcionDialog(
                    categorias = categorias,
                    onDismiss = { showAddDialog = false },
                    onConfirm = { nombre, monto, catId, frecuencia, fecha, automatica, recordatorio, dias ->
                        viewModel.agregarSuscripcion(
                            nombre = nombre,
                            costo = monto,
                            categoriaId = catId,
                            frecuencia = frecuencia,
                            fechaProximoCobro = fecha,
                            esAutomatica = automatica,
                            activarRecordatorio = recordatorio,
                            diasAviso = dias
                        )
                        showAddDialog = false
                    }
                )
            }
            editingSuscripcion?.let { sub ->
                EditSuscripcionDialog(
                    suscripcion = sub,
                    categorias = categorias,
                    onDismiss = { editingSuscripcion = null },
                    onConfirm = { actualizada ->
                        viewModel.actualizarSuscripcion(actualizada)
                        editingSuscripcion = null
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
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onTogglePausa: () -> Unit,
    estaPausada: Boolean = false
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

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "$${String.format(java.util.Locale.US, "%.0f", suscripcion.costo)}",
                    fontWeight = FontWeight.ExtraBold,
                    color = itemColor,
                    fontSize = 18.sp
                )
                Row {
                    IconButton(
                        onClick = onTogglePausa,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            if (estaPausada) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = if (estaPausada) "Reactivar" else "Pausar",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
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
    onConfirm: (String, Double, Int?, String, String, Boolean, Boolean, Int) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var monto by remember { mutableStateOf("") }
    var selectedCatId by remember { mutableStateOf<Int?>(categorias.firstOrNull()?.id) }
    var catMenuExpanded by remember { mutableStateOf(false) }

    var frecuencia by remember { mutableStateOf("MENSUAL") }

    var fecha by remember { mutableStateOf(java.time.LocalDate.now().toString()) }
    var showDatePicker by remember { mutableStateOf(false) }
    val context = LocalContext.current

    var esAutomatica by remember { mutableStateOf(true) }
    var activarRecordatorio by remember { mutableStateOf(true) }
    var diasAviso by remember { mutableStateOf("1") }

    val selectedCatNombre = categorias.find { it.id == selectedCatId }?.nombre ?: "Sin categoría"

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

                // Selector de categoría
                ExposedDropdownMenuBox(
                    expanded = catMenuExpanded,
                    onExpandedChange = { catMenuExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedCatNombre,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoría") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catMenuExpanded) },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = catMenuExpanded,
                        onDismissRequest = { catMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Sin categoría") },
                            onClick = { selectedCatId = null; catMenuExpanded = false }
                        )
                        categorias.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.nombre) },
                                onClick = { selectedCatId = cat.id; catMenuExpanded = false }
                            )
                        }
                    }
                }

                // Selector de frecuencia
                Column {
                    Text(
                        "Frecuencia",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    ) {
                        listOf("SEMANAL" to "Semanal", "QUINCENAL" to "Quincenal", "MENSUAL" to "Mensual")
                            .forEach { (value, label) ->
                                FilterChip(
                                    selected = frecuencia == value,
                                    onClick = { frecuencia = value },
                                    label = { Text(label) }
                                )
                            }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true }
                ) {
                    OutlinedTextField(
                        value = fecha,
                        onValueChange = {},
                        enabled = false,
                        label = { Text("Próximo cobro") },
                        trailingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Elegir fecha") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (showDatePicker) {
                    val fechaActual = runCatching { java.time.LocalDate.parse(fecha) }
                        .getOrDefault(java.time.LocalDate.now())

                    DisposableEffect(Unit) {
                        val dialog = android.app.DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                fecha = java.time.LocalDate.of(year, month + 1, day).toString()
                                showDatePicker = false
                            },
                            fechaActual.year,
                            fechaActual.monthValue - 1,
                            fechaActual.dayOfMonth
                        )
                        dialog.setOnCancelListener { showDatePicker = false }
                        dialog.show()
                        onDispose { dialog.dismiss() }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cobro automático", modifier = Modifier.weight(1f))
                    Switch(checked = esAutomatica, onCheckedChange = { esAutomatica = it })
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Activar recordatorio", modifier = Modifier.weight(1f))
                    Switch(checked = activarRecordatorio, onCheckedChange = { activarRecordatorio = it })
                }

                if (activarRecordatorio) {
                    OutlinedTextField(
                        value = diasAviso,
                        onValueChange = { if (it.isEmpty() || it.toIntOrNull() != null) diasAviso = it },
                        label = { Text("Días de aviso antes del cobro") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nombre.isNotBlank()) {
                        onConfirm(
                            nombre,
                            monto.toDoubleOrNull() ?: 0.0,
                            selectedCatId,
                            frecuencia,
                            fecha,
                            esAutomatica,
                            activarRecordatorio,
                            diasAviso.toIntOrNull() ?: 1
                        )
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSuscripcionDialog(
    suscripcion: Suscripcion,
    categorias: List<com.app.organigasto.data.local.entity.Categoria>,
    onDismiss: () -> Unit,
    onConfirm: (Suscripcion) -> Unit
) {
    var nombre by remember { mutableStateOf(suscripcion.nombre) }
    var monto by remember { mutableStateOf(suscripcion.costo.toString()) }
    var selectedCatId by remember { mutableStateOf(suscripcion.categoriaId) }
    var catMenuExpanded by remember { mutableStateOf(false) }

    var frecuencia by remember { mutableStateOf(suscripcion.frecuencia) }

    var fecha by remember { mutableStateOf(suscripcion.fechaProximoCobro) }
    var showDatePicker by remember { mutableStateOf(false) }
    val context = LocalContext.current

    var esAutomatica by remember { mutableStateOf(suscripcion.esAutomatica) }
    var activarRecordatorio by remember { mutableStateOf(suscripcion.activarRecordatorio) }
    var diasAviso by remember { mutableStateOf(suscripcion.diasAviso.toString()) }

    val selectedCatNombre = categorias.find { it.id == selectedCatId }?.nombre ?: "Sin categoría"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Editar Suscripción",
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

                ExposedDropdownMenuBox(
                    expanded = catMenuExpanded,
                    onExpandedChange = { catMenuExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedCatNombre,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoría") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catMenuExpanded) },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = catMenuExpanded,
                        onDismissRequest = { catMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Sin categoría") },
                            onClick = { selectedCatId = null; catMenuExpanded = false }
                        )
                        categorias.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.nombre) },
                                onClick = { selectedCatId = cat.id; catMenuExpanded = false }
                            )
                        }
                    }
                }

                Column {
                    Text(
                        "Frecuencia",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    ) {
                        listOf("SEMANAL" to "Semanal", "QUINCENAL" to "Quincenal", "MENSUAL" to "Mensual")
                            .forEach { (value, label) ->
                                FilterChip(
                                    selected = frecuencia == value,
                                    onClick = { frecuencia = value },
                                    label = { Text(label) }
                                )
                            }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true }
                ) {
                    OutlinedTextField(
                        value = fecha,
                        onValueChange = {},
                        enabled = false,
                        label = { Text("Próximo cobro") },
                        trailingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Elegir fecha") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (showDatePicker) {
                    val fechaActual = runCatching { java.time.LocalDate.parse(fecha) }
                        .getOrDefault(java.time.LocalDate.now())

                    DisposableEffect(Unit) {
                        val dialog = android.app.DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                fecha = java.time.LocalDate.of(year, month + 1, day).toString()
                                showDatePicker = false
                            },
                            fechaActual.year,
                            fechaActual.monthValue - 1,
                            fechaActual.dayOfMonth
                        )
                        dialog.setOnCancelListener { showDatePicker = false }
                        dialog.show()
                        onDispose { dialog.dismiss() }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cobro automático", modifier = Modifier.weight(1f))
                    Switch(checked = esAutomatica, onCheckedChange = { esAutomatica = it })
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Activar recordatorio", modifier = Modifier.weight(1f))
                    Switch(checked = activarRecordatorio, onCheckedChange = { activarRecordatorio = it })
                }

                if (activarRecordatorio) {
                    OutlinedTextField(
                        value = diasAviso,
                        onValueChange = { if (it.isEmpty() || it.toIntOrNull() != null) diasAviso = it },
                        label = { Text("Días de aviso antes del cobro") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nombre.isNotBlank()) {
                        onConfirm(
                            suscripcion.copy(
                                nombre = nombre,
                                costo = monto.toDoubleOrNull() ?: suscripcion.costo,
                                categoriaId = selectedCatId,
                                frecuencia = frecuencia,
                                fechaProximoCobro = fecha,
                                esAutomatica = esAutomatica,
                                activarRecordatorio = activarRecordatorio,
                                diasAviso = diasAviso.toIntOrNull() ?: suscripcion.diasAviso
                            )
                        )
                    }
                },
                enabled = nombre.isNotBlank(),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PurpuraPrimario)
            ) {
                Text("GUARDAR CAMBIOS", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCELAR") }
        }
    )
}