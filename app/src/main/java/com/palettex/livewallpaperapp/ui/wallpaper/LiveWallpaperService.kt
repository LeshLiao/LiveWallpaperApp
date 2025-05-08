package com.palettex.livewallpaperapp.ui.wallpaper

import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.palettex.livewallpaperapp.ui.model.ImageItem
import kotlinx.coroutines.*

class LiveWallpaperService : WallpaperService() {
    override fun onCreateEngine(): Engine {
        return WallpaperEngine()
    }

    inner class WallpaperEngine : Engine() {
        private val wallpaperAnimator = WallpaperAnimator()
        private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())
        private var currentImages: List<ImageItem> = emptyList()

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            if (visible) {
                startWallpaperAnimation()
            } else {
                wallpaperAnimator.stopAnimation()
            }
        }

        private fun startWallpaperAnimation() {
            wallpaperAnimator.startAnimation(currentImages) { updatedImages, progress ->
                val holder = surfaceHolder
                val canvas = holder.lockCanvas()
                if (canvas != null) {
                    try {
                        // Clear the canvas
                        canvas.drawColor(Color.Black.toArgb())

                        // Draw the images at their current positions
                        // Note: You'll need to implement actual image drawing here
                        // This is just a placeholder

                    } finally {
                        holder.unlockCanvasAndPost(canvas)
                    }
                }
            }
        }

        override fun onDestroy() {
            super.onDestroy()
            wallpaperAnimator.stopAnimation()
            coroutineScope.cancel()
        }

        fun updateImages(images: List<ImageItem>) {
            currentImages = images
            if (isVisible) {
                startWallpaperAnimation()
            }
        }
    }
}