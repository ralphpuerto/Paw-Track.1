package com.example.pawtrack2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.pawtrack2.ui.theme.PawTrackTheme
import com.example.pawtrack2.view.PawTrackApp

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PawTrackTheme {
                PawTrackApp()
            }
        }
    }
}