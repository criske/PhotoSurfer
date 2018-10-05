package com.crskdev.photosurfer.util.glide

import android.graphics.Rect
import android.widget.ImageView
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.Option
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.ImageViewTarget
import com.bumptech.glide.request.target.Target

/**
 * Created by Cristian Pela on 05.10.2018.
 */


fun RequestManager.asBitmapPalette() = this.`as`(BitmapPalette::class.java)

fun RequestBuilder<BitmapPalette>.setSamplingRegions(map: Map<Int, Rect>): RequestBuilder<BitmapPalette> {
    return apply (
        RequestOptions().set(Option.memory(BitmapPaletteTranscoder.SAMPLING_REGIONS_KEY), map)
    )
}

inline fun <R : BitmapCarrier> RequestBuilder<R>.into(imageView: ImageView, crossinline onReady: (R) -> Unit): ImageViewTarget<R> {
    return this.into(object : ImageViewTarget<R>(imageView) {
        override fun setResource(resource: R?) {
            resource?.let {
                view.setImageBitmap(it.bitmap)
                onReady(resource)
            }
        }
    })
}

inline fun <R : Any> RequestBuilder<R>.onError(crossinline onError: (GlideException) -> Unit): RequestBuilder<R> {
    return this.addListener(object : RequestListener<R> {
        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<R>?, isFirstResource: Boolean): Boolean {
            e?.let { onError(it) }
            return true
        }

        override fun onResourceReady(resource: R, model: Any?, target: Target<R>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean = false
    })
}