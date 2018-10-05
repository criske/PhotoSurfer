package com.crskdev.photosurfer.util.glide

import android.graphics.Bitmap
import androidx.palette.graphics.Palette

/**
 * Created by Cristian Pela on 05.10.2018.
 */
class BitmapPalette(override val bitmap: Bitmap, val paletteRegions: Map<Int, Palette>) : BitmapCarrier {
    companion object {
        const val NO_REGIONS_ID = -1
    }
}