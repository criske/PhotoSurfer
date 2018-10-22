package com.crskdev.photosurfer.services.playwave

import android.os.Handler
import android.os.HandlerThread
import androidx.core.os.postDelayed
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong


/**
 * Created by Cristian Pela on 19.10.2018.
 */
class MockPlaywaveSoundPlayer : PlaywaveSoundPlayer {

    private var handler: Handler? = null

    private val playerThread: HandlerThread = object : HandlerThread("Player-Thread") {
        override fun onLooperPrepared() {
            handler = Handler(looper)
        }
    }

    companion object {
        private val ONE_SECOND = TimeUnit.SECONDS.toMillis(1)
    }

    private var listener: PlaywaveSoundPlayer.TrackListener? = null

    private val position: AtomicLong = AtomicLong(0)

    private val total: AtomicLong = AtomicLong(0)

    private val playingJob: Runnable = object : Runnable {
        override fun run() {
            val total = total.get()
            assert(total > 0)
            val nextPosition = position.addAndGet(ONE_SECOND)
            if (nextPosition >= total) {
                listener?.complete()
                position.set(0)
                handler?.removeCallbacks(this)
            } else {
                listener?.onTrack(nextPosition)
                handler?.postDelayed(this, ONE_SECOND)
                Integer.MAX_VALUE
            }
        }
    }

    init {
        playerThread.start()
    }


    override fun pause() {
        handler?.removeCallbacks(playingJob)
    }

    override fun release() {
        listener = null
        handler?.removeCallbacks(playingJob)
    }

    override fun load(songPath: String, duration: Long) {
        unload()//unload first
        total.set(duration)
        handler?.postDelayed(1000) {
            listener?.onReady()
        }
    }

    override fun play() {
        assert(total.get() > 0) {
            "Song not loaded"
        }
        handler?.removeCallbacks(playingJob)
        handler?.post(playingJob)
    }

    override fun stop() {
        handler?.removeCallbacks(playingJob)
        position.set(0)
    }

    override fun seekTo(position: Long) {
        this.position.set(position)
        play()
    }

    override fun unload() {
        handler?.removeCallbacks(playingJob)
        position.set(0)
        total.set(0)
    }

    override fun setTrackListener(trackListener: PlaywaveSoundPlayer.TrackListener) {
        this.listener = trackListener
    }

}