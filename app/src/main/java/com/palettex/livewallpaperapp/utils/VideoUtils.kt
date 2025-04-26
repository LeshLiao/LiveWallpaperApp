package com.palettex.livewallpaperapp.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.opengl.*
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.MediaStore.Files.FileColumns.RELATIVE_PATH
import android.view.Surface
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

object VideoUtils {
    private const val VERTEX_SHADER = """
        attribute vec4 aPosition;
        attribute vec2 aTexCoord;
        varying vec2 vTexCoord;
        uniform float uOffset;
        void main() {
            vec4 pos = aPosition;
            pos.x += uOffset;
            gl_Position = pos;
            vTexCoord = aTexCoord;
        }
    """

    private const val FRAGMENT_SHADER = """
        precision mediump float;
        varying vec2 vTexCoord;
        uniform sampler2D sTexture;
        void main() {
            gl_FragColor = texture2D(sTexture, vTexCoord);
        }
    """

    private val VERTEX_DATA = floatArrayOf(
        -1.0f, -1.0f, 0.0f,  // Bottom left
        1.0f, -1.0f, 0.0f,   // Bottom right
        -1.0f, 1.0f, 0.0f,   // Top left
        1.0f, 1.0f, 0.0f     // Top right
    )

    private val TEXTURE_DATA = floatArrayOf(
        0.0f, 1.0f,    // Bottom left
        1.0f, 1.0f,    // Bottom right
        0.0f, 0.0f,    // Top left
        1.0f, 0.0f     // Top right
    )

    fun createAnimatedVideo(
        context: Context,
        bitmap: Bitmap,
        onProgress: (Float) -> Unit,
        onComplete: (String) -> Unit
    ) {
        Thread {
            var encoder: MediaCodec? = null
            var muxer: MediaMuxer? = null
            var inputSurface: Surface? = null
            var eglDisplay: EGLDisplay? = null
            var eglContext: EGLContext? = null
            var eglSurface: EGLSurface? = null
            var outputFile: File? = null

            try {
                // Resize bitmap to 200px width while maintaining aspect ratio
                val targetWidth = 200
                val aspectRatio = bitmap.height.toFloat() / bitmap.width.toFloat()
                val targetHeight = (targetWidth * aspectRatio).toInt()
                val resizedBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)

                // Video configuration
                val width = 1080  // Standard HD width
                val height = 1920 // Standard HD height
                val bitRate = 8000000 // 8Mbps for better quality
                val frameRate = 30
                val iFrameInterval = 1
                val durationSeconds = 3
                val numFrames = frameRate * durationSeconds

                // Create output file using MediaStore for Android 10 and above
                val timestamp = System.currentTimeMillis()
                val fileName = "PaletteWall-$timestamp.mp4"

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val contentValues = ContentValues().apply {
                        put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
                        put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                        put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES)
                    }

                    val resolver = context.contentResolver
                    val uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)

