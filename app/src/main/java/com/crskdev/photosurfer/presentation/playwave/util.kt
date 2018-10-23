package com.crskdev.photosurfer.presentation.playwave

import com.crskdev.photosurfer.data.local.playwave.song.Song
import com.crskdev.photosurfer.entities.Playwave
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


internal fun Song.toUI(): SongUI = SongUI(id, path, title, artist, prettySongDuration(duration), toString(), duration, exists)

internal fun SongUI.toEntity(): Song = Song(id, path, title, artist, durationLong, exists)

internal fun Playwave.toUI(): PlaywaveUI = PlaywaveUI(this.id, this.title, this.size, this.song.toUI(),
        !this.song.exists, this.photos)
