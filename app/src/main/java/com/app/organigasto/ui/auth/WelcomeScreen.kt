package com.app.organigasto.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.organigasto.ui.theme.PurpuraPrimario
import com.app.organigasto.ui.theme.PurpuraSecundario

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder

@Composable
fun WelcomeScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(PurpuraPrimario, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.size(64.dp)) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(Color.White, CircleShape)
                            .align(Alignment.BottomEnd),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$",
                            color = PurpuraPrimario,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "OrganiGasto",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = PurpuraSecundario
            )

            Text(
                text = "Tu dinero, bajo control",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(60.dp))

            Button(
                onClick = onLoginClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PurpuraPrimario)
            ) {
                Text(text = "Iniciar Sesión", fontSize = 16.sp, color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onRegisterClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PurpuraSecundario)
            ) {
                Text(text = "Registrarse", fontSize = 16.sp)
            }
        }
    }
}
