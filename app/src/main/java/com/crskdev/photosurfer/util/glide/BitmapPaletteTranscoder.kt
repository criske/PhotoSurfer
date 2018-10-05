package com.crskdev.photosurfer.util.glide

import android.graphics.Bitmap
import androidx.palette.graphics.Palette
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.transcode.ResourceTranscoder

/**
 * Created by Cristian Pela on 05.10.2018.
 */
class BitmapPaletteTranscoder(private val bitmapPool: BitmapPool)
    : ResourceTranscoder<Bitmap, BitmapPalette> {

    override fun transcode(toTranscode: Resource<Bitmap>, options: Options): Resource<BitmapPalette>? {
        val bitmap = toTranscode.get()
        val paletteQuadrants = obtainPaletteQuadrants(bitmap)
        return BitmapPaletteResource(BitmapPalette(bitmap, paletteQuadrants), bitmapPool)
    }

    private fun obtainPaletteQuadrants(bitmap: Bitmap): Array<Palette> {
        val w = bitmap.width
        val h = bitmap.height
        val wHalf = w / 2
        val hHalf = h / 2
        //clockwise
        val firstQuadrant = Palette.from(bitmap).setRegion(
                0, 0, wHalf, hHalf).generate()
        val secondQuadrant = Palette.from(bitmap)
                .setRegion(wHalf, 0, w, hHalf).generate()
        val thirdQuadrant = Palette.from(bitmap)
                .setRegion(wHalf, hHalf, w, h).generate()
        val fourthQuadrant = Palette.from(bitmap)
                .setRegion(0, hHalf, wHalf, h).generate()
        return arrayOf(firstQuadrant, secondQuadrant, thirdQuadrant, fourthQuadrant)
    }


}