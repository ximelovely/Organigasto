package com.app.organigasto.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp

@Composable
fun AnimatedFinanceBackground(modifier: Modifier = Modifier) {
    val icons = listOf(
        Icons.Default.Payments,
        Icons.Default.CreditCard,
        Icons.Default.AccountBalance,
        Icons.Default.Savings,
        Icons.Default.TrendingUp,
        Icons.Default.ReceiptLong
    )

    val painters = icons.map { rememberVectorPainter(it) }
    val primaryColor = MaterialTheme.colorScheme.primary
    val infiniteTransition = rememberInfiniteTransition(label = "background")

    // Animación suave de flotación y rotación
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val iconSize = 40.dp.toPx()
        val spacing = 120.dp.toPx()
        val columns = (size.width / spacing).toInt() + 1
        val rows = (size.height / spacing).toInt() + 1

        for (i in 0 until columns) {
            for (j in 0 until rows) {
                val index = (i * rows + j) % painters.size
                val painter = painters[index]
                
                // Variación de movimiento por posición
                val offsetX = Math.sin((phase + i * 0.5).toDouble()).toFloat() * 20f
                val offsetY = Math.cos((phase + j * 0.5).toDouble()).toFloat() * 20f
                
                val x = i * spacing + (if (j % 2 == 0) spacing / 2 else 0f)
                val y = j * spacing

                translate(left = x + offsetX, top = y + offsetY) {
                    with(painter) {
                        draw(
                            size = androidx.compose.ui.geometry.Size(iconSize, iconSize),
                            alpha = 0.05f, // Muy sutil para no distraer
                            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(primaryColor)
                        )
                    }
                }
            }
        }
    }
}
