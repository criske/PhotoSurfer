package com.crskdev.photosurfer.util

import android.animation.Animator
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.util.TypedValue
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewPropertyAnimator
import androidx.annotation.FloatRange
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.GestureDetectorCompat
import androidx.navigation.NavOptions
import androidx.recyclerview.widget.RecyclerView
import com.crskdev.photosurfer.R
import java.util.concurrent.Executor
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

fun Int.setAlphaComponent(@FloatRange(from = 0.0, to = 1.0, fromInclusive = true, toInclusive = true) alpha: Float): Int =
        ColorUtils.setAlphaComponent(this, (alpha * 255).toInt())

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


fun Float.dpToPx(resources: Resources): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, resources.displayMetrics)

fun Int.dpToPx(resources: Resources): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), resources.displayMetrics)

fun defaultTransitionNavOptionsBuilder(): NavOptions.Builder = NavOptions.Builder()
        .setEnterAnim(R.anim.in_from_right)
        .setExitAnim(R.anim.out_to_left)
        .setPopEnterAnim(R.anim.in_from_left)
        .setPopExitAnim(R.anim.out_to_right)

fun defaultTransitionNavOptions() = defaultTransitionNavOptionsBuilder().build()

fun getSpanCountByScreenWidth(resources: Resources, itemWidthDp: Int, spacingDp: Int = 0): Int {
    kotlin.assert(itemWidthDp > 0) {
        "Item Width must bigger than 0. Provided : $itemWidthDp"
    }
    val screenWidth = resources.displayMetrics.widthPixels
    val spacingGrid = if (spacingDp > 0) 2 * spacingDp.dpToPx(resources).toInt() else 0
    val spanCount = screenWidth / (itemWidthDp.dpToPx(resources) + spacingGrid)
    return Math.round(spanCount)
}


inline fun <T> T.runOn(executor: Executor, crossinline block: T.() -> Unit) {
    executor.execute {
        this@runOn.block()
    }
}

fun RecyclerView.addOnItemGestureDetectListener(gestureListener: SimpleOnGestureListener2<RecyclerView>) {

    val gestureDetector = GestureDetectorCompat(context, gestureListener)

    addOnItemTouchListener(object : RecyclerView.SimpleOnItemTouchListener() {
        override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
            return gestureDetector.onTouchEvent(e)
        }
    })
}

abstract class SimpleOnGestureListener2<V>(val gesturedView: V) : GestureDetector.SimpleOnGestureListener()


fun Context.systemNotification(message: String) {
    val context = applicationContext
    val channelID = "PhotoSurfer-Notification"
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = channelID
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelID, name, importance)
        channel.description = channelID
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }
    val notification = NotificationCompat
            .Builder(context, channelID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Photo Surfer")
            .setContentText(message)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
    val notificationManager = NotificationManagerCompat.from(context)
    notificationManager.notify(1337, notification)
}