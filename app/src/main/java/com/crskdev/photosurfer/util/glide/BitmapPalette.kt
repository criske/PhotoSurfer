package com.crskdev.photosurfer.util.glide

import android.graphics.*
import androidx.palette.graphics.Palette

/**
 * Created by Cristian Pela on 05.10.2018.
 */
class BitmapPalette(override val bitmap: Bitmap, val paletteSampler: PaletteSampler) : BitmapCarrier {
    companion object {
        const val NO_REGIONS_ID = -1
    }
}


class PaletteSampler(private val paletteRegions: Map<Int, Palette>, private val nonRegionPalette: Palette = NON_REGION_PALETTE) {

    companion object {
        private val NON_REGION_PALETTE = Bitmap
                .createBitmap(1, 1, Bitmap.Config.ARGB_8888)
                .let {
                    val canvas = Canvas(it)
                    val paint = Paint().apply {
                        color = Color.WHITE
                        style = Paint.Style.FILL
                    }
                    canvas.drawRect(0.0f, 0.0f, 1f, 1f, paint)
                    it
                }
                .let {
                    Palette.from(it).generate()
                }
    }

    operator fun get(id: Int): Palette = paletteRegions.takeIf { it.containsKey(id) }?.let { it[id]!! }
            ?: NON_REGION_PALETTE

}