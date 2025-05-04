package com.palettex.livewallpaperapp.ui.template

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.palettex.livewallpaperapp.R
import com.palettex.livewallpaperapp.ui.model.ImageItem

@Composable
fun WallpaperTemplate(
    modifier: Modifier = Modifier,
    selectedImageIndex: Int = -1,
    images: List<ImageItem> = emptyList(),
    onImageClick: (Int) -> Unit = {}
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            itemsIndexed(images) { index, image ->
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .padding(8.dp)
                        .border(
                            width = if (selectedImageIndex == index) 3.dp else 0.dp,
                            color = if (selectedImageIndex == index) Color.Red else Color.Transparent
                        )
                        .clickable { onImageClick(index) }
                ) {
                    if (image.customImageUri != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(image.customImageUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Image ${image.id}",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Image(
                            painter = painterResource(id = image.defaultResourceId),
                            contentDescription = "Default image ${image.id}",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
        }
    }
}