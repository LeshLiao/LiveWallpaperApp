package com.palettex.livewallpaperapp.wallpaper

import android.media.MediaPlayer
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import android.net.Uri
import android.content.Context
import android.content.SharedPreferences
import android.util.Log

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
        private var isVideoPlaying = false

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

                // Create MediaPlayer and use the holder's surface directly
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(applicationContext, Uri.parse(videoPath))
                    setSurface(holder.surface) // Use the system-provided surface
                    isLooping = false

                    setOnPreparedListener { mp ->
                        Log.d(TAG, "MediaPlayer prepared")
                        mp.start()
                        isVideoPlaying = true
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
                        isVideoPlaying = false
                        true
                    }

                    setOnCompletionListener {
                        Log.d(TAG, "MediaPlayer playback completed")
                    }

                    prepareAsync()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error setting up MediaPlayer", e)
                stopVideoPlayback()
            }
        }

        private fun stopVideoPlayback() {
            mediaPlayer?.apply {
                try {
                    if (isPlaying) {
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
            isVideoPlaying = false
        }
    }
}