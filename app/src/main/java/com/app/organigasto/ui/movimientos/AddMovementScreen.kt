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
    
    var selectedType by remember { mutableStateOf(TipoMovimiento.GASTO) }
    var amount by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var recurrencia by remember { mutableStateOf(Recurrencia.INDIVIDUAL) }
    var nota by remember { mutableStateOf("") }

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

            Text("Monto", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
            OutlinedTextField(
                value = amount,
                onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) amount = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("0.00", fontSize = 48.sp) },
                textStyle = LocalTextStyle.current.copy(fontSize = 48.sp, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )

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

            AccountAndDateSection()

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val montoDouble = amount.toDoubleOrNull() ?: 0.0
                    if (montoDouble > 0 && selectedCategoryId != null) {
                        viewModel.agregarMovimiento(
                            tipo = selectedType,
                            monto = montoDouble,
                            categoriaId = selectedCategoryId!!,
                            cuenta = "Tarjeta debito BBVA",
                            fecha = LocalDate.now(),
                            recurrencia = recurrencia,
                            nota = if (nota.isEmpty()) null else nota
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
    Card(
        modifier = modifier
            .height(80.dp)
            .clickable { onClick() }
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) PurpuraSecundario else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = PurpuraSecundario)
            Text(name, fontSize = 12.sp, color = PurpuraSecundario)
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
fun AccountAndDateSection() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column {
            Text("Cuenta/Tarjeta", fontWeight = FontWeight.Bold, color = PurpuraSecundario)
            Surface(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color.White
            ) {
                Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Tarjeta debito BBVA", color = PurpuraSecundario)
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = PurpuraSecundario)
                }
            }
        }
        Column {
            Text("Fecha", fontWeight = FontWeight.Bold, color = PurpuraSecundario)
            Surface(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color.White
            ) {
                Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("29 de Agosto 2026", color = PurpuraSecundario)
                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = PurpuraSecundario)
                }
            }
        }
    }
}
