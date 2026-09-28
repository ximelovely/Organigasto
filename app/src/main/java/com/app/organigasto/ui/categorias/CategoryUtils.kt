package com.app.organigasto.ui.categorias

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.organigasto.ui.theme.PurpuraPrimario
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

data class EnvelopeColorOption(
    val hex: String,
    val name: String,
    val color: Color
)

val PRESET_ENVELOPE_COLORS = listOf(
    EnvelopeColorOption("#7C4DFF", "Púrpura", Color(0xFF7C4DFF)),
    EnvelopeColorOption("#0288D1", "Azul Océano", Color(0xFF0288D1)),
    EnvelopeColorOption("#00897B", "Esmeralda", Color(0xFF00897B)),
    EnvelopeColorOption("#43A047", "Verde Menta", Color(0xFF43A047)),
    EnvelopeColorOption("#F57C00", "Naranja Sol", Color(0xFFF57C00)),
    EnvelopeColorOption("#E64A19", "Coral Vivo", Color(0xFFE64A19)),
    EnvelopeColorOption("#D81B60", "Rosa Neón", Color(0xFFD81B60)),
    EnvelopeColorOption("#E53935", "Rojo Fuego", Color(0xFFE53935)),
    EnvelopeColorOption("#FFA000", "Ámbar Cálido", Color(0xFFFFA000)),
    EnvelopeColorOption("#3F51B5", "Índigo", Color(0xFF3F51B5)),
    EnvelopeColorOption("#00796B", "Teal", Color(0xFF00796B)),
    EnvelopeColorOption("#546E7A", "Gris Pizarra", Color(0xFF546E7A))
)

data class PresetIconOption(
    val key: String,
    val name: String,
    val icon: ImageVector
)

val PRESET_ENVELOPE_ICONS = listOf(
    PresetIconOption("comida", "Comida", Icons.Default.Restaurant),
    PresetIconOption("compras", "Compras", Icons.Default.ShoppingCart),
    PresetIconOption("vivienda", "Hogar", Icons.Default.Home),
    PresetIconOption("transporte", "Auto/Gas", Icons.Default.DirectionsCar),
    PresetIconOption("ocio", "Ocio/Juegos", Icons.Default.SportsEsports),
    PresetIconOption("salud", "Salud", Icons.Default.MedicalServices),
    PresetIconOption("viajes", "Viajes", Icons.Default.Flight),
    PresetIconOption("educacion", "Educación", Icons.Default.School),
    PresetIconOption("servicios", "Servicios", Icons.Default.Receipt),
    PresetIconOption("regalos", "Regalos", Icons.Default.CardGiftcard),
    PresetIconOption("mascotas", "Mascotas", Icons.Default.Pets),
    PresetIconOption("ahorro", "Ahorro", Icons.Default.Savings),
    PresetIconOption("deporte", "Deporte", Icons.Default.FitnessCenter),
    PresetIconOption("trabajo", "Trabajo", Icons.Default.Work),
    PresetIconOption("suscripciones", "Digital", Icons.Default.Smartphone),
    PresetIconOption("categoria", "General", Icons.Default.Category)
)

fun parseCategoryColor(colorHex: String?, defaultColor: Color = PurpuraPrimario): Color {
    if (colorHex.isNullOrBlank()) return defaultColor
    return try {
        val cleanHex = colorHex.removePrefix("#")
        val colorInt = when (cleanHex.length) {
            6 -> (0xFF000000 or cleanHex.toLong(16)).toInt()
            8 -> cleanHex.toLong(16).toInt()
            else -> return defaultColor
        }
        Color(colorInt)
    } catch (e: Exception) {
        defaultColor
    }
}

fun getPresetIconVector(key: String?): ImageVector? {
    if (key.isNullOrBlank()) return null
    return PRESET_ENVELOPE_ICONS.find { it.key == key }?.icon
}

fun saveImageToInternalStorage(context: Context, uri: Uri): String? {
    return try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val directory = File(context.filesDir, "envelope_icons")
        if (!directory.exists()) {
            directory.mkdirs()
        }
        val fileName = "envelope_${System.currentTimeMillis()}.jpg"
        val file = File(directory, fileName)
        val outputStream = FileOutputStream(file)
        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
        file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        uri.toString()
    }
}

