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
import com.palettex.livewallpaperapp.ui.gallery.GalleryScreen
import com.palettex.livewallpaperapp.ui.template.TemplatePage

@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel()
) {
    when (viewModel.currentScreen) {
        Screen.Gallery -> GalleryScreen(
            onTemplateSelected = viewModel::selectTemplate
        )
        Screen.Editor -> TemplatePage(
            selectedTemplate = viewModel.selectedTemplate,
            images = viewModel.images,
            selectedImageIndex = viewModel.selectedImageIndex,
            onImageSelected = viewModel::selectImage,
            onImageUpdated = viewModel::updateImage,
            onSelectionCleared = viewModel::clearSelection,
            onBackPressed = { viewModel.navigateToScreen(Screen.Gallery) }
        )
    }
}