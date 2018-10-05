package com.crskdev.photosurfer.util

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import com.jsibbold.zoomage.ZoomageView

/**
 * Created by Cristian Pela on 02.10.2018.
 */
class ExtraGesturedZoomageView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ZoomageView(context, attrs, defStyleAttr) {

    private var delegateGestures: Boolean = true

    private var onLongClickListener: OnLongClickListener? = null

    private var onClickListener: OnClickListener? = null

    private val gestureDetector: GestureDetector  by lazy {
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onLongPress(e: MotionEvent?) {
                onLongClickListener?.let {
                    delegateGestures = false
                    onLongClickListener?.onLongClick(this@ExtraGesturedZoomageView)
                }
            }

            override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
                onClickListener?.let {
                    delegateGestures = false
                    onClickListener?.onClick(this@ExtraGesturedZoomageView)
                }
                return delegateGestures
            }
        })
    }

    override fun setOnClickListener(l: OnClickListener) {
        onClickListener = l
    }

    override fun setOnLongClickListener(l: OnLongClickListener) {
        onLongClickListener = l
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        return if (delegateGestures) {
            super.onTouchEvent(event)
        } else {
            delegateGestures = true
            false
        }
    }

}