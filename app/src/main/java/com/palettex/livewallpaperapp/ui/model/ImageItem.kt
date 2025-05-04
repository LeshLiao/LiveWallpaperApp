package com.palettex.livewallpaperapp.ui.model

import android.net.Uri
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class ImageItem(
    val id: Int,
    val defaultResourceId: Int,
    val customImageUri: Uri? = null,
    val x: Dp = 0.dp,
    val y: Dp = 0.dp
)