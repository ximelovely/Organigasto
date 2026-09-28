package com.app.organigasto.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.organigasto.ui.movimientos.MovimientosViewModel
import com.app.organigasto.data.local.entity.Gasto
import com.app.organigasto.domain.utils.CategoryBudgetStatus
import com.app.organigasto.domain.utils.CategoryForecast
import com.app.organigasto.domain.utils.ProyeccionSaldo
import com.app.organigasto.domain.utils.ReporteVolatilidad
import com.app.organigasto.ui.theme.*
import com.app.organigasto.ui.movimientos.MetaAhorro
import com.app.organigasto.ui.categorias.CategoryIconView
import com.app.organigasto.ui.categorias.parseCategoryColor
import com.app.organigasto.ui.presupuesto.EditGastoDialog

import androidx.compose.ui.platform.LocalClipboardManager

@Composable
fun HomeScreen(
    onAddMovementClick: () -> Unit,
    onBudgetEditClick: () -> Unit,
    onAddCategoryClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onAccountClick: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: MovimientosViewModel
) {
    val gastos by viewModel.gastos.collectAsState()
    val presupuestoStatus by viewModel.presupuestoStatus.collectAsState()
    val proyeccion by viewModel.proyeccionSaldo.collectAsState()
    val metasAhorro by viewModel.metasAhorro.collectAsState()
    val forecasts by viewModel.categoryForecasts.collectAsState()
    val volatilidad by viewModel.reporteVolatilidad.collectAsState()
    val gastoDiarioMap by viewModel.gastoDiarioStatus.collectAsState()
    val diasHastaSueldo by viewModel.diasHastaProximoSueldo.collectAsState()
    val query by viewModel.busqueda.collectAsState()
    val categorias by viewModel.categorias.collectAsState()

    val clipboardManager = LocalClipboardManager.current
    var quickEntryData by remember { mutableStateOf<Pair<Double, String>?>(null) }
    var editingGasto by remember { mutableStateOf<Gasto?>(null) }

    LaunchedEffect(Unit) {
        val text = clipboardManager.getText()?.text
        if (text != null && text.contains("$")) {
            quickEntryData = viewModel.parsearTextoGasto(text)
        }
    }

    if (quickEntryData != null) {
        AlertDialog(
            onDismissRequest = { quickEntryData = null },
            title = { Text("¿Registrar gasto detectado?") },
            text = { 
                Column {
                    Text("Detectamos: $${quickEntryData!!.first} en '${quickEntryData!!.second}'")
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Selecciona un sobre:", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                        items(categorias) { cat ->
                            val catColor = parseCategoryColor(cat.colorHex)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.agregarGasto(quickEntryData!!.first, cat.id, quickEntryData!!.second)
                                        quickEntryData = null
                                    }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(catColor, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CategoryIconView(
                                        iconoNombre = cat.iconoNombre,
                                        iconoUri = cat.iconoUri,
                                        tint = Color.White,
                                        size = 16.dp
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(cat.nombre, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { quickEntryData = null }) { Text("Ignorar") }
            }
        )
    }

    Scaffold(
        topBar = { HomeTopBar(onCalendarClick, onAccountClick, onSettingsClick) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddMovementClick,
                containerColor = PurpuraPrimario,
                contentColor = Color.White,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                CustomSearchBar(query) { viewModel.buscar(it) }
            }
            
            // Header Card con Proyección y Gradiente
            item {
                HeaderBalanceCard(proyeccion)
            }

            // Dashboard de Inteligencia (Volatilidad y Gasto Diario)
            item {
                val totalGastoDiarioSobres = gastoDiarioMap.values.sum()
                val gastoDiarioSaldoReal = proyeccion.saldoActual.coerceAtLeast(0.0) / diasHastaSueldo.coerceAtLeast(1)

                val gastoDiarioMostrar = if (totalGastoDiarioSobres > 0) {
                    totalGastoDiarioSobres
                } else {
                    gastoDiarioSaldoReal
                }

                IntelligenceDashboard(
                    reporte = volatilidad,
                    gastoDiarioTotal = gastoDiarioMostrar,
                    diasHastaSueldo = diasHastaSueldo
                )
            }

            // Alertas de Salud Financiera (Forecasting)
            val criticalForecasts = forecasts.filter { it.agotamientoAntesDeSueldo }
            if (criticalForecasts.isNotEmpty()) {
                item {
                    ForecastingAlertsSection(criticalForecasts)
                }
            }

            // Presupuesto por categoria (Sobres)
            item {
                BudgetSection(
                    status = presupuestoStatus,
                    onEditClick = onBudgetEditClick,
                    onAddCategoryClick = onAddCategoryClick
                )
            }

            // Metas de Ahorro
            if (metasAhorro.isNotEmpty()) {
                item {
                    SavingsGoalsSection(
                        metas = metasAhorro,
                        onAbonar = { id, monto -> viewModel.abonarAMeta(id, monto) },
                        onAddMeta = { nombre, monto -> viewModel.registrarMetaAhorro(nombre, monto) }
                    )
                }
            }

            // Sobrantes a Ahorro Widget
            val totalSobranteSobres = presupuestoStatus.sumOf { it.restante.coerceAtLeast(0.0) }
            if (metasAhorro.isNotEmpty() && totalSobranteSobres > 0) {
                item {
                    SweepToSavingsWidget(
                        totalSobrante = totalSobranteSobres,
                        metasAhorro = metasAhorro,
                        onAbonar = { id, monto -> viewModel.abonarAMeta(id, monto) }
                    )
                }
            }

            // Provisión Recomendada (Sinking Funds logic)
            val totalFaltanteAhorros = metasAhorro.sumOf { it.faltante }
            if (totalFaltanteAhorros > 0) {
                item {
                    SinkingFundWidget(totalFaltanteAhorros / 12) // Sugerencia simple a 1 año
                }
            }

            // Lista de gastos recientes
            item {
                Text(
                    "Movimientos Recientes",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = PurpuraSecundario
                )
            }

            items(gastos) { gasto ->
                GastoItem(
                    gasto = gasto,
                    onDelete = { viewModel.eliminarGasto(gasto) },
                    onEdit = { editingGasto = gasto }
                )
            }
            
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }

        if (editingGasto != null) {
            EditGastoDialog(
                gasto = editingGasto!!,
                onDismiss = { editingGasto = null },
                onConfirm = { updatedGasto ->
                    viewModel.actualizarGasto(updatedGasto)
                    editingGasto = null
                }
            )
        }
    }
}

@Composable
fun SweepToSavingsWidget(
    totalSobrante: Double,
    metasAhorro: List<MetaAhorro>,
    onAbonar: (Int, Double) -> Unit
) {
    var showSavingsDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = VerdeExito.copy(alpha = 0.12f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, VerdeExito.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(VerdeExito.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Savings, contentDescription = null, tint = VerdeExito)
                }

                Column {
                    Text("SOBRANTE DISPONIBLE", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = VerdeExito)
                    Text("Tienes $${String.format(java.util.Locale.US, "%.2f", totalSobrante)} sin gastar", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
            }

            Button(
                onClick = { showSavingsDialog = true },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VerdeExito, contentColor = Color.White),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("Guardar Ahorro", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showSavingsDialog) {
        var selectedMetaId by remember { mutableStateOf(metasAhorro.firstOrNull()?.id ?: 0) }
        var montoAhorro by remember { mutableStateOf(totalSobrante.toString()) }

        AlertDialog(
            onDismissRequest = { showSavingsDialog = false },
            title = { Text("Mover Sobrante a Ahorro", fontWeight = FontWeight.Bold) },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Transfiere el dinero no gastado a tu meta de ahorro preferida:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))

                    metasAhorro.forEach { meta ->
                        FilterChip(
                            selected = selectedMetaId == meta.id,
                            onClick = { selectedMetaId = meta.id },
                            label = { Text("${meta.nombre} ($${String.format(java.util.Locale.US, "%.0f", meta.montoActual)}/$${String.format(java.util.Locale.US, "%.0f", meta.montoObjetivo)})", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VerdeExito,
                                selectedLabelColor = Color.White
                            )
                        )
                    }

                    OutlinedTextField(
                        value = montoAhorro,
                        onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) montoAhorro = it },
                        label = { Text("Monto a guardar ($)") },
                        prefix = { Text("$ ") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val m = montoAhorro.toDoubleOrNull() ?: 0.0
                        if (m > 0 && selectedMetaId > 0) {
                            onAbonar(selectedMetaId, m)
                            showSavingsDialog = false
                        }
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VerdeExito)
                ) { Text("GUARDAR EN AHORRO", color = Color.White, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showSavingsDialog = false }) { Text("CANCELAR") }
            }
        )
    }
}

