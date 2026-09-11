package com.app.organigasto.ui.movimientos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
    val cuentas by viewModel.cuentas.collectAsState()
    
    var selectedType by remember { mutableStateOf(TipoMovimiento.GASTO) }
    var amount by remember { mutableStateOf("") }
    var selectedCurrency by remember { mutableStateOf("MXN") }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var selectedCuentaId by remember { mutableStateOf<Long?>(null) }
    var selectedCuentaDestinoId by remember { mutableStateOf<Long?>(null) }
    var recurrencia by remember { mutableStateOf(Recurrencia.INDIVIDUAL) }
    var nota by remember { mutableStateOf("") }

    val exchangeRate = viewModel.obtenerTasaCambio(selectedCurrency)
    val convertedAmount = (amount.toDoubleOrNull() ?: 0.0) * exchangeRate

    val scrollState = rememberScrollState()

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
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Type Selector
                TypeSegmentedControl(
                    selected = selectedType,
                    onSelected = { 
                        selectedType = it 
                        selectedCategoryId = null 
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Monto",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                
                // Currency Selector
                CurrencySelector(
                    selected = selectedCurrency,
                    onSelected = { selectedCurrency = it }
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (amount.isEmpty()) {
                        Text(
                            "0.00", 
                            fontSize = 64.sp, 
                            fontWeight = FontWeight.Black, 
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        ) 
                    }
                    androidx.compose.foundation.text.BasicTextField(
                        value = amount,
                        onValueChange = { 
                            if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) amount = it 
                        },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 64.sp, 
                            fontWeight = FontWeight.Black, 
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal),
                        cursorBrush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary),
                        singleLine = true
                    )
                }

                if (selectedCurrency != "MXN") {
                    Text(
                        text = "≈ $${String.format("%.2f", convertedAmount)} MXN",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Categories or Destination Account
                if (selectedType == TipoMovimiento.TRANSFERENCIA) {
                    Text(
                        "Cuenta Destino",
                        modifier = Modifier.fillMaxWidth(),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    AccountSelector(
                        cuentas = cuentas.filter { it.id != selectedCuentaId },
                        selectedId = selectedCuentaDestinoId,
                        onSelected = { selectedCuentaDestinoId = it },
                        label = "Seleccionar cuenta destino"
                    )
                } else {
                    val filteredCategories = categorias.filter { it.tipo == null || it.tipo == selectedType }
                    if (filteredCategories.isNotEmpty()) {
                        Text(
                            "Categoria",
                            modifier = Modifier.fillMaxWidth(),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        CategoryGrid(
                            categorias = filteredCategories,
                            selectedId = selectedCategoryId,
                            onSelected = { selectedCategoryId = it }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Recurrence
                if (selectedType != TipoMovimiento.TRANSFERENCIA) {
                    Text(
                        "Recurrencia",
                        modifier = Modifier.fillMaxWidth(),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    RecurrenceSelector(
                        current = recurrencia,
                        onSelected = { recurrencia = it }
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Account and Date
                Text(
                    if (selectedType == TipoMovimiento.TRANSFERENCIA) "Cuenta Origen" else "Cuenta/Tarjeta",
                    modifier = Modifier.fillMaxWidth(),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                AccountSelector(
                    cuentas = cuentas,
                    selectedId = selectedCuentaId,
                    onSelected = { selectedCuentaId = it },
                    label = "Seleccionar cuenta"
                )

                Spacer(modifier = Modifier.height(24.dp))

                Column {
                    Text("Fecha", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(LocalDate.now().toString(), color = MaterialTheme.colorScheme.onSurface)
                            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Recibo
                Text(
                    "Recibo / Factura",
                    modifier = Modifier.fillMaxWidth(),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
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

                Spacer(modifier = Modifier.height(32.dp))
            }

            // Fixed Button at bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Button(
                    onClick = {
                        val montoOriginalDouble = amount.toDoubleOrNull() ?: 0.0
                        val cuentaSeleccionada = cuentas.find { it.id == selectedCuentaId }
                        
                        if (selectedType == TipoMovimiento.TRANSFERENCIA) {
                            val cuentaDestino = cuentas.find { it.id == selectedCuentaDestinoId }
                            if (montoOriginalDouble > 0 && cuentaSeleccionada != null && cuentaDestino != null) {
                                viewModel.transferir(
                                    monto = convertedAmount,
                                    cuentaOrigenId = cuentaSeleccionada.id,
                                    cuentaOrigenNombre = cuentaSeleccionada.nombre,
                                    cuentaDestinoId = cuentaDestino.id,
                                    cuentaDestinoNombre = cuentaDestino.nombre,
                                    fecha = LocalDate.now(),
                                    nota = if (nota.isEmpty()) null else nota
                                )
                                onBackClick()
                            }
                        } else {
                            if (montoOriginalDouble > 0 && selectedCategoryId != null && cuentaSeleccionada != null) {
                                viewModel.agregarMovimiento(
                                    tipo = selectedType,
                                    monto = convertedAmount,
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
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = if (selectedType == TipoMovimiento.TRANSFERENCIA) "Realizar transferencia" else "Guardar movimiento", 
                        fontSize = 16.sp, 
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
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
                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        RoundedCornerShape(8.dp)
                    )
                    .clickable { onSelected(currency) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = currency,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
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
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
            .padding(4.dp)
    ) {
        TipoMovimiento.entries.forEach { type ->
            val isSelected = selected == type
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        RoundedCornerShape(20.dp)
                    )
                    .clickable { onSelected(type) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = type.name.lowercase().replaceFirstChar { it.uppercase() },
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
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
                "payments" -> Icons.Default.Payments
                "shopping_bag" -> Icons.Default.ShoppingBag
                "redeem" -> Icons.Default.Redeem
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
fun AccountSelector(
    cuentas: List<com.app.organigasto.data.local.entity.CuentaEntity>,
    selectedId: Long?,
    onSelected: (Long) -> Unit,
    label: String
) {
    var showDialog by remember { mutableStateOf(false) }
    val cuenta = cuentas.find { it.id == selectedId }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .clickable { showDialog = true },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = cuenta?.nombre ?: label,
                color = if (cuenta != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(label, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(28.dp),
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    cuentas.forEach { c ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelected(c.id)
                                    showDialog = false
                                },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedId == c.id) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = c.nombre, 
                                        fontWeight = FontWeight.Bold, 
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = c.tipo, 
                                        fontSize = 11.sp, 
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                                Text(
                                    text = "$${String.format("%.2f", c.saldoActual)}", 
                                    fontWeight = FontWeight.Black, 
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cerrar", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun RecurrenceSelector(current: Recurrencia, onSelected: (Recurrencia) -> Unit) {
    val isRecurrent = current != Recurrencia.INDIVIDUAL
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
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
    val bgColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    
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
                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                RoundedCornerShape(8.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
    }
}

@Composable
fun RecurrenceSubButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(80.dp)
            .height(30.dp)
            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent, RoundedCornerShape(8.dp))
            .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 12.sp)
    }
}
