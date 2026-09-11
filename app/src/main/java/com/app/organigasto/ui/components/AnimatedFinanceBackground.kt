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
import kotlin.random.Random

@Composable
fun AnimatedFinanceBackground(modifier: Modifier = Modifier) {
    val icons = listOf(
        Icons.Default.Payments,
        Icons.Default.CreditCard,
        Icons.Default.AccountBalance,
        Icons.Default.Savings,
        Icons.Default.TrendingUp,
        Icons.Default.ReceiptLong,
        Icons.Default.Wallet,
        Icons.Default.MonetizationOn,
        Icons.Default.AttachMoney,
        Icons.Default.ShoppingBag,
        Icons.Default.Calculate,
        Icons.Default.Store
    )

    val painters = icons.map { rememberVectorPainter(it) }
    val primaryColor = MaterialTheme.colorScheme.primary
    val infiniteTransition = rememberInfiniteTransition(label = "finance_bg")

    // Generar datos aleatorios para 15 iconos flotantes
    val iconData = remember {
        List(15) {
            FloatingIconData(
                painterIndex = Random.nextInt(painters.size),
                startX = Random.nextFloat(),
                speed = 0.0001f + Random.nextFloat() * 0.0002f,
                scale = 0.5f + Random.nextFloat() * 0.8f,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 0.5f
            )
        }
    }

    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "loop"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val iconSizeBase = 40.dp.toPx()
        
        iconData.forEach { data ->
            val painter = painters[data.painterIndex]
            val iconSize = iconSizeBase * data.scale
            
            // Calcular posición vertical: empieza abajo y sube
            // El residuo hace que vuelva a empezar abajo al terminar
            val yProgress = (progress * (1f / data.speed)) % 1f
            val yPos = size.height * (1f - yProgress)
            val xPos = size.width * data.startX
            
            translate(left = xPos, top = yPos) {
                with(painter) {
                    draw(
                        size = androidx.compose.ui.geometry.Size(iconSize, iconSize),
                        alpha = 0.08f,
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
    val speed: Float,
    val scale: Float,
    val rotationSpeed: Float
)
