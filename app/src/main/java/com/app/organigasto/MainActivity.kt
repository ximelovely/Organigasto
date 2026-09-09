package com.app.organigasto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.app.organigasto.ui.navigation.OrganigastoNavGraph
import com.app.organigasto.ui.theme.OrganigastoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OrganigastoTheme {
                OrganigastoNavGraph()
            }
        }
    }
}
