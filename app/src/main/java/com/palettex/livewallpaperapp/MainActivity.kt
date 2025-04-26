package com.palettex.livewallpaperapp

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.palettex.livewallpaperapp.ui.theme.LiveWallpaperAppTheme
import com.palettex.livewallpaperapp.utils.ImageUtils
import com.palettex.livewallpaperapp.utils.VideoUtils
import com.palettex.livewallpaperapp.wallpaper.LiveWallpaperService
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : ComponentActivity() {
    companion object {
        private const val TAG = "GDT"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Initializing MainActivity")
        setContent {
            LiveWallpaperAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    var videoPath by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        Log.d("GDT", "Image selected: $uri")
        selectedImageUri = uri
        videoPath = null // Reset video path when new image is selected
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Widgify",
                    style = MaterialTheme.typography.headlineLarge
                )

                // Image Preview
                if (selectedImageUri != null) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(selectedImageUri),
                            contentDescription = "Selected image",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Fit
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No image selected",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Buttons at the bottom
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { imagePicker.launch("image/*") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isProcessing
                    ) {
                        Text(if (selectedImageUri == null) "Select Image" else "Change Image")
                    }

                    if (selectedImageUri != null && videoPath == null) {
                        Button(
                            onClick = {
                                scope.launch {
                                    Log.d("GDT", "Starting animation creation process")
                                    isProcessing = true
                                    progress = 0f

                                    // Get bitmap from URI
                                    val bitmap = getBitmapFromUri(context, selectedImageUri!!)
                                    if (bitmap != null) {
                                        Log.d("GDT", "Successfully loaded bitmap from URI")
                                        // Create Movies directory if it doesn't exist
                                        val moviesDir = File(
                                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
                                            "Widgify"
                                        ).apply { mkdirs() }

                                        VideoUtils.createAnimatedVideo(
                                            context,
                                            bitmap,
                                            onProgress = {
                                                progress = it
                                                Log.d("GDT", "Animation progress: ${(progress * 100).toInt()}%")
                                            },
                                            onComplete = { path ->
                                                Log.d("GDT", "Animation creation completed. Path: $path")
                                                videoPath = path
                                                isProcessing = false
                                                scope.launch {
                                                    snackbarHostState.showSnackbar(
                                                        message = "Animation created successfully!",
                                                        actionLabel = "View Location",
                                                        duration = SnackbarDuration.Long
                                                    ).let { result ->
                                                        if (result == SnackbarResult.ActionPerformed) {
                                                            Log.d("GDT", "Opening file location: $path")
                                                            // Open file location
                                                            val intent = Intent(Intent.ACTION_VIEW)
                                                            intent.setDataAndType(Uri.parse(path), "video/*")
                                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                            try {
                                                                context.startActivity(intent)
                                                            } catch (e: Exception) {
                                                                Log.e("GDT", "Error opening file location", e)
                                                                e.printStackTrace()
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        )
                                    } else {
                                        Log.e("GDT", "Failed to load bitmap from URI")
                                        isProcessing = false
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = "Failed to process image",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isProcessing
                        ) {
                            Text("Create Animation")
                        }
                    }

                    // Replace this section in the MainScreen() composable where you set the wallpaper

                    if (videoPath != null) {
                        Button(
                            onClick = {
                                Log.d("GDT", "Setting live wallpaper with video path: $videoPath")

                                // Save video path to SharedPreferences
                                // Important: We're now storing the full URI string
                                context.getSharedPreferences("LiveWallpaperPrefs", Context.MODE_PRIVATE)
                                    .edit()
                                    .putString("video_path", videoPath)
                                    .apply()

                                // First ensure we have proper permissions to read the video file
                                // This is important when using ContentResolver
                                try {
                                    // Take persistent permissions for the URI if possible
                                    // This helps maintain access after app restarts
                                    val contentResolver = context.contentResolver
                                    val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                    contentResolver.takePersistableUriPermission(Uri.parse(videoPath), takeFlags)
                                } catch (e: Exception) {
                                    Log.e("GDT", "Failed to take persistable URI permission", e)
                                    // Continue anyway as it might work without this
                                }

                                // Launch the wallpaper picker
                                val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
                                intent.putExtra(
                                    WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                                    ComponentName(context, LiveWallpaperService::class.java)
                                )
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isProcessing
                        ) {
                            Text("Set as Live Wallpaper")
                        }
                    }
                }
            }

            // Overlay progress indicator
            if (isProcessing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = progress,
                            modifier = Modifier.size(64.dp),
                            strokeWidth = 6.dp
                        )
                        Text(
                            text = "Creating animation... ${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

private suspend fun getBitmapFromUri(context: Context, uri: Uri): Bitmap? {
    return try {
        Log.d("GDT", "Loading bitmap from URI: $uri")
        val request = ImageRequest.Builder(context)
            .data(uri)
            .allowHardware(false)
            .build()

        val imageLoader = coil.ImageLoader(context)
        val result = imageLoader.execute(request)

        if (result is SuccessResult) {
            Log.d("GDT", "Successfully loaded bitmap")
            (result.drawable as BitmapDrawable).bitmap
        } else {
            Log.e("GDT", "Failed to load bitmap: result is not SuccessResult")
            null
        }
    } catch (e: Exception) {
        Log.e("GDT", "Error loading bitmap from URI", e)
        e.printStackTrace()
        null
    }
}