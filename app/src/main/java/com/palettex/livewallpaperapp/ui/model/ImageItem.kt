package com.palettex.livewallpaperapp.ui.model

import android.net.Uri
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class ImageItem(
    val id: Int,
    val defaultResourceId: Int,
    val customImageUri: Uri? = null,
    val xPercent: Float = 0f,      // target x position as percentage of container width (-100f to 100f)
    val yPercent: Float = 0f,      // target y position as percentage of container height (-100f to 100f)
    val xPercentInit: Float = 0f,  // initial x position for animation
    val yPercentInit: Float = 0f,  // initial y position for animation
    val width: Dp = 150.dp,        // width of the image
    val height: Dp = 150.dp        // height of the image
)