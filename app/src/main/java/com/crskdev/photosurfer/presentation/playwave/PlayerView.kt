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
            listener?.onAction(Action.PlayOrStop)
        }
        seekBarPlayer.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            var startManualSeek = false

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (startManualSeek) {
                    listener?.onAction(Action.SeekTo(progress))
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                startManualSeek = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                listener?.onAction(Action.SeekTo(seekBar.progress, true))
                startManualSeek = false
            }
        })
    }

    fun changeState(state: PlayingSongState) {
        when (state) {
            is PlayingSongState.Prepare -> prepare(state.song!!)
            is PlayingSongState.Ready -> ready()
            is PlayingSongState.Playing -> playing(state)
            is PlayingSongState.Seeking -> seeking(state)
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

    private fun prepare(song: SongUI) {
        isVisible = true
        textPlayerSongInfo.text = song.fullInfo
        imgBtnPlayerPlayStop.apply {
            setImageResource(R.drawable.ic_play_arrow_white_24dp)
            isEnabled = false
        }
        seekBarPlayer.apply {
            isEnabled = false
            max = song.durationLong.toInt()
        }
    }

    private fun makeSureIsPreparedAndReady(state: PlayingSongState) {
        if (!isVisible) {
            prepare(state.song!!)
            ready()
        }
    }

    private fun seeking(state: PlayingSongState.Seeking) {
        makeSureIsPreparedAndReady(state)
        seekBarPlayer.apply {
            progress = state.position.toInt()
        }
        textPlayerSeekPosition.text = state.positionDisplay
    }


    private fun playing(state: PlayingSongState.Playing) {
        makeSureIsPreparedAndReady(state)
        imgBtnPlayerPlayStop.apply {
            setImageResource(R.drawable.ic_stop_white_24dp)
        }
        imgBtnPlayerPause.isVisible = true
        seekBarPlayer.apply {
            progress = state.position.toInt()
        }
        textPlayerSeekPosition.text = state.positionDisplay
    }

    private fun pauseOrComplete(state: PlayingSongState) {
        makeSureIsPreparedAndReady(state)
        imgBtnPlayerPlayStop.apply {
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
        object PlayOrStop : Action()
        object Pause : Action()
        object Close : Action()
        class SeekTo( val position: Int, val confirmedToPlay: Boolean = false) : Action()
    }

    interface PlayerListener {
        fun onAction(action: Action)
    }


}

