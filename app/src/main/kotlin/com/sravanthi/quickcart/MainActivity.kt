package com.sravanthi.quickcart

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.sravanthi.quickcart.core.designsystem.theme.QuickCartTheme
import com.sravanthi.quickcart.navigation.QuickCartNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            QuickCartTheme { QuickCartNavHost() }
        }
    }
}
