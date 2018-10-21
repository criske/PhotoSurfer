package com.crskdev.photosurfer.presentation.playwave

import java.util.concurrent.TimeUnit

/**
 * Created by Cristian Pela on 21.10.2018.
 */
fun prettySongDuration(duration: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(duration)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(minutes)
    val minutesFormat = if (minutes < 10) "%02d" else "%d"
    val secondsFormat = if (seconds < 10) "%02d" else "%d"
    return String.format("$minutesFormat:$secondsFormat", minutes, seconds)
}