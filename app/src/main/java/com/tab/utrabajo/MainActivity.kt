package com.tab.utrabajo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.tab.utrabajo.data.UTrabajoRepository
import com.tab.utrabajo.presentation.navigation.NavGraph
import com.tab.utrabajo.ui.theme.UTrabajoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        UTrabajoRepository.initialize(applicationContext)
        setContent {
            UTrabajoTheme {
                NavGraph()
            }
        }
    }
}
