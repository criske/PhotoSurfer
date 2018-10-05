package com.crskdev.photosurfer.util

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import com.jsibbold.zoomage.ZoomageView

/**
 * Created by Cristian Pela on 02.10.2018.
 */
class ExtraGesturedZoomageView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ZoomageView(context, attrs, defStyleAttr) {

    private var addedLongClickListener: Boolean = false

    private var addedClickListener: Boolean = false

    private val gestureDetector: GestureDetector  by lazy {
        GestureDetector(context, object: GestureDetector.SimpleOnGestureListener(){
            override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
                return addedClickListener
            }

            override fun onLongPress(e: MotionEvent?) {
            }
        })
    }

    override fun setOnClickListener(l: OnClickListener) {
        addedClickListener = true
        super.setOnClickListener(OnClickListenerWrapper(l))
    }

    override fun setOnLongClickListener(l: OnLongClickListener) {
        addedLongClickListener = true
        super.setOnLongClickListener(OnLongClickListenerWrapper(l))
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (!gestureDetector.onTouchEvent(event)) {
            super.onTouchEvent(event)
        } else {
            false
        }
    }

    private inner class OnClickListenerWrapper(private val listener: View.OnClickListener) : View.OnClickListener {
        override fun onClick(v: View?) {
            listener.let {
                it.onClick(v)
            }
        }
    }

    private inner class OnLongClickListenerWrapper(private val listener: View.OnLongClickListener) : View.OnLongClickListener {
        override fun onLongClick(v: View?): Boolean {
            return listener.let {
                it.onLongClick(v)
            }
        }

    }
}