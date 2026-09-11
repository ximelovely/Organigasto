package com.app.organigasto.ui.presupuesto

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import com.app.organigasto.ui.theme.PurpuraPrimario
import com.app.organigasto.ui.theme.PurpuraSecundario
import com.app.organigasto.ui.theme.CremaFondo
import com.app.organigasto.ui.movimientos.MovimientosViewModel
import com.app.organigasto.data.local.entity.CategoriaEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetManagementScreen(
    onBackClick: () -> Unit,
    onManageCategoriesClick: () -> Unit, // Añadido
    viewModel: MovimientosViewModel
) {
    val categorias by viewModel.categorias.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestionar Presupuestos", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Asigna cuánto quieres gastar al mes.",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    TextButton(onClick = onManageCategoriesClick) {
                        Text("Gestionar Categorías", color = PurpuraPrimario, fontWeight = FontWeight.Bold)
                    }
                }
            }

            items(categorias) { categoria ->
                BudgetItem(
                    categoria = categoria,
                    onUpdateBudget = { nuevoMonto ->
                        viewModel.actualizarPresupuesto(categoria.id, nuevoMonto)
                    }
                )
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun BudgetItem(
    categoria: CategoriaEntity,
    onUpdateBudget: (Double) -> Unit
) {
    var textValue by remember(categoria.presupuestoMensual) { 
        mutableStateOf(if (categoria.presupuestoMensual > 0) categoria.presupuestoMensual.toString() else "") 
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val icon = when (categoria.icono) {
                "restaurant" -> Icons.Default.Restaurant
                "directions_car" -> Icons.Default.DirectionsCar
                "home" -> Icons.Default.Home
                "movie" -> Icons.Default.Movie
                "shopping_cart" -> Icons.Default.ShoppingCart
                "health_and_safety" -> Icons.Default.HealthAndSafety
                else -> Icons.Default.Category
            }
            
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(categoria.nombre, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
                Text(
                    text = "Límite: $${categoria.presupuestoMensual}", 
                    fontSize = 11.sp, 
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Medium
                )
            }

            OutlinedTextField(
                value = textValue,
                onValueChange = { 
                    if (it.isEmpty() || it.toDoubleOrNull() != null) {
                        textValue = it
                        it.toDoubleOrNull()?.let { monto -> onUpdateBudget(monto) }
                    }
                },
                modifier = Modifier.width(110.dp),
                placeholder = { Text("0.00", fontSize = 14.sp) },
                prefix = { Text("$", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    focusedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                textStyle = LocalTextStyle.current.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold)
            )
        }
    }
}
