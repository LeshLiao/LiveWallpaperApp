package com.palettex.livewallpaperapp.ui.template

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.palettex.livewallpaperapp.ui.components.NewEditPhotoBottomMenu
import com.palettex.livewallpaperapp.ui.model.ImageItem
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplatePage(
    selectedTemplate: TemplateStyle,
    images: List<ImageItem>,
    selectedImageIndex: Int,
    onImageSelected: (Int) -> Unit,
    onImageUpdated: (Uri?) -> Unit,
    onSelectionCleared: () -> Unit,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        onImageUpdated(uri)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(selectedTemplate.name) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            NewEditPhotoBottomMenu(
                isVisible = selectedImageIndex != -1,
                onDoneClick = onSelectionCleared,
                onBatchReplaceClick = { /* TODO: Implement batch replace */ },
                onSingleReplaceClick = { imagePicker.launch("image/*") },
                onRotateClick = { /* TODO: Implement rotation */ },
                onHorizontalClick = { /* TODO: Implement horizontal flip */ },
                onVerticalClick = { /* TODO: Implement vertical flip */ }
            )
        }
    ) { paddingValues ->
        // Template Preview Area
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(9f/16f) // Phone screen aspect ratio
                    .align(Alignment.Center)
            ) {
                when (selectedTemplate.id) {
                    "two_image" -> TwoImageLayout(
                        images = images,
                        selectedImageIndex = selectedImageIndex,
                        onImageSelected = onImageSelected
                    )
                    "three_image" -> ThreeImageLayout(
                        images = images,
                        selectedImageIndex = selectedImageIndex,
                        onImageSelected = onImageSelected
                    )
                    else -> {
                        Text("Unsupported template type")
                    }
                }
            }
        }
    }
}

@Composable
fun TwoImageLayout(
    images: List<ImageItem>,
    selectedImageIndex: Int,
    onImageSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val parentSize = remember { mutableStateOf(IntSize(0, 0)) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { parentSize.value = it }
    ) {
        images.take(2).forEachIndexed { index, imageItem ->
            val imageWidth = with(LocalDensity.current) { imageItem.width.toPx() }
            val imageHeight = with(LocalDensity.current) { imageItem.height.toPx() }
            TemplateImage(
                imageItem = imageItem,
                isSelected = selectedImageIndex == index,
                onClick = { onImageSelected(index) },
                modifier = Modifier
                    .size(width = imageItem.width, height = imageItem.height)
                    .offset {
                        IntOffset(
                            x = ((imageItem.xPercent / 100f) * (parentSize.value.width - imageWidth)).roundToInt(),
                            y = ((imageItem.yPercent / 100f) * (parentSize.value.height - imageHeight)).roundToInt()
                        )
                    }
            )
        }
    }
}

@Composable
fun ThreeImageLayout(
    images: List<ImageItem>,
    selectedImageIndex: Int,
    onImageSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val parentSize = remember { mutableStateOf(IntSize(0, 0)) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { parentSize.value = it }
    ) {
        images.take(3).forEachIndexed { index, imageItem ->
            val imageWidth = with(LocalDensity.current) { imageItem.width.toPx() }
            val imageHeight = with(LocalDensity.current) { imageItem.height.toPx() }
            TemplateImage(
                imageItem = imageItem,
                isSelected = selectedImageIndex == index,
                onClick = { onImageSelected(index) },
                modifier = Modifier
                    .size(width = imageItem.width, height = imageItem.height)
                    .offset {
                        IntOffset(
                            x = ((imageItem.xPercent / 100f) * (parentSize.value.width - imageWidth)).roundToInt(),
                            y = ((imageItem.yPercent / 100f) * (parentSize.value.height - imageHeight)).roundToInt()
                        )
                    }
            )
        }
    }
}

@Composable
fun TemplateImage(
    imageItem: ImageItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.then(
            if (isSelected) {
                Modifier.border(2.dp, Color.Red)
            } else {
                Modifier
            }
        )
    ) {
        if (imageItem.customImageUri != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageItem.customImageUri)
                    .crossfade(true)
                    .build(),
                contentDescription = "Template Image",
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(onClick = onClick),
                contentScale = ContentScale.Crop
            )
        } else {
            Image(
                painter = painterResource(id = imageItem.defaultResourceId),
                contentDescription = "Template Image",
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(onClick = onClick),
                contentScale = ContentScale.Crop
            )
        }
    }
}