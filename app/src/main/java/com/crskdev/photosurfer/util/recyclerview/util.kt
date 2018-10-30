package com.crskdev.photosurfer.util.recyclerview

import androidx.recyclerview.widget.RecyclerView
import kotlin.math.roundToInt

/**
 * Created by Cristian Pela on 30.10.2018.
 */

fun RecyclerView.getSpanCountByScreenWidth(itemWidthPx: Int, spacingPx: Int = 0): Int {
    assert(itemWidthPx > 0) {
        "Item Width must bigger than 0. Provided : $itemWidthPx"
    }
    val screenWidth = resources.displayMetrics.widthPixels
    val spacingGrid = if (spacingPx > 0) 2 * spacingPx else 0
    val spanCount = screenWidth.toFloat() / (itemWidthPx + spacingGrid)
    return spanCount.roundToInt()
}