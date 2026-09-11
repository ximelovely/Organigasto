package com.app.organigasto.ui.movimientos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.organigasto.ui.theme.PurpuraPrimario
import com.app.organigasto.ui.theme.PurpuraSecundario
import com.app.organigasto.ui.movimientos.MovimientosViewModel
import com.app.organigasto.domain.model.TipoMovimiento
import com.app.organigasto.domain.model.Recurrencia
import com.app.organigasto.data.local.entity.CategoriaEntity
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMovementScreen(
    onBackClick: () -> Unit,
    viewModel: MovimientosViewModel
) {
    val categorias by viewModel.categorias.collectAsState()
    val cuentas by viewModel.cuentas.collectAsState() // Añadido
    
    var selectedType by remember { mutableStateOf(TipoMovimiento.GASTO) }
    var amount by remember { mutableStateOf("") }
    var selectedCurrency by remember { mutableStateOf("MXN") } // Añadido
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var selectedCuentaId by remember { mutableStateOf<Long?>(null) }
    var recurrencia by remember { mutableStateOf(Recurrencia.INDIVIDUAL) }
    var nota by remember { mutableStateOf("") }

    val exchangeRate = viewModel.obtenerTasaCambio(selectedCurrency) // Añadido
    val convertedAmount = (amount.toDoubleOrNull() ?: 0.0) * exchangeRate // Añadido

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo movimiento", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Type Selector
            TypeSegmentedControl(
                selected = selectedType,
                onSelected = { selectedType = it }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Monto",
                color = PurpuraSecundario.copy(alpha = 0.6f),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            
            // Currency Selector
            CurrencySelector(
                selected = selectedCurrency,
                onSelected = { selectedCurrency = it }
            )

            OutlinedTextField(
                value = amount,
                onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) amount = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { 
                    Text(
                        "0.00", 
                        fontSize = 54.sp, 
                        fontWeight = FontWeight.Black, 
                        color = Color.LightGray,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    ) 
                },
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 54.sp, 
                    fontWeight = FontWeight.Black, 
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = PurpuraSecundario
                ),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = PurpuraPrimario
                )
            )

            if (selectedCurrency != "MXN") {
                Text(
                    text = "≈ $${String.format("%.2f", convertedAmount)} MXN",
                    color = PurpuraPrimario,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Categories
            Text(
                "Categoria",
                modifier = Modifier.fillMaxWidth(),
                fontWeight = FontWeight.Bold,
                color = PurpuraSecundario
            )
            CategoryGrid(
                categorias = categorias,
                selectedId = selectedCategoryId,
                onSelected = { selectedCategoryId = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Recurrence
            Text(
                "Recurrencia",
                modifier = Modifier.fillMaxWidth(),
                fontWeight = FontWeight.Bold,
                color = PurpuraSecundario
            )
            RecurrenceSelector(
                current = recurrencia,
                onSelected = { recurrencia = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Account and Date
            AccountAndDateSection(
                cuentas = cuentas,
                selectedCuentaId = selectedCuentaId,
                onCuentaSelected = { selectedCuentaId = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Recibo
            Text(
                "Recibo / Factura",
                modifier = Modifier.fillMaxWidth(),
                fontWeight = FontWeight.Bold,
                color = PurpuraSecundario
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { /* TODO: Abrir Cámara/Galería */ },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PhotoCamera, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Adjuntar foto del ticket")
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val montoOriginalDouble = amount.toDoubleOrNull() ?: 0.0
                    val cuentaSeleccionada = cuentas.find { it.id == selectedCuentaId }
                    if (montoOriginalDouble > 0 && selectedCategoryId != null && cuentaSeleccionada != null) {
                        viewModel.agregarMovimiento(
                            tipo = selectedType,
                            monto = convertedAmount, // Guardar el monto convertido a MXN
                            categoriaId = selectedCategoryId!!,
                            cuentaId = cuentaSeleccionada.id,
                            cuentaNombre = cuentaSeleccionada.nombre,
                            fecha = LocalDate.now(),
                            recurrencia = recurrencia,
                            nota = if (nota.isEmpty()) null else nota,
                            moneda = selectedCurrency,
                            montoOriginal = montoOriginalDouble,
                            tasaCambio = exchangeRate
                        )
                        onBackClick()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PurpuraPrimario)
            ) {
                Text("Guardar movimiento", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun CurrencySelector(selected: String, onSelected: (String) -> Unit) {
    val currencies = listOf("MXN", "USD", "EUR", "GBP")
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        currencies.forEach { currency ->
            val isSelected = selected == currency
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .background(
                        if (isSelected) PurpuraSecundario else Color.LightGray.copy(alpha = 0.2f),
                        RoundedCornerShape(8.dp)
                    )
                    .clickable { onSelected(currency) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = currency,
                    color = if (isSelected) Color.White else PurpuraSecundario,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun TypeSegmentedControl(selected: TipoMovimiento, onSelected: (TipoMovimiento) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
            .padding(4.dp)
    ) {
        TipoMovimiento.entries.forEach { type ->
            val isSelected = selected == type
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        if (isSelected) PurpuraSecundario else Color.Transparent,
                        RoundedCornerShape(20.dp)
                    )
                    .clickable { onSelected(type) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = type.name.lowercase().replaceFirstChar { it.uppercase() },
                    color = if (isSelected) Color.White else PurpuraSecundario,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun CategoryGrid(categorias: List<CategoriaEntity>, selectedId: Long?, onSelected: (Long) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        categorias.take(3).forEach { cat ->
            val icon = when (cat.icono) {
                "restaurant" -> Icons.Default.Restaurant
                "directions_car" -> Icons.Default.DirectionsCar
                "home" -> Icons.Default.Home
                else -> Icons.Default.Category
            }
            CategoryItem(
                name = cat.nombre,
                icon = icon,
                isSelected = selectedId == cat.id,
                onClick = { onSelected(cat.id) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun RecurrenceSelector(current: Recurrencia, onSelected: (Recurrencia) -> Unit) {
    val isRecurrent = current != Recurrencia.INDIVIDUAL
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .height(100.dp)
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RecurrenceButton("Individual", !isRecurrent, { onSelected(Recurrencia.INDIVIDUAL) }, Modifier.weight(1f))
                RecurrenceButton("Recurrente", isRecurrent, { onSelected(Recurrencia.MENSUAL) }, Modifier.weight(1f))
            }
            if (isRecurrent) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RecurrenceSubButton("Semanal", current == Recurrencia.SEMANAL) { onSelected(Recurrencia.SEMANAL) }
                    RecurrenceSubButton("Quincenal", current == Recurrencia.QUINCENAL) { onSelected(Recurrencia.QUINCENAL) }
                    RecurrenceSubButton("Mensual", current == Recurrencia.MENSUAL) { onSelected(Recurrencia.MENSUAL) }
                }
            }
        }
    }
}

@Composable
fun CategoryItem(name: String, icon: ImageVector, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val bgColor = if (isSelected) PurpuraPrimario else Color.White
    val contentColor = if (isSelected) Color.White else PurpuraSecundario
    
    Card(
        modifier = modifier
            .height(80.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon, 
                contentDescription = null, 
                tint = contentColor,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(name, fontSize = 11.sp, color = contentColor, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun RecurrenceButton(text: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    Box(
        modifier = modifier
            .height(40.dp)
            .background(
                if (isSelected) PurpuraSecundario else Color.LightGray.copy(alpha = 0.2f),
                RoundedCornerShape(8.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (isSelected) Color.White else PurpuraSecundario, fontSize = 14.sp)
    }
}

@Composable
fun RecurrenceSubButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(80.dp)
            .height(30.dp)
            .background(if (isSelected) PurpuraPrimario.copy(alpha = 0.2f) else Color.Transparent, RoundedCornerShape(8.dp))
            .border(1.dp, if (isSelected) PurpuraPrimario else Color.LightGray, RoundedCornerShape(8.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (isSelected) PurpuraPrimario else Color.LightGray, fontSize = 12.sp)
    }
}

@Composable
fun AccountAndDateSection(
    cuentas: List<com.app.organigasto.data.local.entity.CuentaEntity>,
    selectedCuentaId: Long?,
    onCuentaSelected: (Long) -> Unit
) {
    var showCuentaDialog by remember { mutableStateOf(false) }
    val cuentaSeleccionada = cuentas.find { it.id == selectedCuentaId }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column {
            Text("Cuenta/Tarjeta", fontWeight = FontWeight.Bold, color = PurpuraSecundario)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .clickable { showCuentaDialog = true },
                shape = RoundedCornerShape(12.dp),
                color = Color.White
            ) {
                Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = cuentaSeleccionada?.nombre ?: "Seleccionar cuenta",
                        color = if (cuentaSeleccionada != null) PurpuraSecundario else Color.Gray
                    )
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = PurpuraSecundario)
                }
            }
        }
        
        if (showCuentaDialog) {
            AlertDialog(
                onDismissRequest = { showCuentaDialog = false },
                title = { Text("¿De qué cuenta sale el dinero?", fontWeight = FontWeight.Bold, color = PurpuraSecundario) },
                containerColor = Color.White,
                shape = RoundedCornerShape(28.dp),
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        cuentas.forEach { cuenta ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onCuentaSelected(cuenta.id)
                                        showCuentaDialog = false
                                    },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selectedCuentaId == cuenta.id) PurpuraPrimario.copy(alpha = 0.1f) else Color.Transparent
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(cuenta.nombre, fontWeight = FontWeight.Bold, color = PurpuraSecundario)
                                        Text(cuenta.tipo, fontSize = 10.sp, color = Color.Gray)
                                    }
                                    Text(
                                        "$${String.format("%.2f", cuenta.saldoActual)}", 
                                        fontWeight = FontWeight.Black, 
                                        color = PurpuraSecundario
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showCuentaDialog = false }) {
                        Text("Cerrar", color = PurpuraPrimario, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        Column {
            Text("Fecha", fontWeight = FontWeight.Bold, color = PurpuraSecundario)
            Surface(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color.White
            ) {
                Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(LocalDate.now().toString(), color = PurpuraSecundario)
                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = PurpuraSecundario)
                }
            }
        }
    }
}
