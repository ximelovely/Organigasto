package com.app.organigasto.ui.calendario

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.organigasto.ui.theme.*
import com.app.organigasto.ui.movimientos.MovimientosViewModel
import com.app.organigasto.data.local.entity.MovimientoEntity
import com.app.organigasto.domain.model.TipoMovimiento
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancialCalendarScreen(
    onBackClick: () -> Unit,
    viewModel: MovimientosViewModel
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    val movimientosPorDia by viewModel.movimientosPorDia.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendario", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp)) {
            CalendarHeader(
                currentMonth = currentMonth,
                onMonthChange = { currentMonth = it }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            CalendarGrid(
                currentMonth = currentMonth,
                selectedDate = selectedDate,
                onDateSelected = { selectedDate = it },
                movimientosMap = movimientosPorDia
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Movimientos del ${selectedDate.dayOfMonth} de ${selectedDate.month.getDisplayName(TextStyle.FULL, Locale("es", "ES"))}",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 16.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            val movementsOfDay = movimientosPorDia[selectedDate] ?: emptyList()
            
            if (movementsOfDay.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Text("No hay movimientos este día", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(movementsOfDay) { mov ->
                        CalendarMovementItem(mov)
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarHeader(currentMonth: YearMonth, onMonthChange: (YearMonth) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { onMonthChange(currentMonth.minusMonths(1)) }) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
        
        Text(
            text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale("es", "ES")).uppercase()} ${currentMonth.year}",
            fontWeight = FontWeight.Black,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.primary
        )
        
        IconButton(onClick = { onMonthChange(currentMonth.plusMonths(1)) }) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun CalendarGrid(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    movimientosMap: Map<LocalDate, List<MovimientoEntity>>
) {
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfMonth = currentMonth.atDay(1).dayOfWeek.value % 7 // Ajuste para Domingo = 0
    val days = (1..daysInMonth).toList()
    
    val dayLabels = listOf("D", "L", "M", "M", "J", "V", "S")

    Column {
        Row(modifier = Modifier.fillMaxWidth()) {
            dayLabels.forEach { label ->
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        val totalCells = daysInMonth + firstDayOfMonth
        val rows = (totalCells + 6) / 7
        
        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val cellIndex = row * 7 + col
                    val dayNum = cellIndex - firstDayOfMonth + 1
                    
                    if (dayNum in 1..daysInMonth) {
                        val date = currentMonth.atDay(dayNum)
                        val isSelected = date == selectedDate
                        val hasMovements = movimientosMap.containsKey(date)
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { onDateSelected(date) },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = dayNum.toString(),
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                if (hasMovements) {
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .background(if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary, CircleShape)
                                    )
                                }
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarMovementItem(mov: MovimientoEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        if (mov.tipo == TipoMovimiento.INGRESO) Color(0xFFE8F5E9) else Color(0xFFFBE9E7),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (mov.tipo == TipoMovimiento.INGRESO) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = if (mov.tipo == TipoMovimiento.INGRESO) Color(0xFF4CAF50) else Color(0xFFF44336),
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(mov.nota ?: "Sin nota", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(mov.cuenta, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (mov.tipo == TipoMovimiento.INGRESO) "+" else "-"}$${mov.monto}",
                    color = if (mov.tipo == TipoMovimiento.INGRESO) Color(0xFF4CAF50) else Color(0xFFF44336),
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp
                )
                if (mov.moneda != "MXN") {
                    Text(
                        text = "(${mov.montoOriginal} ${mov.moneda})",
                        fontSize = 9.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}
