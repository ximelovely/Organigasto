package com.app.organigasto.ui.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.organigasto.ui.theme.PurpuraPrimario
import com.app.organigasto.ui.theme.PurpuraSecundario
import com.app.organigasto.ui.theme.GrayText
import com.app.organigasto.ui.theme.CremaFondo

import com.app.organigasto.ui.movimientos.MovimientosViewModel
import com.app.organigasto.data.local.entity.MovimientoEntity
import com.app.organigasto.domain.model.TipoMovimiento
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(viewModel: MovimientosViewModel) {
    val filtro by viewModel.filtroTiempo.collectAsState()
    val movimientos by viewModel.movimientosFiltrados.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Estadísticas",
                        fontWeight = FontWeight.Bold,
                        color = PurpuraSecundario
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = null, tint = PurpuraSecundario)
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        viewModel.exportarDatosACsv()
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Exportar CSV", tint = PurpuraSecundario)
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Settings, contentDescription = null, tint = PurpuraSecundario)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = CremaFondo,
                    titleContentColor = PurpuraSecundario,
                    navigationIconContentColor = PurpuraSecundario,
                    actionIconContentColor = PurpuraSecundario
                )
            )
        },
        containerColor = CremaFondo
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                TimeSelector(filtro) { viewModel.setFiltro(it) }
            }

            item {
                Text(
                    "Distribucion de gastos",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = PurpuraSecundario
                )
                Spacer(modifier = Modifier.height(16.dp))
                DonutChart(movimientos)
            }

            item {
                BudgetUsageSection(movimientos)
            }

            item {
                Text(
                    "Tendencia de gastos",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = PurpuraSecundario
                )
                Spacer(modifier = Modifier.height(16.dp))
                TrendChart(movimientos)
            }

            item {
                Text(
                    "Lista de gastos",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = PurpuraSecundario
                )
                Spacer(modifier = Modifier.height(16.dp))
                ExpenseBarChart(movimientos)
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun TimeSelector(selected: String, onSelected: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
            .padding(4.dp)
    ) {
        listOf("Semanal", "Mensual", "Anual").forEach { tab ->
            val isSelected = selected == tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        if (isSelected) PurpuraSecundario else Color.Transparent,
                        RoundedCornerShape(20.dp)
                    )
                    .clickable { onSelected(tab) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tab,
                    color = if (isSelected) Color.White else PurpuraSecundario.copy(alpha = 0.6f),
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun DonutChart(movimientos: List<MovimientoEntity>) {
    val gastosPorCategoria = movimientos
        .filter { it.tipo == TipoMovimiento.GASTO }
        .groupBy { it.categoriaId }
        .mapValues { it.value.sumOf { m -> m.monto } }
    
    val totalGastos = gastosPorCategoria.values.sum()
    
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
        Canvas(modifier = Modifier.size(200.dp)) {
            val strokeWidth = 50f
            var startAngle = -90f
            
            if (totalGastos == 0.0) {
                drawArc(
                    color = Color.LightGray.copy(alpha = 0.3f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth),
                    size = Size(size.width - strokeWidth, size.height - strokeWidth),
                    topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                )
            } else {
                val colors = listOf(PurpuraSecundario, PurpuraPrimario, Color(0xFF4C8C7E), Color(0xFFE0E0E0))
                var colorIndex = 0
                
                gastosPorCategoria.forEach { (_, monto) ->
                    val sweepAngle = (monto / totalGastos).toFloat() * 360f
                    drawArc(
                        color = colors[colorIndex % colors.size],
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = strokeWidth),
                        size = Size(size.width - strokeWidth, size.height - strokeWidth),
                        topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                    )
                    startAngle += sweepAngle
                    colorIndex++
                }
            }
        }
    }
}

@Composable
fun BudgetUsageSection(movimientos: List<MovimientoEntity>) {
    val totalIngresos = movimientos.filter { it.tipo == TipoMovimiento.INGRESO }.sumOf { it.monto }
    val totalGastos = movimientos.filter { it.tipo == TipoMovimiento.GASTO }.sumOf { it.monto }
    
    val porcentaje = if (totalIngresos > 0) (totalGastos / totalIngresos).toFloat().coerceIn(0f, 1f) else 0f

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "Cantidad de presupuesto usado",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = PurpuraSecundario
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            LinearProgressIndicator(
                progress = { porcentaje },
                modifier = Modifier
                    .width(200.dp)
                    .height(12.dp),
                color = PurpuraSecundario,
                trackColor = PurpuraSecundario.copy(alpha = 0.2f),
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text("${(porcentaje * 100).toInt()} %", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PurpuraSecundario)
        }
    }
}

@Composable
fun TrendChart(movimientos: List<MovimientoEntity>) {
    val gastosPorDia = movimientos
        .filter { it.tipo == TipoMovimiento.GASTO }
        .groupBy { it.fecha }
        .mapValues { it.value.sumOf { m -> m.monto } }
        .toList()
        .sortedBy { it.first }

    if (gastosPorDia.isEmpty()) {
        Card(modifier = Modifier.fillMaxWidth().height(150.dp), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text("No hay suficientes datos", color = Color.Gray)
            }
        }
        return
    }

    val maxGasto = gastosPorDia.maxOf { it.second }.toFloat().coerceAtLeast(1f)

    Card(
        modifier = Modifier.fillMaxWidth().height(200.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            val spaceX = size.width / (gastosPorDia.size.coerceAtLeast(2) - 1)
            val points = gastosPorDia.mapIndexed { index, (_, monto) ->
                Offset(index * spaceX, size.height - (monto.toFloat() / maxGasto * size.height))
            }

            for (i in 0 until points.size - 1) {
                drawLine(
                    color = PurpuraPrimario,
                    start = points[i],
                    end = points[i + 1],
                    strokeWidth = 8f,
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            }
            
            points.forEach { point ->
                drawCircle(color = PurpuraSecundario, radius = 6f, center = point)
            }
        }
    }
}

@Composable
fun ExpenseBarChart(movimientos: List<MovimientoEntity>) {
    val gastosPorCategoria = movimientos
        .filter { it.tipo == TipoMovimiento.GASTO }
        .groupBy { it.categoriaId }
        .mapValues { it.value.sumOf { m -> m.monto } }
        .toList()
        .sortedByDescending { it.second }

    val maxGasto = gastosPorCategoria.firstOrNull()?.second ?: 1.0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            gastosPorCategoria.take(5).forEach { (catId, monto) ->
                ExpenseBarItem("Cat #$catId", (monto / maxGasto).toFloat())
            }
            
            if (gastosPorCategoria.isEmpty()) {
                Text("No hay gastos registrados", modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.BottomEnd) {
                Text("Expandir", color = PurpuraSecundario, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ExpenseBarItem(label: String, progress: Float) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.width(80.dp), fontSize = 12.sp, color = PurpuraSecundario)
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .weight(1f)
                .height(24.dp),
            color = PurpuraSecundario,
            trackColor = Color.Transparent,
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Square
        )
    }
}