@Composable
fun SinkingFundWidget(cuotaSugerida: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = PurpuraClaro.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.AutoMode, contentDescription = null, tint = PurpuraPrimario)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("RESERVA MENSUAL RECOMENDADA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PurpuraPrimario)
                Text("Separa $${String.format(java.util.Locale.US, "%.0f", cuotaSugerida)} de tu sueldo para tus metas futuras.", fontSize = 13.sp, color = PurpuraSecundario)
            }
        }
    }
}

@Composable
fun GastoItem(
    gasto: Gasto,
    onDelete: () -> Unit,
    onEdit: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(RojoError.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                    contentDescription = null,
                    tint = RojoError,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = gasto.descripcion, 
                    fontWeight = FontWeight.Bold, 
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp
                )
                Text(
                    text = gasto.fecha, 
                    fontSize = 12.sp, 
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "-$${String.format(java.util.Locale.US, "%.2f", gasto.monto)}",
                    color = RojoError,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (onEdit != null) {
                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Editar",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CustomSearchBar(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        placeholder = { Text("Buscar gastos...", color = GrayText, fontSize = 14.sp) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PurpuraPrimario) },
        trailingIcon = { if (query.isNotEmpty()) IconButton(onClick = { onQueryChange("") }) { Icon(Icons.Default.Close, contentDescription = null, tint = PurpuraPrimario) } },
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PurpuraPrimario,
            unfocusedBorderColor = PurpuraPrimario.copy(alpha = 0.2f),
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        ),
        singleLine = true
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(onCalendarClick: () -> Unit, onAccountClick: () -> Unit, onSettingsClick: () -> Unit) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                "OrganiGasto",
                fontWeight = FontWeight.ExtraBold,
                color = PurpuraSecundario,
                letterSpacing = 0.5.sp
            )
        },
        navigationIcon = {
            IconButton(onClick = onAccountClick) {
                Icon(Icons.Default.AccountCircle, contentDescription = "Perfil", tint = PurpuraSecundario, modifier = Modifier.size(28.dp))
            }
        },
        actions = {
            IconButton(onClick = onCalendarClick) {
                Icon(Icons.Default.CalendarMonth, contentDescription = "Calendario", tint = PurpuraSecundario)
            }
            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Default.Settings, contentDescription = "Ajustes", tint = PurpuraSecundario)
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
        )
    )
}

