package com.app.organigasto.ui.movimientos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.organigasto.data.local.entity.Categoria
import com.app.organigasto.domain.model.TipoPrestamo
import com.app.organigasto.domain.utils.LoanCalculator
import com.app.organigasto.ui.categorias.CategoryIconView
import com.app.organigasto.ui.categorias.parseCategoryColor
import com.app.organigasto.ui.components.AnimatedFinanceBackground
import com.app.organigasto.ui.theme.*
import java.time.LocalDate
import java.time.format.TextStyle as SystemTextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMovementScreen(
    onBackClick: () -> Unit,
    viewModel: MovimientosViewModel
) {
    val categorias by viewModel.categorias.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Gasto Rápido, 1: Deuda / Préstamo

    // Campos de Gasto
    var amount by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var descripcion by remember { mutableStateOf("") }
    var cuentaPago by remember { mutableStateOf("Débito") }

    // Campos de Deuda / Préstamo
    var tipoCompromiso by remember { mutableStateOf("DEUDA") } // "DEUDA" vs "PRÉSTAMO"
    var loanNombre by remember { mutableStateOf("") }
    var loanAmount by remember { mutableStateOf("") }
    var loanMeses by remember { mutableStateOf("12") }
    var loanDiaPago by remember { mutableStateOf("15") }
    var loanTasa by remember { mutableStateOf("0") }

    val catMap = remember(categorias) { categorias.associateBy { it.id } }
    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        AnimatedFinanceBackground(modifier = Modifier.fillMaxSize())

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = if (selectedTab == 0) "Registrar Gasto" else "Registrar Deuda",
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
            ) {
                // Main Tab Bar
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = PurpuraPrimario,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = PurpuraPrimario
                        )
                    },
                    divider = {}
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        selectedContentColor = PurpuraPrimario,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    ) {
                        Text(
                            "GASTO RÁPIDO",
                            modifier = Modifier.padding(16.dp),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        selectedContentColor = PurpuraPrimario,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    ) {
                        Text(
                            "REGISTRAR DEUDA",
                            modifier = Modifier.padding(16.dp),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState)
                        .padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(20.dp))

                    if (selectedTab == 0) {
                        // -------------------------------------------------------------
                        // TAB 0: GASTO RÁPIDO
                        // -------------------------------------------------------------
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(28.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "¿CUÁNTO GASTASTE?",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    letterSpacing = 1.sp
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                BasicTextField(
                                    value = amount,
                                    onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) amount = it },
                                    textStyle = TextStyle(
                                        fontSize = 44.sp,
                                        fontWeight = FontWeight.Black,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurface
                                    ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    decorationBox = { innerTextField ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Text("$", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = PurpuraPrimario)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            if (amount.isEmpty()) {
                                                Text(
                                                    "0.00",
                                                    fontSize = 44.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                                )
                                            }
                                            innerTextField()
                                        }
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Payment Method Selector (Método de Pago)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(28.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    "MÉTODO / CUENTA DE PAGO",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    letterSpacing = 0.5.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf(
                                        "Débito" to "💳 Débito",
                                        "Efectivo" to "💵 Efectivo",
                                        "Crédito" to "💳 Crédito"
                                    ).forEach { (key, label) ->
                                        val isSelected = cuentaPago == key
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { cuentaPago = key },
                                            label = { Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                                            modifier = Modifier.weight(1f).height(44.dp),
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = PurpuraPrimario,
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(28.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    "SELECCIONA EL SOBRE AFECTADO",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    letterSpacing = 0.5.sp
                                )

                                CategoryList(
                                    categorias = categorias,
                                    selectedId = selectedCategoryId,
                                    onSelected = { selectedCategoryId = it }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(28.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    "DETALLES (OPCIONAL)",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    letterSpacing = 0.5.sp
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = descripcion,
                                    onValueChange = { descripcion = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = { Text("Ej: Cena, Supermercado, Uber") },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = PurpuraPrimario,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }

                    } else {
                        // -------------------------------------------------------------
                        // TAB 1: REGISTRAR DEUDA
                        // -------------------------------------------------------------

                        // Form Inputs Card
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
                                OutlinedTextField(
                                    value = loanNombre,
                                    onValueChange = { loanNombre = it },
                                    label = { Text("Concepto o Titular") },
                                    placeholder = { Text("Ej: Tarjeta de Crédito, Muebles, Casa") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = PurpuraPrimario,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                                    )
                                )

                                OutlinedTextField(
                                    value = loanAmount,
                                    onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) loanAmount = it },
                                    label = { Text("Monto Total ($)") },
                                    prefix = { Text("$ ", color = PurpuraPrimario, fontWeight = FontWeight.Bold) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = PurpuraPrimario,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                                    )
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    OutlinedTextField(
                                        value = loanMeses,
                                        onValueChange = { loanMeses = it },
                                        label = { Text("Plazo (meses)") },
                                        placeholder = { Text("1") },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(16.dp),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = PurpuraPrimario,
                                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                                        )
                                    )

                                    OutlinedTextField(
                                        value = loanDiaPago,
                                        onValueChange = { if (it.isEmpty() || (it.toIntOrNull() in 1..31)) loanDiaPago = it },
                                        label = { Text("Día de Pago (1-31)") },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(16.dp),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = PurpuraPrimario,
                                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                }

                                OutlinedTextField(
                                    value = loanTasa,
                                    onValueChange = { loanTasa = it },
                                    label = { Text("Tasa de Interés Anual (%)") },
                                    placeholder = { Text("0") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = PurpuraPrimario,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Target Envelope Selection
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(28.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    "INCLUIR PAGO EN ESTE SOBRE",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    letterSpacing = 0.5.sp
                                )

                                Text(
                                    "El pago mensual calculado se descontará del sobre que elijas.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                                )

                                CategoryList(
                                    categorias = categorias,
                                    selectedId = selectedCategoryId,
                                    onSelected = { selectedCategoryId = it }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Live Calculation Preview Card with Expiry Month Calculation
                        val mTotal = loanAmount.toDoubleOrNull() ?: 0.0
                        val nMeses = (loanMeses.toIntOrNull() ?: 1).coerceAtLeast(1)
                        val tTasa = loanTasa.toDoubleOrNull() ?: 0.0
                        val dDia = (loanDiaPago.toIntOrNull() ?: 15).coerceIn(1, 31)

                        if (mTotal > 0) {
                            val tipoLoan = if (tTasa > 0) TipoPrestamo.CON_INTERES else TipoPrestamo.SIN_INTERESES
                            val resumenLoan = LoanCalculator.calcularPrestamo(mTotal, nMeses, tipoLoan, tTasa)
                            val targetCat = selectedCategoryId?.let { catMap[it] }

                            val startToday = LocalDate.now()
                            val expiryDate = startToday.plusMonths(nMeses.toLong())
                            val expiryMonthName = expiryDate.month.getDisplayName(SystemTextStyle.FULL, Locale.getDefault()).replaceFirstChar { it.uppercase() }

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
                                        "PAGO MENSUAL CALCULADO",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White.copy(alpha = 0.7f),
                                        letterSpacing = 1.sp
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        "$${String.format(Locale.US, "%.2f", resumenLoan.pagoMensual)} / mes",
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        "Se pagará el día $dDia de cada mes",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )

                                    Text(
                                        "Vencimiento final: $expiryMonthName ${expiryDate.year} (en $nMeses mes${if (nMeses != 1) "es" else ""})",
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.8f),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )

                                    if (targetCat != null) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        val catColor = parseCategoryColor(targetCat.colorHex)

                                        Surface(
                                            shape = RoundedCornerShape(16.dp),
                                            color = Color.White.copy(alpha = 0.15f)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier.size(20.dp).background(catColor, CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    CategoryIconView(
                                                        iconoNombre = targetCat.iconoNombre,
                                                        iconoUri = targetCat.iconoUri,
                                                        size = 12.dp
                                                    )
                                                }
                                                Text(
                                                    "Anotado en sobre: ${targetCat.nombre}",
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
                    }

                    Spacer(modifier = Modifier.height(28.dp))
                }

                // Bottom Action Button
                val isFormValid = if (selectedTab == 0) {
                    (amount.toDoubleOrNull() ?: 0.0) > 0 && selectedCategoryId != null
                } else {
                    (loanAmount.toDoubleOrNull() ?: 0.0) > 0 && (loanMeses.toIntOrNull() ?: 0) > 0 && loanNombre.isNotBlank()
                }

                Button(
                    onClick = {
                        if (selectedTab == 0) {
                            val monto = amount.toDoubleOrNull() ?: 0.0
                            if (monto > 0 && selectedCategoryId != null) {
                                viewModel.agregarGasto(
                                    monto = monto,
                                    categoriaId = selectedCategoryId!!,
                                    descripcion = descripcion,
                                    cuentaPago = cuentaPago
                                )
                                onBackClick()
                            }
                        } else {
                            val monto = loanAmount.toDoubleOrNull() ?: 0.0
                            val meses = (loanMeses.toIntOrNull() ?: 1).coerceAtLeast(1)
                            val tasa = loanTasa.toDoubleOrNull() ?: 0.0
                            val dia = (loanDiaPago.toIntOrNull() ?: 15).coerceIn(1, 31)

                            val expiryDate = LocalDate.now().plusMonths(meses.toLong()).toString()

                            if (monto > 0 && loanNombre.isNotBlank()) {
                                viewModel.registrarPrestamo(
                                    nombre = loanNombre,
                                    monto = monto,
                                    meses = meses,
                                    tasa = tasa,
                                    categoriaId = selectedCategoryId,
                                    diaPagoMensual = dia,
                                    tipoCompromiso = tipoCompromiso,
                                    fechaVencimiento = expiryDate
                                )
                                onBackClick()
                            }
                        }
                    },
                    enabled = isFormValid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PurpuraPrimario,
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (selectedTab == 0) "GUARDAR GASTO" else if (tipoCompromiso == "DEUDA") "GUARDAR DEUDA" else "CREAR PRÉSTAMO",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryList(categorias: List<Categoria>, selectedId: Int?, onSelected: (Int) -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(top = 10.dp)
    ) {
        categorias.forEach { cat ->
            val isSelected = selectedId == cat.id
            val catColor = parseCategoryColor(cat.colorHex)

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { onSelected(cat.id) },
                shape = RoundedCornerShape(18.dp),
                color = if (isSelected) catColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, catColor) else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                shadowElevation = if (isSelected) 0.dp else 1.dp
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(catColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        CategoryIconView(
                            iconoNombre = cat.iconoNombre,
                            iconoUri = cat.iconoUri,
                            tint = Color.White,
                            size = 20.dp
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Text(
                        cat.nombre,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if (isSelected) catColor else MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    if (isSelected) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = catColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}
