package com.palettex.livewallpaperapp.ui.template

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.palettex.livewallpaperapp.R

@Composable
fun WallpaperTemplate(
    modifier: Modifier = Modifier,
    selectedImageIndex: Int = -1,
    onImageClick: (Int) -> Unit = {}
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left image (index 0)
            Image(
                painter = painterResource(id = R.drawable.default_image_one),
                contentDescription = "Left default image",
                modifier = Modifier
                    .size(150.dp)
                    .padding(8.dp)
                    .border(
                        width = if (selectedImageIndex == 0) 3.dp else 0.dp,
                        color = if (selectedImageIndex == 0) Color.Red else Color.Transparent
                    )
                    .clickable { onImageClick(0) },
                contentScale = ContentScale.Fit
            )

            // Right image (index 1)
            Image(
                painter = painterResource(id = R.drawable.default_image_two),
                contentDescription = "Right default image",
                modifier = Modifier
                    .size(150.dp)
                    .padding(8.dp)
                    .border(
                        width = if (selectedImageIndex == 1) 3.dp else 0.dp,
                        color = if (selectedImageIndex == 1) Color.Red else Color.Transparent
                    )
                    .clickable { onImageClick(1) },
                contentScale = ContentScale.Fit
            )
        }
    }
}