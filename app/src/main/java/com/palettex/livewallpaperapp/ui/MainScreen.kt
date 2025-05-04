package com.palettex.livewallpaperapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.palettex.livewallpaperapp.ui.components.EditPhotoBottomMenu
import com.palettex.livewallpaperapp.ui.template.WallpaperTemplate

@Composable
fun MainScreen() {
    var selectedImageIndex by remember { mutableStateOf(-1) }

    Scaffold(
        bottomBar = {
            EditPhotoBottomMenu(
                isVisible = selectedImageIndex != -1,
                onDoneClick = { selectedImageIndex = -1 },
                onBatchReplaceClick = { /* TODO */ },
                onSingleReplaceClick = { /* TODO */ },
                onRotateClick = { /* TODO */ },
                onHorizontalClick = { /* TODO */ },
                onVerticalClick = { /* TODO */ }
            )
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            WallpaperTemplate(
                selectedImageIndex = selectedImageIndex,
                onImageClick = { index ->
                    selectedImageIndex = if (selectedImageIndex == index) -1 else index
                }
            )
        }
    }
}