package com.app.organigasto.ui.categorias

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.organigasto.ui.theme.PurpuraPrimario
import com.app.organigasto.ui.theme.PurpuraSecundario
import com.app.organigasto.ui.theme.CremaFondo
import com.app.organigasto.ui.movimientos.MovimientosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCategoryScreen(
    onBackClick: () -> Unit,
    viewModel: MovimientosViewModel
) {
    var nombre by remember { mutableStateOf("") }
    var selectedIconName by remember { mutableStateOf("restaurant") }
    var presupuesto by remember { mutableStateOf("") }

    val iconOptions = listOf(
        "restaurant" to Icons.Default.Restaurant,
        "directions_car" to Icons.Default.DirectionsCar,
        "home" to Icons.Default.Home,
        "movie" to Icons.Default.Movie,
        "shopping_cart" to Icons.Default.ShoppingCart,
        "health_and_safety" to Icons.Default.HealthAndSafety,
        "school" to Icons.Default.School,
        "fitness_center" to Icons.Default.FitnessCenter,
        "pets" to Icons.Default.Pets,
        "celebration" to Icons.Default.Celebration
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva Categoría", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre de la categoría") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = presupuesto,
                onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) presupuesto = it },
                label = { Text("Presupuesto mensual (Opcional)") },
                modifier = Modifier.fillMaxWidth(),
                prefix = { Text("$", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Selecciona un icono",
                modifier = Modifier.fillMaxWidth(),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(iconOptions) { (name, icon) ->
                    IconOptionItem(
                        icon = icon,
                        isSelected = selectedIconName == name,
                        onClick = { selectedIconName = name }
                    )
                }
            }

            Button(
                onClick = {
                    if (nombre.isNotBlank()) {
                        viewModel.agregarCategoria(
                            nombre = nombre,
                            icono = selectedIconName,
                            colorHex = "#8B7BB8",
                            presupuesto = presupuesto.toDoubleOrNull() ?: 0.0
                        )
                        onBackClick()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Crear Categoría", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

@Composable
fun IconOptionItem(icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                RoundedCornerShape(16.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(24.dp)
        )
    }
}
