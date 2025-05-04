package com.palettex.livewallpaperapp.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.palettex.livewallpaperapp.ui.components.EditPhotoBottomMenu
import com.palettex.livewallpaperapp.ui.template.WallpaperTemplate

@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel()
) {
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.updateImage(uri)
    }

    Scaffold(
        bottomBar = {
            EditPhotoBottomMenu(
                isVisible = viewModel.selectedImageIndex != -1,
                onDoneClick = { viewModel.clearSelection() },
                onBatchReplaceClick = { /* TODO */ },
                onSingleReplaceClick = {
                    imagePicker.launch("image/*")
                },
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
                selectedImageIndex = viewModel.selectedImageIndex,
                images = viewModel.images,
                onImageClick = { index ->
                    viewModel.selectImage(index)
                }
            )
        }
    }
}