                    if (uri != null) {
                        resolver.openOutputStream(uri)?.use { outputStream ->
                            outputFile = File(context.cacheDir, fileName)
                            FileOutputStream(outputFile).use { fileOutputStream ->
                                // We'll write to this file first, then copy to MediaStore
                            }
                        }
                    }
                } else {
                    val outputDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
                    outputFile = File(outputDir, fileName)
                }

                if (outputFile == null) {
                    throw IllegalStateException("Failed to create output file")
                }

                // Configure video encoder with optimized settings for live wallpaper
                val mediaFormat = MediaFormat.createVideoFormat(
                    MediaFormat.MIMETYPE_VIDEO_AVC,
                    width,
                    height
                ).apply {
                    setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
                    setInteger(MediaFormat.KEY_FRAME_RATE, frameRate)
                    setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
                    setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, iFrameInterval)
                    // Add these parameters for better compatibility
                    setInteger(MediaFormat.KEY_PROFILE, MediaCodecInfo.CodecProfileLevel.AVCProfileHigh)
                    setInteger(MediaFormat.KEY_LEVEL, MediaCodecInfo.CodecProfileLevel.AVCLevel41)
                }

                // Create and configure encoder
                encoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
                encoder.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
                inputSurface = encoder.createInputSurface()
                encoder.start()

                // Initialize OpenGL ES
                eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
                val version = IntArray(2)
                EGL14.eglInitialize(eglDisplay, version, 0, version, 1)

                val attribList = intArrayOf(
                    EGL14.EGL_RED_SIZE, 8,
                    EGL14.EGL_GREEN_SIZE, 8,
                    EGL14.EGL_BLUE_SIZE, 8,
                    EGL14.EGL_ALPHA_SIZE, 8,
                    EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                    EGL14.EGL_NONE
                )
                val configs = arrayOfNulls<EGLConfig>(1)
                val numConfigs = IntArray(1)
                EGL14.eglChooseConfig(eglDisplay, attribList, 0, configs, 0, 1, numConfigs, 0)

                val contextAttribs = intArrayOf(
                    EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                    EGL14.EGL_NONE
                )
                eglContext = EGL14.eglCreateContext(
                    eglDisplay, configs[0], EGL14.EGL_NO_CONTEXT,
                    contextAttribs, 0
                )

                val surfaceAttribs = intArrayOf(
                    EGL14.EGL_NONE
                )
                eglSurface = EGL14.eglCreateWindowSurface(
                    eglDisplay, configs[0], inputSurface,
                    surfaceAttribs, 0
                )

                EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)

                // Create and setup GL program
                val program = createProgram()
                val positionHandle = GLES20.glGetAttribLocation(program, "aPosition")
                val texCoordHandle = GLES20.glGetAttribLocation(program, "aTexCoord")
                val textureHandle = GLES20.glGetUniformLocation(program, "sTexture")
                val offsetHandle = GLES20.glGetUniformLocation(program, "uOffset")

                // Calculate scale factor to fit the image in the video frame
                val scaleX = targetWidth.toFloat() / width
                val scaleY = targetHeight.toFloat() / height
                val scale = maxOf(scaleX, scaleY)

                // Setup vertex buffer with scaled coordinates
                val scaledVertexData = floatArrayOf(
                    -scale, -scale, 0.0f,  // Bottom left
                    scale, -scale, 0.0f,   // Bottom right
                    -scale, scale, 0.0f,   // Top left
                    scale, scale, 0.0f     // Top right
                )

                val vertexBuffer = ByteBuffer.allocateDirect(scaledVertexData.size * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer()
                    .put(scaledVertexData)
                vertexBuffer.position(0)

                val texCoordBuffer = ByteBuffer.allocateDirect(TEXTURE_DATA.size * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer()
                    .put(TEXTURE_DATA)
                texCoordBuffer.position(0)

                // Create texture
                val textures = IntArray(1)
                GLES20.glGenTextures(1, textures, 0)
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0])
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
                GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, resizedBitmap, 0)

                // Setup muxer
                muxer = MediaMuxer(outputFile!!.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
                var trackIndex = -1
                var muxerStarted = false

                val bufferInfo = MediaCodec.BufferInfo()
                var generateIndex = 0

                // ADD THIS LINE - define the frame duration in microseconds
                val frameDurationUs = 1_000_000L / frameRate

                while (generateIndex < numFrames) {
                    // Calculate offset for animation (from -1 to 1)
                    val progress = generateIndex.toFloat() / numFrames
                    val offset = -1.0f + (progress * 2.0f)

                    // Draw frame
                    GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
                    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

                    GLES20.glUseProgram(program)

                    GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0])
                    GLES20.glUniform1i(textureHandle, 0)
                    GLES20.glUniform1f(offsetHandle, offset)

                    GLES20.glEnableVertexAttribArray(positionHandle)
                    GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)

                    GLES20.glEnableVertexAttribArray(texCoordHandle)
                    GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 0, texCoordBuffer)

                    GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

                    // REMOVE THIS LINE - delete the sleep as it's causing timing issues
                    // Thread.sleep((1000L / frameRate))

                    EGL14.eglSwapBuffers(eglDisplay, eglSurface)

                    // Get encoded data with timeout
                    var encoderOutputAvailable = true
                    var timeoutMs = 10000 // 10 second timeout
                    val startTime = System.currentTimeMillis()

                    while (encoderOutputAvailable && (System.currentTimeMillis() - startTime) < timeoutMs) {
                        val outputBufferId = encoder.dequeueOutputBuffer(bufferInfo, 10000) // 10 second timeout

                        if (outputBufferId == MediaCodec.INFO_TRY_AGAIN_LATER) {
                            encoderOutputAvailable = false
                        } else if (outputBufferId == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                            if (!muxerStarted) {
                                trackIndex = muxer.addTrack(encoder.outputFormat)
                                muxer.start()
                                muxerStarted = true
                            }
                        } else if (outputBufferId >= 0) {
                            val encodedData = encoder.getOutputBuffer(outputBufferId)
                            if (encodedData != null) {
                                if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                                    bufferInfo.size = 0
                                }

                                if (bufferInfo.size != 0) {
                                    if (muxerStarted) {
                                        // SET PRECISE TIMESTAMPS - this is critical for duration control
                                        bufferInfo.presentationTimeUs = generateIndex * frameDurationUs

                                        encodedData.position(bufferInfo.offset)
                                        encodedData.limit(bufferInfo.offset + bufferInfo.size)
                                        muxer.writeSampleData(trackIndex, encodedData, bufferInfo)
                                    }
                                }
                            }
                            encoder.releaseOutputBuffer(outputBufferId, false)

                            if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                                encoderOutputAvailable = false
                                break
                            }
                        }
                    }

                    // Check if we've exceeded the timeout
                    if ((System.currentTimeMillis() - startTime) >= timeoutMs) {
                            throw IllegalStateException("Encoder output timeout")
                        }

                    generateIndex++
                    onProgress(generateIndex.toFloat() / numFrames)
                } //here

                // Send end-of-stream to encoder
                encoder.signalEndOfInputStream()

                // Release resources
                GLES20.glDeleteTextures(1, textures, 0)
                GLES20.glDeleteProgram(program)
                EGL14.eglDestroySurface(eglDisplay, eglSurface)
                EGL14.eglDestroyContext(eglDisplay, eglContext)
                EGL14.eglTerminate(eglDisplay)
                encoder.stop()
                encoder.release()
                inputSurface.release()
                muxer.stop()
                muxer.release()

                // After encoding is complete, move the file to MediaStore if needed
                val finalUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val contentValues = ContentValues().apply {
                        put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
                        put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                        put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES)
                    }

                    val resolver = context.contentResolver
                    val uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)

                    if (uri != null) {
                        val tempFile = outputFile
                        if (tempFile != null) {
                            resolver.openOutputStream(uri)?.use { outputStream ->
                                tempFile.inputStream().use { inputStream ->
                                    inputStream.copyTo(outputStream)
                                }
                            }
                            tempFile.delete()
                        }
                        uri
                    } else null
                } else null

                // Notify completion with the final path
                val finalPath = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // For Android 10 and above, return the MediaStore URI path
                    finalUri?.let { "content://media/external/video/media/${it.lastPathSegment}" } ?: ""
                } else {
                    outputFile?.absolutePath ?: ""
                }
                onComplete(finalPath)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    private fun createProgram(): Int {
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER)

        return GLES20.glCreateProgram().also { program ->
            GLES20.glAttachShader(program, vertexShader)
            GLES20.glAttachShader(program, fragmentShader)
            GLES20.glLinkProgram(program)

            GLES20.glDeleteShader(vertexShader)
            GLES20.glDeleteShader(fragmentShader)
        }
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        return GLES20.glCreateShader(type).also { shader ->
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
        }
    }
}