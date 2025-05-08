package com.palettex.livewallpaperapp.ui.wallpaper

import android.animation.ValueAnimator
import android.view.animation.LinearInterpolator
import com.palettex.livewallpaperapp.ui.model.ImageItem

class WallpaperAnimator(
    private val duration: Long = 3000, // 3 seconds for full animation
    private val interpolator: LinearInterpolator = LinearInterpolator()
) {
    private var animator: ValueAnimator? = null

    fun startAnimation(
        images: List<ImageItem>,
        onUpdate: (List<ImageItem>, Float) -> Unit
    ) {
        stopAnimation()

        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            this.duration = this@WallpaperAnimator.duration
            this.interpolator = this@WallpaperAnimator.interpolator

            addUpdateListener { animation ->
                val progress = animation.animatedValue as Float
                val updatedImages = images.map { image ->
                    image.copy(
                        xPercent = lerp(image.xPercentInit, image.xPercent, progress),
                        yPercent = lerp(image.yPercentInit, image.yPercent, progress)
                    )
                }
                onUpdate(updatedImages, progress)
            }

            start()
        }
    }

    fun stopAnimation() {
        animator?.cancel()
        animator = null
    }

    private fun lerp(start: Float, end: Float, fraction: Float): Float {
        return start + (end - start) * fraction
    }
}