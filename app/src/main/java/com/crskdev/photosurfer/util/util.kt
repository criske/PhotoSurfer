package com.crskdev.photosurfer.util

import android.animation.Animator
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.ImageSpan
import android.text.style.StyleSpan
import android.util.TypedValue
import android.view.*
import androidx.annotation.ColorRes
import androidx.annotation.FloatRange
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.iterator
import androidx.navigation.NavOptions
import androidx.recyclerview.widget.RecyclerView
import com.crskdev.photosurfer.R
import com.google.android.material.chip.ChipDrawable
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


fun Toolbar.tintIcons(@ColorRes color: Int = android.R.color.darker_gray) {
    post {
        val c = ContextCompat.getColor(context, color)
        menu.iterator().forEach {
            it.icon = DrawableCompat.wrap(it.icon)?.mutate()?.apply {
                DrawableCompat.setTint(this, c)
            }
        }
        navigationIcon?.let { DrawableCompat.setTint(DrawableCompat.wrap(it), c) }
    }
}

fun Toolbar.tintIcon(menuItemId: Int, @ColorRes color: Int = android.R.color.darker_gray) {
    post {
        val c = ContextCompat.getColor(context, color)
        menu.findItem(menuItemId)?.apply {
            this.icon = DrawableCompat.wrap(this.icon)?.mutate()?.apply {
                DrawableCompat.setTint(this, c)
            }
        }
    }
}

fun Drawable.tint(context: Context, @ColorRes color: Int = android.R.color.darker_gray) =
        DrawableCompat.setTint(DrawableCompat.wrap(this).mutate(), ContextCompat.getColor(context, color))

object IntentUtils {

    fun webIntentUnsplash(): Intent =
            Intent(Intent.ACTION_VIEW, Uri
                    .parse("https://unsplash.com/?utm_source=Photo+Surfer&utm_medium=referral"))

    fun webIntentUnsplashPhotographer(authorUsername: String): Intent =
            Intent(Intent.ACTION_VIEW, Uri
                    .parse("https://unsplash.com/@$authorUsername?utm_source=Photo+Surfer&utm_medium=referral"))

}

fun View.getDrawingRect(): Rect = Rect().apply {
    getDrawingRect(this)
}

fun View.getHitRect(): Rect = Rect().apply {
    getHitRect(this)
}

fun CharSequence.decorateWithChip(context: Context, bgColor: Int): Spannable {
    return SpannableString(this).apply {
               setSpan(ChipSpan(Color.RED, 8.dpToPx(context.resources), 10.dpToPx(context.resources)),
                       0, this@decorateWithChip.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
       // setSpan(BackgroundColorSpan(bgColor), 0, this@decorateWithChip.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
}