package com.palettex.livewallpaperapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.palettex.livewallpaperapp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPhotoBottomMenu(
    isVisible: Boolean,
    onDoneClick: () -> Unit = {},
    onBatchReplaceClick: () -> Unit = {},
    onSingleReplaceClick: () -> Unit = {},
    onRotateClick: () -> Unit = {},
    onHorizontalClick: () -> Unit = {},
    onVerticalClick: () -> Unit = {}
) {
    if (isVisible) {
        BottomAppBar(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title row with Done button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Edit Photos",
                        style = MaterialTheme.typography.titleMedium
                    )
                    TextButton(onClick = onDoneClick) {
                        Text("Done")
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Batch Replace
                    EditButton(
                        text = "Batch\nReplace",
                        icon = R.drawable.ic_batch_replace,
                        onClick = onBatchReplaceClick
                    )

                    // Single Replace
                    EditButton(
                        text = "Single\nReplace",
                        icon = R.drawable.ic_single_replace,
                        onClick = onSingleReplaceClick
                    )

                    // Rotate
                    EditButton(
                        text = "Rotate",
                        icon = R.drawable.ic_rotate,
                        onClick = onRotateClick
                    )

                    // Horizontal
                    EditButton(
                        text = "Horizontal",
                        icon = R.drawable.ic_horizontal,
                        onClick = onHorizontalClick
                    )

                    // Vertical
                    EditButton(
                        text = "Vertical",
                        icon = R.drawable.ic_vertical,
                        onClick = onVerticalClick
                    )
                }
            }
        }
    }
}

@Composable
private fun EditButton(
    text: String,
    icon: Int,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(64.dp)
    ) {
        FilledTonalIconButton(
            onClick = onClick,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = text,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}