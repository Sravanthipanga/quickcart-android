package com.sravanthi.quickcart

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import com.sravanthi.quickcart.core.designsystem.theme.QuickCartTheme
import com.sravanthi.quickcart.navigation.QuickCartNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            QuickCartTheme {
                // Exposes test tags as resource ids so the UiAutomator benchmarks can find views.
                @OptIn(ExperimentalComposeUiApi::class)
                QuickCartNavHost(Modifier.semantics { testTagsAsResourceId = true })
            }
        }
    }
}
