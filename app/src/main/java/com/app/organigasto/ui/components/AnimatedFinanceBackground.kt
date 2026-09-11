package com.app.organigasto.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import kotlin.random.Random

@Composable
fun AnimatedFinanceBackground(modifier: Modifier = Modifier) {
    val icons = listOf(
        Icons.Default.Payments,
        Icons.Default.CreditCard,
        Icons.Default.AccountBalance,
        Icons.Default.Savings,
        Icons.AutoMirrored.Filled.TrendingUp,
        Icons.AutoMirrored.Filled.ReceiptLong,
        Icons.Default.Wallet,
        Icons.Default.Paid, 
        Icons.Default.ShoppingBag,
        Icons.Default.Calculate,
        Icons.Default.Store,
        Icons.Default.PriceCheck,
        Icons.Default.PointOfSale,
        Icons.Default.Analytics,
        Icons.Default.CurrencyExchange,
        Icons.Default.Balance,
        Icons.AutoMirrored.Filled.ShowChart,
        Icons.Default.Insights,
        Icons.Default.CreditScore,
        Icons.Default.PriceChange,
        Icons.Default.Sell,
        Icons.Default.LocalMall
    )

    val painters = icons.map { rememberVectorPainter(it) }
    val primaryColor = MaterialTheme.colorScheme.primary
    val infiniteTransition = rememberInfiniteTransition(label = "finance_bg")

    // Generar datos aleatorios para 40 iconos flotantes
    val iconData = remember {
        List(40) {
            FloatingIconData(
                painterIndex = Random.nextInt(painters.size),
                startX = Random.nextFloat(),
                // Escala y variación para dar profundidad
                scale = 0.4f + Random.nextFloat() * 0.8f,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 0.5f,
                // El desplazamiento inicial permite que los iconos estén esparcidos
                startYOffset = Random.nextFloat() 
            )
        }
    }

    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(30000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "loop"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val iconSizeBase = 52.dp.toPx()
        
        iconData.forEach { data ->
            val painter = painters[data.painterIndex]
            val iconSize = iconSizeBase * data.scale
            
            // Lógica SEAMLESS: (progress + offset) % 1f garantiza que no haya saltos
            // porque 1.0 % 1.0 es igual a 0.0 % 1.0.
            val yProgress = (progress + data.startYOffset) % 1f
            
            // El rango de movimiento incluye márgenes para fade-in/out
            val verticalRange = size.height + (iconSize * 2)
            val yPos = verticalRange * (1f - yProgress) - iconSize
            
            // Ligera deriva lateral para naturalidad
            val sideOscillation = Math.sin(yProgress.toDouble() * Math.PI).toFloat() * 30f
            val xPos = (size.width * data.startX) + (size.width * 0.1f * yProgress) + sideOscillation
            
            // Efecto Fade quirúrgico para evitar parpadeos en los bordes
            val alphaEffect = if (yProgress < 0.15f) {
                yProgress / 0.15f // Aparece suavemente
            } else if (yProgress > 0.85f) {
                (1f - yProgress) / 0.15f // Desaparece suavemente
            } else {
                1f
            }

            translate(left = xPos, top = yPos) {
                with(painter) {
                    draw(
                        size = androidx.compose.ui.geometry.Size(iconSize, iconSize),
                        alpha = (alphaEffect * 0.08f).coerceIn(0f, 0.1f),
                        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(primaryColor)
                    )
                }
            }
        }
    }
}

private data class FloatingIconData(
    val painterIndex: Int,
    val startX: Float,
    val scale: Float,
    val rotationSpeed: Float,
    val startYOffset: Float
)
