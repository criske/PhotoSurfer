package com.crskdev.photosurfer.presentation.playwave

import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt
import kotlin.math.roundToLong

/**
 * Created by Cristian Pela on 21.10.2018.
 */
fun prettySongDuration(durationMillis: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMillis) - TimeUnit.MINUTES.toSeconds(minutes)
    val minutesFormat = if (minutes < 10) "%02d" else "%d"
    val secondsFormat = if (seconds < 10) "%02d" else "%d"
    return String.format("$minutesFormat:$secondsFormat", minutes, seconds)
}

internal fun positionPercent(realPosition: Long, duration: Long): Int =
        realPosition.toFloat().div(duration).times(100).roundToInt()

internal fun prettySongPosition(percent: Int, duration: Long): String =
        prettySongDuration(realPosition(percent, duration))

internal fun realPosition(percent: Int, duration: Long): Long =
        percent.toFloat().div(100).times(duration).roundToLong()
