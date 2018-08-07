package com.crskdev.photosurfer

import android.animation.Animator
import android.content.Context
import android.content.res.Resources
import android.util.TypedValue
import android.view.ViewPropertyAnimator
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by Cristian Pela on 05.08.2018.
 */
fun AtomicInteger.safeSet(value: Int) {
    do {
        val lastValue = get()
    } while (!compareAndSet(lastValue, value))
}

fun AtomicBoolean.safeSet(value: Boolean) {
    do {
        val lastValue = get()
        println(lastValue)
    } while (!compareAndSet(lastValue, value))
}

inline fun ViewPropertyAnimator.onEnded(crossinline action: () -> Unit): ViewPropertyAnimator {
    this.setListener(object : Animator.AnimatorListener {

        override fun onAnimationRepeat(p0: Animator?) {

        }

        override fun onAnimationEnd(p0: Animator?) {
            action()
        }

        override fun onAnimationCancel(p0: Animator?) {

        }

        override fun onAnimationStart(p0: Animator?) {

        }

    })
    return this
}

fun <T> LiveData<T>.filter(predicate: (T) -> Boolean): LiveData<T> =
        MediatorLiveData<T>().apply {
            addSource(this@filter) {
                if (predicate(it)) {
                    value = it
                }
            }
        }

fun Float.dpToPx(resources: Resources) =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, resources.displayMetrics)

