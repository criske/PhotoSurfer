package com.crskdev.photosurfer.services.playwave

import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaPlayer.MEDIA_ERROR_SERVER_DIED
import android.os.Build
import android.os.Handler
import android.os.Looper

/**
 * Created by Cristian Pela on 19.10.2018.
 */
interface PlaywaveSoundPlayer {

    interface TrackListener {

        fun onReady()

        fun onTrack(position: Int)

        fun complete()

    }

    fun loadAndPlay(songPath: String, duration: Int, atPosition: Int = 0)

    fun play(position: Int)

    fun stop()

    fun pause()

    fun seekTo(position: Int)

    fun unload()

    fun setTrackListener(trackListener: TrackListener)

    fun release()
}


interface PlaywaveSoundPlayerProvider {

    fun create(): PlaywaveSoundPlayer

}

class PlaywaveSoundPlayerImpl : PlaywaveSoundPlayer {

    private var mediaPlayer: MediaPlayer? = null

    private var startMediaPlayerSeverFailedAttempts = 0

    private val trackHandler = Handler(Looper.getMainLooper())

    private var trackListener: PlaywaveSoundPlayer.TrackListener? = null

    private var startAtPosition: Int? = null

    private val trackRunnable = object : Runnable {
        override fun run() {
            mediaPlayer?.let {
                trackListener?.onTrack(it.currentPosition)
                trackHandler.postDelayed(this, 1000)
            }
        }

    }

    private val onPrepareListener: MediaPlayer.OnPreparedListener = MediaPlayer.OnPreparedListener {
        trackListener?.onReady()
        startMediaPlayerSeverFailedAttempts = 0
        it.start()
        val sp = startAtPosition
        if (sp != null) {
            it.seekTo(sp)
        }
        trackHandler.post(trackRunnable)
    }

    private val onCompleteListener: MediaPlayer.OnCompletionListener = MediaPlayer.OnCompletionListener {
        trackHandler.removeCallbacks(trackRunnable)
        trackListener?.complete()
    }

    private val onSeekCompleteListener: MediaPlayer.OnSeekCompleteListener = MediaPlayer.OnSeekCompleteListener {
        if (!it.isPlaying)
            it.start()
    }

    override fun pause() {
        mediaPlayer?.pause()
        trackHandler.removeCallbacks(trackRunnable)
    }

    override fun release() {
        trackHandler.removeCallbacks(trackRunnable)
        mediaPlayer?.release()
        mediaPlayer = null
        trackListener = null
        startAtPosition = null
    }

    override fun loadAndPlay(songPath: String, duration: Int, atPosition: Int) {
        trackHandler.removeCallbacks(trackRunnable)
        mediaPlayer?.reset()
        startAtPosition = atPosition.takeIf { it > 0 }
        mediaPlayer = MediaPlayer().apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                setAudioAttributes(AudioAttributes
                        .Builder()
                        //.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build())
            } else {
                setAudioStreamType(AudioManager.STREAM_MUSIC)
            }
            setDataSource(songPath)
            setOnPreparedListener(onPrepareListener)
            setOnCompletionListener(onCompleteListener)
            setOnSeekCompleteListener(onSeekCompleteListener)
            setOnErrorListener { mp, what, extra ->
                if (what == MEDIA_ERROR_SERVER_DIED) {
                    startMediaPlayerSeverFailedAttempts += 1
                    if (startMediaPlayerSeverFailedAttempts < 3)
                        loadAndPlay(songPath, duration, atPosition)
                    else {
                        throw Exception("Could not start player server")
                    }
                }
                true
            }
            prepareAsync()
        }
    }

    override fun play(position: Int) {
        trackHandler.removeCallbacks(trackRunnable)
        if (position > 0) {
            mediaPlayer?.seekTo(position)
        } else {
            mediaPlayer?.start()
        }
        trackHandler.post(trackRunnable)
    }

    override fun stop() {
        mediaPlayer?.stop()
        trackHandler.removeCallbacks(trackRunnable)
    }

    override fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
    }

    //TODO maybe remove.. release is enough?
    override fun unload() {
        release()
    }

    override fun setTrackListener(trackListener: PlaywaveSoundPlayer.TrackListener) {
        this.trackListener = trackListener
    }

}