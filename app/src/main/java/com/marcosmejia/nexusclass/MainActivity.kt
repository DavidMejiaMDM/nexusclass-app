package com.marcosmejia.nexusclass

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.marcosmejia.nexusclass.ui.navigation.AppNavigation
import com.marcosmejia.nexusclass.ui.theme.NexusClassTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NexusClassTheme {
                AppNavigation()
            }
        }
    }
}