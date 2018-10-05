package com.crskdev.photosurfer.util.glide

import android.graphics.Bitmap
import android.graphics.Rect
import androidx.palette.graphics.Palette
import com.bumptech.glide.load.Option
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.transcode.ResourceTranscoder

/**
 * Created by Cristian Pela on 05.10.2018.
 */
class BitmapPaletteTranscoder(private val bitmapPool: BitmapPool)
    : ResourceTranscoder<Bitmap, BitmapPalette> {

    companion object {
        internal const val SAMPLING_REGIONS_KEY = "com.crskdev.photosurfer.util.glide.BitmapPaletteTranscoder.SAMPLING_REGIONS_KEY"
    }

    override fun transcode(toTranscode: Resource<Bitmap>, options: Options): Resource<BitmapPalette>? {
        val bitmap = toTranscode.get()
        val regions = options.get(Option.memory(SAMPLING_REGIONS_KEY,
                mapOf(BitmapPalette.NO_REGIONS_ID to Rect(0, 0, bitmap.width, bitmap.height))))!!
        val paletteRegions = regions.mapValues {
                    Palette.from(bitmap).setRegion(
                            it.value.left,
                            it.value.top,
                            it.value.right,
                            it.value.bottom)
                            .generate() }
        return BitmapPaletteResource(BitmapPalette(bitmap, paletteRegions), bitmapPool)
    }
}