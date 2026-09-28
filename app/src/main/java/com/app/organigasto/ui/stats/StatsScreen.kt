package com.app.organigasto.ui.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.organigasto.data.local.entity.Gasto
import com.app.organigasto.ui.categorias.CategoryIconView
import com.app.organigasto.ui.categorias.parseCategoryColor
import com.app.organigasto.ui.components.AnimatedFinanceBackground
import com.app.organigasto.ui.movimientos.MovimientosViewModel
import com.app.organigasto.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(viewModel: MovimientosViewModel) {
    val filtro by viewModel.filtroTiempo.collectAsState()
    val gastos by viewModel.gastos.collectAsState()
    val categorias by viewModel.categorias.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        AnimatedFinanceBackground(modifier = Modifier.fillMaxSize())

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Análisis y Estadísticas",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { }) {
                            Icon(
                                Icons.Default.Analytics,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    TimeSelector(filtro) { viewModel.setFiltro(it) }
                }

                // Distribution Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Distribución por Sobres",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            DonutChart(gastos = gastos, categorias = categorias)
                        }
                    }
                }

                // Trend Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                "Tendencia de Gastos",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            TrendChart(gastos)
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
fun TimeSelector(selected: String, onSelected: (String) -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(25.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
        ) {
            listOf("Mensual", "Anual").forEach { tab ->
                val isSelected = selected == tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(
                            if (isSelected) PurpuraPrimario else Color.Transparent,
                            RoundedCornerShape(21.dp)
                        )
                        .clickable { onSelected(tab) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun DonutChart(gastos: List<Gasto>, categorias: List<com.app.organigasto.data.local.entity.Categoria>) {
    val gastosPorCategoria = gastos.groupBy { it.categoriaId }.mapValues { it.value.sumOf { g -> g.monto } }
    val totalGastos = gastosPorCategoria.values.sum()

    val catMap = remember(categorias) { categorias.associateBy { it.id } }

    if (totalGastos == 0.0) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            Text(
                "Aún no hay gastos registrados en este período",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                fontSize = 13.sp
            )
        }
        return
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
            Canvas(modifier = Modifier.size(200.dp)) {
                val strokeWidth = 36f
                var startAngle = -90f

                gastosPorCategoria.forEach { (catId, monto) ->
                    val cat = catMap[catId]
                    val catColor = parseCategoryColor(cat?.colorHex)
                    val sweepAngle = (monto / totalGastos).toFloat() * 360f

                    drawArc(
                        color = catColor,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = strokeWidth),
                        size = Size(size.width - strokeWidth, size.height - strokeWidth),
                        topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                    )
                    startAngle += sweepAngle
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "TOTAL",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Text(
                    "$${String.format(java.util.Locale.US, "%.0f", totalGastos)}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = PurpuraPrimario
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Category Legend Breakdown
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            gastosPorCategoria.entries.sortedByDescending { it.value }.forEach { (catId, monto) ->
                val cat = catMap[catId]
                val catColor = parseCategoryColor(cat?.colorHex)
                val porcentaje = (monto / totalGastos) * 100

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(catColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            CategoryIconView(
                                iconoNombre = cat?.iconoNombre,
                                iconoUri = cat?.iconoUri,
                                tint = Color.White,
                                size = 18.dp
                            )
                        }
                        Text(
                            cat?.nombre ?: "General",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "${String.format(java.util.Locale.US, "%.1f", porcentaje)}%",
                            fontSize = 12.sp,
                            color = catColor,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "$${String.format(java.util.Locale.US, "%.2f", monto)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TrendChart(gastos: List<Gasto>) {
    val gastosPorDia = gastos.groupBy { it.fecha }.mapValues { it.value.sumOf { g -> g.monto } }.toList().sortedBy { it.first }
    if (gastosPorDia.isEmpty()) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            Text("Aún no hay datos para mostrar tendencia", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), fontSize = 13.sp)
        }
        return
    }

    val maxGasto = gastosPorDia.maxOf { it.second }.toFloat().coerceAtLeast(1f)

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val spaceX = size.width / (gastosPorDia.size.coerceAtLeast(2) - 1)
                val points = gastosPorDia.mapIndexed { index, (_, monto) ->
                    Offset(
                        x = index * spaceX,
                        y = size.height - (monto.toFloat() / maxGasto * (size.height - 20f)) - 10f
                    )
                }

                // Fill area gradient
                if (points.size >= 2) {
                    val path = Path().apply {
                        moveTo(points.first().x, size.height)
                        points.forEach { lineTo(it.x, it.y) }
                        lineTo(points.last().x, size.height)
                        close()
                    }
                    drawPath(
                        path = path,
                        brush = Brush.verticalGradient(
                            colors = listOf(PurpuraPrimario.copy(alpha = 0.3f), PurpuraPrimario.copy(alpha = 0.0f))
                        )
                    )
                }

                // Line
                for (i in 0 until points.size - 1) {
                    drawLine(
                        color = PurpuraPrimario,
                        start = points[i],
                        end = points[i + 1],
                        strokeWidth = 6f,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                }

                // Points
                points.forEach { point ->
                    drawCircle(color = PurpuraSecundario, radius = 5f, center = point)
                    drawCircle(color = Color.White, radius = 2.5f, center = point)
                }
            }
        }
    }
}
