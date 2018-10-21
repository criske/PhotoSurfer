package com.crskdev.photosurfer.presentation.playwave

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.SeekBar
import androidx.annotation.IntRange
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.crskdev.photosurfer.R
import kotlinx.android.synthetic.main.player_layout.view.*

/**
 * Created by Cristian Pela on 19.10.2018.
 */
class PlayerView : ConstraintLayout {

    private var listener: PlayerListener? = null

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        LayoutInflater.from(context).inflate(R.layout.player_layout, this, true)
        imgBtnPlayerClose.setOnClickListener {
            listener?.onAction(Action.Close)
            close(null)

        }
        imgBtnPlayerPause.setOnClickListener {
            listener?.onAction(Action.Pause)
        }
        imgBtnPlayerPlayStop?.setOnClickListener { v ->
            (v.tag as Boolean?)?.let {
                val isPlaying = it
                v.tag = !it // toggle  playing state
                listener?.onAction(if (isPlaying) Action.Stop else Action.Play)
            }
        }
        seekBarPlayer.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                listener?.onAction(Action.SkipTo(progress))
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) = Unit

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                listener?.onAction(Action.SkipTo(seekBar.progress, true))
            }
        })
    }

    fun changeState(state: PlayingSongState) {
        when (state) {
            is PlayingSongState.Prepare -> prepare(state.song!!.fullInfo)
            is PlayingSongState.Ready -> ready()
            is PlayingSongState.Playing -> playing(state)
            is PlayingSongState.Paused,
            is PlayingSongState.Completed -> pauseOrComplete(state)
            is PlayingSongState.Stopped -> stop(state)
            else -> close(state)
        }
    }

    private fun ready() {
        seekBarPlayer.isEnabled = true
        imgBtnPlayerPlayStop.isEnabled = true
    }

    private fun prepare(fullInfo: String) {
        isVisible = true
        textPlayerSongInfo.text = fullInfo
        imgBtnPlayerPlayStop.apply {
            tag = false // mark this as playing/pending playing
            setImageResource(R.drawable.ic_play_arrow_white_24dp)
            isEnabled = false
        }
        seekBarPlayer.isEnabled = false
    }

    private fun makeSureIsPreparedAndReady(state: PlayingSongState) {
        if (!isVisible) {
            prepare(state.song!!.fullInfo)
            ready()
        }
    }

    private fun playing(state: PlayingSongState.Playing) {
        makeSureIsPreparedAndReady(state)
        imgBtnPlayerPlayStop.apply {
            setImageResource(R.drawable.ic_stop_white_24dp)
        }
        seekBarPlayer.apply {
            progress = state.percent
        }
        imgBtnPlayerPause.isVisible = true
        textPlayerSeekPosition.text = state.positionDisplay
    }

    private fun pauseOrComplete(state: PlayingSongState) {
        makeSureIsPreparedAndReady(state)
        imgBtnPlayerPlayStop.apply {
            tag = true //mark this as stopped/paused
            setImageResource(R.drawable.ic_play_arrow_white_24dp)
        }
        imgBtnPlayerPause.isVisible = false
    }

    private fun stop(state: PlayingSongState) {
        pauseOrComplete(state)
        seekBarPlayer.progress = 0
    }

    fun setOnPlayerListener(listener: PlayerListener) {
        this.listener = listener
    }

    private fun close(state: PlayingSongState?) {
        state?.let {
            stop(state)
        }
        isVisible = false
        textPlayerSongInfo.text = null
    }

    sealed class Action {
        object Play : Action()
        object Stop : Action()
        object Pause : Action()
        object Close : Action()
        class SkipTo(@IntRange(from = 0, to = 100) val percent: Int, val confirmedToPlay: Boolean = false) : Action()
    }

    interface PlayerListener {
        fun onAction(action: Action)
    }


}

