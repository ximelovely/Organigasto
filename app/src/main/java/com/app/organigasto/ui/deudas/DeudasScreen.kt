package com.app.organigasto.ui.deudas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.app.organigasto.data.local.entity.PagoPrestamo
import com.app.organigasto.data.local.entity.Prestamo
import com.app.organigasto.ui.components.AnimatedFinanceBackground
import com.app.organigasto.ui.movimientos.MovimientosViewModel
import com.app.organigasto.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeudasScreen(
    viewModel: MovimientosViewModel,
    onAddDebtClick: (() -> Unit)? = null
) {
    val deudasReales by viewModel.deudasReales.collectAsState()
    val estrategia by viewModel.deudaEstrategia.collectAsState()
    var selectedPrestamo by remember { mutableStateOf<Prestamo?>(null) }
    var showStrategy by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        AnimatedFinanceBackground(modifier = Modifier.fillMaxSize())

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            if (selectedPrestamo == null) "Préstamos y Deudas" else "Detalle de Deuda",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    navigationIcon = {
                        if (selectedPrestamo != null) {
                            IconButton(onClick = { selectedPrestamo = null }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Volver",
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    },
                    actions = {
                        if (selectedPrestamo == null && deudasReales.size > 1) {
                            IconButton(onClick = { showStrategy = !showStrategy }) {
                                Icon(
                                    imageVector = if (showStrategy) Icons.Default.List else Icons.Default.AutoAwesome,
                                    contentDescription = "Estrategia",
                                    tint = PurpuraPrimario
                                )
                            }
                        }
                    }
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            if (selectedPrestamo == null) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(4.dp)) }

                    item {
                        val total = deudasReales.sumOf { it.montoOriginal }
                        DeudaSummaryCard("TOTAL EN COMPROMISOS", total, deudasReales.size)
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (showStrategy) "Estrategia Avalancha" else "Tus Préstamos Activos",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 17.sp
                            )
                            if (showStrategy) {
                                Surface(
                                    color = PurpuraPrimario.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        "Mayor interés primero",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PurpuraPrimario
                                    )
                                }
                            }
                        }
                    }

                    if (deudasReales.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(28.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            Icons.Default.AccountBalance,
                                            contentDescription = null,
                                            tint = PurpuraPrimario.copy(alpha = 0.4f),
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            "No tienes deudas o préstamos activos",
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        val listToDisplay = if (showStrategy) estrategia else deudasReales
                        items(listToDisplay) { prestamo ->
                            PrestamoItem(
                                prestamo = prestamo,
                                onClick = { selectedPrestamo = prestamo }
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.height(32.dp)) }
                }
            } else {
                val pagos by viewModel.observarPagosPrestamo(selectedPrestamo!!.id).collectAsState(initial = emptyList())
                AmortizationTable(selectedPrestamo!!, pagos, padding)
            }
        }
    }
}

@Composable
fun DeudaSummaryCard(label: String, amount: Double, count: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = PurpuraSecundario),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                label,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.6f),
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "$${String.format(java.util.Locale.US, "%.2f", amount)}",
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                color = Color.White.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "$count préstamo${if (count != 1) "s" else ""} registrado${if (count != 1) "s" else ""}",
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
fun PrestamoItem(prestamo: Prestamo, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
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
                    .background(PurpuraPrimario.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalance,
                    contentDescription = null,
                    tint = PurpuraPrimario,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    prestamo.nombre,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp
                )
                Text(
                    "${prestamo.meses} meses | Tasa: ${prestamo.tasaAnual}%",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "$${String.format(java.util.Locale.US, "%.0f", prestamo.montoOriginal)}",
                    fontWeight = FontWeight.ExtraBold,
                    color = PurpuraPrimario,
                    fontSize = 18.sp
                )
                Text(
                    "Tabla de pagos >",
                    fontSize = 10.sp,
                    color = PurpuraPrimario,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun AmortizationTable(prestamo: Prestamo, pagos: List<PagoPrestamo>, padding: PaddingValues) {
    var showSimulator by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .padding(padding)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            prestamo.nombre,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Tabla de Amortización (${prestamo.meses} meses)",
                            color = PurpuraPrimario,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = { showSimulator = true },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = PurpuraPrimario.copy(alpha = 0.1f))
                    ) {
                        Icon(Icons.Default.Calculate, contentDescription = "Simular", tint = PurpuraPrimario)
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Nº", fontWeight = FontWeight.Bold, modifier = Modifier.width(30.dp), color = MaterialTheme.colorScheme.onSurface)
                        Text("PAGO", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface)
                        Text("INTERÉS", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface)
                        Text("SALDO", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface)
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    if (pagos.isEmpty()) {
                        Text(
                            "No hay pagos registrados.",
                            fontSize = 13.sp,
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }

                    pagos.forEach { pago ->
                        PagoRow(pago)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }

    if (showSimulator) {
        DebtSimulatorDialog(prestamo, onDismiss = { showSimulator = false })
    }
}

@Composable
fun DebtSimulatorDialog(prestamo: Prestamo, onDismiss: () -> Unit) {
    var extraAmount by remember { mutableStateOf("") }

    val m = extraAmount.toDoubleOrNull() ?: 0.0
    val mesesAhorrados = if (m > 0) (m / (prestamo.montoOriginal / prestamo.meses)).toInt() else 0
    val interesAhorrado = if (m > 0) mesesAhorrados * (prestamo.montoOriginal * (prestamo.tasaAnual / 100) / 12) else 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Simulador de Pago Extra", fontWeight = FontWeight.Bold) },
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        text = {
            Column {
                Text(
                    "¿Cuánto dinero extra podrías abonar hoy a este préstamo?",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = extraAmount,
                    onValueChange = { extraAmount = it },
                    label = { Text("Monto abonado extra") },
                    prefix = { Text("$ ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )

                if (m > 0) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = VerdeExito.copy(alpha = 0.12f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("IMPACTO POSITIVO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = VerdeExito)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Terminarías $mesesAhorrados meses antes", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Ahorrarías $${String.format(java.util.Locale.US, "%.0f", interesAhorrado)} en intereses totales", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PurpuraPrimario)
            ) { Text("ENTENDIDO", color = Color.White, fontWeight = FontWeight.Bold) }
        }
    )
}

@Composable
fun PagoRow(pago: PagoPrestamo) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(pago.numeroPago.toString(), modifier = Modifier.width(30.dp), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        Text("$${String.format(java.util.Locale.US, "%.0f", pago.pagoMensual)}", modifier = Modifier.weight(1f), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
        Text("$${String.format(java.util.Locale.US, "%.0f", pago.interesPagado)}", modifier = Modifier.weight(1f), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Text("$${String.format(java.util.Locale.US, "%.0f", pago.saldoRestante)}", modifier = Modifier.weight(1f), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PurpuraPrimario)
        if (pago.pagado) {
            Icon(Icons.Default.CheckCircle, contentDescription = "Pagado", tint = VerdeExito, modifier = Modifier.size(18.dp))
        }
    }
}
