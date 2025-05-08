package com.palettex.livewallpaperapp.ui

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
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
        val newXPercent = when (newId % 3) {
            0 -> -40f  // Left
            1 -> 0f    // Center
            else -> 40f // Right
        }
        images = images + ImageItem(
            id = newId,
            defaultResourceId = R.drawable.default_image_one,
            xPercent = newXPercent,
            yPercent = 0f,
            width = 150.dp,    // Default size for new images
            height = 150.dp
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

    fun updateImagePosition(index: Int, xPercent: Float, yPercent: Float) {
        if (index >= 0 && index < images.size) {
            val updatedImages = images.toMutableList()
            updatedImages[index] = images[index].copy(
                xPercent = xPercent.coerceIn(-100f, 100f),
                yPercent = yPercent.coerceIn(-100f, 100f)
            )
            images = updatedImages
        }
    }

    fun updateInitialPosition(index: Int, xPercentInit: Float, yPercentInit: Float) {
        if (index >= 0 && index < images.size) {
            val updatedImages = images.toMutableList()
            updatedImages[index] = images[index].copy(
                xPercentInit = xPercentInit.coerceIn(-100f, 100f),
                yPercentInit = yPercentInit.coerceIn(-100f, 100f)
            )
            images = updatedImages
        }
    }

    fun updateImageSize(index: Int, width: Dp, height: Dp) {
        if (index >= 0 && index < images.size) {
            val updatedImages = images.toMutableList()
            updatedImages[index] = images[index].copy(
                width = width.coerceAtLeast(50.dp),  // Minimum size of 50dp
                height = height.coerceAtLeast(50.dp)
            )
            images = updatedImages
        }
    }

    fun generateLiveWallpaper() {
        // This function will be called when the "Save" button is clicked
        // It should:
        // 1. Save the current state of images
        // 2. Start the LiveWallpaperService
        // 3. Pass the images to the service

        // Note: Implementation will require integration with Android's
        // WallpaperManager and proper permission handling
    }
}