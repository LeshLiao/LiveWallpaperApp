package com.palettex.livewallpaperapp.wallpaper

import android.media.MediaPlayer
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import android.net.Uri
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.graphics.SurfaceTexture
import android.view.Surface
import android.graphics.Matrix
import android.graphics.Paint
import java.io.File
import java.io.FileOutputStream

class LiveWallpaperService : WallpaperService() {
    companion object {
        private const val PREFS_NAME = "LiveWallpaperPrefs"
        private const val KEY_VIDEO_PATH = "video_path"
        private const val TAG = "GDT"
    }

    private lateinit var prefs: SharedPreferences

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "LiveWallpaperService onCreate")
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "LiveWallpaperService onDestroy")
    }

    override fun onCreateEngine(): Engine {
        Log.d(TAG, "LiveWallpaperService onCreateEngine")
        return WallpaperEngine()
    }

    inner class WallpaperEngine : Engine() {
        private var mediaPlayer: MediaPlayer? = null
        private var surfaceTexture: SurfaceTexture? = null
        private var surface: Surface? = null
        private var isVideoPlaying = false
        private var tempVideoFile: File? = null
        private val paint = Paint().apply {
            isAntiAlias = true
            isFilterBitmap = true
        }

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
            Log.d(TAG, "WallpaperEngine onCreate")
        }

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            Log.d(TAG, "WallpaperEngine onSurfaceCreated")
            startVideoPlayback(holder)
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            Log.d(TAG, "WallpaperEngine onSurfaceChanged: width=$width, height=$height")
            // Restart playback if dimensions change
            if (isVideoPlaying) {
                stopVideoPlayback()
                startVideoPlayback(holder)
            }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            Log.d(TAG, "WallpaperEngine onSurfaceDestroyed")
            stopVideoPlayback()
            // Clean up temp file
            tempVideoFile?.delete()
            tempVideoFile = null
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            Log.d(TAG, "WallpaperEngine onVisibilityChanged: $visible")
            if (visible) {
                if (!isVideoPlaying) {
                    startVideoPlayback(surfaceHolder)
                }
            } else {
                stopVideoPlayback()
            }
        }

        private fun startVideoPlayback(holder: SurfaceHolder) {
            if (isVideoPlaying) {
                Log.d(TAG, "Already playing, skipping")
                return
            }

            val videoPath = prefs.getString(KEY_VIDEO_PATH, null)
            Log.d(TAG, "Video path from prefs: $videoPath")

            if (videoPath == null) {
                Log.e(TAG, "No video path found in preferences")
                return
            }

            try {
                Log.d(TAG, "Creating MediaPlayer with URI: $videoPath")

                // Create a SurfaceTexture and Surface
                surfaceTexture = SurfaceTexture(0).apply {
                    setDefaultBufferSize(holder.surfaceFrame.width(), holder.surfaceFrame.height())
                }
                surface = Surface(surfaceTexture)

                // Copy video to temp file
                val uri = Uri.parse(videoPath)
                val contentResolver = applicationContext.contentResolver
                val inputStream = contentResolver.openInputStream(uri)
                if (inputStream == null) {
                    Log.e(TAG, "Failed to open input stream for URI: $uri")
                    return
                }

                tempVideoFile = File(applicationContext.cacheDir, "temp_video_${System.currentTimeMillis()}.mp4")
                FileOutputStream(tempVideoFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
                inputStream.close()

                Log.d(TAG, "Successfully copied video to temp file: ${tempVideoFile!!.absolutePath}")

                mediaPlayer = MediaPlayer().apply {
                    setDataSource(tempVideoFile!!.absolutePath)
                    setSurface(surface)
                    isLooping = true
                    setOnPreparedListener { mp ->
                        Log.d(TAG, "MediaPlayer prepared")
                        mp.start()
                        // Update the class variable outside of the MediaPlayer scope
                        this@WallpaperEngine.isVideoPlaying = true
                    }
                    setOnErrorListener { mp, what, extra ->
                        val errorMsg = when (what) {
                            MediaPlayer.MEDIA_ERROR_UNKNOWN -> "Unknown error"
                            MediaPlayer.MEDIA_ERROR_SERVER_DIED -> "Server died"
                            MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK -> "Not valid for progressive playback"
                            MediaPlayer.MEDIA_ERROR_IO -> "IO error"
                            MediaPlayer.MEDIA_ERROR_MALFORMED -> "Malformed media"
                            MediaPlayer.MEDIA_ERROR_TIMED_OUT -> "Timed out"
                            else -> "Unknown error code: $what"
                        }
                        Log.e(TAG, "MediaPlayer error: $errorMsg (what=$what, extra=$extra)")
                        // Update the class variable outside of the MediaPlayer scope
                        this@WallpaperEngine.isVideoPlaying = false
                        true
                    }
                    setOnCompletionListener {
                        Log.d(TAG, "MediaPlayer playback completed")
                    }
                    prepareAsync()
                }

                // Set up the SurfaceTexture listener to update the wallpaper surface
                surfaceTexture?.setOnFrameAvailableListener { texture ->
                    try {
                        texture.updateTexImage()
                        val transformMatrix = FloatArray(16)
                        texture.getTransformMatrix(transformMatrix)

                        // Convert FloatArray to Matrix
                        val matrix = Matrix().apply {
                            setValues(transformMatrix)
                        }

                        // Draw the texture to the wallpaper surface
                        holder.lockCanvas()?.let { canvas ->
                            try {
                                // Clear the canvas
                                canvas.drawColor(0xFF000000.toInt())

                                // Draw the texture
                                canvas.save()
                                canvas.concat(matrix)
                                canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), paint)
                                canvas.restore()
                            } finally {
                                holder.unlockCanvasAndPost(canvas)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error updating texture", e)
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error setting up MediaPlayer", e)
                stopVideoPlayback()
            }
        }

        private fun stopVideoPlayback() {
            mediaPlayer?.apply {
                try {
                    if (isPlaying()) {
                        Log.d(TAG, "Stopping video playback")
                        stop()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error stopping MediaPlayer", e)
                }
                reset()  // Reset before release
                release()
            }
            mediaPlayer = null
            surface?.release()
            surface = null
            surfaceTexture?.release()
            surfaceTexture = null
            isVideoPlaying = false
        }
    }
}
