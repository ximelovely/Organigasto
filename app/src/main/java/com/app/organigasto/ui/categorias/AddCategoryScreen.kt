package com.app.organigasto.ui.categorias

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.app.organigasto.domain.model.TipoPeriodo
import com.app.organigasto.ui.components.AnimatedFinanceBackground
import com.app.organigasto.ui.movimientos.MovimientosViewModel
import com.app.organigasto.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCategoryScreen(
    onBackClick: () -> Unit,
    viewModel: MovimientosViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var nombre by remember { mutableStateOf("") }
    var presupuesto by remember { mutableStateOf("") }
    var tipoPeriodo by remember { mutableStateOf(TipoPeriodo.MENSUAL) }
    
    // Color & Icon state
    var selectedColorHex by remember { mutableStateOf("#7C4DFF") }
    var selectedIconNombre by remember { mutableStateOf<String?>("categoria") }
    var selectedIconUri by remember { mutableStateOf<String?>(null) }
    
    var showColorPicker by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedPath = saveImageToInternalStorage(context, uri)
            if (savedPath != null) {
                selectedIconUri = savedPath
                selectedIconNombre = null // Override preset icon when photo is set
            }
        }
    }

    val currentColor = parseCategoryColor(selectedColorHex)

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        AnimatedFinanceBackground(modifier = Modifier.fillMaxSize())

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Nuevo Sobre",
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
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Live Envelope Preview
                CategoryEnvelopePreview(
                    nombre = nombre,
                    presupuesto = presupuesto.toDoubleOrNull() ?: 0.0,
                    frecuencia = tipoPeriodo.name.lowercase().replaceFirstChar { it.uppercase() },
                    color = currentColor,
                    iconoNombre = selectedIconNombre,
                    iconoUri = selectedIconUri
                )

                // Main Form Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        // 1. Name Input
                        OutlinedTextField(
                            value = nombre,
                            onValueChange = { nombre = it },
                            label = { Text("Nombre del sobre") },
                            placeholder = { Text("Ej: Comida, Renta, Salidas") },
                            leadingIcon = {
                                Icon(Icons.Default.Tag, contentDescription = null, tint = currentColor)
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = currentColor,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                        )

                        // 2. Budget Input & Frequency
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = presupuesto,
                                onValueChange = {
                                    if (it.isEmpty() || it.toDoubleOrNull() != null) presupuesto = it
                                },
                                label = { Text("Presupuesto") },
                                prefix = { Text("$ ", color = currentColor, fontWeight = FontWeight.Bold) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = currentColor,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                )
                            )
                        }

                        // Frequency chips
                        Column {
                            Text(
                                "Frecuencia del Presupuesto",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TipoPeriodo.entries.forEach { tipo ->
                                    FilterChip(
                                        selected = tipoPeriodo == tipo,
                                        onClick = { tipoPeriodo = tipo },
                                        label = {
                                            Text(
                                                tipo.name.lowercase().replaceFirstChar { it.uppercase() },
                                                fontSize = 12.sp
                                            )
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = currentColor,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }
                        }

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )

                        // 3. Color Picker Selection
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Color del Sobre",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                TextButton(onClick = { showColorPicker = true }) {
                                    Icon(
                                        Icons.Default.Palette,
                                        contentDescription = null,
                                        tint = currentColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Personalizar", color = currentColor, fontSize = 12.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

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
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )

                        // 4. Icon & Photo Selection
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Icono o Foto del Sobre",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                OutlinedButton(
                                    onClick = { photoPickerLauncher.launch("image/*") },
                                    shape = RoundedCornerShape(20.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        Icons.Default.AddPhotoAlternate,
                                        contentDescription = "Subir foto",
                                        tint = currentColor,
                                        modifier = Modifier.size(18.dp)
                                    )
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
                                            CategoryIconView(
                                                iconoNombre = null,
                                                iconoUri = selectedIconUri,
                                                size = 40.dp
                                            )
                                            Text(
                                                "Foto personalizada seleccionada",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        IconButton(onClick = {
                                            selectedIconUri = null
                                            selectedIconNombre = "categoria"
                                        }) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Quitar foto",
                                                tint = Color.Red
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                "O elige un icono predeterminado:",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            LazyVerticalGrid(
                                columns = GridCells.Fixed(4),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 220.dp)
                            ) {
                                items(PRESET_ENVELOPE_ICONS) { item ->
                                    val isSelected = selectedIconUri == null && selectedIconNombre == item.key
                                    Box(
                                        modifier = Modifier
                                            .height(56.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(
                                                if (isSelected) currentColor else currentColor.copy(alpha = 0.08f)
                                            )
                                            .clickable {
                                                selectedIconNombre = item.key
                                                selectedIconUri = null
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = item.icon,
                                                contentDescription = item.name,
                                                tint = if (isSelected) Color.White else currentColor,
                                                modifier = Modifier.size(22.dp)
                                            )
                                            Text(
                                                text = item.name,
                                                fontSize = 9.sp,
                                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Action Button
                Button(
                    onClick = {
                        if (nombre.isNotBlank()) {
                            scope.launch {
                                val m = presupuesto.toDoubleOrNull() ?: 0.0
                                viewModel.agregarCategoriaConPresupuesto(
                                    nombre = nombre,
                                    presupuesto = m,
                                    tipoPeriodo = tipoPeriodo,
                                    colorHex = selectedColorHex,
                                    iconoNombre = selectedIconNombre,
                                    iconoUri = selectedIconUri
                                )
                                delay(200)
                                onBackClick()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = nombre.isNotBlank(),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = currentColor,
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "CREAR SOBRE",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showColorPicker) {
        CustomColorPickerDialog(
            initialColorHex = selectedColorHex,
            onColorSelected = { hex -> selectedColorHex = hex },
            onDismiss = { showColorPicker = false }
        )
    }
}
