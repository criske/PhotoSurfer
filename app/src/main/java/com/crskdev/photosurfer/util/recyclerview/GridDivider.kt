package com.crskdev.photosurfer.util.recyclerview

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by Cristian Pela on 30.10.2018.
 */
class GridDivider(private val spacePx: Int, private val spans: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)
        val column = position % spans
        outRect.left = if (column == 0) spacePx else spacePx / 2
        outRect.right = if (column == spans - 1) spacePx else spacePx / 2
        if (position < spans) {
            outRect.top = spacePx
        }
        outRect.bottom = spacePx
    }

}