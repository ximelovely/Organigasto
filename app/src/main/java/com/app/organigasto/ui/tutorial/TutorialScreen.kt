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

import com.app.organigasto.ui.components.AnimatedFinanceBackground
import com.app.organigasto.ui.theme.*

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.launch
import com.app.organigasto.domain.model.TipoPeriodo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorialScreen(
    onFinish: () -> Unit,
    viewModel: MovimientosViewModel
) {
    var step by remember { mutableIntStateOf(1) }
    var salario by remember { mutableStateOf("") }
    var saldoInicial by remember { mutableStateOf("") }
    var frecuenciaSueldo by remember { mutableStateOf(TipoPeriodo.MENSUAL) }
    var diaPago by remember { mutableStateOf("15") }
    var esquemaQuincenal by remember { mutableStateOf("15_30") }
    var diaSemana by remember { mutableIntStateOf(5) } // 5: Viernes

    var efectivoSelected by remember { mutableStateOf(true) }
    var debitoSelected by remember { mutableStateOf(true) }
    var creditoSelected by remember { mutableStateOf(false) }

    var netflixSelected by remember { mutableStateOf(false) }
    var spotifySelected by remember { mutableStateOf(false) }
    var primeSelected by remember { mutableStateOf(false) }

    val customSubscriptions = remember { mutableStateListOf<Pair<String, Double>>() }
    var showAddCustomDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        AnimatedFinanceBackground(modifier = Modifier.fillMaxSize())

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Configuración Inicial", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                    )
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Indicador de Progreso superior con estilo
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    repeat(3) { i ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .background(
                                    if (step >= i + 1) PurpuraPrimario else PurpuraClaro.copy(alpha = 0.3f),
                                    RoundedCornerShape(3.dp)
                                )
                        )
                    }
                }

                // Cuerpo del paso actual - Envuelto en una Card estilizada
                Card(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(modifier = Modifier.padding(24.dp).fillMaxSize(), contentAlignment = Alignment.Center) {
                        when (step) {
                            1 -> TutorialStep1(
                                salario = salario,
                                onSalarioChange = { salario = it },
                                saldoInicial = saldoInicial,
                                onSaldoChange = { saldoInicial = it },
                                frecuencia = frecuenciaSueldo,
                                onFrecuenciaChange = { frecuenciaSueldo = it },
                                dia = diaPago,
                                onDiaChange = { diaPago = it },
                                esquemaQuincenal = esquemaQuincenal,
                                onEsquemaQuincenalChange = { esquemaQuincenal = it },
                                diaSemana = diaSemana,
                                onDiaSemanaChange = { diaSemana = it }
                            )
                            2 -> TutorialStep2(
                                efectivoSelected, { efectivoSelected = it },
                                debitoSelected, { debitoSelected = it },
                                creditoSelected, { creditoSelected = it }
                            )
                            3 -> TutorialStep3(
                                netflixSelected, { netflixSelected = it },
                                spotifySelected, { spotifySelected = it },
                                primeSelected, { primeSelected = it },
                                customSubscriptions, { showAddCustomDialog = true }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Botón de acción inferior (Continuar / Finalizar)
                Button(
                    onClick = {
                        if (step < 3) {
                            step++
                        } else {
                            scope.launch {
                                try {
                                    val ingresoMonto = salario.toDoubleOrNull() ?: 0.0
                                    val saldoAct = saldoInicial.toDoubleOrNull() ?: 0.0

                                    if (saldoAct > 0) {
                                        viewModel.establecerSaldoInicial(saldoAct)
                                    }
                                    if (ingresoMonto > 0) {
                                        viewModel.inicializarSueldoTutorial(
                                            monto = ingresoMonto,
                                            frecuencia = frecuenciaSueldo,
                                            diaPago = diaPago.toIntOrNull() ?: 15,
                                            esquemaQuincenal = esquemaQuincenal,
                                            diaSemana = diaSemana
                                        )
                                    }
                                    if (netflixSelected) viewModel.agregarSuscripcion("Netflix", 219.0)
                                    if (spotifySelected) viewModel.agregarSuscripcion("Spotify", 129.0)
                                    if (primeSelected) viewModel.agregarSuscripcion("Amazon Prime", 99.0)
                                    customSubscriptions.forEach { (name, amount) -> viewModel.agregarSuscripcion(name, amount) }

                                    kotlinx.coroutines.delay(300)
                                    onFinish()
                                } catch (e: Exception) {
                                    onFinish()
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PurpuraPrimario)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(if (step == 3) "FINALIZAR" else "CONTINUAR", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(imageVector = if (step == 3) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                    }
                }
            }
        }
    }

    if (showAddCustomDialog) {
        AddCustomSubDialog({ showAddCustomDialog = false }, { name, amount -> customSubscriptions.add(name to amount) })
    }
}

@Composable
fun TutorialStep1(
    salario: String,
    onSalarioChange: (String) -> Unit,
    saldoInicial: String,
    onSaldoChange: (String) -> Unit,
    frecuencia: TipoPeriodo,
    onFrecuenciaChange: (TipoPeriodo) -> Unit,
    dia: String,
    onDiaChange: (String) -> Unit,
    esquemaQuincenal: String,
    onEsquemaQuincenalChange: (String) -> Unit,
    diaSemana: Int,
    onDiaSemanaChange: (Int) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.verticalScroll(rememberScrollState())) {
        Text("¡Bienvenido!", fontSize = 28.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(12.dp))
        Text("Empecemos configurando tus ingresos mensuales para organizar tus presupuestos.", textAlign = TextAlign.Center, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        Spacer(modifier = Modifier.height(28.dp))

        OutlinedTextField(
            value = saldoInicial,
            onValueChange = onSaldoChange,
            label = { Text("Dinero real que tienes ahora ($)") },
            placeholder = { Text("Ej: 5000") },
            prefix = { Text("$ ") },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = salario,
            onValueChange = onSalarioChange,
            label = { Text("Monto de tu Sueldo ($)") },
            prefix = { Text("$ ") },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("¿Cada cuánto te pagan?", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.fillMaxWidth(), fontSize = 13.sp)
        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TipoPeriodo.entries.forEach { t ->
                FilterChip(
                    selected = frecuencia == t,
                    onClick = { onFrecuenciaChange(t) },
                    label = { Text(t.name.lowercase().replaceFirstChar { it.uppercase() }, fontSize = 12.sp) },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        labelColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Payday options based on frequency
        when (frecuencia) {
            TipoPeriodo.MENSUAL -> {
                OutlinedTextField(
                    value = dia,
                    onValueChange = onDiaChange,
                    label = { Text("Día de pago del mes (1-31)") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
            TipoPeriodo.QUINCENAL -> {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Esquema de cobro quincenal:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        FilterChip(
                            selected = esquemaQuincenal == "15_30",
                            onClick = { onEsquemaQuincenalChange("15_30") },
                            label = { Text("Días 15 y 30", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            )
                        )
                        FilterChip(
                            selected = esquemaQuincenal == "1_16",
                            onClick = { onEsquemaQuincenalChange("1_16") },
                            label = { Text("Días 1 y 16", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
            TipoPeriodo.SEMANAL -> {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Día de cobro de la semana:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        listOf(5 to "Viernes", 1 to "Lunes", 6 to "Sábado", 7 to "Domingo").forEach { (d, label) ->
                            FilterChip(
                                selected = diaSemana == d,
                                onClick = { onDiaSemanaChange(d) },
                                label = { Text(label, fontSize = 11.sp) },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
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

@Composable
fun TutorialStep2(efectivo: Boolean, onEfectivoChange: (Boolean) -> Unit, debito: Boolean, onDebitoChange: (Boolean) -> Unit, credito: Boolean, onCreditoChange: (Boolean) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Tus Cuentas", fontSize = 24.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(12.dp))
        Text("Selecciona los métodos que usas habitualmente para pagar.", textAlign = TextAlign.Center, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        Spacer(modifier = Modifier.height(24.dp))

        val items = listOf(
            Triple("Efectivo", Icons.Default.Payments, efectivo to onEfectivoChange),
            Triple("Débito", Icons.Default.CreditCard, debito to onDebitoChange),
            Triple("Crédito", Icons.Default.AccountBalance, credito to onCreditoChange)
        )

        items.forEach { (label, icon, state) ->
            FilterChip(
                selected = state.first,
                onClick = { state.second(!state.first) },
                label = { Text(label, fontWeight = FontWeight.Bold) },
                leadingIcon = { Icon(icon, null, tint = if (state.first) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary) },
                modifier = Modifier.fillMaxWidth().height(56.dp).padding(vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    labelColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}

@Composable
fun TutorialStep3(netflix: Boolean, onNetflixChange: (Boolean) -> Unit, spotify: Boolean, onSpotifyChange: (Boolean) -> Unit, prime: Boolean, onPrimeChange: (Boolean) -> Unit, custom: List<Pair<String, Double>>, onAdd: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Suscripciones", fontSize = 24.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(12.dp))
        Text("¿Tienes gastos recurrentes? Agrégalos ahora.", textAlign = TextAlign.Center, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        Spacer(modifier = Modifier.height(20.dp))

        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            val pre = listOf(
                Triple("Netflix ($219)", Icons.Default.Movie, netflix to onNetflixChange),
                Triple("Spotify ($129)", Icons.Default.MusicNote, spotify to onSpotifyChange),
                Triple("Prime ($99)", Icons.Default.Inventory2, prime to onPrimeChange)
            )
            pre.forEach { (label, icon, state) ->
                FilterChip(
                    selected = state.first,
                    onClick = { state.second(!state.first) },
                    label = { Text(label) },
                    leadingIcon = { Icon(icon, null) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        labelColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
            custom.forEach { (n, a) ->
                FilterChip(
                    selected = true,
                    onClick = {},
                    label = { Text("$n ($$a)") },
                    leadingIcon = { Icon(Icons.Default.AddCircle, null) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
            TextButton(onClick = onAdd, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("Añadir otra...", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun AddCustomSubDialog(onDismiss: () -> Unit, onConfirm: (String, Double) -> Unit) {
    var n by remember { mutableStateOf("") }
    var a by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Suscripción", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = n,
                    onValueChange = { n = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = a,
                    onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) a = it },
                    label = { Text("Costo") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        },
        confirmButton = { TextButton(onClick = { if (n.isNotBlank()) { onConfirm(n, a.toDoubleOrNull() ?: 0.0); onDismiss() } }) { Text("Añadir") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
