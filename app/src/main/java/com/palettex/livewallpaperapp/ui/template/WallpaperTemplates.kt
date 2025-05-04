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
                x = (-100).dp,
                y = 0.dp
            ),
            ImageItem(
                id = 1,
                defaultResourceId = R.drawable.default_image_two,
                x = 100.dp,
                y = 0.dp
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
                x = (-100).dp,
                y = (-50).dp
            ),
            ImageItem(
                id = 1,
                defaultResourceId = R.drawable.default_image_two,
                x = 100.dp,
                y = (-50).dp
            ),
            ImageItem(
                id = 2,
                defaultResourceId = R.drawable.default_image_three,
                x = 0.dp,
                y = 50.dp
            )
        )
    }
)

val availableTemplates = listOf(TwoImageTemplate, ThreeImageTemplate)