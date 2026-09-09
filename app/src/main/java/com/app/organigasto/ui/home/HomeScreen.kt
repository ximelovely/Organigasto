package com.app.organigasto.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.organigasto.ui.theme.PurpuraPrimario
import com.app.organigasto.ui.theme.PurpuraSecundario
import com.app.organigasto.ui.theme.Black
import com.app.organigasto.ui.theme.GrayText

import com.app.organigasto.ui.theme.CremaFondo
import com.app.organigasto.ui.movimientos.MovimientosViewModel
import com.app.organigasto.data.local.entity.MovimientoEntity
import com.app.organigasto.domain.model.TipoMovimiento
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun HomeScreen(
    onAddMovementClick: () -> Unit,
    viewModel: MovimientosViewModel
) {
    val movimientos by viewModel.movimientosFiltrados.collectAsState()
    val balance by viewModel.balanceTotal.collectAsState()
    val filtro by viewModel.filtroTiempo.collectAsState()
    val gastosPorCategoria by viewModel.gastosPorCategoria.collectAsState()
    val movimientosRecurrentes by viewModel.movimientosRecurrentes.collectAsState()

    Scaffold(
        topBar = { HomeTopBar() },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddMovementClick,
                containerColor = PurpuraPrimario,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar")
            }
        },
        containerColor = CremaFondo,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }
            
            // Header Card
            item {
                HeaderBalanceCard(balance, filtro)
            }

            // Presupuesto por categoria
            if (gastosPorCategoria.isNotEmpty()) {
                item {
                    BudgetSection(gastosPorCategoria)
                }
            }

            // Gastos recurrentes
            if (movimientosRecurrentes.isNotEmpty()) {
                item {
                    RecurrentExpensesSection(movimientosRecurrentes)
                }
            }

            // Meta de ahorro
            item {
                SavingsGoalCard()
            }

            // Lista de movimientos recientes
            item {
                Text(
                    "Movimientos recientes",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = PurpuraSecundario
                )
            }

            items(movimientos.size) { index ->
                val movimiento = movimientos[index]
                MovementItem(
                    movimiento = movimiento,
                    onDelete = { viewModel.eliminarMovimiento(movimiento) }
                )
            }
            
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun MovementItem(movimiento: MovimientoEntity, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (movimiento.tipo == TipoMovimiento.INGRESO) Color(0xFFE8F5E9) else Color(0xFFFBE9E7),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (movimiento.tipo == TipoMovimiento.INGRESO) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = if (movimiento.tipo == TipoMovimiento.INGRESO) Color(0xFF4CAF50) else Color(0xFFF44336)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(movimiento.cuenta, fontWeight = FontWeight.Bold)
                Text(movimiento.fecha.toString(), fontSize = 12.sp, color = Color.Gray)
            }
            Text(
                text = "${if (movimiento.tipo == TipoMovimiento.INGRESO) "+" else "-"}$${movimiento.monto}",
                color = if (movimiento.tipo == TipoMovimiento.INGRESO) Color(0xFF4CAF50) else Color(0xFFF44336),
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Gray)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar() {
    CenterAlignedTopAppBar(
        title = {
            Text(
                "OrganiGasto",
                fontWeight = FontWeight.Bold,
                color = PurpuraSecundario
            )
        },
        navigationIcon = {
            IconButton(onClick = { }) {
                Icon(Icons.Default.AccountCircle, contentDescription = "Perfil", tint = PurpuraSecundario)
            }
        },
        actions = {
            IconButton(onClick = { }) {
                Icon(Icons.Default.Settings, contentDescription = "Ajustes", tint = PurpuraSecundario)
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = CremaFondo,
            titleContentColor = PurpuraSecundario,
            navigationIconContentColor = PurpuraSecundario,
            actionIconContentColor = PurpuraSecundario
        )
    )
}

@Composable
fun HeaderBalanceCard(balance: Double, filtro: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = PurpuraSecundario)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "DISPONIBLE PARA GASTAR ESTE ${filtro.uppercase()}",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "$${String.format("%.2f", balance)}",
                color = Color.White,
                fontSize = 40.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun BudgetSection(gastos: List<com.app.organigasto.ui.movimientos.GastoCategoria>) {
    Column {
        Text(
            "Tu presupuesto por categoria",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = PurpuraSecundario
        )
        Spacer(modifier = Modifier.height(16.dp))
        gastos.forEach { gasto ->
            BudgetBar(gasto.nombre, gasto.gastado.toFloat(), gasto.presupuesto.toFloat())
        }
    }
}

@Composable
fun BudgetBar(label: String, current: Float, total: Float) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontWeight = FontWeight.Bold, color = PurpuraSecundario)
            Text("$$current de $$total", fontSize = 12.sp, color = PurpuraSecundario)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { current / total },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = PurpuraPrimario,
            trackColor = PurpuraPrimario.copy(alpha = 0.1f),
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    }
}

@Composable
fun RecurrentExpensesSection(movimientos: List<MovimientoEntity>) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Gastos recurrentes",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = PurpuraSecundario
            )
            TextButton(onClick = { }) {
                Text("Ver todos", color = PurpuraPrimario)
            }
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(movimientos.size) { index ->
                val mov = movimientos[index]
                RecurrentItemCard(
                    title = mov.nota ?: "Gasto fijo",
                    amount = "$${mov.monto}",
                    date = mov.recurrencia.name.lowercase().replaceFirstChar { it.uppercase() },
                    icon = Icons.Default.Repeat
                )
            }
        }
    }
}

@Composable
fun RecurrentItemCard(title: String, amount: String, date: String, icon: ImageVector) {
    Card(
        modifier = Modifier.width(160.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(PurpuraPrimario.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = PurpuraPrimario)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, fontWeight = FontWeight.Bold, color = Black)
            Text(amount, fontSize = 20.sp, fontWeight = FontWeight.Black, color = Black)
            Text(date, fontSize = 10.sp, color = GrayText)
        }
    }
}

@Composable
fun SavingsGoalCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Meta de ahorro", fontWeight = FontWeight.Bold, color = PurpuraSecundario)
                Text("Vacaciones", color = GrayText)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text("$2,100 ahorrados", fontSize = 12.sp, color = PurpuraSecundario)
                Text("Meta: $5,000 - 15 Dic", fontSize = 10.sp, color = GrayText)
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { 2100f / 5000f },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = Color(0xFF4CAF50),
                trackColor = Color(0xFF4CAF50).copy(alpha = 0.1f),
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        }
    }
}
