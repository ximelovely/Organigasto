package com.app.organigasto.ui.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import com.app.organigasto.domain.model.TipoPeriodo
import com.app.organigasto.ui.components.AnimatedFinanceBackground
import com.app.organigasto.ui.movimientos.MovimientosViewModel
import com.app.organigasto.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageSalaryScreen(
    onBackClick: () -> Unit,
    viewModel: MovimientosViewModel
) {
    var salario by remember { mutableStateOf("") }
    var saldoActual by remember { mutableStateOf("") }
    var frecuenciaSueldo by remember { mutableStateOf(TipoPeriodo.MENSUAL) }
    var diaPago by remember { mutableStateOf("15") }
    var esquemaQuincenal by remember { mutableStateOf("15_30") }
    var diaSemana by remember { mutableIntStateOf(5) } // 5: Viernes

    val currentSalaryVal = salario.toDoubleOrNull() ?: 0.0
    val currentBalanceVal = saldoActual.toDoubleOrNull() ?: 0.0

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        AnimatedFinanceBackground(modifier = Modifier.fillMaxSize())

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Gestionar Ingresos",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Atrás",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Live Salary Preview Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = PurpuraPrimario),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Text(
                            "RESUMEN DE INGRESOS PROYECTADOS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White.copy(alpha = 0.7f),
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Saldo Disponible Hoy",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Text(
                                    "$${String.format(Locale.US, "%.2f", currentBalanceVal)}",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "Sueldo (${frecuenciaSueldo.name.lowercase().replaceFirstChar { it.uppercase() }})",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Text(
                                    "$${String.format(Locale.US, "%.2f", currentSalaryVal)}",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // Inputs Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            "Ajusta tus ingresos reales para el presupuesto.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )

                        OutlinedTextField(
                            value = saldoActual,
                            onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) saldoActual = it },
                            label = { Text("Dinero real que tienes hoy ($)") },
                            prefix = { Text("$ ", color = PurpuraPrimario, fontWeight = FontWeight.Bold) },
                            leadingIcon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = PurpuraPrimario) },
                            shape = RoundedCornerShape(16.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PurpuraPrimario,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                        )

                        OutlinedTextField(
                            value = salario,
                            onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) salario = it },
                            label = { Text("Monto de tu Sueldo ($)") },
                            prefix = { Text("$ ", color = PurpuraPrimario, fontWeight = FontWeight.Bold) },
                            leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null, tint = PurpuraPrimario) },
                            shape = RoundedCornerShape(16.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PurpuraPrimario,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                        )

                        Column {
                            Text("Frecuencia de pago:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TipoPeriodo.entries.forEach { tipo ->
                                    FilterChip(
                                        selected = frecuenciaSueldo == tipo,
                                        onClick = { frecuenciaSueldo = tipo },
                                        label = { Text(tipo.name.lowercase().replaceFirstChar { it.uppercase() }, fontSize = 12.sp) },
                                        modifier = Modifier.weight(1f),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = PurpuraPrimario,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }
                        }

                        // Payday Picker according to Frequency
                        when (frecuenciaSueldo) {
                            TipoPeriodo.MENSUAL -> {
                                OutlinedTextField(
                                    value = diaPago,
                                    onValueChange = { if (it.isEmpty() || (it.toIntOrNull() in 1..31)) diaPago = it },
                                    label = { Text("Día de cobro del mes (1-31)") },
                                    leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = PurpuraPrimario) },
                                    shape = RoundedCornerShape(16.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = PurpuraPrimario,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                    )
                                )
                            }
                            TipoPeriodo.QUINCENAL -> {
                                Column {
                                    Text("Esquema de cobro quincenal:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                        FilterChip(
                                            selected = esquemaQuincenal == "15_30",
                                            onClick = { esquemaQuincenal = "15_30" },
                                            label = { Text("Días 15 y 30", fontSize = 12.sp) },
                                            modifier = Modifier.weight(1f),
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = PurpuraPrimario,
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                        FilterChip(
                                            selected = esquemaQuincenal == "1_16",
                                            onClick = { esquemaQuincenal = "1_16" },
                                            label = { Text("Días 1 y 16", fontSize = 12.sp) },
                                            modifier = Modifier.weight(1f),
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = PurpuraPrimario,
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                    }
                                }
                            }
                            TipoPeriodo.SEMANAL -> {
                                Column {
                                    Text("Día de cobro de la semana:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                                        listOf(5 to "Viernes", 1 to "Lunes", 6 to "Sábado", 7 to "Domingo").forEach { (d, label) ->
                                            FilterChip(
                                                selected = diaSemana == d,
                                                onClick = { diaSemana = d },
                                                label = { Text(label, fontSize = 11.sp) },
                                                modifier = Modifier.weight(1f),
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = PurpuraPrimario,
                                                    selectedLabelColor = Color.White
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Action Button
                Button(
                    onClick = {
                        val mSueldo = salario.toDoubleOrNull() ?: 0.0
                        val mSaldo = saldoActual.toDoubleOrNull() ?: 0.0

                        if (mSaldo > 0) {
                            viewModel.establecerSaldoInicial(mSaldo)
                        }
                        if (mSueldo > 0) {
                            viewModel.inicializarSueldoTutorial(
                                monto = mSueldo,
                                frecuencia = frecuenciaSueldo,
                                diaPago = diaPago.toIntOrNull() ?: 15,
                                esquemaQuincenal = esquemaQuincenal,
                                diaSemana = diaSemana
                            )
                        }
                        onBackClick()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PurpuraPrimario, contentColor = Color.White)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("GUARDAR CAMBIOS", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
