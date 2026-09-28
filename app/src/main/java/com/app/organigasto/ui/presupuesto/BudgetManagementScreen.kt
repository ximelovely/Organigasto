package com.app.organigasto.ui.presupuesto

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.organigasto.data.local.entity.Categoria
import com.app.organigasto.data.local.entity.Gasto
import com.app.organigasto.domain.model.TipoPeriodo
import com.app.organigasto.ui.categorias.*
import com.app.organigasto.ui.movimientos.MovimientosViewModel
import com.app.organigasto.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetManagementScreen(
    onBackClick: () -> Unit,
    onManageCategoriesClick: () -> Unit,
    viewModel: MovimientosViewModel
) {
    val categorias by viewModel.categorias.collectAsState()
    val dailyAllowanceMap by viewModel.gastoDiarioStatus.collectAsState()
    var selectedCategoria by remember { mutableStateOf<Categoria?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (selectedCategoria == null) "Gestionar Sobres" else "Detalle del Sobre", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    IconButton(onClick = { if (selectedCategoria == null) onBackClick() else selectedCategoria = null }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (selectedCategoria == null) {
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
                            "Toca un sobre para ver y editar sus gastos.",
                            fontSize = 14.sp,
                            color = GrayText,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        TextButton(onClick = onManageCategoriesClick) {
                            Text("+ Nuevo Sobre", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                items(categorias) { categoria ->
                    BudgetItem(
                        categoria = categoria,
                        dailyAllowance = dailyAllowanceMap[categoria.id] ?: 0.0,
                        onClick = { selectedCategoria = categoria }
                    )
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        } else {
            EditCategoriaContent(
                categoria = selectedCategoria!!,
                viewModel = viewModel,
                onBack = { selectedCategoria = null },
                onDelete = {
                    viewModel.eliminarCategoria(selectedCategoria!!.id)
                    selectedCategoria = null
                },
                onSave = { nombre, monto, tipo, colorHex, iconoNombre, iconoUri ->
                    viewModel.actualizarCategoria(selectedCategoria!!.id, nombre, colorHex, iconoNombre, iconoUri)
                    if (monto > 0) {
                        viewModel.configurarPresupuesto(selectedCategoria!!.id, monto, tipo)
                    }
                    selectedCategoria = null
                },
                padding = padding
            )
        }
    }
}

@Composable
fun BudgetItem(
    categoria: Categoria,
    dailyAllowance: Double,
    onClick: () -> Unit
) {
    val categoryColor = parseCategoryColor(categoria.colorHex)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(categoryColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                CategoryIconView(
                    iconoNombre = categoria.iconoNombre,
                    iconoUri = categoria.iconoUri,
                    tint = Color.White,
                    size = 24.dp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    categoria.nombre,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp
                )
                if (dailyAllowance > 0) {
                    Text(
                        "Límite diario sugerido: $${String.format(java.util.Locale.US, "%.0f", dailyAllowance)} / día",
                        fontSize = 12.sp,
                        color = categoryColor,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text("Toca para ver gastos y configurar", fontSize = 12.sp, color = GrayText)
                }
            }

            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = GrayText)
        }
    }
}

@Composable
fun EditCategoriaContent(
    categoria: Categoria,
    viewModel: MovimientosViewModel,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    onSave: (String, Double, TipoPeriodo, String?, String?, String?) -> Unit,
    padding: PaddingValues
) {
    val context = LocalContext.current

    var nombre by remember { mutableStateOf(categoria.nombre) }
    var monto by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf(TipoPeriodo.MENSUAL) }

    var selectedColorHex by remember { mutableStateOf(categoria.colorHex ?: "#7C4DFF") }
    var selectedIconNombre by remember { mutableStateOf<String?>(categoria.iconoNombre ?: "categoria") }
    var selectedIconUri by remember { mutableStateOf(categoria.iconoUri) }

    var showColorPicker by remember { mutableStateOf(false) }

    val gastosDeEsteSobre by viewModel.observarGastosPorCategoria(categoria.id).collectAsState(initial = emptyList())
    var editingGasto by remember { mutableStateOf<Gasto?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedPath = saveImageToInternalStorage(context, uri)
            if (savedPath != null) {
                selectedIconUri = savedPath
                selectedIconNombre = null
            }
        }
    }

    val currentColor = parseCategoryColor(selectedColorHex)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Live Envelope Preview Card
        CategoryEnvelopePreview(
            nombre = nombre,
            presupuesto = monto.toDoubleOrNull() ?: 0.0,
            frecuencia = tipo.name.lowercase().replaceFirstChar { it.uppercase() },
            color = currentColor,
            iconoNombre = selectedIconNombre,
            iconoUri = selectedIconUri
        )

        // SECCIÓN: Gastos Registrados en este Sobre
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Gastos en este Sobre",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = currentColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            "${gastosDeEsteSobre.size} gasto${if (gastosDeEsteSobre.size != 1) "s" else ""}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = currentColor,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                if (gastosDeEsteSobre.isEmpty()) {
                    Text(
                        "Aún no hay gastos registrados en este sobre.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        gastosDeEsteSobre.forEach { gasto ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(currentColor.copy(alpha = 0.08f))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        gasto.descripcion,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        gasto.fecha,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        "-$${String.format(java.util.Locale.US, "%.2f", gasto.monto)}",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp,
                                        color = RojoError
                                    )

                                    IconButton(
                                        onClick = { editingGasto = gasto },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "Editar gasto",
                                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { viewModel.eliminarGasto(gasto) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Eliminar gasto",
                                            tint = RojoError.copy(alpha = 0.8f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // SECCIÓN: Configuración y Propiedades del Sobre
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
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre del sobre") },
                    leadingIcon = { Icon(Icons.Default.Tag, contentDescription = null, tint = currentColor) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = currentColor,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                )

                OutlinedTextField(
                    value = monto,
                    onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) monto = it },
                    label = { Text("Nuevo presupuesto") },
                    prefix = { Text("$ ", color = currentColor, fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = currentColor,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                )

                Column {
                    Text("Frecuencia:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TipoPeriodo.entries.forEach { t ->
                            FilterChip(
                                selected = tipo == t,
                                onClick = { tipo = t },
                                label = { Text(t.name.lowercase().replaceFirstChar { it.uppercase() }, fontSize = 12.sp) },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = currentColor,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Color Selection
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Color del Sobre", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        TextButton(onClick = { showColorPicker = true }) {
                            Icon(Icons.Default.Palette, contentDescription = null, tint = currentColor, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Personalizar", color = currentColor, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(PRESET_ENVELOPE_COLORS) { option ->
                            val isSelected = selectedColorHex.equals(option.hex, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(option.color, CircleShape)
                                    .border(
                                        width = if (isSelected) 3.dp else 0.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColorHex = option.hex },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Icon Selection
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Icono o Foto del Sobre", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        OutlinedButton(
                            onClick = { photoPickerLauncher.launch("image/*") },
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Subir foto", tint = currentColor, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Subir Foto", fontSize = 12.sp, color = currentColor)
                        }
                    }

                    if (selectedIconUri != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = currentColor.copy(alpha = 0.1f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    CategoryIconView(iconoNombre = null, iconoUri = selectedIconUri, size = 40.dp)
                                    Text("Foto personalizada cargada", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                }
                                IconButton(onClick = {
                                    selectedIconUri = null
                                    selectedIconNombre = "categoria"
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = "Quitar foto", tint = Color.Red)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp)
                    ) {
                        items(PRESET_ENVELOPE_ICONS) { item ->
                            val isSelected = selectedIconUri == null && selectedIconNombre == item.key
                            Box(
                                modifier = Modifier
                                    .height(56.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) currentColor else currentColor.copy(alpha = 0.08f))
                                    .clickable {
                                        selectedIconNombre = item.key
                                        selectedIconUri = null
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.name,
                                        tint = if (isSelected) Color.White else currentColor,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Text(text = item.name, fontSize = 9.sp, color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Action Buttons
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = onDelete,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = RojoError)
            ) {
                Text("ELIMINAR", fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = {
                    onSave(
                        nombre,
                        monto.toDoubleOrNull() ?: 0.0,
                        tipo,
                        selectedColorHex,
                        selectedIconNombre,
                        selectedIconUri
                    )
                },
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = currentColor, contentColor = Color.White)
            ) {
                Text("GUARDAR", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showColorPicker) {
        CustomColorPickerDialog(
            initialColorHex = selectedColorHex,
            onColorSelected = { hex -> selectedColorHex = hex },
            onDismiss = { showColorPicker = false }
        )
    }

    if (editingGasto != null) {
        EditGastoDialog(
            gasto = editingGasto!!,
            onDismiss = { editingGasto = null },
            onConfirm = { updatedGasto ->
                viewModel.actualizarGasto(updatedGasto)
                editingGasto = null
            }
        )
    }
}

@Composable
fun EditGastoDialog(
    gasto: Gasto,
    onDismiss: () -> Unit,
    onConfirm: (Gasto) -> Unit
) {
    var descripcion by remember { mutableStateOf(gasto.descripcion) }
    var montoStr by remember { mutableStateOf(gasto.monto.toString()) }
    var fechaStr by remember { mutableStateOf(gasto.fecha) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Movimiento", fontWeight = FontWeight.Bold) },
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = montoStr,
                    onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) montoStr = it },
                    label = { Text("Monto ($)") },
                    prefix = { Text("$ ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = fechaStr,
                    onValueChange = { fechaStr = it },
                    label = { Text("Fecha (yyyy-MM-dd)") },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newMonto = montoStr.toDoubleOrNull() ?: gasto.monto
                    if (descripcion.isNotBlank() && newMonto > 0) {
                        onConfirm(gasto.copy(monto = newMonto, descripcion = descripcion, fecha = fechaStr))
                    }
                },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PurpuraPrimario)
            ) {
                Text("GUARDAR", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCELAR")
            }
        }
    )
}
