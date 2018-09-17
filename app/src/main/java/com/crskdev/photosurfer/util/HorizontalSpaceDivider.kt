package com.crskdev.photosurfer.util

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by Cristian Pela on 17.09.2018.
 */
class HorizontalSpaceDivider(private val spacePx: Int) : RecyclerView.ItemDecoration() {

    companion object {

        fun withDpOf(spaceDp: Int, context: Context): HorizontalSpaceDivider = HorizontalSpaceDivider(
                spaceDp.dpToPx(context.resources).toInt()
        )

        fun default(context: Context): HorizontalSpaceDivider = withDpOf(4, context)
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val margin = spacePx
        val isFirstOrLast = parent.getChildAdapterPosition(view).let {
            it == 0 || it == parent.adapter?.itemCount?.minus(1)
        }
        val bottomTop = if (isFirstOrLast) margin else margin / 2
        outRect.set(margin, bottomTop, margin, bottomTop)
    }

}