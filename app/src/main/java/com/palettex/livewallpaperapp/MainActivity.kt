package com.palettex.livewallpaperapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.palettex.livewallpaperapp.ui.MainScreen
import com.palettex.livewallpaperapp.ui.theme.LiveWallpaperAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LiveWallpaperAppTheme {
                MainScreen()
            }
        }
    }
}