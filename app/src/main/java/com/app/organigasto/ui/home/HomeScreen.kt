package com.app.organigasto.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.organigasto.ui.theme.PurpuraPrimario
import com.app.organigasto.ui.theme.PurpuraSecundario
import com.app.organigasto.ui.theme.Black
import com.app.organigasto.ui.theme.GrayText
import com.app.organigasto.ui.theme.CremaFondo
import com.app.organigasto.ui.movimientos.MovimientosViewModel
import com.app.organigasto.data.local.entity.MovimientoEntity
import com.app.organigasto.domain.model.TipoMovimiento

@Composable
fun HomeScreen(
    onAddMovementClick: () -> Unit,
    onBudgetEditClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onAccountClick: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: MovimientosViewModel
) {
    val movimientos by viewModel.movimientosFiltrados.collectAsState()
    val balance by viewModel.balanceTotal.collectAsState()
    val filtro by viewModel.filtroTiempo.collectAsState()
    val query by viewModel.busqueda.collectAsState()
    val gastosPorCategoria by viewModel.gastosPorCategoria.collectAsState()
    val movimientosRecurrentes by viewModel.movimientosRecurrentes.collectAsState()
    val cuentas by viewModel.cuentas.collectAsState()
    val metasAhorro by viewModel.metasAhorro.collectAsState() // Añadido

    Scaffold(
        topBar = { HomeTopBar(onCalendarClick, onAccountClick, onSettingsClick) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddMovementClick,
                containerColor = PurpuraPrimario,
                contentColor = Color.White,
                shape = CircleShape
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
            
            // Header Card
            item {
                val totalCuentas = cuentas.sumOf { it.saldoActual }
                HeaderBalanceCard(totalCuentas, filtro)
            }

            // Cuentas Section
            item {
                AccountsSection(cuentas)
            }

            // Presupuesto por categoria
            if (gastosPorCategoria.isNotEmpty()) {
                item {
                    BudgetSection(gastosPorCategoria, onBudgetEditClick)
                }
            }

            // Ingresos recurrentes
            val ingresosRecurrentes = movimientosRecurrentes.filter { it.tipo == TipoMovimiento.INGRESO }
            if (ingresosRecurrentes.isNotEmpty()) {
                item {
                    RecurrentMovementsSection("Ingresos Programados", ingresosRecurrentes)
                }
            }

            // Gastos recurrentes
            val gastosRecurrentes = movimientosRecurrentes.filter { it.tipo == TipoMovimiento.GASTO }
            if (gastosRecurrentes.isNotEmpty()) {
                item {
                    RecurrentMovementsSection("Gastos Programados", gastosRecurrentes)
                }
            }

            // Meta de ahorro
            if (metasAhorro.isNotEmpty()) {
                item {
                    SavingsGoalSection(
                        metas = metasAhorro,
                        onAbonar = { id, monto -> viewModel.abonarAMeta(id, monto) },
                        onEditMeta = { id, monto -> viewModel.actualizarMeta(id, monto) }
                    )
                }
            }

            // Lista de movimientos recientes
            item {
                Text(
                    "Movimientos recientes",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            items(movimientos.size) { index ->
                val movimiento = movimientos[index]
                MovementItem(
                    movimiento = movimiento,
                    onDelete = { viewModel.eliminarMovimiento(movimiento) }
                )
            }
            
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun MovementItem(movimiento: MovimientoEntity, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (movimiento.tipo == TipoMovimiento.INGRESO) Color(0xFFE8F5E9) else Color(0xFFFBE9E7),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (movimiento.tipo == TipoMovimiento.INGRESO) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = if (movimiento.tipo == TipoMovimiento.INGRESO) Color(0xFF4CAF50) else Color(0xFFF44336)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(movimiento.cuenta, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(movimiento.fecha.toString(), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (movimiento.tipo == TipoMovimiento.INGRESO) "+" else "-"}$${movimiento.monto}",
                    color = if (movimiento.tipo == TipoMovimiento.INGRESO) Color(0xFF4CAF50) else Color(0xFFF44336),
                    fontWeight = FontWeight.Bold
                )
                if (movimiento.moneda != "MXN") {
                    Text(
                        text = "(${movimiento.montoOriginal} ${movimiento.moneda})",
                        fontSize = 9.sp,
                        color = Color.Gray
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsSection(cuentas: List<com.app.organigasto.data.local.entity.CuentaEntity>) {
    Column {
        Text(
            "Mis Cuentas",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(cuentas.size) { index ->
                val cuenta = cuentas[index]
                AccountCard(cuenta)
            }
        }
    }
}

@Composable
fun AccountCard(cuenta: com.app.organigasto.data.local.entity.CuentaEntity) {
    val cardColor = if (cuenta.tipo == "Debito") PurpuraSecundario else PurpuraPrimario
    Card(
        modifier = Modifier.width(180.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (cuenta.tipo == "Efectivo") Icons.Default.Payments else Icons.Default.CreditCard,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = cuenta.tipo,
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "$${String.format("%.2f", cuenta.saldoActual)}",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Text(
                text = cuenta.nombre,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
    @Composable
fun CustomSearchBar(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Buscar movimientos...", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        trailingIcon = { if (query.isNotEmpty()) IconButton(onClick = { onQueryChange("") }) { Icon(Icons.Default.Close, contentDescription = null) } },
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
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
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        },
        navigationIcon = {
            IconButton(onClick = onAccountClick) {
                Icon(Icons.Default.AccountCircle, contentDescription = "Perfil", tint = MaterialTheme.colorScheme.primary)
            }
        },
        actions = {
            IconButton(onClick = onCalendarClick) {
                Icon(Icons.Default.CalendarMonth, contentDescription = "Calendario", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Default.Settings, contentDescription = "Ajustes", tint = MaterialTheme.colorScheme.primary)
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.primary,
            navigationIconContentColor = MaterialTheme.colorScheme.primary,
            actionIconContentColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
fun HeaderBalanceCard(balance: Double, filtro: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(PurpuraSecundario, Color(0xFF2D244F))
                    )
                )
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "BALANCE TOTAL ${filtro.uppercase()}",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "$${String.format("%.2f", balance)}",
                    color = Color.White,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = Color.White.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Gastos bajo control",
                        color = Color.White,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun BudgetSection(
    gastos: List<com.app.organigasto.ui.movimientos.GastoCategoria>,
    onEditClick: () -> Unit // Añadido
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Tu presupuesto por categoria",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            IconButton(onClick = onEditClick) {
                Icon(Icons.Default.Edit, contentDescription = "Editar presupuestos", tint = MaterialTheme.colorScheme.primary)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        gastos.forEach { gasto ->
            BudgetBar(gasto.nombre, gasto.gastado.toFloat(), gasto.presupuesto.toFloat())
        }
    }
}

@Composable
fun BudgetBar(label: String, current: Float, total: Float) {
    val progress = if (total > 0) current / total else 0f
    val barColor = when {
        progress >= 1.0f -> Color(0xFFF44336)
        progress >= 0.8f -> Color(0xFFFF9800)
        else -> MaterialTheme.colorScheme.primary
    }

    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Text("$$current de $$total", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = barColor,
            trackColor = barColor.copy(alpha = 0.1f),
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        )
        if (progress >= 0.8f) {
            Text(
                text = if (progress >= 1.0f) "¡Presupuesto agotado!" else "Cerca del límite (80%)",
                color = barColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
fun RecurrentMovementsSection(title: String, movimientos: List<MovimientoEntity>) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            TextButton(onClick = { }) {
                Text("Ver todos", color = MaterialTheme.colorScheme.primary)
            }
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(movimientos.size) { index ->
                val mov = movimientos[index]
                
                RecurrentItemCard(
                    title = mov.nota ?: "Programado",
                    amount = "$${mov.monto}",
                    date = "Siguiente: ${mov.fecha}",
                    icon = if (mov.tipo == TipoMovimiento.INGRESO) Icons.AutoMirrored.Filled.TrendingUp else Icons.Default.Repeat
                )
            }
        }
    }
}

@Composable
fun RecurrentItemCard(title: String, amount: String, date: String, icon: ImageVector) {
    Card(
        modifier = Modifier.width(160.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(amount, fontSize = 20.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
            Text(date, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
    }
}

@Composable
fun SavingsGoalSection(
    metas: List<com.app.organigasto.data.local.entity.MetaAhorroEntity>,
    onAbonar: (Long, Double) -> Unit,
    onEditMeta: (Long, Double) -> Unit
) {
    Column {
        Text(
            "Metas de ahorro",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        metas.forEach { meta ->
            SavingsGoalItem(meta, onAbonar, onEditMeta)
        }
    }
}

@Composable
fun SavingsGoalItem(
    meta: com.app.organigasto.data.local.entity.MetaAhorroEntity,
    onAbonar: (Long, Double) -> Unit,
    onEditMeta: (Long, Double) -> Unit
) {
    var showAbonarDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp).clickable { showEditDialog = true },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                            .background(Color(0xFFE8F5E9), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Savings, contentDescription = null, tint = Color(0xFF4CAF50))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(meta.nombre, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
                }
                Row {
                    IconButton(
                        onClick = { showAbonarDialog = true },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Ajustar monto", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$${String.format("%.2f", meta.montoActual)} de $${String.format("%.2f", meta.montoObjetivo)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${if (meta.montoObjetivo > 0) (meta.montoActual / meta.montoObjetivo * 100).toInt() else 0}%",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { (if (meta.montoObjetivo > 0) meta.montoActual / meta.montoObjetivo else 0.0).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(10.dp),
                color = Color(0xFF4CAF50),
                trackColor = Color(0xFF4CAF50).copy(alpha = 0.1f),
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        }
    }

    if (showAbonarDialog) {
        var montoInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAbonarDialog = false },
            title = { Text("Ajustar ahorro", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Ingresa el monto (positivo para añadir, negativo para retirar)", fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = montoInput,
                        onValueChange = { montoInput = it },
                        placeholder = { Text("0.00") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    montoInput.toDoubleOrNull()?.let { onAbonar(meta.id, it) }
                    showAbonarDialog = false
                }) { Text("Aplicar") }
            },
            dismissButton = { TextButton(onClick = { showAbonarDialog = false }) { Text("Cancelar") } }
        )
    }

    if (showEditDialog) {
        var objetivoInput by remember { mutableStateOf(meta.montoObjetivo.toString()) }
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Editar meta de ahorro", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Nuevo objetivo de ahorro:", fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = objetivoInput,
                        onValueChange = { objetivoInput = it },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    objetivoInput.toDoubleOrNull()?.let { onEditMeta(meta.id, it) }
                    showEditDialog = false
                }) { Text("Guardar") }
            },
            dismissButton = { TextButton(onClick = { showEditDialog = false }) { Text("Cancelar") } }
        )
    }
}
