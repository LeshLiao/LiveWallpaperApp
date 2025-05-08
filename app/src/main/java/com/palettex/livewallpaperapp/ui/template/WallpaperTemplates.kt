package com.palettex.livewallpaperapp.ui.template

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.palettex.livewallpaperapp.R
import com.palettex.livewallpaperapp.ui.model.ImageItem

data class TemplateStyle(
    val id: String,
    val name: String,
    val imageCount: Int,
    val createDefaultImages: () -> List<ImageItem>
)

val TwoImageTemplate = TemplateStyle(
    id = "two_image",
    name = "Two Images",
    imageCount = 2,
    createDefaultImages = {
        listOf(
            ImageItem(
                id = 0,
                defaultResourceId = R.drawable.default_image_one,
                xPercent = 20f,       // final position
                yPercent = 50f,
                xPercentInit = -80f,  // start from left edge
                yPercentInit = 50f,   // same vertical position
                width = 80.dp,
                height = 80.dp
            ),
            ImageItem(
                id = 1,
                defaultResourceId = R.drawable.default_image_two,
                xPercent = 80f,      // final position
                yPercent = 50f,
                xPercentInit = 180f, // start from right edge
                yPercentInit = 50f,  // same vertical position
                width = 80.dp,
                height = 80.dp
            )
        )
    }
)

val ThreeImageTemplate = TemplateStyle(
    id = "three_image",
    name = "Three Images",
    imageCount = 3,
    createDefaultImages = {
        listOf(
            ImageItem(
                id = 0,
                defaultResourceId = R.drawable.default_image_one,
                xPercent = -40f,      // final position
                yPercent = -20f,
                xPercentInit = -120f, // start from far left
                yPercentInit = -20f,
                width = 150.dp,
                height = 150.dp
            ),
            ImageItem(
                id = 1,
                defaultResourceId = R.drawable.default_image_two,
                xPercent = 40f,       // final position
                yPercent = -20f,
                xPercentInit = 120f,  // start from far right
                yPercentInit = -20f,
                width = 150.dp,
                height = 150.dp
            ),
            ImageItem(
                id = 2,
                defaultResourceId = R.drawable.default_image_three,
                xPercent = 0f,        // final position
                yPercent = 20f,
                xPercentInit = 0f,    // start from bottom
                yPercentInit = 120f,
                width = 180.dp,
                height = 180.dp
            )
        )
    }
)

val availableTemplates = listOf(TwoImageTemplate, ThreeImageTemplate)