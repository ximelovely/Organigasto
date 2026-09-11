package com.app.organigasto.ui.categorias

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.organigasto.ui.theme.PurpuraPrimario
import com.app.organigasto.ui.theme.PurpuraSecundario
import com.app.organigasto.ui.theme.CremaFondo
import com.app.organigasto.ui.movimientos.MovimientosViewModel
import com.app.organigasto.data.local.entity.CategoriaEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryListScreen(
    onBackClick: () -> Unit,
    onAddCategoryClick: () -> Unit,
    viewModel: MovimientosViewModel
) {
    val categorias by viewModel.categorias.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Categorías", fontWeight = FontWeight.Bold, color = PurpuraSecundario) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = PurpuraSecundario)
                    }
                },
                actions = {
                    IconButton(onClick = onAddCategoryClick) {
                        Icon(Icons.Default.Add, contentDescription = "Añadir Categoría", tint = PurpuraSecundario)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CremaFondo)
            )
        },
        containerColor = CremaFondo
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }
            
            items(categorias) { categoria ->
                CategoryListItem(
                    categoria = categoria,
                    onDelete = { viewModel.eliminarCategoria(categoria) }
                )
            }
            
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun CategoryListItem(categoria: CategoriaEntity, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
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
                    .background(PurpuraPrimario.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = PurpuraPrimario, modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = categoria.nombre,
                    fontWeight = FontWeight.Bold,
                    color = PurpuraSecundario,
                    fontSize = 17.sp
                )
                if (categoria.presupuestoMensual > 0) {
                    Text(
                        text = "Presupuesto: $${categoria.presupuestoMensual}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }

            IconButton(
                onClick = onDelete,
                colors = IconButtonDefaults.iconButtonColors(contentColor = Color.Red.copy(alpha = 0.5f))
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar")
            }
        }
    }
}
