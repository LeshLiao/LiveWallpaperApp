package com.palettex.livewallpaperapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.palettex.livewallpaperapp.ui.MainViewModel
import com.palettex.livewallpaperapp.ui.Screen
import com.palettex.livewallpaperapp.ui.gallery.GalleryScreen
import com.palettex.livewallpaperapp.ui.template.TemplatePage
import com.palettex.livewallpaperapp.ui.theme.LiveWallpaperAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LiveWallpaperAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

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