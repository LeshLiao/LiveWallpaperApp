package com.palettex.livewallpaperapp.ui

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.palettex.livewallpaperapp.R
import com.palettex.livewallpaperapp.ui.model.ImageItem
import com.palettex.livewallpaperapp.ui.template.TemplateStyle
import com.palettex.livewallpaperapp.ui.template.TwoImageTemplate

class MainViewModel : ViewModel() {
    var currentScreen by mutableStateOf<Screen>(Screen.Gallery)
        private set

    var selectedImageIndex by mutableStateOf(-1)
        private set

    var selectedTemplate by mutableStateOf<TemplateStyle>(TwoImageTemplate)
        private set

    var images by mutableStateOf(TwoImageTemplate.createDefaultImages())
        private set

    fun navigateToScreen(screen: Screen) {
        currentScreen = screen
    }

    fun selectTemplate(template: TemplateStyle) {
        selectedTemplate = template
        images = template.createDefaultImages()
        selectedImageIndex = -1
        navigateToScreen(Screen.Editor)
    }

    fun selectImage(index: Int) {
        selectedImageIndex = if (selectedImageIndex == index) -1 else index
    }

    fun clearSelection() {
        selectedImageIndex = -1
    }

    fun updateImage(uri: Uri?) {
        if (selectedImageIndex >= 0 && selectedImageIndex < images.size) {
            val updatedImages = images.toMutableList()
            updatedImages[selectedImageIndex] = images[selectedImageIndex].copy(customImageUri = uri)
            images = updatedImages
        }
    }

    fun addImage() {
        val newId = images.size
        val newX = when (newId % 3) {
            0 -> (-100).dp  // Left
            1 -> 0.dp      // Center
            else -> 100.dp // Right
        }
        images = images + ImageItem(
            id = newId,
            defaultResourceId = R.drawable.default_image_one,
            x = newX,
            y = 0.dp
        )
    }

    fun removeImage(index: Int) {
        if (index >= 0 && index < images.size) {
            images = images.filterIndexed { i, _ -> i != index }
            if (selectedImageIndex == index) {
                selectedImageIndex = -1
            }
        }
    }

    fun updateImagePosition(index: Int, x: Float, y: Float) {
        if (index >= 0 && index < images.size) {
            val updatedImages = images.toMutableList()
            updatedImages[index] = images[index].copy(
                x = x.dp,
                y = y.dp
            )
            images = updatedImages
        }
    }
}