@Composable
fun CategoryIconView(
    iconoNombre: String?,
    iconoUri: String?,
    defaultIcon: ImageVector = Icons.Default.Category,
    tint: Color = Color.White,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp
) {
    val context = LocalContext.current
    var loadedBitmap by remember(iconoUri) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(iconoUri) {
        if (!iconoUri.isNullOrEmpty()) {
            try {
                val file = File(iconoUri)
                val bitmap = if (file.exists()) {
                    BitmapFactory.decodeFile(file.absolutePath)
                } else {
                    val uri = Uri.parse(iconoUri)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        val source = ImageDecoder.createSource(context.contentResolver, uri)
                        ImageDecoder.decodeBitmap(source)
                    } else {
                        @Suppress("DEPRECATION")
                        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                    }
                }
                loadedBitmap = bitmap?.asImageBitmap()
            } catch (e: Exception) {
                loadedBitmap = null
            }
        } else {
            loadedBitmap = null
        }
    }

    if (loadedBitmap != null) {
        Image(
            bitmap = loadedBitmap!!,
            contentDescription = "Icono personalizado",
            contentScale = ContentScale.Crop,
            modifier = modifier
                .size(size)
                .clip(CircleShape)
        )
    } else {
        val vector = getPresetIconVector(iconoNombre) ?: defaultIcon
        Icon(
            imageVector = vector,
            contentDescription = null,
            tint = tint,
            modifier = modifier.size(size)
        )
    }
}

@Composable
fun CategoryEnvelopePreview(
    nombre: String,
    presupuesto: Double,
    frecuencia: String,
    color: Color,
    iconoNombre: String?,
    iconoUri: String?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.12f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(color, CircleShape)
                            .border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        CategoryIconView(
                            iconoNombre = iconoNombre,
                            iconoUri = iconoUri,
                            tint = Color.White,
                            size = 28.dp
                        )
                    }

                    Column {
                        Text(
                            text = if (nombre.isBlank()) "Nombre del Sobre" else nombre,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = color.copy(alpha = 0.2f),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                text = "Sobre $frecuencia",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = color,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "PRESUPUESTO",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "$${String.format(java.util.Locale.US, "%.2f", presupuesto)}",
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = color
                    )
                }
            }
        }
    }
}

@Composable
fun CustomColorPickerDialog(
    initialColorHex: String,
    onColorSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var hexInput by remember { mutableStateOf(initialColorHex.removePrefix("#")) }
    var red by remember { mutableFloatStateOf(parseCategoryColor(initialColorHex).red) }
    var green by remember { mutableFloatStateOf(parseCategoryColor(initialColorHex).green) }
    var blue by remember { mutableFloatStateOf(parseCategoryColor(initialColorHex).blue) }

    val currentColor = Color(red, green, blue)

    fun updateHexFromRGB(r: Float, g: Float, b: Float) {
        val rInt = (r * 255).toInt().coerceIn(0, 255)
        val gInt = (g * 255).toInt().coerceIn(0, 255)
        val bInt = (b * 255).toInt().coerceIn(0, 255)
        hexInput = String.format("%02X%02X%02X", rInt, gInt, bInt)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Color Personalizado", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(currentColor, CircleShape)
                        .border(3.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "#$hexInput",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if ((red * 0.299 + green * 0.587 + blue * 0.114) > 0.5) Color.Black else Color.White
                    )
                }

                OutlinedTextField(
                    value = hexInput,
                    onValueChange = { input ->
                        val clean = input.filter { it.isLetterOrDigit() }.take(6).uppercase()
                        hexInput = clean
                        if (clean.length == 6) {
                            try {
                                val c = parseCategoryColor("#$clean")
                                red = c.red
                                green = c.green
                                blue = c.blue
                            } catch (_: Exception) {}
                        }
                    },
                    label = { Text("Código HEX") },
                    prefix = { Text("#") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Rojo: ${(red * 255).toInt()}", fontSize = 12.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                    Slider(
                        value = red,
                        onValueChange = {
                            red = it
                            updateHexFromRGB(red, green, blue)
                        },
                        colors = SliderDefaults.colors(thumbColor = Color.Red, activeTrackColor = Color.Red)
                    )

                    Text("Verde: ${(green * 255).toInt()}", fontSize = 12.sp, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                    Slider(
                        value = green,
                        onValueChange = {
                            green = it
                            updateHexFromRGB(red, green, blue)
                        },
                        colors = SliderDefaults.colors(thumbColor = Color(0xFF4CAF50), activeTrackColor = Color(0xFF4CAF50))
                    )

                    Text("Azul: ${(blue * 255).toInt()}", fontSize = 12.sp, color = Color(0xFF2196F3), fontWeight = FontWeight.Bold)
                    Slider(
                        value = blue,
                        onValueChange = {
                            blue = it
                            updateHexFromRGB(red, green, blue)
                        },
                        colors = SliderDefaults.colors(thumbColor = Color(0xFF2196F3), activeTrackColor = Color(0xFF2196F3))
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onColorSelected("#$hexInput")
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = currentColor)
            ) {
                Text("SELECCIONAR", color = if ((red * 0.299 + green * 0.587 + blue * 0.114) > 0.5) Color.Black else Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