@Composable
fun SavingsGoalsSection(
    metas: List<MetaAhorro>,
    onAbonar: (Int, Double) -> Unit,
    onAddMeta: (String, Double) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Metas de Ahorro",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = PurpuraSecundario
            )
            IconButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.AddCircle, contentDescription = "Nueva Meta", tint = PurpuraPrimario)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        metas.forEach { meta ->
            SavingsGoalItem(meta, onAbonar)
        }
    }

    if (showAddDialog) {
        var nombre by remember { mutableStateOf("") }
        var monto by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Nueva Meta de Ahorro") },
            text = {
                Column {
                    OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre (Ej: Viaje)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = monto, onValueChange = { monto = it }, label = { Text("Monto Objetivo") }, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                }
            },
            confirmButton = {
                TextButton(onClick = { 
                    if (nombre.isNotBlank() && monto.toDoubleOrNull() != null) {
                        onAddMeta(nombre, monto.toDoubleOrNull()!!)
                        showAddDialog = false
                    }
                }) { Text("Crear") }
            },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("Cancelar") } }
        )
    }
}

@Composable
fun SavingsGoalItem(
    meta: MetaAhorro,
    onAbonar: (Int, Double) -> Unit
) {
    var showAbonarDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(VerdeExito.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Savings, contentDescription = null, tint = VerdeExito)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = meta.nombre,
                        fontWeight = FontWeight.Bold,
                        color = PurpuraSecundario,
                        fontSize = 16.sp
                    )
                }
                IconButton(
                    onClick = { showAbonarDialog = true },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = PurpuraPrimario.copy(alpha = 0.1f))
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Abonar", tint = PurpuraPrimario)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$${String.format("%.0f", meta.montoActual)} de $${String.format("%.0f", meta.montoObjetivo)}",
                    fontSize = 13.sp,
                    color = GrayText
                )
                Text(
                    text = "${(meta.porcentaje * 100).toInt()}%",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = VerdeExito
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { meta.porcentaje.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = VerdeExito,
                trackColor = VerdeExito.copy(alpha = 0.1f),
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        }
    }

    if (showAbonarDialog) {
        var montoInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAbonarDialog = false },
            title = { Text("Abonar a meta", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = montoInput,
                    onValueChange = { montoInput = it },
                    label = { Text("Monto a guardar") },
                    prefix = { Text("$ ") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    montoInput.toDoubleOrNull()?.let { onAbonar(meta.id, it) }
                    showAbonarDialog = false
                }) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { showAbonarDialog = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
fun IntelligenceDashboard(reporte: ReporteVolatilidad, gastoDiarioTotal: Double, diasHastaSueldo: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Card de Gasto Diario
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("GASTO DIARIO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(4.dp))
                Text("$${String.format(java.util.Locale.US, "%.0f", gastoDiarioTotal)}", fontSize = 20.sp, fontWeight = FontWeight.Black)
                Text("disp. / día ($diasHastaSueldo días p/ cobro)", fontSize = 9.sp, color = GrayText)
            }
        }

        // Card de Estabilidad
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("ESTABILIDAD", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PurpuraSecundario)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${reporte.indiceEstabilidad}%", fontSize = 20.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (reporte.indiceEstabilidad > 70) Icons.Default.VerifiedUser else Icons.Default.NewReleases,
                        contentDescription = null,
                        tint = if (reporte.indiceEstabilidad > 70) VerdeExito else NaranjaAlerta,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text("Día crítico: ${reporte.diaMasCritico}", fontSize = 10.sp, color = GrayText)
            }
        }
    }
}

