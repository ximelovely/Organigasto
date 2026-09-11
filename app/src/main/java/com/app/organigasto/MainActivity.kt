package com.app.organigasto

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.app.organigasto.ui.navigation.OrganigastoNavGraph
import com.app.organigasto.ui.theme.OrganigastoTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OrganigastoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    OrganigastoNavGraph()
                }
            }
        }
    }

    fun showBiometricPrompt(onSuccess: () -> Unit) {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Desbloquear Organigasto")
            .setSubtitle("Usa tu huella para entrar")
            .setNegativeButtonText("Cancelar")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}
