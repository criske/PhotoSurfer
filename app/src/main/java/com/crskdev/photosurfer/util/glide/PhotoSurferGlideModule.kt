package com.crskdev.photosurfer.util.glide

import android.content.Context
import android.graphics.Bitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule

/**
 * Created by Cristian Pela on 05.10.2018.
 */
@GlideModule
class PhotoSurferGlideModule : AppGlideModule() {
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.register(Bitmap::class.java, BitmapPalette::class.java,
                BitmapPaletteTranscoder(glide.bitmapPool))
    }
}