@Composable
fun ForecastingAlertsSection(forecasts: List<CategoryForecast>) {
    Column {
        Text(
            "Alertas de Salud Financiera",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = PurpuraSecundario
        )
        Spacer(modifier = Modifier.height(12.dp))
        forecasts.forEach { forecast ->
            ForecastingAlertItem(forecast)
        }
    }
}

@Composable
fun ForecastingAlertItem(forecast: CategoryForecast) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = RojoError.copy(alpha = 0.05f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, RojoError.copy(alpha = 0.2f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(RojoError.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = RojoError, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "¡Alerta en ${forecast.nombre}!",
                    fontWeight = FontWeight.Bold,
                    color = PurpuraSecundario,
                    fontSize = 14.sp
                )
                Text(
                    text = "Se agotará en aprox. ${forecast.diasRestantesEstimados} días.",
                    fontSize = 12.sp,
                    color = GrayText
                )
                if (forecast.sugerencia != null) {
                    Text(
                        text = forecast.sugerencia,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = PurpuraPrimario,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun HeaderBalanceCard(proyeccion: ProyeccionSaldo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(PurpuraPrimario, PurpuraSecundario)
                    )
                )
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "BALANCE ACTUAL",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "$${String.format("%.2f", proyeccion.saldoActual)}",
                    color = Color.White,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Proyectado: $${String.format("%.2f", proyeccion.saldoFinalProyectado)}",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BudgetSection(
    status: List<CategoryBudgetStatus>,
    onEditClick: () -> Unit,
    onAddCategoryClick: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Tus Sobres",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(
                    onClick = onAddCategoryClick,
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        tint = PurpuraPrimario,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Crear Sobre",
                        fontWeight = FontWeight.Bold,
                        color = PurpuraPrimario,
                        fontSize = 13.sp
                    )
                }

                IconButton(onClick = onEditClick) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Editar Sobres",
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (status.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(PurpuraPrimario.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Folder,
                            contentDescription = null,
                            tint = PurpuraPrimario,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        "¡Organiza tu dinero en sobres!",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        "Crea sobres para tus comidas, transporte, salidas o renta y asigna un presupuesto a tu gusto.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onAddCategoryClick,
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PurpuraPrimario,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("CREAR MI PRIMER SOBRE", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        } else {
            status.forEach { item ->
                BudgetBar(item = item, onClick = onEditClick)
            }
        }
    }
}

@Composable
fun BudgetBar(item: CategoryBudgetStatus, onClick: () -> Unit) {
    val progress = if (item.montoAsignado > 0) (item.montoGastado / item.montoAsignado).toFloat() else 0f
    val baseColor = parseCategoryColor(item.colorHex)
    val barColor = when {
        progress >= 1.0f -> RojoError
        progress >= 0.8f -> NaranjaAlerta
        else -> baseColor
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(bottom = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(baseColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    CategoryIconView(
                        iconoNombre = item.iconoNombre,
                        iconoUri = item.iconoUri,
                        tint = Color.White,
                        size = 18.dp
                    )
                }

                Text(
                    text = item.nombre,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp
                )
            }

            Text(
                text = if (item.montoAsignado > 0)
                    "$${String.format(java.util.Locale.US, "%.0f", item.montoGastado)} de $${String.format(java.util.Locale.US, "%.0f", item.montoAsignado)}"
                else
                    "$${String.format(java.util.Locale.US, "%.0f", item.montoGastado)} gastado",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .background(barColor.copy(alpha = 0.15f), RoundedCornerShape(5.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(
                        Brush.horizontalGradient(listOf(barColor.copy(alpha = 0.7f), barColor)),
                        RoundedCornerShape(5.dp)
                    )
            )
        }
        if (progress >= 0.9f && item.montoAsignado > 0) {
            Text(
                text = if (progress >= 1.0f) "¡Presupuesto agotado!" else "Cerca del límite",
                color = barColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
